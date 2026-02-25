package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public interface CustomBatchRepository extends DeletableRepository {
    void pushSpoolTime(List<String> batchIds, long time);
    void spoolForBatchPrinting(List<String> batchIds, long time);
    void setPrintedTime(List<String> batchIds, long time);
    void pushTimeToPrint(List<String> batchIds, long time);
    void setImmediatePrintFlag(List<String> batchIds, boolean flag);
    void setPrinterId(List<String> batchIds, String printerId);
}
