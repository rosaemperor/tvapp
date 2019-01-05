package com.qubuxing.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.qubuxing.qbx.utils.SharePrefenceHelper;

/**
 * Created by jiahongfei on 2017/9/27.
 */

public class TodayStepShutdownReceiver extends BroadcastReceiver {

    private static final String TAG = "TodayStepShutdownReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            Logger.e(TAG,"TodayStepShutdownReceiver");
            SharePrefenceHelper.Companion.saveBolean("isReBoot",true);
            PreferencesHelper.setShutdown(context,true);
        }
    }

}
