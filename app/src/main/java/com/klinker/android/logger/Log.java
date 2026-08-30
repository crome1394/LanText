package com.klinker.android.logger;

/** Minimal stand-in for klinker's logger so AOSP PDU classes compile. */
public class Log {
    public static void v(String tag, String msg) { android.util.Log.v(tag, msg); }
    public static void v(String tag, String msg, Throwable t) { android.util.Log.v(tag, msg, t); }
    public static void d(String tag, String msg) { android.util.Log.d(tag, msg); }
    public static void i(String tag, String msg) { android.util.Log.i(tag, msg); }
    public static void w(String tag, String msg) { android.util.Log.w(tag, msg); }
    public static void e(String tag, String msg) { android.util.Log.e(tag, msg); }
    public static void e(String tag, String msg, Throwable t) { android.util.Log.e(tag, msg, t); }
    public static void e(String tag, Throwable t) { android.util.Log.e(tag, "", t); }
}
