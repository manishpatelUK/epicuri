package uk.co.epicuri.serverapi;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Created by manish.
 */
public class CommonEpicuriBeanUtil {
    public static CommonsRequestLoggingFilter createLoggingFilter() {
        CommonsRequestLoggingFilter crlf = new CommonsRequestLoggingFilter();
        crlf.setIncludeClientInfo(true);
        crlf.setIncludeQueryString(true);
        crlf.setIncludePayload(true);
        return crlf;
    }
}
