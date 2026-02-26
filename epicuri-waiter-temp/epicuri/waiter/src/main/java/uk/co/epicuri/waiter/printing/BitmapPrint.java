package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import uk.co.epicuri.waiter.webservice.EditPrinterWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by pharris on 03/03/15.
 */
public class BitmapPrint {
    public static final int MAX_WIDTH = 576;

    private final String portSettings;
    private final String printerId;
    private final String macAddress;
    private String portName;

    private String error;
    public String getError() {
        return error;
    }

    public BitmapPrint(String portName, String portSettings, String printerId, String macAddress) {
        this.portName = portName;
        this.portSettings = portSettings;
        this.printerId = printerId;
        this.macAddress = macAddress;
    }

    public Boolean print(Bitmap bitmap, Context context) throws StarIOPortException {
        StarIOPort port = null;

        try {
            port = StarIOPort.getPort(portName, portSettings, 5000, context);
        } catch (StarIOPortException ex) {
            Log.e("BitmapPrint",ex.getMessage());
        }

        if(null == port){
            // try searching for the printer by mac address
            PortInfo portInfo = null;
            if(macAddress != null) {
                portInfo = PrintUtil.searchPrinterByMacAddress(macAddress);
            }
            if(portInfo == null) {
                Log.d("BitmapPrint", "Could not find printer at: " + portName);
                error = "Failed to connect to printer";
                return false;
            } else {
                // get port and update server
                port = StarIOPort.getPort(portInfo.getPortName(), portSettings, 5000, context);
                if(port != null) {
                    portName = port.getPortName();
                    new WebServiceTask(context, new EditPrinterWebServiceCall(printerId, getIpAddress(portName), macAddress), false).execute();
                } else {
                    Log.d("BitmapPrint", "Could not find printer at: " + portName);
                    error = "Failed to connect to printer";
                    return false;
                }
            }
        }
        try {
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException e) {}


            byte[] command;

            StarBitmap starbitmap = new StarBitmap(bitmap, false, MAX_WIDTH);


//	        port.writePort(data, 0, data.length);
            RasterDocument rasterDoc = new RasterDocument(
                    RasterDocument.RasSpeed.Medium,
                    RasterDocument.RasPageEndMode.FeedAndFullCut,
                    RasterDocument.RasPageEndMode.FeedAndFullCut,
                    RasterDocument.RasTopMargin.Standard, 0, 0, 0);

            command = rasterDoc.BeginDocumentCommandData();
            port.writePort(command, 0, command.length);
            command = starbitmap.getImageRasterDataForPrinting();
            port.writePort(command, 0, command.length);
            command = rasterDoc.EndDocumentCommandData();
            port.writePort(command, 0, command.length);


//			port.writePort(command, 0, command.length);


//			byte[] data = new byte[]{0x1b, 0x64, 0x02};    // Cut
//	        port.writePort(data, 0, data.length);
            try
            {
                Thread.sleep(3000);
            }
            catch(InterruptedException e) {}


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
        return true;
    }

    private String getIpAddress(String portName) {
        if(portName.startsWith("TCP:")) {
            return portName.substring(4);
        }
        return portName;
    }

    private static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
        for (int index = 0; index < cpyArray.length; index++) {
            cpyArray[index] = srcArray[index];
        }
    }
}
