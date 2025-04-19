package com.rfid.hf;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;


public class MyService extends Service {

    PowerManager.WakeLock mWakeLock;// 电源锁
    public MyService() {

    }
    private NotificationManager notificationManager;
    /**
     * 把服务设置成前台，提升优先级
     * @param context
     * @return
     */
    private static final String TAG = "LocalService";
    private IBinder binder=new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //定义内容类继承Binder
    public class LocalBinder extends Binder {
        //返回本地服务
        MyService getService(){
            return MyService.this;
        }
    }
    volatile boolean mWorking;
    Thread mThread;
    volatile static boolean rfClosed =false;
    private static  long curTime;

    public static void setTime(long Time)
    {
        curTime = Time;
    }

    public static long GetTime()
    {
        return curTime;
    }

    public static void SetRfStatus(boolean onOff)
    {
        rfClosed = onOff;
    }

    public static boolean GetRfStatus()
    {
        return rfClosed;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWorking =true;
        setTime(System.currentTimeMillis());
        startThread();
        //acquireWakeLock();
       // notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       // startForeground(1,getNotification());
    }


    void startThread()
    {

        if(mThread==null) {
            mWorking =true;
            mThread = new Thread() {
                public void run() {
                    while (mWorking) {
                        try{
                            if((System.currentTimeMillis() - GetTime())>1000*10)
                            {
                                if(GetRfStatus())
                                {
                                    HfData.reader.CloseRf();
                                    setTime(System.currentTimeMillis());
                                }
                            }
                            SystemClock.sleep(5000);
                        }catch(Exception ex)
                        {
                            ex.toString();
                        }
                    }
                    mThread = null;
                }
            };
            mThread.start();
        }
    }

    /*private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.menu_icon)
                .setContentTitle("蓝牙服务")
                .setContentText("可通过蓝牙修改RFID信息");

        Notification notification = builder.build();
        return notification;
    }*/

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == mWakeLock) {
          try{
              PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
              mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK ,"MyService");
              if (null != mWakeLock) {
                  mWakeLock.acquire();
              }
          }catch(Exception ex)
          {

          }
        }
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        // 注销action接收器
        mWorking=false;
    }

}
