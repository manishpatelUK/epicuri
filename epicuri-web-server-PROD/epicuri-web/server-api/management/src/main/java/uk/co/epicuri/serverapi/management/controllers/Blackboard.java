package uk.co.epicuri.serverapi.management.controllers;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by manish
 */
public class Blackboard {
    private static final Map<String,Refreshable> REFRESHABLE_MAP = new HashMap<>();
    public static List<String> TIMEZONES = getTimeZones();
    private List<String> timeZones;

    @SuppressWarnings("unchecked")
    public static <T extends Refreshable> T get(Class<T> clazz) {
        return (T)REFRESHABLE_MAP.get(clazz.getName());
    }

    public static void put(Refreshable refreshable) {
        REFRESHABLE_MAP.put(refreshable.getClass().getName(), refreshable);
    }

    public static Collection<Refreshable> get() {
        return REFRESHABLE_MAP.values();
    }

    private static List<String> getTimeZones() {
        return getList("/timezones.txt");
    }

    private static List<String> getList(String file) {
        List<String> list = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Blackboard.class.getResourceAsStream(file)))) {
            while(reader.ready()) {
                final String string = reader.readLine().trim();
                if(StringUtils.isNotBlank(string)) {
                    list.add(string);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }
}
