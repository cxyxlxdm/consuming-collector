package me.ailurus.consuming.collector

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:33 
 */
class Constants {

    public static final String PLUGIN_NAME = 'consuming'

    public static final String BEFORE_METHOD = 'startCollectConsuming = java.lang.System.currentTimeMillis();\n'
    public static final String LOGCAT_BEFORE_DEBUG = 'android.util.Log.d("'
    public static final String LOGCAT_AFTER_START = '", "startCollectConsuming = " + startCollectConsuming);\n'

    public static final String AFTER_METHOD = 'endCollectConsuming = java.lang.System.currentTimeMillis();\n'
    public static final String LOGCAT_AFTER_END = '", "endCollectConsuming = " + endCollectConsuming);\n'
    public static final String LOGCAT_DURATION = 'durationCollectConsuming = endCollectConsuming - startCollectConsuming;\n'
    public static final String LOGCAT_AFTER_DURATION = '", "durationCollectConsuming = " + durationCollectConsuming);}\n'

    public static final String LOGCAT_BEFORE_WARN = 'if (durationCollectConsuming > 50) {android.util.Log.d("'
}
