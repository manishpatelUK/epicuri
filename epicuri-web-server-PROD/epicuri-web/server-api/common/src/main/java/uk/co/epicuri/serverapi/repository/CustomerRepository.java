package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

/**
 * Created by manish
 */
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String>, CustomCustomerRepository{
    Customer findByEmail(String email);
    Customer findByPhoneNumber(String phoneNumber);
}
