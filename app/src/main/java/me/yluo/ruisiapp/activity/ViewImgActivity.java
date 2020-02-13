package me.yluo.ruisiapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.widget.ScaleImageView;

public class ViewImgActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private ViewPager pager;
    private List<String> datas;
    private String aid = "0";
    private TextView index;
    private MyAdapter adapter;
    private int position = 0;
    private static boolean needAnimate = false;


    public static void open(Context context, String url) {
        Intent intent = new Intent(context, ViewImgActivity.class);
        intent.putExtra("url", url);
        needAnimate = true;
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out);
        ActivityCompat.startActivity(context, intent, compat.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_img);
        datas = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.rgb(138, 145, 151));
        }

        pager = findViewById(R.id.pager);
        index = findViewById(R.id.index);
        findViewById(R.id.nav_back).setOnClickListener(v -> finish());

        pager.addOnPageChangeListener(this);
        adapter = new MyAdapter();
        pager.setAdapter(adapter);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (currentPosition < 0) {
                return;
            }

            if (ContextCompat.checkSelfPermission(ViewImgActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ViewImgActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            } else {
                saveToGallery(datas.get(currentPosition));
            }
        });


        Bundle b = getIntent().getExtras();
        String url = b.getString("url");

        final String tid = GetId.getId("tid=", url);
        aid = GetId.getId("aid=", url);
        String urll = "forum.php?mod=viewthread&tid="
                + tid + "&aid=" + aid + "&from=album&mobile=2";
        HttpUtil.get(urll, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Document doc = Jsoup.parse(res);

                int headStart = res.indexOf("<title>");
                int headEnd = res.indexOf("</title>");
                if (headStart > 0 && headEnd > headStart) {
                    String title = res.substring(headStart + 7, headEnd);
                    if (title.contains("-")) {
                        title = title.substring(0, title.indexOf("-"));
                    }
                    TextView v = findViewById(R.id.title);
                    v.setText(title);
                }

                Elements elements = doc.select("ul.postalbum_c").select("li");
                int i = 0;
                for (Element e : elements) {
                    String zsrc = e.select("img").attr("zsrc");
                    if (zsrc.contains(aid)) {
                        position = i;
                    }
                    String src = e.select("img").attr("orig");
                    if (TextUtils.isEmpty("src")) {
                        continue;
                    }
                    if (src.startsWith("./")) {
                        src = src.substring(2);
                    }
                    if (!src.startsWith("http")) {
                        src = App.getBaseUrl() + src;
                    }

                    i++;
                    datas.add(src);
                }
                adapter.notifyDataSetChanged();
                changeIndex(position);
                pager.setCurrentItem(position, false);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        if (needAnimate) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private int currentPosition = -1;

    private void changeIndex(int pos) {
        index.setText((pos + 1) + "/" + datas.size());
        currentPosition = pos;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {
        position = position % datas.size();
        changeIndex(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            ScaleImageView v = container.findViewWithTag(position);
            if (v == null) {
                v = new ScaleImageView(ViewImgActivity.this);
                v.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                v.setLayoutParams(params);
                Picasso.get()
                        .load(datas.get(position))
                        .placeholder(R.drawable.loading)
                        .into(v);
                v.setTag(position);
                container.addView(v);
            }
            return v;
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }
    }

    private void saveToGallery(String url) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                Bitmap bitmap = null;
                try {
                    bitmap = Picasso.get().load(url).get();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                // Save image to gallery
                String savedImageUrl = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "IMAGE_" + System.currentTimeMillis(),
                        null
                );

                Log.d("===", "saved " + savedImageUrl);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    showToast("保存图片成功");
                } else {
                    showToast("保存图片失败");
                }

            }
        }.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery(datas.get(currentPosition));
                } else {
                    showToast("你拒绝了保存到相册的权限，无法保存");
                }
                break;
            }
            default:
                break;
        }
    }
}
