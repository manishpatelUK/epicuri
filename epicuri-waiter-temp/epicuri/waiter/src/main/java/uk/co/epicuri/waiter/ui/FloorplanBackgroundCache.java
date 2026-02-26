package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.UUID;

import uk.co.epicuri.waiter.interfaces.BitmapCallback;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class FloorplanBackgroundCache {
    private static final String FLOORPLAN_BACKGROUND_CACHE = "FloorplanBCache";

    private final SharedPreferences prefs;
    private File cacheDir;

    private static class QueueItem {

        private QueueItem(String url, BitmapCallback callback, int width, int height) {
            this.url = url;
            this.callback = callback;
            this.width = width;
            this.height = height;
        }

        String url;
        BitmapCallback callback;
        int width;
        int height;
    }

    private LinkedList<QueueItem> bitmapQueue = new LinkedList<QueueItem>();

    private FloorplanBackgroundCache(Context c) {
        prefs = c.getSharedPreferences(GlobalSettings.PREF_FILE, Context.MODE_PRIVATE);

        cacheDir = c.getExternalCacheDir();
        if (null == cacheDir) {
            cacheDir = c.getCacheDir();
        }
    }

    private static FloorplanBackgroundCache mInstance;

    public static FloorplanBackgroundCache getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new FloorplanBackgroundCache(c);
        }
        return mInstance;
    }

    public Bitmap getCachedBitmap(String uri, int width, int height) {
        if (prefs.contains(uri)) {
            String cacheFilename = prefs.getString(uri, null);
            if (null != cacheFilename) {
                return getBitmapFromSD(cacheFilename, width, height);
            }
        }
        return null;
    }

    private Bitmap getBitmapFromSD(String filename, int width, int height) {
        File imageFile = new File(cacheDir, filename);

        if (width > 0 && height > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        }

        return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    int bitmapsLoading = 0;

    public void downloadAndCacheBitmap(String uri, BitmapCallback callback, int width, int height) throws IOException {

        bitmapQueue.add(new QueueItem(uri, callback, width, height));
        if (bitmapsLoading == 0) {
            loadQueueItem(bitmapQueue.removeFirst());
        }
        bitmapQueue.add(new QueueItem(uri, callback, width, height));
        bitmapQueue.add(new QueueItem(uri, callback, width, height));
        bitmapQueue.add(new QueueItem(uri, callback, width, height));
        bitmapQueue.add(new QueueItem(uri, callback, width, height));
        bitmapQueue.add(new QueueItem(uri, callback, width, height));
    }

    private void loadQueueItem(final QueueItem item) {
        bitmapsLoading++;
        new AsyncTask<String, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {
                if (!cacheDir.exists()) {
                    boolean success = cacheDir.mkdirs();
                    if (!success) {
                        throw new RuntimeException("Can't create cache dir");
                    }
                }

                String filename = UUID.randomUUID().toString();
                File outfile = new File(cacheDir, filename);
                if (outfile.exists()) {
                    throw new RuntimeException("UUID random filename already exists" + outfile.getName());
                }

                URL url;
                try {
                    Log.i(FLOORPLAN_BACKGROUND_CACHE, "Fetch " + item.url);
                    url = new URL(item.url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(20000);
                    connection.setRequestMethod("GET");

                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                        OutputStream out = new FileOutputStream(outfile);
                        try {
                            byte[] buffer = new byte[1024];
                            int count;
                            while (-1 < (count = inputStream.read(buffer))) {
                                out.write(buffer, 0, count);
                            }
                        } finally {
                            inputStream.close();
                            out.close();
                        }
                    }
                } catch (IOException e) {
                    Log.e(FLOORPLAN_BACKGROUND_CACHE, "Could not get floorplan", e);
                    return null;
                }
                prefs.edit().putString(item.url, filename).apply();

                if (item.width > 0 && item.height > 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(outfile.getAbsolutePath(), options);
                    options.inSampleSize = calculateInSampleSize(options, item.width, item.height);

                    options.inJustDecodeBounds = false;

                    return BitmapFactory.decodeFile(outfile.getAbsolutePath(), options);
                }

                return BitmapFactory.decodeFile(outfile.getAbsolutePath());
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                item.callback.onBitmapLoaded(result);
                bitmapQueue.remove(item);

                bitmapsLoading--;

                if (!bitmapQueue.isEmpty() && bitmapsLoading == 0) {
                    loadQueueItem(bitmapQueue.removeFirst());

                }
            }

        }.execute(item.url);


    }
}