package uk.co.epicuri.serverapi.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.co.epicuri.serverapi.Params;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by manish
 */
@Component
public class VersionCheckInterceptor extends HandlerInterceptorAdapter  {

    @Value("${epicuri.version.minimum}")
    private int minimumSupportedVersion = 0;

    @Value("${epicuri.version.current}")
    private int currentVersion = 0;

    public VersionCheckInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.addHeader(Params.HEADER_EPICURI_API, String.valueOf(currentVersion));

        if(StringUtils.isNotBlank(request.getHeader(Params.HEADER_EPICURI_API))) {
            int version = Integer.valueOf(request.getHeader(Params.HEADER_EPICURI_API));
            if(version < minimumSupportedVersion) {
                response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
                response.addHeader(Params.HEADER_EPICURI_API, String.valueOf(currentVersion));
                return false;
            }
        } /*else {
            response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            response.addHeader(Params.HEADER_EPICURI_API, String.valueOf(currentVersion));
            return false;
        }*/
        return true;
    }
}
