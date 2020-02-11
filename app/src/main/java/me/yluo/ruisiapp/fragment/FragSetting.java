package me.yluo.ruisiapp.fragment;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.DataManager;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.IntentUtils;

/**
 * Created by yang on 16-7-18.
 * 设置页面
 */

public class FragSetting extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //小尾巴string
    private EditTextPreference settingUserTail;
    //论坛地址
    private ListPreference settingForumsUrl;
    private SharedPreferences sharedPreferences;
    private Preference clearCache;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        settingUserTail = findPreference("setting_user_tail");
        settingForumsUrl = findPreference("setting_forums_url");
        clearCache = findPreference("clean_cache");
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        boolean b = sharedPreferences.getBoolean("setting_show_tail", false);
        settingUserTail.setEnabled(b);
        settingUserTail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
        settingForumsUrl.setSummary(App.IS_SCHOOL_NET ? "当前网络校园网，点击切换" : "当前网络校外网，点击切换");
        settingForumsUrl.setValue(App.IS_SCHOOL_NET ? "1" : "2");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        PackageManager manager;
        PackageInfo info = null;
        manager = getActivity().getPackageManager();
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long versionCode = 1;
        String versionName = "1.0";
        if (info != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = info.getLongVersionCode();
            } else {
                versionCode = info.versionCode;
            }
            versionName = info.versionName;
        }

        findPreference("about_this")
                .setSummary("当前版本" + versionName + "  version code:" + versionCode);


        //[2016年6月9日更新][code:25]睿思手机客户端
        //更新逻辑 检查睿思帖子标题 比对版本号
        final long finalVersionCode = versionCode;
        findPreference("about_this").setOnPreferenceClickListener(
                preference -> {
                    Toast.makeText(getActivity(), "正在检查更新", Toast.LENGTH_SHORT).show();
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
                                    new AlertDialog.Builder(getActivity()).
                                            setTitle("检测到新版本").
                                            setMessage(title).
                                            setPositiveButton("查看",
                                                    (dialog, which) -> PostActivity.open(getActivity(),
                                                            App.CHECK_UPDATE_URL, "谁用了FREEDOM"))
                                            .setNegativeButton("取消", null)
                                            .setCancelable(true)
                                            .create()
                                            .show();

                                } else {
                                    Toast.makeText(getActivity(), "暂无更新", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    return true;
                });

        findPreference("open_sourse").setOnPreferenceClickListener(preference -> {
            IntentUtils.openBroswer(getActivity(), "https://github.com/freedom10086/Ruisi");
            return false;
        });
        clearCache.setSummary("缓存大小：" + DataManager.getTotalCacheSize(getActivity()));
        clearCache.setOnPreferenceClickListener(preference -> {
            DataManager.cleanApplicationData(getActivity());

            Toast.makeText(getActivity(), "缓存清理成功!请重新登录", Toast.LENGTH_SHORT).show();
            clearCache.setSummary("缓存大小：" + DataManager.getTotalCacheSize(getActivity()));
            return false;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "settingForumsUrl":
                switch (sharedPreferences.getString("setting_forums_url", "2")) {
                    case "1":
                        settingForumsUrl.setSummary("当前网络校园网，点击切换");
                        Toast.makeText(getActivity(), "切换到校园网!", Toast.LENGTH_SHORT).show();
                        App.IS_SCHOOL_NET = true;
                        break;
                    case "2":
                        settingForumsUrl.setSummary("当前网络校外网，点击切换");
                        Toast.makeText(getActivity(), "切换到外网!", Toast.LENGTH_SHORT).show();
                        App.IS_SCHOOL_NET = false;
                        break;
                    default:
                        break;
                }
                break;
            case "setting_show_tail":
                boolean b = sharedPreferences.getBoolean("setting_show_tail", false);
                settingUserTail.setEnabled(b);
                settingUserTail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
                break;
            case "settingUserTail":
                settingUserTail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
                break;
            case "setting_hide_zhidin":
                break;
            case "setting_show_plain":
                boolean bbbb = sharedPreferences.getBoolean("setting_show_plain", false);
                Toast.makeText(getActivity(), bbbb ? "文章显示模式：简洁" : "文章显示模式：默认",
                        Toast.LENGTH_SHORT).show();
                break;
            case "setting_show_recent_forum":
                break;
            default:
                Log.w(getClass().getName(), "unknown setting changed: " + key);
                break;
        }
    }
}