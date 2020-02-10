package me.yluo.ruisiapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.model.Forum;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.myhttp.UploadImageResponseHandler;
import me.yluo.ruisiapp.utils.DimenUtils;
import me.yluo.ruisiapp.utils.RuisUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.InputValidDialog;
import me.yluo.ruisiapp.widget.MyColorPicker;
import me.yluo.ruisiapp.widget.MySmileyPicker;
import me.yluo.ruisiapp.widget.MySpinner;
import me.yluo.ruisiapp.widget.emotioninput.EmotionInputHandler;

/**
 * Created by free2 on 16-3-6.
 * 发帖activity
 * 参数：FID TITLE
 */
public class NewPostActivity extends BaseActivity implements View.OnClickListener,
        InputValidDialog.OnInputValidListener {

    private EditText edTitle, edContent;
    private MySpinner<Forum> typeidSpinner;
    private MyColorPicker myColorPicker;
    private MySmileyPicker smileyPicker;
    private TextView tvSelectType;

    private View typeIdContainer;
    private EmotionInputHandler handler;

    // 验证码相关
    private boolean haveValid = false;
    private String seccodehash = null;
    private String validValue = null; //验证码输入值

    //板块列表
    private List<Forum> datas = new ArrayList<>();
    //子版块列表
    private List<Forum> typeiddatas = new ArrayList<>();
    private String uploadHash = null;
    private ProgressDialog uploadDialog;
    private ProgressDialog postDialog;

    private int fid;
    private String title;
    private int typeId;

    public static final String FID = "fid";
    public static final String TITLE = "title";

    public static void open(Context context, int fid, String title) {
        Intent intent = new Intent(context, NewPostActivity.class);
        intent.putExtra(FID, fid);
        intent.putExtra(TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_topic);

        if (getIntent().getExtras() != null) {
            fid = getIntent().getExtras().getInt(FID);
            title = getIntent().getExtras().getString(TITLE);
        }

        if (!TextUtils.isEmpty(title)) {
            title = "发帖-" + title;
        } else {
            title = "发表新帖";
        }

        initToolBar(true, title);

        addToolbarMenu(R.drawable.ic_send_white_24dp).setOnClickListener(this);
        myColorPicker = new MyColorPicker(this);
        smileyPicker = new MySmileyPicker(this);
        typeidSpinner = new MySpinner<>(this);
        typeIdContainer = findViewById(R.id.type_id_container);
        typeIdContainer.setVisibility(View.GONE);
        tvSelectType = findViewById(R.id.tv_select_type);

        tvSelectType.setOnClickListener(this);
        edTitle = findViewById(R.id.ed_title);
        edContent = findViewById(R.id.ed_content);

        if (fid <= 0) {
            showToast("缺少必要参数无法发帖");
            new Handler().postDelayed(() -> finish(), 500);
            return;
        }

        typeidSpinner.setListener((pos, v) -> {
            typeId = typeiddatas.get(pos).fid;
            tvSelectType.setText(typeiddatas.get(pos).name);
        });
        final LinearLayout editBar = findViewById(R.id.edit_bar);
        for (int i = 0; i < editBar.getChildCount(); i++) {
            View c = editBar.getChildAt(i);
            if (c instanceof ImageView) {
                c.setOnClickListener(this);
            }
        }

        Spinner setSize = findViewById(R.id.action_text_size);
        setSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //[size=7][/size]
                if (edContent == null || (edContent.getText().length() <= 0 && i == 0)) {
                    return;
                }
                handleInsert("[size=" + (i + 1) + "][/size]");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        myColorPicker.setListener((pos, v, color) -> handleInsert("[color=" + color + "][/color]"));

        handler = new EmotionInputHandler(edContent, (enable, s) -> {

        });

        smileyPicker.setListener((str, a) -> {
            handler.insertSmiley(str, a);
        });

        findViewById(R.id.action_backspace).setOnLongClickListener(v -> {
            int start = edContent.getSelectionStart();
            int end = edContent.getSelectionEnd();
            if (start == 0) {
                return false;
            }
            if ((start == end) && start > 0) {
                start = start - 5;
            }
            if (start < 0) {
                start = 0;
            }
            edContent.getText().delete(start, end);
            return true;
        });

        loadTypeIds(fid);
        checkValid();
    }

    private boolean checkPostInput() {
        if (typeiddatas.size() > 0 && typeId <= 0) {
            Toast.makeText(this, "请选择主题分类", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(edTitle.getText().toString().trim())) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(edContent.getText().toString().trim())) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //显示填写验证码框子
    private void showInputValidDialog() {
        InputValidDialog dialog = InputValidDialog.newInstance(this, seccodehash, "");
        dialog.show(getSupportFragmentManager(), "valid");
    }

    // 判断是否有验证码
    private void checkValid() {
        HttpUtil.get(App.CHECK_POST_URL, new ResponseHandler() {
            @Override
            public void onStart() {
                haveValid = false;
            }

            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                //检查验证码
                int index = res.indexOf("updateseccode");
                if (index > 0) {
                    haveValid = true;
                    int start = res.indexOf("'", index) + 1;
                    int end = res.indexOf("'", start);
                    seccodehash = res.substring(start, end);
                    Log.v("valid", "seccodehash:" + seccodehash);
                }
            }
        });
    }

    private void prePost() {
        if (checkPostInput()) {
            if (haveValid && TextUtils.isEmpty(validValue)) {
                showInputValidDialog();
                return;
            }
            postDialog = new ProgressDialog(this);
            postDialog.setMessage("发帖中,请稍后......");
            postDialog.show();
            beginPost();
        }
    }

    //开始发帖
    private void beginPost() {
        String url = UrlUtils.getPostUrl(fid);
        Map<String, String> params = new HashMap<>();
        params.put("topicsubmit", "yes");
        if (typeId > 0) {
            params.put("typeid", String.valueOf(typeId));
        }
        params.put("subject", edTitle.getText().toString());
        params.put("message", edContent.getText().toString());

        if (haveValid) { //是否有验证码
            params.put("seccodehash", seccodehash);
            params.put("seccodeverify", validValue);
        }

        //params["attachnew[\(aid)]"] = ""
        List<String> aids = handler.getImagesAids();
        for (String aid : aids) {
            params.put("attachnew[" + aid + "]", "");
        }

        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                String errorText = RuisUtils.getErrorText(res);
                if (errorText != null) {
                    if ("抱歉，验证码填写错误".equals(errorText)) {
                        showInputValidDialog();
                        errorText = "抱歉，验证码填写错误";
                    }
                    postFail(errorText);
                } else {
                    postSuccess();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                postFail("发帖失败:" + e.getMessage());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                postDialog.dismiss();
            }
        });
    }

    //发帖成功执行
    private void postSuccess() {
        postDialog.dismiss();
        Toast.makeText(this, "主题发表成功", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra("status", "ok");
        //设置返回数据
        NewPostActivity.this.setResult(RESULT_OK, intent);
        finish();

    }

    //发帖失败执行
    private void postFail(String str) {
        postDialog.dismiss();
        Toast.makeText(this, "发帖失败:" + str, Toast.LENGTH_SHORT).show();
    }

    private void handleInsert(String s) {
        int start = edContent.getSelectionStart();
        int end = edContent.getSelectionEnd();
        int p = s.indexOf("[/");//相对于要插入的文本光标所在位置

        Editable edit = edContent.getEditableText();//获取EditText的文字

        if (start < 0 || start >= edit.length()) {
            edit.append(s);
        } else if (start != end && start > 0 && start < end && p > 0) {
            edit.insert(start, s.substring(0, p));//插入bbcode标签开始部分
            end = end + p;
            edit.insert(end, s.substring(p));//插入bbcode标签结束部分
            p = end - start;
        } else {
            edit.insert(start, s);//光标所在位置插入文字
        }

        if (p > 0) {
            edContent.setSelection(start + p);
        }
    }


    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.menu:
                prePost();
                break;
            case R.id.action_bold:
                handleInsert("[b][/b]");
                break;
            case R.id.action_italic:
                handleInsert("[i][/i]");
                break;
            case R.id.action_quote:
                handleInsert("[quote][/quote]");
                break;
            case R.id.action_color_text:
                myColorPicker.showAsDropDown(view, 0, 10);
                break;
            case R.id.action_emotion:
                ((ImageView) view).setImageResource(R.drawable.ic_edit_emoticon_accent_24dp);
                smileyPicker.showAsDropDown(view, 0, 10);
                smileyPicker.setOnDismissListener(() -> ((ImageView) view).setImageResource(R.drawable.ic_edit_emoticon_24dp));
                break;
            case R.id.action_insert_photo:
                if (TextUtils.isEmpty(uploadHash)) {
                    Toast.makeText(NewPostActivity.this, "你无法上传图片", Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(getPickImageChooserIntent(), 200);
                }
                break;
            case R.id.action_backspace:
                int start = edContent.getSelectionStart();
                int end = edContent.getSelectionEnd();
                if (start == 0) {
                    return;
                }
                if ((start == end) && start > 0) {
                    start = start - 1;
                }
                edContent.getText().delete(start, end);
                break;
            case R.id.tv_select_type:
                typeidSpinner.setData(typeiddatas);
                typeidSpinner.setWidth(view.getWidth());
                typeidSpinner.showAsDropDown(view, 0, 15);
                break;
            default:
                break;
        }

    }

    private void loadTypeIds(int fid) {
        typeiddatas.clear();
        typeId = 0;
        String url = "forum.php?mod=post&action=newthread&fid=" + fid + "&mobile=2";
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                String errorText = RuisUtils.getErrorText(res);

                if (errorText != null) {
                    Log.e("==", errorText);
                    showToast(errorText);
                    new Handler().postDelayed(() -> finish(), 500);
                    return;
                }

                Document document = Jsoup.parse(res);
                Elements types = document.select("#typeid").select("option");
                for (Element e : types) {
                    typeiddatas.add(new Forum(Integer.parseInt(e.attr("value")), e.text()));
                }

                if (typeiddatas.size() > 0) {
                    typeIdContainer.setVisibility(View.VISIBLE);
                    tvSelectType.setText(typeiddatas.get(0).name);
                    typeId = typeiddatas.get(0).fid;
                } else {
                    typeIdContainer.setVisibility(View.GONE);
                }

                //检查是否能上传图片
                //uploadformdata:{uid:"252553", hash:"fe626ed21ff334263dfe552cd9a4c209"},
                int index = res.indexOf("uploadformdata:");
                if (index > 0) {
                    int start = res.indexOf("hash", index) + 6;
                    int end = res.indexOf("\"", start + 5);
                    uploadHash = res.substring(start, end);
                    Log.d("===", "uploadhash:" + uploadHash);
                }
            }
        });
    }

    @Override
    public void onInputFinish(boolean click, String hash, String value) {
        // 输入验证码
        seccodehash = hash;
        validValue = value;
        if (click) { //提交
            prePost();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        Log.v("=======", "REQUEST_CODE:" + requestCode + "result:" + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if (getPickImageResultUri(data) != null) {
                Uri picUri = getPickImageResultUri(data);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = (Bitmap) data.getExtras().get("data");
            }
        }

        if (bitmap != null) {
            uploadImage(bitmap);
        }
    }

    private void uploadImage(Bitmap bitmap) {
        uploadDialog = new ProgressDialog(this);
        uploadDialog.setTitle("上传中...");
        uploadDialog.setMessage("图片上传中请稍后");
        uploadDialog.setCancelable(false);
        uploadDialog.show();
        new UploadTask().execute(bitmap);
    }


    private Bitmap returnBitmap = null;

    private class UploadTask extends AsyncTask<Bitmap, Void, byte[]> {
        @Override
        protected byte[] doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            bitmap = getResizedBitmap(bitmap, 1080);
            byte[] bytes = bitmap2Bytes(bitmap);
            returnBitmap = bitmap;

            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] data) {
            Map<String, String> params = new HashMap<>();
            params.put("uid", App.getUid(NewPostActivity.this));
            params.put("hash", uploadHash);

            HttpUtil.uploadImage(NewPostActivity.this, UrlUtils.getUploadImageUrl(),
                    params, System.currentTimeMillis() + ".jpg", data, new UploadImageResponseHandler() {
                        @Override
                        public void onSuccess(String aid) {
                            Log.v("===", "upload success aid:" + aid);
                            handler.insertImage(aid, new BitmapDrawable(getResources(), returnBitmap),
                                    edContent.getWidth() - DimenUtils.dip2px(NewPostActivity.this, 16));
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            Log.v("===", "upload failed:" + e.getMessage());
                            Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            uploadDialog.dismiss();
                        }
                    });
        }
    }


    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        return baos.toByteArray();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxWidth) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > maxWidth) {
            float bitmapRatio = (float) width / (float) height;
            width = maxWidth;
            height = (int) (width / bitmapRatio);
            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        return image;
    }

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? lastFile : data.getData();
    }

    public Intent getPickImageChooserIntent() {
        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if ("com.android.documentsui.DocumentsActivity".equals(intent.getComponent().getClassName())) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "选择图片");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private Uri lastFile;

    private Uri getCaptureImageOutputUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();
        Uri outputFileUri = null;
        if (storageDir != null) {
            File image = null;
            try {
                image = File.createTempFile(imageFileName, ".jpg", storageDir);
                Log.d("==", "create file success " + image.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputFileUri = Uri.fromFile(image);
        }
        lastFile = outputFileUri;
        return outputFileUri;
    }


    //动态申请权限 Android6.0+
    private boolean checkCameraPermission() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (permissions.size() > 0) {
            String[] s = new String[permissions.size()];
            for (int i = 0; i < permissions.size(); i++) {
                s[i] = permissions.get(i);
            }
            ActivityCompat.requestPermissions(this, s, 0);

            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        File storageDir = getExternalCacheDir();
        if (storageDir != null) {
            for (File f : storageDir.listFiles()) {
                if (f.getName().startsWith("JPEG_")) {
                    f.delete();
                }
            }
        }

        super.onDestroy();
    }
}
