package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;

import java.util.List;

public interface PaymentSenseReportRepository extends MongoRepository<PaymentSenseReport,String> {
    List<PaymentSenseReport> findByRestaurantId(String restaurantId);
}
