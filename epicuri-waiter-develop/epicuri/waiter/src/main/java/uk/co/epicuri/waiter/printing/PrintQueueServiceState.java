package uk.co.epicuri.waiter.printing;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.co.epicuri.waiter.model.EpicuriPrintBatch;

import static uk.co.epicuri.waiter.utils.IdUtil.getIntId;

/**
 * Created by manish on 11/01/2018.
 */

public class PrintQueueServiceState {
    /** time of most recent refresh */
    public static long REFRESH_TIME = 0;
    /** time of most recent reply */
    public static long REPLY_TIME = 0;
    /** time of most recent printer refresh */
    public static long PRINTER_REFRESH_TIME = 0;

    private static int numberOfJobs = 0;
    private static final LinkedList<EpicuriPrintBatch> pendingJobs = new LinkedList<>();
    private static final LinkedList<EpicuriPrintBatch> completedJobs = new LinkedList<>();
    private static final SparseArray<EpicuriPrintBatch> errorJobs = new SparseArray<>();

    public static ChargeState LAST_KNOWN_CHARGE_STATE = ChargeState.UNKNOWN;
    public static boolean SWITCHED_ON_MANUALLY = false;

    public static int getNumberOfJobs() {
        return numberOfJobs;
    }

    public static void incrementNumberOfJobs() {
        numberOfJobs += 1;
    }

    public static void resetNumberOfJobs() {
        numberOfJobs = 0;
    }

    public static int getNumberOfErroredJobs() {
        return errorJobs.size();
    }

    public static void clearErroredJobs() {
        synchronized (errorJobs) {
            errorJobs.clear();
        }
    }

    public static void addErroredJob(EpicuriPrintBatch batch) {
        synchronized (errorJobs) {
            errorJobs.put(getIntId(batch.getId()), batch);
        }
    }

    public static void removeErroredJob(int id) {
        synchronized (errorJobs) {
            errorJobs.remove(id);
        }
    }

    public static boolean addPending(EpicuriPrintBatch batch) {
        synchronized(pendingJobs) {
            for(EpicuriPrintBatch job : pendingJobs) {
                if(job.getId() != null && job.getId().equals(batch.getId())) {
                    return false;
                }
            }

            return pendingJobs.add(batch);
        }
    }

    @NonNull
    public static List<EpicuriPrintBatch> getCompletedJobs() {
        synchronized (completedJobs) {
            return new ArrayList<>(completedJobs);
        }
    }

    public static void addCompletedJob(EpicuriPrintBatch job) {
        synchronized (completedJobs) {
            completedJobs.add(job);
        }
    }

    public static void clearCompletedJobs(List<EpicuriPrintBatch> jobs) {
        synchronized (completedJobs) {
            Iterator<EpicuriPrintBatch> it = completedJobs.iterator();
            while(it.hasNext()) {
                for(EpicuriPrintBatch job : jobs) {
                    if(it.next().getId().equals(job.getId())) {
                        it.remove();
                    }
                }
            }
        }
    }

    @NonNull
    public static List<EpicuriPrintBatch> getErroredJobs() {
        final List<EpicuriPrintBatch> result = new ArrayList<>(errorJobs.size());
        for(int i=0; i<errorJobs.size(); i++){
            result.add(errorJobs.valueAt(i));
        }
        return result;
    }

    public static void removeErroredJob(List<EpicuriPrintBatch> batches) {
        for(EpicuriPrintBatch batch : batches) {
            errorJobs.remove(getIntId(batch.getId()));
        }
    }

    public static EpicuriPrintBatch getJobAt(int id) {
        return errorJobs.get(id);
    }

    public static boolean anyPendingJobs() {
        return pendingJobs.size() > 0;
    }

    public static boolean anyErroredJobs() {
        return errorJobs.size() > 0;
    }

    public static EpicuriPrintBatch popPending() {
        synchronized (pendingJobs) {
            if (anyPendingJobs()) {
                return pendingJobs.pop();
            } else return null;
        }
    }
}
