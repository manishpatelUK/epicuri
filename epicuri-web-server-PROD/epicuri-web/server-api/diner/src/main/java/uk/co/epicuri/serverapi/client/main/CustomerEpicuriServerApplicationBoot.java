package uk.co.epicuri.serverapi.client.main;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import uk.co.epicuri.serverapi.CommonEpicuriBeanUtil;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.Executor;

/**
 * Created by manish
 */
@SpringBootApplication
@EnableAsync
@EnableMongoRepositories(mongoTemplateRef = "mongoTemplate", basePackages = "uk.co.epicuri.serverapi.repository")
public class CustomerEpicuriServerApplicationBoot extends SpringBootServletInitializer {

    @Autowired
    private MongoDbFactory mongoDbFactory;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CustomerEpicuriServerApplicationBoot.class, args);
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        return CommonEpicuriBeanUtil.createLoggingFilter();
    }

    @Bean(name = "mongoTemplate")
    public MongoTemplate getMongoTemplate() throws UnknownHostException {
        return new MongoTemplate(mongoDbFactory);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
