package uk.co.epicuri.waiter.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.co.epicuri.waiter.interfaces.FSKMessageCallback;

/**
 * http://www.crucible-technologies.co.uk/downloads/COMET/comet_man.pdf
 */
public class BluetoothCallerIDService {
    private String LOGGER = "BTCallerIDService";

    private final BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 101;
    private final String serviceName = "Epicuri CallerID";
    private final UUID serviceUUID = UUID.fromString("38f88d7f-356e-4a87-979c-6a18fe1c5489");
    private InputThread inputThread;

    private final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final Executor executor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));

    private BluetoothCallerIDService(BluetoothAdapter bluetoothAdapter){
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public static BluetoothCallerIDService createService(Activity activity) throws IllegalAccessException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            throw new IllegalAccessException("Bluetooth connectivity not allowed");
        }

        if(!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return null;
        } else {
            return new BluetoothCallerIDService(adapter);
        }
    }

    public void listen(FSKMessageCallback fskMessageCallback) throws IllegalArgumentException {
        BluetoothServerSocket bluetoothServerSocket = null;
        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(serviceName, serviceUUID);
        } catch (IOException e) {
            Log.e(LOGGER,e.getMessage());
            throw new IllegalArgumentException(e);
        }

        if(inputThread != null) {
            inputThread.cancel();
        }
        inputThread = new InputThread(bluetoothServerSocket, fskMessageCallback);
        inputThread.start();
    }

    private class InputThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;
        private final FSKMessageCallback fskMessageCallback;

        private InputThread(BluetoothServerSocket bluetoothServerSocket, FSKMessageCallback fskMessageCallback) {
            this.bluetoothServerSocket = bluetoothServerSocket;
            this.fskMessageCallback = fskMessageCallback;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    Log.e(LOGGER,e.getMessage());
                    break;
                }

                if (socket != null) {
                    executor.execute(new MessageProcessorThread(socket, fskMessageCallback));
                }
            }

            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(LOGGER,e.getMessage());
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageProcessorThread implements Runnable {

        private final BluetoothSocket socket;
        private final FSKMessageCallback fskMessageCallback;

        public MessageProcessorThread(BluetoothSocket socket, FSKMessageCallback fskMessageCallback) {
            this.socket = socket;
            this.fskMessageCallback = fskMessageCallback;
        }

        @Override
        public void run() {
            try {
                readStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readStream() throws IOException {
            InputStream inputStream = socket.getInputStream();
            byte[] header = new byte[2];
            inputStream.read(header);

            int messageType = (int)header[0];
            if(messageType != 128) {
                //not a message
                return;
            }

            int messageLength = (int)header[1];
            byte[] messageBytes = new byte[messageLength];
            inputStream.read(messageBytes, 2, messageLength);
            int numberIndex = -1;
            int numberLength = -1;
            for(int i = 0; i < messageBytes.length; i++) {
                int decimal = (int)messageBytes[i];
                if(decimal == 2) {
                    numberIndex = i+2;
                    numberLength = (int)messageBytes[i+1];
                }
            }

            if(numberIndex == -1) {
                return;
            }

            StringBuilder builder = new StringBuilder();
            for(int i = numberIndex; i < numberIndex+numberLength; i++) {
                int n = messageBytes[i];
                if(n >= 48 && n <=57) {
                    builder.append(Integer.toString(n - 48));
                }
            }

            String number = builder.toString();

            if(!TextUtils.isEmpty(number)) {
                fskMessageCallback.onPhoneNumberReceieved(number);
            }
        }
    }
}
