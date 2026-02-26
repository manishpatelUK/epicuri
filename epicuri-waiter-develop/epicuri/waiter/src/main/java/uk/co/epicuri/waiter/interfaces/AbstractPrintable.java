package uk.co.epicuri.waiter.interfaces;

import java.nio.ByteBuffer;
import java.util.List;

public abstract class AbstractPrintable implements Printable {
    protected static final byte[] DASHED_LINE = "\n---------------------".getBytes();

    @Override
    public byte[] getPrintOutput() {
        return getPrintOutput(true, true);
    }

    protected void preloadWithSizes(List<byte[]> message, boolean doubleWidth, boolean doubleHeight) {
        message.clear();

        message.add("\u001BW0\u001Bh0".getBytes()); // reset font size
        String sizeCode = "";
        if(doubleWidth) {
            sizeCode += "\u001BW1";
        }
        if(doubleHeight) {
            sizeCode += "\u001Bh1";
        }
        if(!sizeCode.equals("")) {
            message.add(sizeCode.getBytes());
        }
    }

    protected byte[] merge(List<byte[]> message) {
        int totalLength = 0;
        for (byte[] b : message) {
            totalLength += b.length;
        }

        ByteBuffer bbf = ByteBuffer.allocate(totalLength);
        for (byte[] b : message) {
            bbf.put(b);
        }
        return bbf.array();
    }
}
