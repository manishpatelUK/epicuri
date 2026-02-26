package uk.co.epicuri.bookingapi.endpoints.msg;

import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QTable;
import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import uk.co.epicuri.bookingapi.email.HTMLEmailBuilder;
import uk.co.epicuri.bookingapi.pojo.EmailRequest;
import uk.co.epicuri.bookingapi.pojo.WelcomeRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("msg")
public class EmailController {

    private final QConnection staticsConnection;
    private final AtomicInteger atomicInteger;

    public EmailController(QConnection staticsConnection) {
        this.staticsConnection = staticsConnection;
        atomicInteger = new AtomicInteger();
        atomicInteger.set(0);
    }

    @POST
    @Path("/billingemail")
    public Response billingemail(EmailRequest request) {
        File temp = null;
        try {
            temp = new File("bill-"+atomicInteger.incrementAndGet(),".pdf");
            OutputStream os = new FileOutputStream(temp);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(request.getBody());
            renderer.layout();
            renderer.createPDF(os);
            os.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            tryDeletion(temp);
            temp = null;
        }

        HTMLEmailBuilder builder = HTMLEmailBuilder.newInstance()
                                    .sender("Epicuri <no-reply@epicuri.email>")
                                    .recipient(request.getEmail())
                                    .subject(request.getSubject())
                                    .line(request.getBody());


        try {
            if(temp != null) {
                builder.attach(temp,MediaType.APPLICATION_OCTET_STREAM_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tryDeletion(temp);
        }


        builder.send();
        tryDeletion(temp);
        return Response.status(Response.Status.CREATED).build();
    }

    private void tryDeletion(File file) {
        if(file == null) {
            return;
        }
        try {
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @POST
    @Path("/email")
    public Response email(EmailRequest request) {
        HTMLEmailBuilder.newInstance()
                .sender("Epicuri <no-reply@epicuri.email>")
                .recipient(request.getEmail())
                .subject(request.getSubject())
                .line(request.getBody())
                .send();
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/welcome")
    public Response welcome(WelcomeRequest request) {
        try {
            QTable table = (QTable) staticsConnection.sync("getEmailStatics", request.getLanguage(), "newdiner");
            HTMLEmailBuilder builder = HTMLEmailBuilder.newInstance()
                                        .sender("Epicuri <no-reply@epicuri.email>")
                                        .recipient(request.getEmail())
                                        .subject("Yay! Welcome to the club!");
            for(int i = 0; i < table.getRowsCount(); i++) {
                QTable.Row row = table.get(i);
                String text = String.valueOf((char[]) row.get(table.getColumnIndex("text")));
                String identifier = (String) row.get(table.getColumnIndex("identifier"));
                if(identifier.equals("misc")) {
                    builder.line(text);
                } // else not supported
            }
            builder.send();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalServerErrorException(ex.getMessage());
        }
        return Response.status(Response.Status.CREATED).build();
    }
}
