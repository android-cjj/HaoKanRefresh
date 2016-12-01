
package com.github.cjj.library.utils;

import android.util.Log;

/**
 * LogHelper log帮助类
 *
 * @author androidcjj
 * @version 1.0.0
 */
public final class LogHelper {

    public static boolean IS_DEBUG = true;

    private static String TAG = "cjj";

    private static final String CLASS_METHOD_LINE_FORMAT = "%s.%s()_%s";

    public static void log(String str) {
        if (IS_DEBUG) {
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[3];// 从堆栈信息中获取当前被调用的方法信息
            String className = traceElement.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            String logText = String.format(CLASS_METHOD_LINE_FORMAT,
                    className,
                    traceElement.getMethodName(),
                    String.valueOf(traceElement.getLineNumber()));
            Log.i(TAG, logText + "->" + str);// 打印Log
        }
    }

    public static void printStackTrace(Throwable throwable) {
        if (IS_DEBUG) {
            Log.w(TAG, "", throwable);
        }
    }

    public static void d(String log) {
        d(TAG, log);
    }

    public static void d(String tag, String log) {
        if (IS_DEBUG) {
            Log.d(tag, log);
        }
    }
}
