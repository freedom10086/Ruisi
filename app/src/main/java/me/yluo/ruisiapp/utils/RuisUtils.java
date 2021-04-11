package me.yluo.ruisiapp.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.model.Category;
import me.yluo.ruisiapp.model.Forum;

public class RuisUtils {

    /**
     * 获得板块图标
     */
    public static Drawable getForumlogo(Context contex, int fid) {
        try {
            InputStream ims = contex.getAssets().open("forumlogo/common_" + fid + "_icon.gif");
            return Drawable.createFromStream(ims, null);
        } catch (IOException ex) {
            return null;
        }
    }

    //加载我的头像
    //size s m l
    public static void loadMyAvatar(WeakReference<Context> context, String uid, WeakReference<ImageView> target, String size) {
        File f = new File(context.get().getFilesDir() + uid + size);
        String url;
        if ("s".equals(size)) {
            url = UrlUtils.getAvaterurls(uid);
        } else if ("b".equals(size)) {
            url = UrlUtils.getAvaterurlb(uid);
        } else {
            url = UrlUtils.getAvaterurlm(uid);
        }

        if (f.exists()) {
            Picasso.get()
                    .load(f)
                    .error(R.drawable.image_placeholder)
                    .into(target.get());
        } else {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    Bitmap b = null;
                    Context c = context.get();
                    if (c == null) {
                        return null;
                    }
                    try {
                        b = Picasso.get().load(params[0]).get();
                        FileOutputStream out = new FileOutputStream(f);
                        b.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return b;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null && target.get() != null && context.get() != null) {
                        Drawable d = new BitmapDrawable(context.get().getResources(), bitmap);
                        target.get().setImageDrawable(d);
                    }
                }
            }.execute(url);
        }
    }

    public static String getLevel(int a) {
        if (a >= 0 && a < 100) {
            return "西电托儿所";
        } else if (a < 200) {
            return " 西电幼儿园";
        } else if (a < 500) {
            return " 西电附小";
        } else if (a < 1000) {
            return " 西电附中";
        } else if (a < 2000) {
            return " 西电大一";
        } else if (a < 2500) {
            return " 西电大二";
        } else if (a < 3000) {
            return " 西电大三";
        } else if (a < 3500) {
            return " 西电大四";
        } else if (a < 6000) {
            return " 西电研一";
        } else if (a < 10000) {
            return " 西电研二";
        } else if (a < 14000) {
            return " 西电研三";
        } else if (a < 20000) {
            return " 西电博一";
        } else if (a < 25000) {
            return " 西电博二";
        } else if (a < 30000) {
            return " 西电博三";
        } else if (a < 35000) {
            return " 西电博四";
        } else if (a < 40000) {
            return " 西电博五";
        } else if (a >= 40000 && a < 100000) {
            return " 西电博士后";
        } else {
            return "新手上路";
        }
    }

    /**
     * 从返回结果中获得报错文字
     * eg:
     * <div class="jump_c">
     * <p>本版块禁止发帖</p>
     * <p><a class="grey" href="javascript:history.back();">[ 点击这里返回上一页 ]</a></p>
     * </div>
     * 提取：本版块禁止发帖
     */
    public static String getErrorText(String res) {
        if (res.contains("class=\"jump_c\"")) {
            int start = res.indexOf("<p>", res.indexOf("class=\"jump_c\"")) + 3;
            int end = res.indexOf("</p>", start);
            return res.substring(start, end);
        }

        return null;
    }

    public static String getErrorTextPC(String res) {
        if (res.contains("class=\"alert_info\"")) {
            int start = res.indexOf("<p>", res.indexOf("class=\"alert_info\"")) + 3;
            int end = res.indexOf("</p>", start);
            return res.substring(start, end);
        }

        return null;
    }

    //<dt id="messagetext">
    //<p>密码太弱，密码中必须包含数字<script type="text/javascript" reload="1">if(typeof errorhandle_=='function') {errorhandle_('密码太弱，密码中必须包含数字', {});}</script></p>
    public static String getRuisiReqAjaxError(String res) {
        if (!res.contains("id=\"messagetext\"")) {
            return null;
        }

        int index = res.indexOf("id=\"messagetext\"");
        if (index < 0) {
            return null;
        }

        index = res.indexOf("<p>", index);
        if (index < 0) {
            return null;
        }

        int startIndex = index + "<p>".length();
        int endIndex = res.indexOf("<script", startIndex);

        return res.substring(startIndex, endIndex);
    }


    //获得到下一等级的积分
    public static int getNextLevel(int a) {
        if (a >= 0 && a < 100) {
            return 100;
        } else if (a < 200) {
            return 200;
        } else if (a < 500) {
            return 500;
        } else if (a < 1000) {
            return 1000;
        } else if (a < 2000) {
            return 2000;
        } else if (a < 2500) {
            return 2500;
        } else if (a < 3000) {
            return 3000;
        } else if (a < 3500) {
            return 3500;
        } else if (a < 6000) {
            return 6000;
        } else if (a < 10000) {
            return 10000;
        } else if (a < 14000) {
            return 14000;
        } else if (a < 20000) {
            return 20000;
        } else if (a < 25000) {
            return 25000;
        } else if (a < 30000) {
            return 30000;
        } else if (a < 35000) {
            return 35000;
        } else if (a < 40000) {
            return 40000;
        } else if (a >= 40000) {
            return 60000;
        } else {
            return 100;
        }
    }

    public static float getLevelProgress(int a) {
        if (a >= 0 && a < 100) {
            return a / 100f;
        } else if (a < 200) {
            return (a - 100) / 100f;
        } else if (a < 500) {
            return (a - 200) / 300f;
        } else if (a < 1000) {
            return (a - 500) / 500f;
        } else if (a < 2000) {
            return (a - 1000) / 1000f;
        } else if (a < 2500) {
            return (a - 2000) / 500f;
        } else if (a < 3000) {
            return (a - 2500) / 500f;
        } else if (a < 3500) {
            return (a - 3000) / 500f;
        } else if (a < 6000) {
            return (a - 3500) / 2500f;
        } else if (a < 10000) {
            return (a - 6000) / 4000f;
        } else if (a < 14000) {
            return (a - 10000) / 4000f;
        } else if (a < 20000) {
            return (a - 14000) / 6000f;
        } else if (a < 25000) {
            return (a - 20000) / 15000f;
        } else if (a < 30000) {
            return (a - 25000) / 5000f;
        } else if (a < 35000) {
            return (a - 30000) / 5000f;
        } else if (a < 40000) {
            return (a - 35000) / 5000f;
        } else if (a >= 40000) {
            float b = (a - 40000) / 60000f;
            if (b > 1) {
                b = 1;
            }
            return b;
        } else {
            return 0;
        }


    }

    public static Map<String, String> getForms(Document document, String id) {
        Element element = document.getElementById(id);
        Map<String, String> params = new HashMap<>();
        if (element == null) {
            return params;
        }
        Elements inputs = element.select("input");
        for (Element ee : inputs) {
            String key = ee.attr("name");
            String type = ee.attr("type");
            String value = ee.attr("value");
            if (!TextUtils.isEmpty(key) && !"submit".equals(type)) {
                params.put(key, value);
            }
        }

        Elements textareas = element.select("textarea");
        for (Element ee : textareas) {
            String key = ee.attr("name");
            String value = ee.html();
            params.put(key, value);
        }

        return params;
    }

    public static List<Category> getForums(Context context, boolean isLogin) {
        InputStream in = null;
        String s;
        try {
            in = context.getAssets().open("forums.json");
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            s = new String(buffer);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<Category> cates = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(s);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                boolean cateLogin = o.getBoolean("login");
                if (!isLogin && cateLogin) {//false true
                    continue;
                }

                boolean cateCanPost = o.getBoolean("canPost");
                List<Forum> fs = new ArrayList<>();
                JSONArray forums = o.getJSONArray("forums");
                for (int j = 0; j < forums.length(); j++) {
                    JSONObject oo = forums.getJSONObject(j);
                    boolean forumLogin = oo.getBoolean("login");
                    if (!isLogin && forumLogin) {//false true
                        continue;
                    }
                    if (oo.has("manager") && !isManager(App.getGrade(context))) {
                        // 需要管理权限
                        continue;
                    }
                    fs.add(new Forum(oo.getString("name"), oo.getInt("fid"), forumLogin));
                }
                cates.add(new Category(o.getString("name"), o.getInt("gid"), cateLogin, cateCanPost, fs));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cates;
    }

    public static String toHtml(String s) {
        s = s.replace("[b]", "<b>");
        s = s.replace("[/b]", "</b>");

        s = s.replace("[i]", "<i>");
        s = s.replace("[/i]", "</i>");

        s = s.replace("[quote]", "<blockquote>");
        s = s.replace("[/quote]", "</blockquote>");

        s = s.replace("[size=1]", "<font size=\"1\">");//<font size="6">哈哈</font>
        s = s.replace("[size=2]", "<font size=\"2\">");
        s = s.replace("[size=3]", "<font size=\"3\">");
        s = s.replace("[size=4]", "<font size=\"4\">");
        s = s.replace("[size=5]", "<font size=\"5\">");
        s = s.replace("[size=6]", "<font size=\"6\">");
        s = s.replace("[size=7]", "<font size=\"7\">");
        s = s.replace("[/size]", "</size>");

        return s;
    }

    public static boolean isManager(String grade) {
        String[] managers = {"管理员", "超级版主", "版主", "游戏补丁更新组",
                "睿思助理", "RS助理", "邀请发放专员", "轮值超版", "美工组", "实习版主"};
        for (String str : managers) {
            if (str.equals(grade)) {
                return true;
            }
        }
        return false;
    }

    public static Document getManageContent(byte[] response) {
        String tmp = new String(response);
        int start = tmp.indexOf("<div");
        int last = tmp.lastIndexOf("</div>") + 6;
        return Jsoup.parse(tmp.substring(start, last));
    }
}
