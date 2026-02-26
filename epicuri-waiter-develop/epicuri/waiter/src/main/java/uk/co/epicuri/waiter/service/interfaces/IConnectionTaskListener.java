package uk.co.epicuri.waiter.service.interfaces;

import java.net.URL;

public interface IConnectionTaskListener {
    void onDownloadTaskFinished(Long timeTaken);
}
