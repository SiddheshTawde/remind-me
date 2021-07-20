package com.remindme;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of App Widget functionality.
 */
public class RemindMeWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("d-MM-yyyy HH:mm:ss");

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.REMIND_ME_STORAGE_KEY), Context.MODE_PRIVATE);

        String dateFromPrefs = sharedPreferences.getString("Day", null);
        Calendar formattedDate = Calendar.getInstance();

        try {
            formattedDate.setTime(dateFormat.parse(dateFromPrefs));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long day = (Calendar.getInstance().getTimeInMillis() - formattedDate.getTimeInMillis()) / (60*60*24*1000);
        Float progress = sharedPreferences.getFloat("Progress", (float) 00.00);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.remind_me_widget);
        views.setTextViewText(R.id.widget_day, "Day " + day);
        views.setTextViewText(R.id.widget_progress, "Progress " + String.format("%.2f", (progress * 100)) + "%");
        views.setProgressBar(R.id.widget_progress_bar, 100, (int) (progress * 100), false);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                for (int appWidgetId : appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}