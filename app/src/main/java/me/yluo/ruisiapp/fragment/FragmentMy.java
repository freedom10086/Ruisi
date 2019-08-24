package me.yluo.ruisiapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.AboutActivity;
import me.yluo.ruisiapp.activity.BaseActivity;
import me.yluo.ruisiapp.activity.FragementActivity;
import me.yluo.ruisiapp.activity.FriendActivity;
import me.yluo.ruisiapp.activity.HomeActivity;
import me.yluo.ruisiapp.activity.LoginActivity;
import me.yluo.ruisiapp.activity.SettingActivity;
import me.yluo.ruisiapp.activity.SignActivity;
import me.yluo.ruisiapp.activity.ThemeActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.model.FrageType;
import me.yluo.ruisiapp.utils.IntentUtils;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.widget.CircleImageView;

import static me.yluo.ruisiapp.App.BASE_URL_ME;
import static me.yluo.ruisiapp.App.BASE_URL_RS;

/**
 * TODO: 16-8-23  打开的时候检查是否签到显示在后面
 * 基础列表后面可以显示一些详情，如收藏的数目等...
 */
public class FragmentMy extends BaseLazyFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private String username, uid;
    private CircleImageView userImg;
    private TextView userName, userGrade;
    //记录上次创建时候是否登录
    private boolean isLoginLast = false;

    private final int[] icons = new int[]{
            R.drawable.ic_autorenew_black_24dp,
            R.drawable.ic_palette_black_24dp,
            R.drawable.ic_settings_24dp,
            R.drawable.ic_info_24dp,
            R.drawable.ic_menu_share_24dp,
            R.drawable.ic_favorite_white_12dp,
    };

    private final String[] titles = new String[]{
            "签到中心",
            "主题设置",
            "设置",
            "关于本程序",
            "分享手机睿思",
            "到商店评分",
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        username = App.getName(getActivity());
        uid = App.getUid(getActivity());

        userImg = mRootView.findViewById(R.id.user_img);
        userName = mRootView.findViewById(R.id.user_name);
        userGrade = mRootView.findViewById(R.id.user_grade);
        userImg.setOnClickListener(this);
        mRootView.findViewById(R.id.history).setOnClickListener(this);
        mRootView.findViewById(R.id.star).setOnClickListener(this);
        mRootView.findViewById(R.id.friend).setOnClickListener(this);
        mRootView.findViewById(R.id.post).setOnClickListener(this);

        ListView listView = mRootView.findViewById(R.id.function_list);
        List<Map<String, Object>> fs = new ArrayList<>();
        for (int i = 0; i < icons.length; i++) {
            Map<String, Object> d = new HashMap<>();
            d.put("icon", icons[i]);
            d.put("title", titles[i]);
            fs.add(d);
        }
        listView.setOnItemClickListener(this);
        listView.setAdapter(new SimpleAdapter(getActivity(), fs, R.layout.item_function, new String[]{"icon", "title"}, new int[]{R.id.icon, R.id.title}));
        return mRootView;
    }

    @Override
    public void onFirstUserVisible() {
        isLoginLast = App.isLogin(getActivity());
        refreshAvatarView();
    }

    @Override
    public void onUserVisible() {
        if (isLoginLast != App.isLogin(getActivity())) {
            isLoginLast = !isLoginLast;
            refreshAvatarView();
        }
    }

    @Override
    public void scrollToTop() {
        //do noting
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my;
    }

    private void refreshAvatarView() {
        if (isLoginLast) {
            uid = App.getUid(getActivity());
            userName.setText(App.getName(getActivity()));
            userGrade.setVisibility(View.VISIBLE);
            userGrade.setText(App.getGrade(getActivity()));

            RuisUtils.loadMyAvatar(new WeakReference<>(getActivity()),
                    uid,
                    new WeakReference<>(userImg), "m");
        } else {
            userName.setText("点击头像登陆");
            userGrade.setVisibility(View.GONE);
            userImg.setImageResource(R.drawable.image_placeholder);
        }
    }

    @Override
    public void onClick(View view) {
        BaseActivity b = (BaseActivity) getActivity();
        switch (view.getId()) {
            case R.id.user_img:
                if (App.isLogin(getActivity())) {
                    UserDetailActivity.openWithAnimation(
                            getActivity(), username, userImg, uid);
                } else {
                    switchActivity(LoginActivity.class);
                }
                break;
            case R.id.post:
                if (b.isLogin()) {
                    FragementActivity.open(getActivity(), FrageType.TOPIC);
                }
                break;
            case R.id.star:
                if (b.isLogin()) {
                    FragementActivity.open(getActivity(), FrageType.START);
                }
                break;
            case R.id.history:
                FragementActivity.open(getActivity(), FrageType.HISTORY);
                break;
            case R.id.friend:
                if (b.isLogin()) {
                    switchActivity(FriendActivity.class);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                HomeActivity a = (HomeActivity) getActivity();
                if (a.isLogin()) {
                    switchActivity(SignActivity.class);
                }
                break;
            case 1:
                Intent i = new Intent(getActivity(), ThemeActivity.class);
                getActivity().startActivityForResult(i, ThemeActivity.REQUEST_CODE);
                break;
            case 2:
                switchActivity(SettingActivity.class);
                break;
            case 3:
                switchActivity(AboutActivity.class);
                break;
            case 4:
                String data = "这个手机睿思客户端非常不错，分享给你们。" +
                        "\n下载地址(校园网): " + BASE_URL_RS + "forum.php?mod=viewthread&tid=" + App.POST_TID +
                        "\n下载地址2(校外网): " + BASE_URL_ME + "forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2" +
                        "\n下载地址3(校外网): " + "https://www.coolapk.com/apk/me.yluo.ruisiapp";
                IntentUtils.shareApp(getActivity(), data);
                break;
            case 5:
                if (!IntentUtils.openStore(getActivity())) {
                    Toast.makeText(getActivity(), "确保你的手机安装了相关应用商城", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                throw new IndexOutOfBoundsException("unknown index: " + position);
        }
    }
}
