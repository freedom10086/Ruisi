package me.yluo.ruisiapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.adapter.BaseAdapter;
import me.yluo.ruisiapp.adapter.SimpleListAdapter;
import me.yluo.ruisiapp.model.FrageType;
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.SimpleListData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.DataManager;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.AddFriendDialog;
import me.yluo.ruisiapp.widget.CircleImageView;
import me.yluo.ruisiapp.widget.GradeProgressView;
import me.yluo.ruisiapp.widget.MyListDivider;

/**
 * 用户信息activity
 */
public class UserDetailActivity extends BaseActivity implements AddFriendDialog.AddFriendListener {
    private static final String NAME_IMG_AVATAR = "imgAvatar";
    private static String userUid = "";
    protected RecyclerView infoList;
    protected CoordinatorLayout layout;
    private CollapsingToolbarLayout toolbarLayout;
    private List<SimpleListData> datas = new ArrayList<>();
    private SimpleListAdapter adapter = null;
    private GradeProgressView progressView;
    private TextView progressText;
    private String username = "";
    private String imageUrl = "";
    private static boolean needAnimate = false;

    public static void openWithAnimation(Activity activity, String username, ImageView imgAvatar, String avatarUrl) {
        Intent intent = new Intent(activity, UserDetailActivity.class);
        intent.putExtra("loginName", username);
        intent.putExtra("avatarUrl", UrlUtils.getAvaterurlm(avatarUrl));
        needAnimate = true;
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imgAvatar, NAME_IMG_AVATAR);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    public static void openWithAnimation(Activity activity, String username, ImageView imgAvatar, int uid) {
        openWithAnimation(activity, username, imgAvatar, String.valueOf(uid));
    }

