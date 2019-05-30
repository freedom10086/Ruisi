package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.listener.ListItemClickListener;
import me.yluo.ruisiapp.model.SingleArticleData;
import me.yluo.ruisiapp.model.SingleType;
import me.yluo.ruisiapp.utils.DimenUtils;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.CircleImageView;
import me.yluo.ruisiapp.widget.htmlview.HtmlView;

/**
 * Created by free2 on 16-3-7.
 * 单篇文章adapter
 * 评论 文章 loadmore
 */

public class PostAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private static final int COMENT = 1;
    private static final int HEADER = 3;
    private int size = 0;


    //数据
    private List<SingleArticleData> datalist;
    private Activity activity;

    public PostAdapter(
            Activity activity, ListItemClickListener itemListener,
            List<SingleArticleData> datalist) {

        this.datalist = datalist;
        this.activity = activity;
        size = DimenUtils.dip2px(activity, 42);
        setItemListener(itemListener);
    }


    @Override
    protected int getDataCount() {
        return datalist.size();
    }

    @Override
    protected int getItemType(int pos) {
        if (datalist.get(pos).type == SingleType.CONTENT) {
            return CONTENT;
        } else if (datalist.get(pos).type == SingleType.HEADER) {
            return HEADER;
        } else {
            return COMENT;
        }
    }

    @Override
    protected BaseAdapter.BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CONTENT:
                return new ArticleContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false));
            case HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_h, parent, false));
            default: // TYPE_COMMENT
                return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        }
    }


    //文章内容 楼主ViewHolder
    private class ArticleContentViewHolder extends BaseViewHolder {
        CircleImageView userAvatar;
        TextView title, postTime, userName, content, btnMore;

        ArticleContentViewHolder(View itemView) {
            super(itemView);
            btnMore = itemView.findViewById(R.id.btn_more);
            title = itemView.findViewById(R.id.article_title);
            userAvatar = itemView.findViewById(R.id.article_user_image);
            userName = itemView.findViewById(R.id.article_username);
            postTime = itemView.findViewById(R.id.article_post_time);
            content = itemView.findViewById(R.id.content);
            userAvatar.setOnClickListener(v -> {
                if (datalist.get(0).uid > 0) {
                    UserDetailActivity.openWithAnimation(
                            activity, datalist.get(0).username, userAvatar, datalist.get(0).uid);
                }
            });
            btnMore.setOnClickListener(this);
        }

        @Override
        void setData(int position) {
            final SingleArticleData single = datalist.get(position);
            title.setText(single.title);
            userName.setText(single.username);
            String img_url = UrlUtils.getAvaterurlm(single.getImg());
            if (single.uid > 0) {
                Picasso.get()
                    .load(img_url)
                    .resize(size, size)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(userAvatar);
            } else {
                Picasso.get()
                    .load(R.drawable.image_placeholder)
                    .resize(size, size)
                    .into(avatar);
            }
            
            String post_time = "发表于:" + single.postTime;
            postTime.setText(post_time);
            HtmlView.parseHtml(single.content).into(content);
        }
    }

    // 评论ViewHolder
    private class CommentViewHolder extends BaseViewHolder {
        ImageView avatar;
        TextView username, index, replyTime, comment, labelLz;
        View btnReplyCz, btnMore;

        CommentViewHolder(View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.article_user_image);
            btnReplyCz = itemView.findViewById(R.id.btn_reply_cz);
            btnMore = itemView.findViewById(R.id.btn_more);
            username = itemView.findViewById(R.id.replay_author);
            index = itemView.findViewById(R.id.replay_index);
            replyTime = itemView.findViewById(R.id.replay_time);
            comment = itemView.findViewById(R.id.html_text);
            labelLz = itemView.findViewById(R.id.bt_lable_lz);

            comment.setOnLongClickListener(view -> {
                String user = datalist.get(getAdapterPosition()).username;
                String content = comment.getText().toString().trim();
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText(null, content));
                    Toast.makeText(activity, "已复制" + user + "的评论", Toast.LENGTH_SHORT).show();
                }

                return true;
            });

            avatar.setOnClickListener(v -> {
                if (datalist.get(getAdapterPosition()).uid > 0) {
                    UserDetailActivity.openWithAnimation(
                            activity, datalist.get(getAdapterPosition()).username,
                            avatar, datalist.get(getAdapterPosition()).uid);
                }
            });


            btnReplyCz.setOnClickListener(this);
            btnMore.setOnClickListener(this);
        }

        //设置listItem的数据
        @Override
        void setData(int position) {
            final SingleArticleData single = datalist.get(position);
            username.setText(single.username);
            //判断是不是楼主
            boolean isLz = datalist.get(position).username.equals(datalist.get(0).username);
            labelLz.setVisibility(isLz ? View.VISIBLE : View.GONE);
            boolean isReply = single.replyUrlTitle.contains("action=reply");
            btnReplyCz.setVisibility(isReply ? View.VISIBLE : View.GONE);
            String imgUrl = UrlUtils.getAvaterurlm(single.getImg());
            if (single.uid > 0) {
                Picasso.get()
                    .load(imgUrl)
                    .resize(size, size)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(avatar);
            } else {
                Picasso.get()
                    .load(R.drawable.image_placeholder)
                    .resize(size, size)
                    .into(avatar);
            }
            replyTime.setText(single.postTime);
            index.setText(single.index);
            HtmlView.parseHtml(single.content).into(comment);
        }
    }

    public void copyItem(int position) {
        String user = datalist.get(position).username;
        Document document = Jsoup.parse(datalist.get(position).content);
        String content = document.text();
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(null, content));
            Toast.makeText(activity, "已复制" + user + "的评论", Toast.LENGTH_SHORT).show();
        }
    }

    //header
    private class HeaderViewHolder extends BaseViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);

        }

        @Override
        void setData(int position) {

        }
    }
}
