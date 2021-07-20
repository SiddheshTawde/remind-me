package com.remindme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RemindMeService extends Service {
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        List<AppWidgetProviderInfo> appWidgetIds = appWidgetManager.getInstalledProviders();
        Log.println(Log.DEBUG, "AppWidgetProviderList", String.valueOf(appWidgetIds));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startServiceInForeground(0, (float) 0.00);
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startServiceInForeground(long day, Float progress) {
        String NOTIFICATION_CHANNEL_ID = "remind.me";
        String channelName = "Background Service";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.update_48dp)
                .setContentTitle("Day " + day)
                .setContentText("Progress " + String.format("%.2f", (progress * 100)) + "%")
                .setProgress(100, (int) (progress * 100), false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationManager.IMPORTANCE_NONE)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.REMIND_ME_STORAGE_KEY), Context.MODE_PRIVATE);

        primaryService(sharedPreferences);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopProcess();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("REMIND_ME_RESTART_SERVICE");
        broadcastIntent.setClass(this, RemindMeReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }

    private void primaryService(SharedPreferences sharedPreferences) {
        timer = new Timer();
        long FULL_DAY = 24 * 60 * 60 * 1000; // 24 Hour Day in Milliseconds

        SimpleDateFormat dateFormat = new SimpleDateFormat("d-MM-yyyy HH:mm:ss");

        TimerTask timerTask = new TimerTask() {

            public void run() {
                Calendar present_day = Calendar.getInstance();
                present_day.set(Calendar.HOUR_OF_DAY, 0);
                present_day.set(Calendar.MINUTE, 0);
                present_day.set(Calendar.SECOND, 0);
                present_day.set(Calendar.MILLISECOND, 0);

                Calendar elapsed_time = Calendar.getInstance();
                long seconds = elapsed_time.getTimeInMillis() - present_day.getTimeInMillis();

                if (sharedPreferences.getString("Day", null) == null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Day", dateFormat.format(present_day.getTime()));
                    editor.putFloat("Progress", (float) seconds / FULL_DAY);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Day", sharedPreferences.getString("Day", dateFormat.format(present_day.getTime())));
                    editor.putFloat("Progress", (float) seconds / FULL_DAY);
                    editor.apply();
                }

                String dateFromPrefs = sharedPreferences.getString("Day", null);
                Calendar formattedDate = Calendar.getInstance();

                try {
                    formattedDate.setTime(dateFormat.parse(dateFromPrefs));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long day = (Calendar.getInstance().getTimeInMillis() - formattedDate.getTimeInMillis()) / (60 * 60 * 24 * 1000);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startServiceInForeground(day, (float) seconds / FULL_DAY);
                else
                    startForeground(1, new Notification());
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void stopProcess() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}