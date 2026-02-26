package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.util.ArrayList;

public class KickDrawerTask extends AsyncTask<byte[], Integer, Boolean> {
	private static final String LOGGER = "EPICURI_LOADER";

	private StarIOPort port = null;
	private Context context;

	String portName; String portSettings;

	private Handler onCompleteHandler;

	/**
	 * set handler to receive succes/fail response.  Message.arg1 == zero means success.
	 * @param handler
	 * @return
	 */
	public KickDrawerTask setHandler(Handler handler){
		onCompleteHandler = handler;
		return this;
	}

	public KickDrawerTask(Context context, String portName, String portSettings) {
		this.context = context;
		this.portName = portName;
		this.portSettings = portSettings;
	}

	@Override
	protected Boolean doInBackground(byte[] ... params) {
		try {
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
		} catch (StarIOPortException e){
			Log.e(LOGGER, e.getMessage());
		}
		if(null == port){
			return false;
		}
		try {
			ArrayList<Byte> commands = new ArrayList<Byte>();
			byte openCashDrawer = 0x07;
			byte[] data = new byte[]{openCashDrawer};

	        port.writePort(data, 0, data.length);
	        return true;
		}
    	catch (StarIOPortException e)
    	{
    		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Failure");
    		alert.setMessage("Failed to connect to printer");
    		alert.show();
	        return false;
    	}
    	finally
		{
			if(port != null)
			{
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {}
			}
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if(null != onCompleteHandler){
			Message msg = Message.obtain(onCompleteHandler, result ? 0 : 1);
			onCompleteHandler.sendMessage(msg);
		}
	}
}
