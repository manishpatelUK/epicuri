package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.lang.ref.WeakReference;

public class KitchenPrintTask extends AsyncTask<byte[], Integer, Boolean> {

	private static final String LOGGER = "KitchenPrintTask";

	private StarIOPort port = null;
	private WeakReference<Context> context;
	
	private String portName;
	private String portSettings;
	
	private Handler onCompleteHandler;
	
	/**
	 * set handler to receive success/fail response.  Message.arg1 == zero means success.
	 * @param handler
	 * @return
	 */
	public KitchenPrintTask setHandler(Handler handler){
		onCompleteHandler = handler;
		return this;
	}
	
	public KitchenPrintTask(Context context, String portName, String portSettings) {
		this.context = new WeakReference<>(context);
		this.portName = portName;
		this.portSettings = portSettings;
	}

	@Override
	protected Boolean doInBackground(byte[] ... params) {
		try {
			port = StarIOPort.getPort(portName, portSettings, 5000, context.get());
		} catch (StarIOPortException e){
			Log.d(LOGGER, "Could not print " + e.getMessage(), e);
		}
		if(null == port){
			return false;
		}
		try {
			byte[] data = params[0];
	        port.writePort(data, 0, data.length);
	        
	        data = new byte[]{0x1b, 0x64, 0x02};
	        port.writePort(data, 0, data.length);
	        return true;
		}
    	catch (StarIOPortException e)
    	{
    		AlertDialog.Builder dialog = new AlertDialog.Builder(context.get());
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
