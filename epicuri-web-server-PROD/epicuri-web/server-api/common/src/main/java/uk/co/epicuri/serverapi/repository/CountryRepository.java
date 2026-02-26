package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Country;

/**
 * Created by manish
 */
@Repository
public interface CountryRepository extends MongoRepository<Country, String>{
}
