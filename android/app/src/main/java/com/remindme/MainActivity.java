package com.remindme;

import android.content.Intent;

import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "RemindMe";
  }

  @Override
  protected void onDestroy() {
    Intent broadcastIntent = new Intent();
    broadcastIntent.setAction("REMIND_ME_RESTART_SERVICE");
    broadcastIntent.setClass(this, RemindMeReceiver.class);
    this.sendBroadcast(broadcastIntent);
    super.onDestroy();
  }
}
