package me.yluo.ruisiapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * Created by free2 on 16-3-19.
 * 启动activity
 * 检查是否登陆
 * 读取相关设置写到{@link App}
 */
public class LaunchActivity extends BaseActivity {

    private static final int WAIT_TIME = 350;//最少等待时间ms

    private TextView launchText;
    private CircleImageView logo;
    private boolean isForeGround = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);

        App app = (App) getApplication();
        app.regReciever();

        launchText = findViewById(R.id.app_name);
        logo = findViewById(R.id.logo);
        loadUserImg();
        setCopyRight();
        new Handler().postDelayed(() -> enterHome(), WAIT_TIME);
    }

    //自动续命copyright
    private void setCopyRight() {
        int year = 2016;
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);

        if (year < yearNow) {
            year = yearNow;
        }
        ((TextView) findViewById(R.id.copyright))
                .setText("©2016-" + year + " 谁用了FREEDOM");
    }

    //设置头像
    private void loadUserImg() {
        String uid = App.getUid(this);
        if (!TextUtils.isEmpty(uid)) {
            RuisUtils.loadMyAvatar(new WeakReference<>(this),
                    uid, new WeakReference<>(logo), "m");
        }
    }

    private void enterHome() {
        if (isForeGround) {
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(this,
                    R.anim.fade_in, R.anim.fade_out);
            ActivityCompat.startActivity(this,
                    new Intent(this, HomeActivity.class), compat.toBundle());
            new Handler().postDelayed(this::finish, 305);
        }
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }


    @Override
    protected void onStart() {
        super.onStart();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.4f, 1.0f);
        alphaAnimation.setDuration((long) (WAIT_TIME * 0.85));// 设置动画显示时间
        launchText.startAnimation(alphaAnimation);
        logo.startAnimation(alphaAnimation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeGround = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeGround = false;
    }
}
