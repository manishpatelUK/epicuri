package uk.co.epicuri.serverapi.spring;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.CSVWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Created by manish.
 */
public class CsvMessageConverter extends AbstractHttpMessageConverter<CSVWrapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvMessageConverter.class);

    public final static String TEXT_CSV = "text/csv";
    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));

    public CsvMessageConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return CSVWrapper.class.equals(aClass);
    }

    @Override
    protected CSVWrapper readInternal(Class<? extends CSVWrapper> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(CSVWrapper wrapper, HttpOutputMessage output) throws IOException, HttpMessageNotWritableException {
        if(StringUtils.isBlank(wrapper.getContent())) {
            LOGGER.trace("No data to write for report");
        }

        output.getHeaders().setContentType(MEDIA_TYPE);
        output.getHeaders().set("Content-Disposition", "attachment; filename=\""+ wrapper.getFileName() + "\"");
        try (OutputStreamWriter writer = new OutputStreamWriter(output.getBody())) {
            IOUtils.write(wrapper.getContent(), writer);
        }
    }
}
