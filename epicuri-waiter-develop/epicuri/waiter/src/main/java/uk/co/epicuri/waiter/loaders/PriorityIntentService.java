package uk.co.epicuri.waiter.loaders;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * http://www.anddev.org/code-snippets-for-android-f33/service-with-priority-queue-t53678.html
 * @author Pete Harris <peteh@thedistance.co.uk>
 */
public abstract class PriorityIntentService extends Service {

   protected final class QueueItem implements Comparable<QueueItem> {
      public Intent intent;
      public int priority;
      public int startId;
      
      @Override
      public int compareTo(QueueItem another) {
         if (this.priority < another.priority) {
            return -1;
         } else if (this.priority > another.priority) {
            return 1;
         } else {
            return (this.startId < another.startId) ? -1 : 1;
         }
      }
   }
   private final class ServiceHandler extends Handler {
      public ServiceHandler(Looper looper) {
         super(looper);
      }

      @Override
      public void handleMessage(Message msg) {
         try {
            final QueueItem item = mQueue.take();
            Log.d("UpdateService", "launching item with priority " + item.priority);
            onHandleIntent(item.intent);
            if (mQueue.isEmpty()) {
               PriorityIntentService.this.stopSelf();
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   public static final String EXTRA_PRIORITY = "priority";
   private String mName;
   private PriorityBlockingQueue<QueueItem> mQueue;
   private boolean mRedelivery;

   private volatile ServiceHandler mServiceHandler;

   private volatile Looper mServiceLooper;

   public PriorityIntentService(String name) {
      super();
      mName = name;
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      HandlerThread thread = new HandlerThread("PriorityIntentService[" + mName + "]");
      thread.start();

      mServiceLooper = thread.getLooper();
      mServiceHandler = new ServiceHandler(mServiceLooper);
      mQueue = new PriorityBlockingQueue<QueueItem>();
   }

   @Override
   public void onDestroy() {
      mServiceLooper.quit();
   }

   protected abstract void onHandleIntent(Intent intent);

   /**
    * Determines if a new item should be added to the queue or not.
    * Subclasses may override this method to avoid adding an intent to the queue.
    * The default implementation always returns true.
    * @param item The item to add to the queue.
    * @param queue The queue.
    * @return True if the item should be added to the queue, false otherwise.
    */
   protected boolean shouldAddToQueue(QueueItem item, PriorityBlockingQueue<QueueItem> queue) {
      return true;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
	   // cope with restart; receive null intent
  //     if(null == intent) this.stopSelf();
       final QueueItem item = new QueueItem();
       item.intent = intent;
       item.startId = startId;

      if(intent != null){
         final int priority = intent.getIntExtra(EXTRA_PRIORITY, 0);
         item.priority = priority;
      }
       if (this.shouldAddToQueue(item, this.mQueue)) {
           mQueue.add(item);
           mServiceHandler.sendEmptyMessage(0);
       }
	   return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
   }

   public void setIntentRedelivery(boolean enabled) {
      mRedelivery = enabled;
   }
}