package uk.co.epicuri.serverapi.client.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.co.epicuri.serverapi.auth.AuthorizationInterceptor;
import uk.co.epicuri.serverapi.auth.VersionCheckInterceptor;
import uk.co.epicuri.serverapi.spring.WebMvcConfig;

import java.util.List;

/**
 * Created by manish
 */
@EnableWebMvc
@ComponentScan(basePackages = {"uk.co.epicuri.serverapi.client.endpoints",
        "uk.co.epicuri.serverapi.repository",
        "uk.co.epicuri.serverapi.service",
        "uk.co.epicuri.serverapi.auth"})
@Configuration
@SuppressWarnings("SpringJavaAutowiringInspection")
public class CustomerWebMvcConfig extends WebMvcConfigurerAdapter{

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    private VersionCheckInterceptor versionCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(versionCheckInterceptor);
        registry.addInterceptor(authorizationInterceptor);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        WebMvcConfig.configureMessageConverters(converters);
        super.configureMessageConverters(converters);
    }
}
