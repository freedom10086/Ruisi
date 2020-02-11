package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.model.ArticleListData;
import me.yluo.ruisiapp.model.GalleryData;
import me.yluo.ruisiapp.widget.MyGuildView;

/**
 * Created by yang on 16-3-31.
 * 支持 gallery
 */
public class HotNewListAdapter extends BaseAdapter {
    private static final int TYPE_ARTICLE_LIST = 3;
    private static final int TYPE_ARTICLE_HEADER = 2;

    private List<ArticleListData> dataSet;
    private List<GalleryData> galleryDatas;
    private Activity activity;
    private int readcolor;

    public HotNewListAdapter(Activity activity, List<ArticleListData> dataSet, @Nullable List<GalleryData> galleryDatas) {
        this.dataSet = dataSet;
        this.activity = activity;
        this.galleryDatas = galleryDatas;
        readcolor = ContextCompat.getColor(activity, R.color.text_color_sec);
    }


    @Override
    protected int getDataCount() {
        int count = dataSet.size();
        if (galleryDatas.size() > 0) {
            count++;
        }
        return count;
    }

    @Override
    protected int getItemType(int position) {
        if (position == 0 && galleryDatas.size() > 0) {
            return TYPE_ARTICLE_HEADER;
        } else {
            return TYPE_ARTICLE_LIST;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE_LIST:
                return new NormalViewHolderMe(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_me, parent, false));
            default:
                return new HeadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false));
        }
    }

    //手机版文章列表
    private class NormalViewHolderMe extends BaseViewHolder {
        TextView articleTitle;
        TextView authorName;
        ImageView imageTag;
        TextView replyCount;

        //构造
        NormalViewHolderMe(View v) {
            super(v);
            articleTitle = v.findViewById(R.id.article_title);
            authorName = v.findViewById(R.id.author_name);
            imageTag = v.findViewById(R.id.image_tag);
            replyCount = v.findViewById(R.id.reply_count);
            v.findViewById(R.id.main_item_btn_item).setOnClickListener(v1 -> onBtnItemClick());
        }

        //设置listItem的数据
        @Override
        void setData(int position) {
            if (galleryDatas.size() > 0 && position > 0) {
                position--;
            }
            ArticleListData single = dataSet.get(position);
            int color = single.titleColor;
            articleTitle.setTextColor(single.isRead ? readcolor : color);
            articleTitle.setText(single.title);
            authorName.setText("\uf2c0 " + single.author);
            replyCount.setText("\uf0e6 " + single.replayCount);
            imageTag.setVisibility(single.mobilePostType != null ? View.VISIBLE : View.GONE);
            if (single.mobilePostType != null) {
                imageTag.setImageResource(single.mobilePostType.resId);
            }

        }

        void onBtnItemClick() {
            int pos = getAdapterPosition();
            if (pos > 0 && galleryDatas.size() > 0) {
                pos--;
            }
            ArticleListData data = dataSet.get(pos);
            if (!data.isRead) {
                data.isRead = true;
                notifyItemChanged(getAdapterPosition());
            }
            PostActivity.open(activity, data.titleUrl, data.author);
        }
    }

    //图片切换view
    private class HeadViewHolder extends BaseViewHolder {
        private MyGuildView guildView;

        HeadViewHolder(View itemView) {
            super(itemView);
            guildView = itemView.findViewById(R.id.myGuideView);

        }

        @Override
        void setData(int position) {
            guildView.setData(galleryDatas);
            guildView.setListener((view, position1) -> {
                String titleUrl = galleryDatas.get(position1).getTitleUrl();
                if (!TextUtils.isEmpty(titleUrl)) {
                    PostActivity.open(activity, titleUrl, null);
                }
            });
        }
    }
}
