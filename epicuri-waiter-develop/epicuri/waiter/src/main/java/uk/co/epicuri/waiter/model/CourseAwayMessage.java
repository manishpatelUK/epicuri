package uk.co.epicuri.waiter.model;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.interfaces.AbstractPrintable;

public class CourseAwayMessage extends AbstractPrintable {
    private final String courseName;
    private final EpicuriSessionDetail session;
    private final String currentUserName;

    public CourseAwayMessage(String courseName, EpicuriSessionDetail session, String currentUserName) {
        this.courseName = courseName;
        this.session = session;
        this.currentUserName = currentUserName;
    }

    @Override
    public byte[] getPrintOutput(boolean doubleHeight, boolean doubleWidth) {
        List<byte[]> message = new ArrayList<>();
        preloadWithSizes(message, doubleWidth, doubleHeight);

        message.add("Table: ".getBytes());
        for (EpicuriTable table : session.getTables()) {
            if(table.getName() != null) {
                message.add(table.getName().getBytes());
                message.add(" ".getBytes());
            }
        }
        message.add("\n".getBytes());

        message.add("Time: ".getBytes());
        message.add(LocalSettings.getDateFormatWithDate().format(System.currentTimeMillis()).getBytes());

        message.add("\n".getBytes());
        message.add(String.format("Sent by: %s", currentUserName).getBytes());
        message.add("\n".getBytes());
        message.add("\n".getBytes());
        if(courseName.contains("AWAY")){
            message.add((courseName).getBytes());
        }else {
            message.add((courseName + " AWAY").getBytes());
        }


        return merge(message);
    }

}
