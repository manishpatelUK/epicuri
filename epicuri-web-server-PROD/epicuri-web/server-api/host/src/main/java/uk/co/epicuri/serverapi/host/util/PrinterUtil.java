package uk.co.epicuri.serverapi.host.util;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by manish on 15/07/2017.
 */
public class PrinterUtil {

    public static boolean isCyclic(Printer from, Printer to, Map<String,Printer> all) {
        return isCyclic(new ArrayList<>(), from, to, all);
    }

    public static boolean isCyclic(List<String> visited, Printer from, Printer to, Map<String,Printer> all) {
        visited.add(from.getId());

        if (recurse(visited, to, all)) {
            return true;
        }

        return false;
    }

    public static boolean recurse(List<String> visited, Printer to, Map<String, Printer> all) {
        if(StringUtils.isNotBlank(to.getRedirect())) {
            Printer next = all.get(getRedirectId(to.getRedirect()));
            if(visited.contains(next.getId())) {
                return true;
            } else {
                visited.add(next.getId());
                return recurse(visited, next, all);
            }
        }
        return false;
    }

    public static String redirectId(Printer printer1, Printer printer2) {
        return redirectId(printer1.getId(), printer2.getId());
    }

    public static String redirectId(String printer1, String printer2) {
        return printer1 + "-" + printer2;
    }

    public static String getRedirectId(String id) {
        return id.split("-")[1];
    }
}
