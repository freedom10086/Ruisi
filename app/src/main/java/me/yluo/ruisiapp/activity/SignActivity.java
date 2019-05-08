package me.yluo.ruisiapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * Created by free2 on 16-3-15.
 * 签到activity
 */
public class SignActivity extends BaseActivity {

    protected CircleImageView userImage;
    protected ProgressBar progressBar;
    private View signYes, signNo;
    private TextView signError;
    private int spinnerSelect = 0;
    private String qdxq = "kx";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initToolBar(true, "签到中心");

        progressBar = findViewById(R.id.progressBar);
        signYes = findViewById(R.id.sign_yes);
        signNo = findViewById(R.id.sign_not);
        signError = findViewById(R.id.sign_error);
        signYes.setVisibility(View.GONE);
        signNo.setVisibility(View.GONE);
        signError.setVisibility(View.GONE);
        userImage = findViewById(R.id.avatar);

        checkState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Picasso.get()
                .load(UrlUtils.getAvaterurlb(App.getUid(this)))
                .placeholder(R.drawable.image_placeholder)
                .into(userImage);
    }

    //看看是否已经签到
    private void checkState() {
        progressBar.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (!(7 <= hourOfDay && hourOfDay < 24)) {
            sign_error();
            return;
        }

        String urlget = "plugin.php?id=dsu_paulsign:sign";
        HttpUtil.get(urlget, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                //// TODO: 16-8-26
                Document doc = Jsoup.parse(res);
                if (res.contains("您今天已经签到过了或者签到时间还未开始")) {
                    String daytxt = "0";
                    String monthtxt = "0";
                    for (Element temp : doc.select(".mn").select("p")) {
                        String temptext = temp.text();
                        if (temptext.contains("您累计已签到")) {
                            int pos = temptext.indexOf("您累计已签到");
                            daytxt = temptext.substring(pos);
                        } else if (temptext.contains("您本月已累计签到")) {
                            monthtxt = temptext;
                        }
                    }

                    sign_yes(daytxt, monthtxt);
                } else {
                    sign_no();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                showNtice("网络错误,请检查网络是否为校园网!");
            }
        });
    }


    private void sign_error() {
        progressBar.setVisibility(View.GONE);
        signError.setVisibility(View.VISIBLE);
    }

    private void sign_yes(String day, String month) {
        progressBar.setVisibility(View.GONE);
        signYes.setVisibility(View.VISIBLE);
        TextView total_day = findViewById(R.id.total_sign_day);
        TextView total_month = findViewById(R.id.total_sign_month);
        total_day.setText(day);
        total_month.setText(month);
    }

    private void sign_no() {
        progressBar.setVisibility(View.GONE);
        signNo.setVisibility(View.VISIBLE);
        Spinner spinner = findViewById(R.id.spinner);
        final String[] mItems = {"开心", "难过", "郁闷", "无聊", "怒", "擦汗", "奋斗", "慵懒", "衰"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerSelect = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button b = findViewById(R.id.btn_submit);
        b.setOnClickListener(view -> startDaka());
    }

    //点击签到按钮
    private void startDaka() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在签到...");
        dialog.show();
        String xinqin = getGroup1_select();
        //String formhash = hash;
        String qdmode;
        String todaysay = "";

        EditText input = findViewById(R.id.input);
        if (!TextUtils.isEmpty(input.getText().toString())) {
            qdmode = "1";
            todaysay = input.getText().toString() + "  --来自睿思手机客户端";
        } else {
            qdmode = "3";
        }

        Map<String, String> params = new HashMap<>();
        //params.put("formhash", formhash);
        params.put("qdxq", xinqin);
        params.put("qdmode", qdmode);
        params.put("todaysay", todaysay);
        params.put("fastreplay", "0");

        String url = UrlUtils.getSignUrl();
        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                int start = res.indexOf("恭喜你签到成功");
                if (start > 0) {
                    int end = res.indexOf("</div>", start);
                    showNtice(res.substring(start, end));
                    signNo.setVisibility(View.GONE);
                    checkState();
                } else {
                    String err = RuisUtils.getErrorText(res);
                    if (err == null) {
                        err = "未知错误,签到失败";
                    }
                    showNtice(err);

                }
            }

            @Override
            public void onFailure(Throwable e) {
                showNtice("网络错误!!!!!");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }

    //获得选择的心情
    private String getGroup1_select() {
        switch (spinnerSelect) {
            case 0:
                qdxq = "kx";
                break;
            case 1:
                qdxq = "ng";
                break;
            case 2:
                qdxq = "ym";
                break;
            case 3:
                qdxq = "wl";
                break;
            case 4:
                qdxq = "nu";
                break;
            case 5:
                qdxq = "ch";
                break;
            case 6:
                qdxq = "fd";
                break;
            case 7:
                qdxq = "yl";
                break;
            case 8:
                qdxq = "shuai";
                break;
        }
        return qdxq;
    }

    private void showNtice(String res) {
        progressBar.setVisibility(View.GONE);
        View container = findViewById(R.id.container);
        Snackbar.make(container, res, Snackbar.LENGTH_LONG).show();
    }
}
