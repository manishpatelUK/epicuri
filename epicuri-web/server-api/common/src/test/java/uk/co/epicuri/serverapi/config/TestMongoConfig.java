package uk.co.epicuri.serverapi.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by manish
 */
@Configuration
@EnableAutoConfiguration(exclude = { EmbeddedMongoAutoConfiguration.class })
@EnableMongoRepositories(basePackages = "uk.co.epicuri.serverapi.repository")
public class TestMongoConfig {
    private static final String DESTROY_METHOD_CLOSE = "close";
    private static final String DESTROY_METHOD_STOP = "stop";

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMongoConfig.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MongoProperties mongoProperties;

    //@Autowired(required = false)
    //private MongoClientOptions mongoClientOptions;

    @Autowired
    private Environment environment;

    @Bean(destroyMethod = DESTROY_METHOD_CLOSE)
    public MongoClient mongo() throws IOException {
        Net net = mongodProcess().getConfig().net();
        mongoProperties.setHost(net.getServerAddress().getHostName());
        mongoProperties.setPort(net.getPort());
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().writeConcern(WriteConcern.ACKNOWLEDGED).build();
        return mongoProperties.createMongoClient(mongoClientOptions, environment);
    }

    @Bean(destroyMethod = DESTROY_METHOD_STOP)
    public MongodProcess mongodProcess() throws IOException {
        return mongodExecutable().start();
    }

    @Bean(destroyMethod = DESTROY_METHOD_STOP)
    public MongodExecutable mongodExecutable() throws IOException {
        return mongodStarter().prepare(mongodConfig());
    }

    @Bean
    public IMongodConfig mongodConfig() throws IOException {
        return new MongodConfigBuilder().version(Version.Main.PRODUCTION).build();
    }

    @Bean
    public MongodStarter mongodStarter() {
        Command command = Command.MongoD;
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaultsWithLogger(command, LOGGER)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                        .defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command).build())
                        .executableNaming(new UserTempNaming()))
                .build();

        try {
            deleteOldMongoFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MongodStarter.getInstance(runtimeConfig);
    }

    private void deleteOldMongoFiles() throws IOException {
        String tempFile = System.getenv("temp") + File.separator + "extract-" + System.getenv("USERNAME") + "-extractmongod";
        String executable;
        if (System.getProperty("os.name").contains("Windows")) {
            executable = tempFile + ".exe";
        } else {
            executable = tempFile + ".sh";
        }
        Files.deleteIfExists(new File(executable).toPath());
        Files.deleteIfExists(new File(tempFile + ".pid").toPath());
    }
}
