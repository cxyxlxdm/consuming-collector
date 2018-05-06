package me.ailurus.consuming.collector

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:33 
 */
class Constants {

    public static final String PLUGIN_NAME = 'consuming'

    public static final String INJECTED_CODE_BEFORE_METHOD = 'long start = java.lang.System.currentTimeMillis();'

    public static final String INJECTED_CODE_AFTER_METHOD =
            'long end = java.lang.System.currentTimeMillis(); long duration = end - start; String funcName = new java.lang.Throwable().getStackTrace()[1].getMethodName(); android.util.Log.d(funcName, duration + "");'
}
