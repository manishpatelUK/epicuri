package uk.co.epicuri.serverapi.common.pojo.host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manish.
 */
public class BasicBusinessIntelligenceReport {
    private Map<String,List<PopularItem>> popularItemsReport = new HashMap<>();
    private Map<String,Map<String,Double>> averageSessionsReport = new HashMap<>();
    private Map<String,Map<String,Double>> averageItemsReport = new HashMap<>();
    private EpicuriSalesReport epicuriSalesReport = new EpicuriSalesReport();

    public Map<String, List<PopularItem>> getPopularItemsReport() {
        return popularItemsReport;
    }

    public void setPopularItemsReport(Map<String, List<PopularItem>> popularItemsReport) {
        this.popularItemsReport = popularItemsReport;
    }

    public Map<String, Map<String, Double>> getAverageSessionsReport() {
        return averageSessionsReport;
    }

    public void setAverageSessionsReport(Map<String, Map<String, Double>> averageSessionsReport) {
        this.averageSessionsReport = averageSessionsReport;
    }

    public Map<String, Map<String, Double>> getAverageItemsReport() {
        return averageItemsReport;
    }

    public void setAverageItemsReport(Map<String, Map<String, Double>> averageItemsReport) {
        this.averageItemsReport = averageItemsReport;
    }

    public EpicuriSalesReport getEpicuriSalesReport() {
        return epicuriSalesReport;
    }

    public void setEpicuriSalesReport(EpicuriSalesReport epicuriSalesReport) {
        this.epicuriSalesReport = epicuriSalesReport;
    }

    public static class PopularItem {
        private int number;
        private String name;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class EpicuriSalesReport {
        private int items = 0;
        private int takeaways = 0;
        private int reservations = 0;
        private String revenue = "0.00";

        public int getItems() {
            return items;
        }

        public void setItems(int items) {
            this.items = items;
        }

        public int getTakeaways() {
            return takeaways;
        }

        public void setTakeaways(int takeaways) {
            this.takeaways = takeaways;
        }

        public int getReservations() {
            return reservations;
        }

        public void setReservations(int reservations) {
            this.reservations = reservations;
        }

        public String getRevenue() {
            return revenue;
        }

        public void setRevenue(String revenue) {
            this.revenue = revenue;
        }
    }
}
