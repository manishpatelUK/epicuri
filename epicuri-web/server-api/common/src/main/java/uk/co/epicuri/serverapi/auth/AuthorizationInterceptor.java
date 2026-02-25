package uk.co.epicuri.serverapi.auth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.service.AuthenticationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Created by manish
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Autowired
    private AuthenticationService authenticationService;

    public AuthorizationInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            String token = extractToken(request);
            LOGGER.trace("Pre-auth for Method {} with token {}", method.getName(), token);
            if(token != null && token.startsWith("Basic ")) {
                token = AuthenticationService.stripOffBasic(token);
            }
            if(method.isAnnotationPresent(HostAuthRequired.class) || method.isAnnotationPresent(HostLevelCheckRequired.class)) {
                if(!authenticationService.verifyStaffToken(token)) {
                    LOGGER.debug("Unauthorized staff access {}", token);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            } else if (method.isAnnotationPresent(CustomerAuthRequired.class)) {
                if(!authenticationService.verifyCustomerToken(token)) {
                    LOGGER.debug("Unauthorized customer access {}", token);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            } else if (method.isAnnotationPresent(OnlineOrderingAuthRequired.class)) {
                if(!authenticationService.verifyOnlineOrderingToken(token)) {
                    LOGGER.debug("Unauthorized online order access {}", token);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            }

            if(method.isAnnotationPresent(HostLevelCheckRequired.class)) {
                HostLevelCheckRequired hostLevelCheckRequired = method.getAnnotation(HostLevelCheckRequired.class);
                if(hostLevelCheckRequired.role().getSecurityLevel() < authenticationService.getStaffSecurityLevel(token).getSecurityLevel()) {
                    LOGGER.debug("Unauthorized access (host level) {}, required level: {} but was: {}", token, hostLevelCheckRequired.role().getSecurityLevel(), authenticationService.getStaffSecurityLevel(token));
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            }

            if(method.isAnnotationPresent(EpicuriAuthRequired.class)) {
                if(!authenticationService.verifyAdminToken(token)) {
                    LOGGER.debug("Unauthorized epicuri admin access {}", token);
                    return false;
                }
            }
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(Params.AUTHORIZATION);
        if(StringUtils.isNotBlank(token)) return token;

        token = request.getParameter("Auth");
        if(StringUtils.isNotBlank(token)) return token;

        token = request.getParameter("token");
        if(StringUtils.isNotBlank(token)) return token;

        return null;
    }


}
