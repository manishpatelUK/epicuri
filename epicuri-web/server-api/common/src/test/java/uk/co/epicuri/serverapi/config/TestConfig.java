package uk.co.epicuri.serverapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.co.epicuri.serverapi.auth.AuthorizationInterceptor;
import uk.co.epicuri.serverapi.auth.VersionCheckInterceptor;
import uk.co.epicuri.serverapi.spring.CsvMessageConverter;
import uk.co.epicuri.serverapi.spring.WebMvcConfig;
import uk.co.epicuri.serverapi.spring.XlsMessageConverter;

import java.util.List;

/**
 * Created by manish
 */
@Configuration
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class})
@ComponentScan(value = {
        "uk.co.epicuri.serverapi.auth",
        "uk.co.epicuri.serverapi.client.endpoints",
        "uk.co.epicuri.serverapi.config",
        "uk.co.epicuri.serverapi.engines",
        "uk.co.epicuri.serverapi.host.endpoints",
        "uk.co.epicuri.serverapi.pojo",
        "uk.co.epicuri.serverapi.repository",
        "uk.co.epicuri.serverapi.service"})
@EnableMongoRepositories(basePackages = {"uk.co.epicuri.serverapi.repository"})
@EnableWebMvc
public class TestConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    private VersionCheckInterceptor versionCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(versionCheckInterceptor);
        registry.addInterceptor(authorizationInterceptor);
    }

    /*@Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }*/

    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
        //configurer.defaultContentType(MediaType.APPLICATION_JSON);
        //configurer.mediaType("jpg", MediaType.APPLICATION_OCTET_STREAM);
        //configurer.mediaType("png", MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
        converters.add(customCsvMessageConverter());
        converters.add(customXlsMessageConverter());
        WebMvcConfig.configureMessageConverters(converters);
        super.configureMessageConverters(converters);
    }

    @Bean
    public CsvMessageConverter customCsvMessageConverter() {
        return new CsvMessageConverter();
    }

    @Bean
    public XlsMessageConverter customXlsMessageConverter() {
        return new XlsMessageConverter();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
}
