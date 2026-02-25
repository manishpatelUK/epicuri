package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public class CustomerRepositoryImpl implements CustomCustomerRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public void pushBlackMark(String id, BlackMark blackMark) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                new Update().addToSet("blackMarks", blackMark), Customer.class);
    }

    @Override
    public void pushBadSessionBlackMark(String id) {
        BlackMark blackMark = createBadSessionBlackMark();

        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                new Update().addToSet("blackMarks", blackMark), Customer.class);
    }

    @Override
    public void pushBadSessionBlackMarks(List<String> ids) {
        BlackMark blackMark = createBadSessionBlackMark();

        operations.updateMulti(Query.query(Criteria.where("_id").in(ids)),
                new Update().addToSet("blackMarks", blackMark), Customer.class);
    }

    @Override
    public void pushCCData(String customerId, CreditCardData ccData) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(customerId)),
                Update.update("ccData", ccData), Customer.class);
    }

    @Override
    public void pushLegalCommunicationSent(String customerId, Long time) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(customerId)),
                Update.update("legalEmailSent", time), Customer.class);
    }

    @Override
    public void pushOptedIntoMarketing(String customerId, boolean optedIn) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(customerId)),
                Update.update("optedIntoMarketing", optedIn), Customer.class);
    }

    @Override
    public void setAuthKey(String customerId, String key) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(customerId)),
                Update.update("authKey", key), Customer.class);
    }

    @Override
    public void setConfirmationCode(String customerId, String code) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(customerId)),
                Update.update("confirmationCode", code), Customer.class);
    }

    @Override
    public List<Customer> searchPhoneNumber(String number) {
        Query query = new Query();
        query.limit(10);
        query.addCriteria(Criteria.where("phoneNumber").regex(".*" + number));

        return operations.find(query, Customer.class);
    }

    private static BlackMark createBadSessionBlackMark() {
        BlackMark blackMark = new BlackMark();
        blackMark.setTime(System.currentTimeMillis());
        blackMark.setReason("Bad Session");
        return blackMark;
    }
}
