package me.yluo.ruisiapp;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

import me.yluo.ruisiapp.checknet.NetworkReceiver;
import me.yluo.ruisiapp.database.MyDB;
import me.yluo.ruisiapp.database.SQLiteHelper;
import me.yluo.ruisiapp.myhttp.HttpUtil;

/**
 * Created by yang on 16-3-11.
 * 共享的全局数据
 */
public class App extends Application {

    public static Context context;
    private NetworkReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化http
        HttpUtil.init(getApplicationContext());

        //清空消息数据库
        MyDB myDB = new MyDB(context);
        //最多缓存2000条历史纪录
        myDB.deleteOldHistory(2000);

        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        // 自定义外网睿思服务器地址
        String customOutServerAddr = shp.getString("setting_rs_out_server_addr", "http://rsbbs.xidian.edu.cn/").trim();
        if (customOutServerAddr.length() > 0) {
            if (!customOutServerAddr.startsWith("http://")) {
                customOutServerAddr = "http://" + customOutServerAddr;
            }

            if (!customOutServerAddr.endsWith("/")) {
                customOutServerAddr += "/";
            }

            Log.i("APP", "设置外网服务器地址:" + customOutServerAddr);
            BASE_URL_ME = customOutServerAddr;
        }

        regReciever();

        CrashReport.initCrashReport(getApplicationContext(), "04a96747f8", false);
    }

    @Override
    public void onTerminate() {
        //关闭数据库
        new SQLiteHelper(context).close();
        unRegRecieve();

        context = null;
        super.onTerminate();
    }

    public Context getContext() {
        return context;
    }

    public void regReciever() {
        if (receiver != null) {
            return;
        }
        //注册网络变化广播
        receiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);
    }

    public void unRegRecieve() {
        //注册网络变化广播
        if (receiver != null) {
            Log.d("onDestroy", "取消注册广播");
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    //发布地址tid
    public static final String POST_TID = "805203";

    //论坛基地址2个地址 第一个校园玩才能访问，第二个都可以
    public static String BASE_URL_ME = "http://rsbbs.xidian.edu.cn/";
    public static final String BASE_URL_RS = "http://rs.xidian.edu.cn/";

    //是否为校园网
    public static boolean IS_SCHOOL_NET = false;


    public static String getBaseUrl() {
        if (IS_SCHOOL_NET) {
            return BASE_URL_RS;
        } else {
            return BASE_URL_ME;
        }
    }

    public static boolean isLogin(Context context) {
        return !TextUtils.isEmpty(App.getUid(context));
    }

    public static String getUid(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_UID_KEY, "");
    }

    public static void setUid(Context context, String uid) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(USER_UID_KEY, uid);
        editor.apply();
    }

    public static void setHash(Context context, String hash) {
        if (TextUtils.isEmpty(hash)) {
            return;
        }
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(HASH_KEY, hash);
        editor.apply();
    }

    public static String getHash(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(HASH_KEY, "");
    }

    public static String getName(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_NAME_KEY, "");
    }

    public static String getGrade(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_GRADE_KEY, "");
    }

    public static int getCustomTheme(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getInt(THEME_KEY, 0);
    }

    public static void setCustomTheme(Context context, int theme) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(THEME_KEY, theme);
        editor.apply();
    }

    public static boolean isAutoDarkMode(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getBoolean(AUTO_DARK_MODE_KEY, true);
    }

    public static void setAutoDarkMode(Context context, boolean value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(AUTO_DARK_MODE_KEY, value);
        editor.apply();
    }

    public static void setDarkModeTime(Context context, boolean isStart, int value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        if (isStart) {
            editor.putInt(START_DARK_TIME_KEY, value);
        } else {
            editor.putInt(END_DARK_TIME_KEY, value);
        }
        editor.apply();
    }

    public static int[] getDarkModeTime(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        int[] ret = new int[2];
        ret[0] = shp.getInt(START_DARK_TIME_KEY, 21);
        ret[1] = shp.getInt(END_DARK_TIME_KEY, 6);
        return ret;
    }

    public static boolean showPlainText(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_PLAIN_TEXT_KEY, false);
    }

    /**
     * config
     * todo 把一些常量移到这儿来
     */

    //记录上次未读消息的id
    public static final String MY_SHP_NAME = "ruisi_shp";

    public static final String NOTICE_MESSAGE_REPLY_KEY = "message_notice_reply";
    public static final String NOTICE_MESSAGE_AT_KEY = "message_notice_at";
    public static final String THEME_KEY = "my_theme_key";
    public static final String SHOW_PLAIN_TEXT_KEY = "setting_show_plain";
    public static final String AUTO_DARK_MODE_KEY = "auto_dark_mode";
    public static final String START_DARK_TIME_KEY = "start_dart_time";
    public static final String END_DARK_TIME_KEY = "end_dark_time";
    public static final String USER_UID_KEY = "user_uid";
    public static final String USER_NAME_KEY = "user_name";
    public static final String HASH_KEY = "forum_hash";
    public static final String USER_GRADE_KEY = "user_grade";
    public static final String IS_REMBER_PASS_USER = "login_rember_pass";
    public static final String LOGIN_NAME = "login_name";
    public static final String LOGIN_PASS = "login_pass";
    public static final String CHECK_UPDATE_KEY = "check_update_time";

    public static final String LOGIN_URL = "member.php?mod=logging&action=login";
    public static final String LOGIN_RS = App.BASE_URL_RS + "member.php?mod=logging&action=login&mobile=2";
    public static final String LOGIN_ME = App.BASE_URL_ME + "member.php?mod=logging&action=login&mobile=2";

    public static final String CHECK_POST_URL = "forum.php?mod=ajax&action=checkpostrule&ac=newthread&mobile=2";
    public static final String CHECK_UPDATE_URL = "forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2";

    public static final int MANAGE_TYPE_EDIT = 0;
    public static final int MANAGE_TYPE_DELETE = 1;
    public static final int MANAGE_TYPE_BLOCK = 2;
    public static final int MANAGE_TYPE_WARN = 3;
    public static final int MANAGE_TYPE_CLOSE = 4;
}
