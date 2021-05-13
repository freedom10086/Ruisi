package me.yluo.ruisiapp.downloadfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import me.yluo.ruisiapp.R;

public class DownLoadActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private downloadMsgReceiver downloadMsgReceiver;
    private TextView downloadInfo;
    private String fileName = "";
    private TextView btnClose = null;
    private TextView btnCancel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        fileName = getIntent().getStringExtra("fileName");
        int progress = getIntent().getIntExtra("progress", 0);
        //FileUtil.requestHandleFile(this,fileName);
        Log.i("fileInfo", fileName);
        TextView downPath = findViewById(R.id.down_path);
        downPath.setText("文件下载目录：" + FileUtil.DOWNLOAD_PATH);

        downloadInfo = findViewById(R.id.download_info);
        mProgressBar = findViewById(R.id.download_progress);
        btnClose = findViewById(R.id.btn_close);
        btnCancel = findViewById(R.id.btn_cancel);
        mProgressBar.setProgress(progress);
        btnCancel.setOnClickListener(v -> cancelDown());
        if (progress == 100) {
            downloadCompete();
            return;
        }

        //动态注册广播接收器
        downloadMsgReceiver = new downloadMsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("me.yluo.ruisiapp.download");
        registerReceiver(downloadMsgReceiver, intentFilter);
        downloadInfo.setText("下载" + fileName + " " + progress + "%");
        btnClose.setOnClickListener(v -> finish());
    }

    private void cancelDown() {
        //to do
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("cancel", true);
        startService(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (downloadMsgReceiver != null) {
            unregisterReceiver(downloadMsgReceiver);
        }
        super.onDestroy();
    }

    /**
     * 下载完成
     */
    private void downloadCompete() {
        downloadInfo.setText(fileName + "下载完成！");
        mProgressBar.setProgress(100);
        Toast.makeText(this, "下载完成,文件保存在" + FileUtil.DOWNLOAD_PATH,Toast.LENGTH_LONG).show();
        btnClose.setText("浏览");
        btnClose.setOnClickListener(v -> FileUtil.requestHandleFile(getApplicationContext(), fileName));
        btnCancel.setText("关闭");
        btnCancel.setOnClickListener(view -> finish());
    }

    /**
     * 广播接收器
     *
     * @author len
     */
    public class downloadMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
            int downloadType = intent.getIntExtra("type", DownloadService.DOWNLOADING);
            int progress = intent.getIntExtra("progress", 0);
            Log.i("recieve from service", progress + " " + downloadType);
            switch (downloadType) {
                case DownloadService.DOWN_ERROR:
                    downloadInfo.setText("文件下载失败！");
                    mProgressBar.setProgress(progress);
                    break;
                case DownloadService.DOWNLOADING:
                    mProgressBar.setProgress(progress);
                    downloadInfo.setText("下载" + fileName + " " + progress + "%");
                    break;
                case DownloadService.DOWN_OK:

                    Log.i("recieve ok 广播", ".............");
                    downloadCompete();
                    break;
                default:
                    break;
            }

        }
    }
}
