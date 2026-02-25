package uk.co.epicuri.serverapi.host.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.co.epicuri.serverapi.auth.AuthorizationInterceptor;
import uk.co.epicuri.serverapi.auth.VersionCheckInterceptor;
import uk.co.epicuri.serverapi.host.interceptors.ResponseTimeInterceptor;
import uk.co.epicuri.serverapi.spring.CsvMessageConverter;
import uk.co.epicuri.serverapi.spring.WebMvcConfig;
import uk.co.epicuri.serverapi.spring.XlsMessageConverter;

import java.util.List;

/**
 * Created by manish
 */
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = {"uk.co.epicuri.serverapi.host.endpoints",
        "uk.co.epicuri.serverapi.repository",
        "uk.co.epicuri.serverapi.service",
        "uk.co.epicuri.serverapi.auth",
        "uk.co.epicuri.serverapi.host.schedules",
        "uk.co.epicuri.serverapi.service.external",
        "uk.co.epicuri.serverapi.host.interceptors"})
@Configuration
@SuppressWarnings("SpringJavaAutowiringInspection")
public class HostWebMvcConfig extends WebMvcConfigurerAdapter{

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    private VersionCheckInterceptor versionCheckInterceptor;

    @Autowired
    private ResponseTimeInterceptor responseTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(versionCheckInterceptor);
        registry.addInterceptor(authorizationInterceptor);
        registry.addInterceptor(responseTimeInterceptor);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customCsvMessageConverter());
        converters.add(customXlsMessageConverter());
        WebMvcConfig.configureMessageConverters(converters);
        super.configureMessageConverters(converters);
    }

    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Bean
    public CsvMessageConverter customCsvMessageConverter() {
        return new CsvMessageConverter();
    }

    @Bean
    public XlsMessageConverter customXlsMessageConverter() {
        return new XlsMessageConverter();
    }
}
