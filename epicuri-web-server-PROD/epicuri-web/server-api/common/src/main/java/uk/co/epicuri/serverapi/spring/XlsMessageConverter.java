package uk.co.epicuri.serverapi.spring;


import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.ExcelWrapper;

import java.io.IOException;
import java.io.OutputStream;

public class XlsMessageConverter extends AbstractHttpMessageConverter<ExcelWrapper> {

    public XlsMessageConverter() {
        super(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return ExcelWrapper.class.equals(aClass);
    }

    @Override
    protected ExcelWrapper readInternal(Class<? extends ExcelWrapper> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(ExcelWrapper wrapper, HttpOutputMessage output) throws IOException, HttpMessageNotWritableException {
        output.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
        output.getHeaders().set("Content-Disposition", "attachment; filename=\""+ wrapper.getFileName() + "\"");
        OutputStream out = output.getBody();
        wrapper.getWorkbook().write(out);
    }
}