    public static void open(Context context, String username, String avatarUrl, String uid) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("loginName", username);
        intent.putExtra("avatarUrl", avatarUrl);
        if (!TextUtils.isEmpty(uid)) {
            intent.putExtra("uid", uid);
        }
        needAnimate = false;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        }
        toolbarLayout = findViewById(R.id.toolbar_layout);
        infoList = findViewById(R.id.recycler_view);
        CircleImageView imageView = findViewById(R.id.user_detail_img_avatar);
        layout = findViewById(R.id.main_window);
        progressView = findViewById(R.id.grade_progress);
        progressText = findViewById(R.id.progress_text);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> fabClick());

        ViewCompat.setTransitionName(imageView, NAME_IMG_AVATAR);
        username = getIntent().getStringExtra("loginName");
        imageUrl = getIntent().getStringExtra("avatarUrl");

        int uid = GetId.getNumber(imageUrl);

        Picasso.get().load(imageUrl).placeholder(R.drawable.image_placeholder).into(imageView);

        toolbarLayout.setTitle(username);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new SimpleListAdapter(ListType.INFO, this, datas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        infoList.setLayoutManager(layoutManager);
        infoList.addItemDecoration(new MyListDivider(this, MyListDivider.VERTICAL));
        adapter.setClickListener((v, position) -> {
            if (position == 0 && App.ISLOGIN(UserDetailActivity.this)) {
                Intent intent = new Intent(UserDetailActivity.this, FragementActivity.class);
                intent.putExtra("TYPE", FrageType.TOPIC);
                intent.putExtra("username", username);
                if (uid > 0) {
                    intent.putExtra("uid", uid);
                }

                UserDetailActivity.this.startActivity(intent);
            }
        });
        infoList.setAdapter(adapter);

        userUid = getIntent().getStringExtra("uid");
        if (TextUtils.isEmpty(userUid)) {
            userUid = GetId.getId("uid=", imageUrl);
        }

        //如果是自己
        if (userUid.equals(App.getUid(this))) {
            fab.setImageResource(R.drawable.ic_close_24dp);
            imageView.setOnClickListener(view -> {
                final String[] items = {"修改密码"};
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("操作");
                alertBuilder.setItems(items, (arg0, index) -> {
                    if (index == 0) {
                        startActivity(new Intent(UserDetailActivity.this, ChangePasswordActivity.class));
                    }
                });
                AlertDialog d = alertBuilder.create();
                d.show();
            });

        }
        loadData(UrlUtils.getUserHomeUrl(userUid, false));
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        if (needAnimate) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void loadData(String url) {
        adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                new GetUserInfoTask().execute(new String(response));
            }

            @Override
            public void onFailure(Throwable e) {
                Toast.makeText(getApplicationContext(), "网络错误！！", Toast.LENGTH_SHORT).show();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    private void fabClick() {
        //如果是自己 退出登录
        if (userUid.equals(App.getUid(this))) {
            new AlertDialog.Builder(this).
                    setTitle("退出登录").
                    setMessage("你确定要注销吗？").
                    setPositiveButton("注销", (dialog, which) -> {
                        App.setUid(UserDetailActivity.this, null);
                        DataManager.cleanApplicationData(UserDetailActivity.this);
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .setCancelable(true)
                    .create()
                    .show();

        } else if (App.ISLOGIN(this)) {
            String url = "home.php?mod=space&do=pm&subop=view&touid=" + userUid + "&mobile=2";
            ChatActivity.open(this, username, url);
        } else {
            Snackbar.make(layout, "你还没有登陆，无法发送消息", Snackbar.LENGTH_LONG)
                    .setAction("点我登陆", view -> startActivity(new Intent(getApplicationContext(), LoginActivity.class))).show();
        }

    }

    //加好友确认按钮点击
    @Override
    public void OnAddFriendOkClick(String mes, String uid) {
        final ProgressDialog dialog1 = new ProgressDialog(this);
        dialog1.setTitle("正在发送请求");
        dialog1.setMessage("请等待......");
        Map<String, String> paras = new HashMap<>();
        paras.put("addsubmit", "true");
        paras.put("handlekey", "friend_" + uid);
        paras.put("note", mes);
        paras.put("gid", "1");
        paras.put("addsubmit_btn", "true");
        HttpUtil.post(UrlUtils.getAddFriendUrl(uid), paras, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (res.contains("好友请求已")) {
                    Toast.makeText(getApplicationContext(), "请求已发送成功，正在请等待对方验证", Toast.LENGTH_SHORT).show();
                } else if (res.contains("正在等待验证")) {
                    Toast.makeText(getApplicationContext(), "好友请求已经发送了，正在等待对方验证", Toast.LENGTH_SHORT).show();
                } else if (res.contains("你们已成为好友")) {
                    Toast.makeText(getApplicationContext(), "你们已经是好友了不用添加了...", Toast.LENGTH_SHORT).show();
                }

                dialog1.dismiss();
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                Toast.makeText(getApplicationContext(), "出错了，我也不知道哪儿错了...", Toast.LENGTH_SHORT).show();
                dialog1.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!userUid.equals(App.getUid(this))) {
            getMenuInflater().inflate(R.menu.menu_userdetail, menu);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            if (!App.ISLOGIN(this)) {
                Snackbar.make(layout, "你还没有登陆，无法进行操作", Snackbar.LENGTH_LONG)
                        .setAction("点我登陆", view -> startActivity(new Intent(getApplicationContext(), LoginActivity.class))).show();
            } else {
                AddFriendDialog dialogFragment = AddFriendDialog.newInstance(
                        this, username, imageUrl);
                dialogFragment.show(getFragmentManager(), "add");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    //获得用户个人信息
    private class GetUserInfoTask extends AsyncTask<String, Void, Void> {
        private int userJf = 0;//积分

        @Override
        protected Void doInBackground(String... params) {
            String res = params[0];
            username = Jsoup.parse(res).select(".user_avatar").select(".name").text();
            Elements lists = Jsoup.parse(res).select(".user_box").select("ul").select("li");
            if (lists == null) return null;

            if (App.ISLOGIN(UserDetailActivity.this)) {
                datas.add(new SimpleListData("帖子", null, ""));
            }

            for (Element tmp : lists) {
                String value = tmp.select("span").text();
                tmp.select("span").remove();
                String key = tmp.text();
                if (key.contains("积分")) {
                    userJf = Integer.parseInt(value);
                    String grade = RuisUtils.getLevel(userJf);
                    datas.add(new SimpleListData("等级", grade, ""));
                } else if (key.contains("上传量") || key.contains("下载量")) {
                    long a = Long.parseLong(value.trim());
                    DecimalFormat decimalFormat = new DecimalFormat(".00");
                    float GBsize = (float) (a / 1024 / 1024 / 1024.0);
                    if (GBsize > 500) {
                        float TBsize = GBsize / 1024.0f;
                        value = decimalFormat.format(TBsize) + " TB";
                    } else {
                        value = decimalFormat.format(GBsize) + " GB";
                    }

                }
                datas.add(new SimpleListData(key, value, ""));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            int nextLevelJf = RuisUtils.getNextLevel(userJf);
            float progress = userJf * 1.0f / nextLevelJf;
            progressView.setProgress(progress);
            progressText.setText(userJf + "/" + nextLevelJf);

            toolbarLayout.setTitle(username);
            adapter.disableLoadMore();
            adapter.notifyDataSetChanged();
        }

    }
}
