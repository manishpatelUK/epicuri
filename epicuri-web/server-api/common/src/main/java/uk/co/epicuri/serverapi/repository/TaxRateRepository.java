package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface TaxRateRepository extends MongoRepository<TaxRate,String> {
    List<TaxRate> findByCountryId(String countryId);
}
