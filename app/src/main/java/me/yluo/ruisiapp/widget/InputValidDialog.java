package me.yluo.ruisiapp.widget;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Random;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.KeyboardUtil;
import me.yluo.ruisiapp.utils.UrlUtils;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifIOException;
import pl.droidsonroids.gif.GifImageView;

/**
 * 输入验证码
 * @author yang
 */
public class InputValidDialog extends DialogFragment {
    private EditText input;
    private GifImageView gifImageView;
    private ProgressBar progressBar;
    private TextView statusView;
    private String hash = "";
    private String update = "48265";
    private OnInputValidListener dialogListener;


    public static InputValidDialog newInstance(OnInputValidListener var, String hash, String update) {
        InputValidDialog frag = new InputValidDialog();
        frag.hash = hash;
        frag.dialogListener = var;
        frag.update = update;
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_input_valid, null);
        builder.setView(view);

        builder.setTitle("输入验证码");
        builder.setCancelable(false);

        input = view.findViewById(R.id.value);
        gifImageView = view.findViewById(R.id.gifview);
        progressBar = view.findViewById(R.id.progress);
        statusView = view.findViewById(R.id.status);
        statusView.setVisibility(View.GONE);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = input.getText().toString().trim();
                if (value.length() == 4) {
                    checkValid(hash, value);
                }
            }
        });


        view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
            if (checkInput()) {
                KeyboardUtil.hideKeyboard(input);
                InputValidDialog.this.getDialog().dismiss();
                dialogListener.onInputFinish(true, hash, input.getText().toString().trim());
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(view12 -> dismiss());
        loadImage(hash);

        gifImageView.setOnClickListener(view13 -> changeValid());
        return builder.create();
    }

    private void changeValid() {
        input.setText(null);
        statusView.setVisibility(View.GONE);

        hash = "S" + new Random().nextInt(999);
        dialogListener.onInputFinish(false, hash, null);
        loadImage(hash);
        //misc.php?mod=seccode&update=27663&idhash=SszZ1&mobile=2
    }

    // 检查验证码
    private void checkValid(String hash, String value) {
        //misc.php?mod=seccode&action=check&inajax=1&modid=forum::ajax&idhash=cSAJKv8nZ&secverify=ewmf
        HttpUtil.get("misc.php?mod=seccode&action=check&inajax=1&idhash=" + hash + "&secverify=" + value + "&mobile=2", new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                // err
                //<?xml version="1.0" encoding="utf-8"?>
                //<root><![CDATA[invalid]]></root>

                // yes
                //<?xml version="1.0" encoding="utf-8"?>
                //<root><![CDATA[succeed]]></root>

                statusView.setVisibility(View.VISIBLE);
                String res = new String(response);
                if (res.contains("succeed")) {
                    statusView.setText("正确");
                    statusView.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_light));
                    dialogListener.onInputFinish(false, hash, value);
                } else {
                    statusView.setText("错误");
                    statusView.setTextColor(ContextCompat.getColor(getActivity(), R.color.orange_light));
                }
            }
        });
    }

    private void loadUpdate() {
        HttpUtil.get("misc.php?mod=seccode&action=update&idhash=" + hash + "&mobile=2", new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res2 = new String(response);
                update = GetId.getId("update=", res2);
                loadImage(hash);
            }
        });
    }

    private void loadImage(String hash) {
        if (TextUtils.isEmpty(update)) {
            loadUpdate();
            return;
        }
        //misc.php?mod=seccode&update=23834&idhash=cSAMi22nd
        String url = "misc.php?mod=seccode&update=" + update + "&idhash=cSAMi22nd&mobile=2";
        if (!url.contains(hash)) {
            int start = url.indexOf("idhash") + 7;
            int end = url.indexOf("&", start);
            if (end < 0) {
                end = url.length();
            }
            url = url.replace(url.substring(start, end), hash);
        }

        //Log.v("===", HttpUtil.getStore(getActivity()).getCookie());
        HttpUtil.getClient().addHeader("Referer", App.getBaseUrl() + UrlUtils.getLoginUrl());
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                gifImageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(byte[] response) {
                try {
                    GifDrawable drawable = new GifDrawable(response);
                    gifImageView.setImageDrawable(drawable);
                } catch (GifIOException e1) {
                    // not gif format
                    gifImageView.setImageBitmap(BitmapFactory.decodeByteArray(response, 0, response.length));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                HttpUtil.getClient().removeHeader("Referer");
                progressBar.setVisibility(View.GONE);
                gifImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean checkInput() {
        String str = input.getText().toString().trim();
        if (str.length() < 3) {
            input.setError("字数不够");
            return false;
        }
        return true;
    }

    public interface OnInputValidListener {
        void onInputFinish(boolean click, String hash, String value);
    }
}
