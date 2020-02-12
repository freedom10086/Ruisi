package me.yluo.ruisiapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.fragment.FrageHistory;
import me.yluo.ruisiapp.fragment.FrageMyStar;
import me.yluo.ruisiapp.fragment.FrageMyTopic;
import me.yluo.ruisiapp.model.FrageType;

public class FragementActivity extends BaseActivity {

    public static void open(Context c, int type) {
        Intent intent = new Intent(c, FragementActivity.class);
        intent.putExtra("TYPE", type);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout f = new FrameLayout(this);
        f.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        f.setId(R.id.container);
        setContentView(f);

        Fragment to = null;
        Bundle b = getIntent().getExtras();
        switch (b.getInt("TYPE")) {
            case FrageType.TOPIC:
                to = new FrageMyTopic();
                Bundle args = new Bundle();
                args.putString("username", b.getString("username"));
                args.putInt("uid", b.getInt("uid", 0));
                to.setArguments(args);
                break;
            case FrageType.STAR:
                to = new FrageMyStar();
                break;
            case FrageType.HISTORY:
                to = new FrageHistory();
                break;
            default:
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, to).commit();
    }

}
