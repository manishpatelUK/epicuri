package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.booking.BookingStatics;

@Repository
public interface BookingStaticsRepository extends MongoRepository<BookingStatics, String>{
    BookingStatics findByLanguage(String language);
}
