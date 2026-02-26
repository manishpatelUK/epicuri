package uk.co.epicuri.waiter.service;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import uk.co.epicuri.waiter.service.interfaces.IConnectionTaskListener;
import uk.co.epicuri.waiter.service.interfaces.IEpicuriConnectionListener;

public class DownloadTask extends AsyncTask<URL,Integer,Long> {
    private final IConnectionTaskListener connectionTaskListener;

    public DownloadTask(IConnectionTaskListener connectionTaskListener) {
        this.connectionTaskListener = connectionTaskListener;
    }

    @Override
    protected Long doInBackground(URL... urls) {
        InputStream is = null;
        try {
            long start = System.currentTimeMillis();
            HttpURLConnection con = (HttpURLConnection) urls[0].openConnection();
            is = con.getInputStream();
            while (is.available() > 0) {
                is.read(new byte[1024]);
            }
            return System.currentTimeMillis()-start;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        if(connectionTaskListener != null) {
            connectionTaskListener.onDownloadTaskFinished(result);
        }
    }
}
