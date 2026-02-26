package com.thinktouchsee.bookingwidget;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;

import com.google.gson.Gson;
import com.google.gwt.user.client.Cookies;
import com.thinktouchsee.bookingwidget.ui.BookingWidgetLayout;
import com.vaadin.annotations.*;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.jasypt.util.password.StrongPasswordEncryptor;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Restaurant;

import java.io.IOException;
import java.util.*;

@Theme("trackbit")
//@PreserveOnRefresh
//@Push
//@SuppressWarnings("serial")
public class BookingWidget extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = BookingWidget.class, widgetset = "com.thinktouchsee.AppWidgetSet")
    public static class Servlet extends VaadinServlet {

    }

    @Override
    protected void init(VaadinRequest request) {
        setLocale(Locale.UK);

        //Cookie[] cookies = request.getCookies();


        // get restaurant id
        // if not present, show a warning notification and cut off the connection after 5 seconds
        String id = request.getParameter("id");
        String var = request.getParameter("var");
        String tz = request.getParameter("tz");
        String env = request.getParameter("env");
        if(StringUtils.isBlank(id)) {
            errorAndClose("No Restaurant ID","Please contact the webmaster");
        }
        if(StringUtils.isBlank(tz)) {
            tz = "Europe/London";
        }

        List<String> sendEmails = new ArrayList<String>();
        if(StringUtils.isNotBlank(var)) {
            try {
                Encryptor encryptor = new Encryptor();
                sendEmails.addAll(encryptor.decrypt(var.trim()));
            }
            catch(Exception ex) {
                System.out.println("ERROR:Could not parse email addresses");
            }
        }

        EpicuriAPI.Environment environment = EpicuriAPI.Environment.PROD;

        if(StringUtils.isNotBlank(env)){
            if(env.equalsIgnoreCase("prod")) {
                environment = EpicuriAPI.Environment.PROD;
            }
            else if(env.equalsIgnoreCase("dev")) {
                environment = EpicuriAPI.Environment.DEV;
            }
            else if(env.equalsIgnoreCase("staging")) {
                environment = EpicuriAPI.Environment.STAGING;
            }
        }

        EpicuriAPI api = new EpicuriAPI(environment);

        setWidth("311px");
        setHeight("300px");

        createBookingWidget(api,id,tz,sendEmails);
    }

    private void errorAndClose(String caption, String message) {
        Notification.show(caption, message, Notification.Type.ERROR_MESSAGE);
        try {
            Thread.sleep(5000);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            close();
        }
    }

    private void createBookingWidget(EpicuriAPI api,String id, String tz, Collection<String> sendEmails) {
        if(id == null) {
            return;
        }

        String[] ids = id.split(",");
        List<Restaurant> list = new ArrayList<Restaurant>();
        for(String anId : ids) {
            Restaurant restaurant = null;
            try {
                int intId = Integer.parseInt(anId);
                restaurant = api.getRestaurant(intId);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }
            list.add(restaurant);
        }

        if(list.size() == 0) {
            errorAndClose("No Restaurant ID","Please contact the webmaster");
        }
        else {
            CssLayout root = new CssLayout();
            root.setSizeFull();
            setContent(root);
            root.addComponent(new BookingWidgetLayout(list, api, tz, sendEmails));
        }
    }
}
