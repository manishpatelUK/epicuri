package uk.co.epicuri.waiter.service;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.service.interfaces.ILoggingListener;

public class LoggerAsyncTask extends AsyncTask<Void, Long, List<String>> {
    private final ILoggingListener iLoggingListener;

    public LoggerAsyncTask(ILoggingListener iLoggingListener) {
        this.iLoggingListener = iLoggingListener;
    }

    public void run() {

    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        BufferedReader bufferedReader = null;
        Process process = null;
        List<String> log = new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec("logcat -t 10000 uk.co.epicuri.waiter:V");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.add(line);
            }
        }
        catch (IOException e) {
            Log.e("LoggerAsyncTask", e.getMessage());
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {}
            }
            if(process != null) {
                try {
                    process.destroy();
                } catch (Exception e){}
            }
        }

        return log;
    }

    @Override
    protected void onPostExecute(List<String> strings) {
        iLoggingListener.onLoggingComplete(strings);
    }
}
