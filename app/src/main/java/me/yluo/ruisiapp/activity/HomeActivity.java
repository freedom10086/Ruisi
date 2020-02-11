package me.yluo.ruisiapp.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.adapter.MainPageAdapter;
import me.yluo.ruisiapp.fragment.BaseLazyFragment;
import me.yluo.ruisiapp.fragment.FrageForums;
import me.yluo.ruisiapp.fragment.FrageHotsNews;
import me.yluo.ruisiapp.fragment.FrageMessage;
import me.yluo.ruisiapp.fragment.FragmentMy;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.widget.MyBottomTab;

/**
 * @author yang
 * @date 16-3-17
 * 这是首页 管理3个fragment
 * 1.板块列表{@link HomeActivity}
 * 2.新帖{@link FrageHotsNews}
 */
public class HomeActivity extends BaseActivity
        implements MyBottomTab.OnTabChangeListener, ViewPager.OnPageChangeListener {

    private long mExitTime;

    private ScheduledExecutorService scheduledExecutorService;
    private MyTimerTask checkMessageTask = null;
    private int checkMessageStep = 1;
    private long checkMessageCount = 1;

    private MyBottomTab bottomTab;
    private static int interval = 180_000;//180s
    private MyHandler messageHandler;
    //间隔20天检查更新一次
    private static final int UPDATE_TIME = 1000 * 3600 * 24 * 20;
    private SharedPreferences sharedPreferences;
    private boolean isNeedCheckUpdate = false;
    private ViewPager viewPager;
    private List<BaseLazyFragment> fragments = new ArrayList<>();

    private static final int MSG_HAVE_REPLY = 1;
    private static final int MSG_NO_REPLY = 2;
    private static final int MSG_HAVE_PM = 3;
    private static final int MSG_NO_PM = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViewpager();

        bottomTab = findViewById(R.id.bottom_bar);
        bottomTab.setOnTabChangeListener(this);

        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay < 9 && hourOfDay > 1) {
            //晚上一点到早上9点间隔,不同时间段检查消息间隔不同 减轻服务器压力
            //240s
            interval = interval * 2;
        }
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        long time = sharedPreferences.getLong(App.CHECK_UPDATE_KEY, 0);
        if (System.currentTimeMillis() - time > UPDATE_TIME) {
            isNeedCheckUpdate = true;
        }
        messageHandler = new MyHandler(bottomTab, this);
    }

    private void initViewpager() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(this);
        fragments.add(new FrageForums());
        fragments.add(new FrageHotsNews());
        fragments.add(FrageMessage.newInstance());
        fragments.add(new FragmentMy());
        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void tabClicked(View v, int position, boolean isChange) {
        if (isChange) {
            if (fragments.get(position) instanceof FrageMessage) {
                FrageMessage m = (FrageMessage) fragments.get(position);
                // TODO
                m.updateNotifiCations(ishaveReply, ishavePm, false);
            }
            switchTab(position);
        } else {
            fragments.get(position).scrollToTop();
        }
    }

    //检查消息程序
    @Override
    protected void onStart() {
        super.onStart();
        checkMessageStep = 1;
        Log.i(getClass().getName(), "==== onStart ====");

        if (App.isLogin(this)) {
            if (checkMessageTask == null) {
                checkMessageTask = new MyTimerTask();
                scheduledExecutorService = Executors.newScheduledThreadPool(1);
                scheduledExecutorService.scheduleWithFixedDelay(checkMessageTask, 3, 120, TimeUnit.SECONDS);
            }
        } else {
            if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdownNow();
            }
            checkMessageTask = null;
        }

        if (isNeedCheckUpdate) {
            checkUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMessageStep = 1;
        Log.i(getClass().getName(), "==== onResume ====");
    }

    @Override
    protected void onStop() {
        checkMessageStep = 10;
        Log.i(getClass().getName(), "==== onStop ====");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(getClass().getName(), "==== onDestroy ====");
        App app = (App) getApplication();
        app.unRegRecieve();

        scheduledExecutorService.shutdownNow();
        checkMessageTask = null;

        super.onDestroy();
    }


    private void switchTab(int pos) {
        if (pos == 2) {
            bottomTab.clearMsg();
            bottomTab.invalidate();
        }
        viewPager.setCurrentItem(pos, false);
    }

    boolean ishaveReply = false;
    boolean ishavePm = false;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomTab.setSelect(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (checkMessageCount % checkMessageStep == 0) {
                Log.i(HomeActivity.this.getClass().getName(), "==== start check new message, school net: "
                        + App.IS_SCHOOL_NET
                        + " count: " + checkMessageCount + " step: " + checkMessageStep
                        + " ===");
                String urlReply = "home.php?mod=space&do=notice&view=mypost&type=post" + (App.IS_SCHOOL_NET ? "" : "&mobile=2");
                String urlPm = "home.php?mod=space&do=pm&mobile=2";
                HttpUtil.syncGet(HomeActivity.this, urlReply, new ResponseHandler() {
                    @Override
                    public void onSuccess(byte[] response) {
                        dealMessage(true, new String(response));
                    }
                });
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                HttpUtil.syncGet(HomeActivity.this, urlPm, new ResponseHandler() {
                    @Override
                    public void onSuccess(byte[] response) {
                        dealMessage(false, new String(response));
                    }
                });
                Log.i(HomeActivity.this.getClass().getName(), "==== check new message finished ===");
            } else {
                Log.i(HomeActivity.this.getClass().getName(), "==== start check new message, skip "
                        + " count: " + checkMessageCount + " step: " + checkMessageStep
                        + " ===");
            }

            checkMessageCount++;
        }
    }


    /**
     * check update
     */
    private void checkUpdate() {
        PackageManager manager;
        PackageInfo info = null;
        manager = getPackageManager();
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long versionCode = 1;
        if (info != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = info.getLongVersionCode();
            } else {
                versionCode = info.versionCode;
            }
        }
        final long finalVersionCode = versionCode;
        HttpUtil.get(App.CHECK_UPDATE_URL, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                int ih = res.indexOf("keywords");
                int hStart = res.indexOf('\"', ih + 15);
                int hEnd = res.indexOf('\"', hStart + 1);
                String title = res.substring(hStart + 1, hEnd);
                if (title.contains("code")) {
                    int st = title.indexOf("code");
                    int code = GetId.getNumber(title.substring(st));
                    if (code > finalVersionCode) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(App.CHECK_UPDATE_KEY, System.currentTimeMillis());
                        editor.apply();
                        isNeedCheckUpdate = false;
                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("检测到新版本")
                                .setMessage(title)
                                .setPositiveButton("查看", (dialog, which) -> PostActivity.open(HomeActivity.this, App.CHECK_UPDATE_URL, "谁用了FREEDOM"))
                                .setNegativeButton("取消", null)
                                .setCancelable(true)
                                .create()
                                .show();
                    }
                }
            }
        });
    }


    /**
     * check unread message
     */
    private void dealMessage(boolean isReply, String res) {
        int hashIndex = res.indexOf("formhash");
        if (hashIndex > 0) {
            String hash = res.substring(hashIndex + 9, hashIndex + 17);
            App.setHash(HomeActivity.this, hash);
        }

        Document document = Jsoup.parse(res);
        //回复
        if (isReply) {
            Elements elemens = document.select(".nts").select("dl.cl");
            if (elemens.size() > 0) {
                int lastMessageId = getSharedPreferences(App.MY_SHP_NAME, MODE_PRIVATE)
                        .getInt(App.NOTICE_MESSAGE_REPLY_KEY, 0);
                int noticeId = Integer.parseInt(elemens.get(0).attr("notice"));
                ishaveReply = lastMessageId < noticeId;
            }
        } else {
            Elements lists = document.select(".pmbox").select("ul").select("li");
            if (lists.size() > 0) {
                ishavePm = lists.get(0).select(".num").text().length() > 0;
            }
        }

        if (isReply) {
            if (ishaveReply) {
                messageHandler.sendEmptyMessage(MSG_HAVE_REPLY);
            } else {
                messageHandler.sendEmptyMessage(MSG_NO_REPLY);
            }
        } else {
            if (ishavePm) {
                messageHandler.sendEmptyMessage(MSG_HAVE_PM);
            } else {
                messageHandler.sendEmptyMessage(MSG_NO_PM);
            }
        }
    }


    //deal unread message show red point
    private static class MyHandler extends Handler {
        private final WeakReference<MyBottomTab> mytab;
        private final WeakReference<HomeActivity> act;

        private MyHandler(MyBottomTab tab, HomeActivity aa) {
            mytab = new WeakReference<>(tab);
            act = new WeakReference<>(aa);
        }

        @Override
        public void handleMessage(Message msg) {
            MyBottomTab t = mytab.get();
            HomeActivity a = act.get();
            switch (msg.what) {
                case MSG_HAVE_REPLY:
                    Log.d("message", "有新回复");
                    a.mkNotify();
                    t.setHaveReply(true);
                    break;
                case MSG_NO_REPLY:
                    Log.d("message", "无新回复");
                    t.setHaveReply(false);
                    break;
                case MSG_HAVE_PM:
                    Log.d("message", "有新pm");
                    a.mkNotify();
                    t.setHavePm(true);
                    break;
                case MSG_NO_PM:
                    Log.d("message", "无新pm");
                    t.setHavePm(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void mkNotify() {
        boolean isNotify = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                .getBoolean("setting_show_notify", false);
        if (!isNotify) {
            return;
        }
        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("未读消息提醒")
                .setContentText("你有未读的消息哦,去我的消息页面查看吧！")
                .setAutoCancel(true);
        final NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(10, builder.build());
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 1500) {
                Toast.makeText(this, "再按一次退出手机睿思(｡･ω･｡)~~", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ThemeActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            //切换主题
            Log.d("main", "切换主题");
            recreate();
        }
    }
}
