package uk.co.epicuri.bookingapi;

import com.exxeleron.qjava.QConnection;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.bookingapi.endpoints.aurusit.AurusitController;
import uk.co.epicuri.bookingapi.endpoints.auth.LogonController;
import uk.co.epicuri.bookingapi.endpoints.booking.Reserve;
import uk.co.epicuri.bookingapi.endpoints.booking.Statics;
import uk.co.epicuri.bookingapi.endpoints.booking.TimeSlots;
import uk.co.epicuri.bookingapi.endpoints.menu.MenuController;
import uk.co.epicuri.bookingapi.endpoints.msg.EmailController;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * 30/04/2015
 */
public class BookingAPIApplication extends Application<BookingAPIConfiguration> {

    public static void main(String[] args) throws Exception {
        new BookingAPIApplication().run(args);
    }

    @Override
    public void run(BookingAPIConfiguration bookingAPIConfiguration, Environment environment) throws Exception {
        EpicuriAPI epicuriAPI = bookingAPIConfiguration.getEpicuriFactory().build(environment);
        QConnection tickerplantConnection = bookingAPIConfiguration.getKxTickerplantFactory().build(environment);
        QConnection securitydbConnection = bookingAPIConfiguration.getKxSecurityDBFactory().build(environment);
        QConnection staticsdbConnection = bookingAPIConfiguration.getKxStaticsFactory().build(environment);
        QConnection aurusitdbConnection = bookingAPIConfiguration.getKxAurusitDBFactory().build(environment);


        final DatabaseHealthCheck databaseHealthCheck = new DatabaseHealthCheck();
        databaseHealthCheck.add(tickerplantConnection);
        databaseHealthCheck.add(securitydbConnection);
        databaseHealthCheck.add(staticsdbConnection);

        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,X-Auth-Token");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        Reserve reserve = new Reserve(epicuriAPI,securitydbConnection,tickerplantConnection,staticsdbConnection);
        Statics statics = new Statics(epicuriAPI,securitydbConnection,staticsdbConnection);
        TimeSlots timeSlots = new TimeSlots(epicuriAPI,securitydbConnection,staticsdbConnection);
        LogonController logonController = new LogonController(epicuriAPI,securitydbConnection);
        MenuController menuController = new MenuController(epicuriAPI,securitydbConnection);
        EmailController emailController = new EmailController(staticsdbConnection);
        AurusitController aurusitController = new AurusitController(epicuriAPI,securitydbConnection,aurusitdbConnection);

        environment.jersey().register(statics);
        environment.jersey().register(reserve);
        environment.jersey().register(timeSlots);
        environment.jersey().register(logonController);
        environment.jersey().register(menuController);
        environment.jersey().register(emailController);
        environment.jersey().register(aurusitController);
        environment.healthChecks().register("database",databaseHealthCheck);
    }
}
