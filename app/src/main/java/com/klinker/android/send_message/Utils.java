package com.klinker.android.send_message;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class Utils {
    public static String getMyPhoneNumberFromSubscription(Context context, int subscriptionId) {
        try {
            TelephonyManager tm = context.getSystemService(TelephonyManager.class);
            if (tm == null) return "";
            if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    && android.os.Build.VERSION.SDK_INT >= 24) {
                tm = tm.createForSubscriptionId(subscriptionId);
            }
            String n = tm.getLine1Number();
            return n == null ? "" : n;
        } catch (Exception e) {
            return "";
        }
    }
}
