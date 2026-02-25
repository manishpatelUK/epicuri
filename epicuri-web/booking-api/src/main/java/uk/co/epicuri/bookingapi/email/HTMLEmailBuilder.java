package uk.co.epicuri.bookingapi.email;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;

/**
 * Created by Manish on 17/06/2015.
 */
public class HTMLEmailBuilder {

    public static HTMLEmailBuilder newInstance() {
        return new HTMLEmailBuilder();
    }

    private final WebTarget webTarget;
    private final FormDataMultiPart formData;

    private HTMLEmailBuilder() {
        ClientConfig clientConfigMail = new ClientConfig();
        Client clientMail = ClientBuilder.newClient(clientConfigMail);
        clientMail.register(HttpAuthenticationFeature.basic("api", "key-40c450e79f813169a600b6e155e94b5c"));
        clientMail.register(MultiPartFeature.class);
        webTarget = clientMail.target("https://api.mailgun.net/v2/epicuri.email/messages");
        formData = new FormDataMultiPart();
    }

    public HTMLEmailBuilder subject(String subject) {
        formData.field("subject", subject);
        return this;
    }

    public HTMLEmailBuilder sender(String email) {
        formData.field("from", email);
        return this;
    }

    public HTMLEmailBuilder recipient(String email) {
        formData.field("to", email);
        return this;
    }

    public HTMLEmailBuilder line(String line) {
        formData.field("html", line);
        return this;
    }

    public HTMLEmailBuilder attach(File file, MediaType mediaType) {
        //formData.field("file",in,mediaType);
        formData.bodyPart(new FileDataBodyPart("attachment",file,mediaType));
        return this;
    }

    public void send() {
        webTarget.request().post(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
    }
}
