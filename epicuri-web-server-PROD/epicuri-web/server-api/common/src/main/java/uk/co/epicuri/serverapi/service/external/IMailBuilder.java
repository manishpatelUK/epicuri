package uk.co.epicuri.serverapi.service.external;

import java.io.File;
import java.io.InputStream;

public interface IMailBuilder {
    IMailBuilder from(String name);

    IMailBuilder from(String name, String email);

    IMailBuilder to(String email);

    IMailBuilder subject(String subject);

    IMailBuilder body(String body);

    IMailBuilder attach(File file);

    IMailBuilder attach(InputStream inputStream);

    boolean build();
}
