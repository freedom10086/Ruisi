package me.yluo.ruisiapp.fragment;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.BaseActivity;
import me.yluo.ruisiapp.activity.LoginActivity;
import me.yluo.ruisiapp.activity.SearchActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.adapter.ForumsAdapter;
import me.yluo.ruisiapp.database.MyDB;
import me.yluo.ruisiapp.model.Category;
import me.yluo.ruisiapp.model.Forum;
import me.yluo.ruisiapp.model.WaterData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.DimenUtils;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * @author yang
 * @date 16-3-19
 * 板块列表fragment
 */
public class FrageForums extends BaseLazyFragment implements View.OnClickListener {
    private ForumsAdapter adapter = null;
    private CircleImageView userImg;
    private RecyclerView formsList;
    private boolean lastLoginState;
    private List<Category> forumDatas;
    private boolean showRecentVisit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forumDatas = new ArrayList<>();
        showRecentVisit = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("setting_show_recent_forum", true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        userImg = mRootView.findViewById(R.id.img);
        formsList = mRootView.findViewById(R.id.recycler_view);
        formsList.setClipToPadding(false);
        formsList.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.bottombarHeight));
        mRootView.findViewById(R.id.search).setOnClickListener(this);
        adapter = new ForumsAdapter(getActivity());

        int spanCount = Math.max(4, DimenUtils.px2dip(getResources(), Resources.getSystem().getDisplayMetrics().widthPixels) / 75);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = adapter.getItemViewType(position);
                if (type == ForumsAdapter.TYPE_HEADER || type == ForumsAdapter.TYPE_WATER) {
                    return spanCount;
                } else {
                    // 4 / 1 = 4 列
                    return 1;
                }
            }
        });
        userImg.setOnClickListener(this);
        formsList.setLayoutManager(layoutManager);
        formsList.setAdapter(adapter);
        return mRootView;
    }

    @Override
    public void onFirstUserVisible() {
        lastLoginState = App.isLogin(getActivity());
        initForums(lastLoginState);
        initAvatar();
    }

    @Override
    public void onUserVisible() {
        Log.d("=========", lastLoginState + "");
        Log.d("=========", "是否是登录状态:" + App.isLogin(getActivity()) + "");
        Log.d("=========", "是否是校园网:" + App.IS_SCHOOL_NET);

        if (lastLoginState != App.isLogin(getActivity())) {
            Log.d("=========", "登录状态改变 " + lastLoginState + " >" + !lastLoginState);
            lastLoginState = !lastLoginState;
            initForums(lastLoginState);
            initAvatar();
        }
    }

    @Override
    public void scrollToTop() {
        if (forumDatas != null && forumDatas.size() > 0) {
            formsList.scrollToPosition(0);
        }
    }

    private void initAvatar() {
        lastLoginState = App.isLogin(getActivity());
        if (lastLoginState) {
            RuisUtils.loadMyAvatar(new WeakReference<>(getActivity()),
                    App.getUid(getActivity()),
                    new WeakReference<>(userImg), "s");
        } else {
            userImg.setImageResource(R.drawable.image_placeholder);
        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_forums;
    }

    void initForums(boolean loginstate) {
        new GetForumList().execute(loginstate);

        if (App.IS_SCHOOL_NET) { //是校园网
            String url = App.BASE_URL_RS + "forum.php";
            HttpUtil.get(url, new ResponseHandler() {
                @Override
                public void onSuccess(byte[] response) {
                    List<WaterData> temps = new ArrayList<>();
                    Document doc = Jsoup.parse(new String(response));
                    Elements waters = doc.select("#portal_block_317").select("li");
                    for (Element e : waters) {
                        Elements es = e.select("p").select("a[href^=home.php?mod=space]");
                        String uid = GetId.getId("uid=", es.attr("href"));
                        String imgSrc = e.select("img").attr("src");
                        String uname = es.text();
                        int num = 0;
                        if (e.select("p").size() > 1) {
                            if (e.select("p").get(1).text().contains("帖数")) {
                                num = GetId.getNumber(e.select("p").get(1).text());
                            }
                        }
                        temps.add(new WaterData(uname, uid, num, imgSrc));
                        if (temps.size() >= 16) {
                            break;
                        }
                    }

                    if (temps.size() > 0) {
                        adapter.setWaterData(temps);
                    }
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                BaseActivity b = (BaseActivity) getActivity();
                if (b.isLogin()) {
                    switchActivity(SearchActivity.class);
                }
                break;
            case R.id.img:
                if (lastLoginState) {
                    String imgurl = UrlUtils.getAvaterurlb(App.getUid(getActivity()));
                    UserDetailActivity.open(getActivity(), App.getName(getActivity()),
                            imgurl, App.getUid(getActivity()));
                } else {
                    switchActivity(LoginActivity.class);
                }
                break;
            default:
                break;
        }
    }

    //获取首页板块数据 板块列表
    private class GetForumList extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... params) {
            Log.d("=========", "载入板块列表:" + params[0]);
            forumDatas = RuisUtils.getForums(getActivity(), params[0]);
            if (forumDatas == null || forumDatas.size() == 0) {
                return true;
            }

            if (showRecentVisit) {
                MyDB myDB = new MyDB(getContext());
                List<Integer> recentVisitFids = myDB.loadRecentVisitForums(10);
                if (recentVisitFids != null && recentVisitFids.size() > 0) {
                    List<Forum> recentForms = new ArrayList<>();
                    for (int f : recentVisitFids) {
                        for (Category c : forumDatas) {
                            Forum ff = null;
                            for (Forum fff : c.forums) {
                                if (fff.fid == f) {
                                    ff = fff;
                                    break;
                                }
                            }
                            if (ff != null) {
                                recentForms.add(ff);
                                break;
                            }
                        }
                    }

                    if (recentForms.size() > 0) {
                        Category category = new Category("最近常逛", 0, false, true, recentForms);
                        forumDatas.add(0, category);
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if (forumDatas == null || forumDatas.size() == 0) {
                Toast.makeText(getActivity(), "获取板块列表失败", Toast.LENGTH_LONG).show();
            }

            adapter.setDatas(forumDatas);
            adapter.notifyDataSetChanged();
        }
    }
}
