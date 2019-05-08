package me.yluo.ruisiapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.yluo.ruisiapp.R;


/**
 * A simple {@link Fragment} subclass.
 */

public abstract class BaseFragment extends Fragment {
    protected View mRootView;
    protected View toolBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
        return mRootView;
    }

    /**
     * 初始化一个toolbar
     * @param closeAble 是否显示返回按钮
     * @param title 标题
     */
    protected void initToolbar(boolean closeAble, String title) {
        toolBar = mRootView.findViewById(R.id.myToolBar);
        if (toolBar != null) {
            TextView titles = toolBar.findViewById(R.id.title);
            titles.setText(title);
            ImageView i = toolBar.findViewById(R.id.logo);
            if (closeAble) {
                i.setImageResource(R.drawable.ic_arrow_back_white);
                i.setOnClickListener(view -> getActivity().finish());
            } else {
                i.setVisibility(View.GONE);
            }
        }
    }

    protected ImageView addToolbarMenu(int resid) {
        toolBar = mRootView.findViewById(R.id.myToolBar);
        if (toolBar != null) {
            ImageView i = toolBar.findViewById(R.id.menu);
            i.setVisibility(View.VISIBLE);
            i.setImageResource(resid);
            return i;
        }
        return null;
    }

    protected ImageView setToolbarLogo(int resid) {
        toolBar = mRootView.findViewById(R.id.myToolBar);
        if (toolBar != null) {
            ImageView i = toolBar.findViewById(R.id.logo);
            i.setVisibility(View.VISIBLE);
            i.setImageResource(resid);
            return i;
        }
        return null;
    }


    protected abstract int getLayoutId();


    protected void switchActivity(Class<?> cls) {
        getActivity().startActivity(new Intent(getActivity(), cls));
    }
}
