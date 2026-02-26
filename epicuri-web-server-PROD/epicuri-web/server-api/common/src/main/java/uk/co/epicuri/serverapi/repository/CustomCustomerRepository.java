package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public interface CustomCustomerRepository {
    void pushBlackMark(String id, BlackMark blackMark);
    void pushBadSessionBlackMark(String id);
    void pushBadSessionBlackMarks(List<String> ids);
    void pushCCData(String customerId, CreditCardData ccData);
    void pushLegalCommunicationSent(String customerId, Long time);
    void pushOptedIntoMarketing(String customerId, boolean optedIn);
    void setAuthKey(String customerId, String key);
    void setConfirmationCode(String customerId, String code);
    List<Customer> searchPhoneNumber(String number);
}
