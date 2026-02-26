package uk.co.epicuri.bookingapi.endpoints.msg;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import org.junit.Test;
import uk.co.epicuri.bookingapi.endpoints.msg.EmailController;
import uk.co.epicuri.bookingapi.pojo.EmailRequest;
import uk.co.epicuri.bookingapi.pojo.WelcomeRequest;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EmailControllerTest {



    @Test
    public void billingEmail() throws Exception {
        QConnection staticsdb = new QBasicConnection("localhost",12100,"statics","13ghunda~Akoti");
        EmailController controller = new EmailController(staticsdb);
        EmailRequest request = new EmailRequest();
        request.setEmail("manni.patel@gmail.com");
        request.setSubject("this is a test email");
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] body = Files.readAllBytes(Paths.get(classLoader.getResource("sample.html").toURI()));
        String html = new String(body, Charset.defaultCharset());
        request.setBody(html);

        for(int i = 0; i < 10; i++) {
            controller.billingemail(request);
        }
    }

    @Test
    public void testEmail() throws Exception {
        QConnection staticsdb = new QBasicConnection("localhost",12100,"statics","13ghunda~Akoti");
        EmailRequest request = new EmailRequest();
        request.setEmail("manni.patel@gmail.com");
        request.setSubject("this is a test email");
        request.setBody("<html><body>foo<emphasis>b</emphasis>ar</body></html>");
        EmailController controller = new EmailController(staticsdb);

        for(int i = 0; i < 10; i++) {
            controller.email(request);
        }
    }

    @Test
    public void testWelcome() throws Exception {
        QConnection staticsdb = new QBasicConnection("localhost",12100,"statics","13ghunda~Akoti");
        staticsdb.open();
        EmailController controller = new EmailController(staticsdb);
        WelcomeRequest request = new WelcomeRequest();
        request.setEmail("manni.patel@gmail.com");

        for(int i = 0; i < 10; i++) {
            controller.welcome(request);
        }
    }
}