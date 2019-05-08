package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.model.ArticleListData;
import me.yluo.ruisiapp.utils.DimenUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * Created by free2 on 16-3-5.
 * 一般文章列表adapter分校园网和外网
 */
public class PostListAdapter extends BaseAdapter {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_NORMAL_MOBILE = 1;
    public static final int TYPE_IMAGE = 2;
    private int size = 0;

    //数据
    private List<ArticleListData> dataSet;
    private int type = 3;

    //上下文
    private Activity activity;

    public PostListAdapter(Activity activity, List<ArticleListData> data, int type) {
        dataSet = data;
        this.activity = activity;
        this.type = type;
        size = DimenUtils.dip2px(activity, 42);
    }


    @Override
    protected int getDataCount() {
        return dataSet.size();
    }

    @Override
    protected int getItemType(int pos) {
        //手机版
        if (!App.IS_SCHOOL_NET || type == TYPE_NORMAL_MOBILE) {
            return TYPE_NORMAL_MOBILE;
        } else if (type == TYPE_IMAGE) {
            //一般板块
            return TYPE_IMAGE;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NORMAL_MOBILE:
                return new NormalViewHolderMe(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_post_me, parent, false));
            case TYPE_IMAGE:
                return new ImageCardViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_post_img, parent, false));
            default: // TYPE_NORMAL
                return new NormalViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_post_rs, parent, false));
        }
    }


    //校园网环境 帖子列表
    private class NormalViewHolder extends BaseViewHolder {
        protected TextView articleTitle;
        protected TextView postTime;
        TextView articleType;
        CircleImageView authorImg;
        TextView authorName;
        TextView replyCount;
        TextView viewCount;

        //构造
        NormalViewHolder(View v) {
            super(v);
            articleType = v.findViewById(R.id.article_type);
            articleTitle = v.findViewById(R.id.article_title);
            authorImg = v.findViewById(R.id.author_img);
            authorName = v.findViewById(R.id.author_name);
            postTime = v.findViewById(R.id.post_time);
            replyCount = v.findViewById(R.id.reply_count);
            viewCount = v.findViewById(R.id.view_count);
            v.findViewById(R.id.main_item_btn_item).setOnClickListener(v1 -> onBtnItemClick());
            authorImg.setOnClickListener(v2 -> onBtnAvatarClick());
        }

        //设置listItem的数据
        @Override
        void setData(int position) {
            ArticleListData single = dataSet.get(position);
            String type = single.type;
            if (TextUtils.isEmpty(type) || !type.equals("normal")) {
                articleType.setText(type);
                articleType.setVisibility(View.VISIBLE);
            } else {
                articleType.setVisibility(View.GONE);
            }

            postTime.setText("\uf017 " + single.postTime);
            viewCount.setText("\uf06e " + single.viewCount);
            replyCount.setText("\uf0e6 " + single.replayCount);
            authorName.setText("\uf2c0 " + single.author);

            String imageUrl = UrlUtils.getAvaterurlm(single.authorUrl);
            Picasso.get()
                    .load(imageUrl)
                    .resize(size, size)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(authorImg);
            int color = single.titleColor;
            int readcolor = ContextCompat.getColor(activity, R.color.text_color_sec);
            articleTitle.setTextColor(single.isRead ? readcolor : color);
            articleTitle.setText(TextUtils.isEmpty(single.tag) ? single.title : "[" + single.tag + "] " + single.title);

        }

        void onBtnAvatarClick() {
            String imageUrl = UrlUtils.getAvaterurlb(dataSet.get(getAdapterPosition()).authorUrl);
            UserDetailActivity.openWithAnimation(
                    activity, dataSet.get(getAdapterPosition()).author, authorImg, imageUrl);
        }

        void onBtnItemClick() {
            ArticleListData data = dataSet.get(getAdapterPosition());
            if (!data.isRead) {
                data.isRead = true;
                notifyItemChanged(getAdapterPosition());
            }
            PostActivity.open(activity, data.titleUrl, data.author);

        }
    }

    //手机版文章列表
    private class NormalViewHolderMe extends BaseViewHolder {
        TextView articleTitle;
        TextView authorName;
        TextView isImage;
        TextView replyCount;

        //构造
        NormalViewHolderMe(View v) {
            super(v);
            articleTitle = v.findViewById(R.id.article_title);
            authorName = v.findViewById(R.id.author_name);
            isImage = v.findViewById(R.id.is_image);
            replyCount = v.findViewById(R.id.reply_count);
            v.findViewById(R.id.main_item_btn_item).setOnClickListener(v1 -> onBtnItemClick());
        }

        //设置listItem的数据
        @Override
        void setData(int position) {
            ArticleListData single = dataSet.get(position);
            int color = single.titleColor;
            articleTitle.setTextColor(single.isRead ? 0xff888888 : color);
            articleTitle.setText(single.title);
            authorName.setText("\uf2c0 " + single.author);
            replyCount.setText("\uf0e6 " + single.replayCount);
            isImage.setVisibility(single.ishaveImage ? View.VISIBLE : View.GONE);
        }

        void onBtnItemClick() {
            ArticleListData data = dataSet.get(getAdapterPosition());
            if (!data.isRead) {
                data.isRead = true;
                notifyItemChanged(getAdapterPosition());
            }
            PostActivity.open(activity, data.titleUrl, data.author);
        }
    }

    //校园网环境 图片板块ViewHolder
    private class ImageCardViewHolder extends BaseViewHolder {

        ImageView imgCardImage;
        TextView imgCardTitle;
        TextView imgCardAuthor;
        TextView imgCardLike;

        ImageCardViewHolder(View itemView) {
            super(itemView);
            imgCardImage = itemView.findViewById(R.id.img_card_image);
            imgCardTitle = itemView.findViewById(R.id.img_card_title);
            imgCardAuthor = itemView.findViewById(R.id.img_card_author);
            imgCardLike = itemView.findViewById(R.id.img_card_like);

            itemView.findViewById(R.id.card_list_item).setOnClickListener(v -> item_click());
        }

        void setData(int position) {
            imgCardAuthor.setText("\uf2c0 " + dataSet.get(position).author);
            imgCardTitle.setText(dataSet.get(position).title);
            imgCardLike.setText("\uf08a " + dataSet.get(position).replayCount);
            if (!TextUtils.isEmpty(dataSet.get(position).imUrl)) {
                Picasso.get()
                        .load(App.getBaseUrl() + dataSet.get(position).imUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(imgCardImage);
            } else {
                imgCardImage.setImageResource(R.drawable.image_placeholder);
            }

        }

        void item_click() {
            ArticleListData articleListData = dataSet.get(getAdapterPosition());
            PostActivity.open(activity, articleListData.titleUrl, articleListData.author);
        }
    }

}
