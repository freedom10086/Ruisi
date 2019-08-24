package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.ChatActivity;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.MessageData;
import me.yluo.ruisiapp.widget.ArrowTextView;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * Created by free2 on 16-3-21.
 * 首页第三页
 * 回复我的 我的消息
 */
public class MessageAdapter extends BaseAdapter {
    protected Activity activity;
    private List<MessageData> dataList;

    public MessageAdapter(Activity activity, List<MessageData> dataSet) {
        dataList = dataSet;
        this.activity = activity;
    }

    @Override
    protected int getDataCount() {
        return dataList.size();
    }

    @Override
    protected int getItemType(int pos) {
        return 0;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new MessageReplyListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messgae, parent, false));

    }


    //用户消息、回复我的 holder
    private class MessageReplyListHolder extends BaseViewHolder {
        protected TextView title;
        protected TextView time;
        CircleImageView articleUserImage;
        ArrowTextView replyContent;
        TextView isRead;

        //url
        MessageReplyListHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            articleUserImage = itemView.findViewById(R.id.article_user_image);
            replyContent = itemView.findViewById(R.id.reply_content);
            isRead = itemView.findViewById(R.id.is_read);

            articleUserImage.setOnClickListener(v -> userClick());
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> itemClick());
        }

        @Override
        void setData(int position) {
            MessageData data = dataList.get(position);
            title.setText(data.getTitle());
            time.setText(data.getTime());
            String imageUrl = data.getauthorImage();
            Picasso.get().load(imageUrl).placeholder(R.drawable.image_placeholder).into(articleUserImage);
            replyContent.setText(data.getcontent());
            if (data.isRead()) {
                isRead.setVisibility(View.GONE);
            } else {
                isRead.setVisibility(View.VISIBLE);
            }
        }

        void itemClick() {
            MessageData messageData = dataList.get(getAdapterPosition());
            if (!messageData.isRead()) {
                messageData.setRead(true);
                notifyItemChanged(getAdapterPosition());
            }
            if (ListType.MYMESSAGE == messageData.getType()) {//用户消息pm
                String username = messageData.getTitle()
                        .replace("我对 ", "")
                        .replace("说:", "")
                        .replace(" 对我", "");
                ChatActivity.open(activity, username, messageData.getTitleUrl());
                messageData.setRead(true);
            } else if (ListType.REPLAYME == messageData.getType()) {//回复我的
                PostActivity.open(activity, messageData.getTitleUrl(), null);
            }

        }

        void userClick() {
            MessageData messageData = dataList.get(getAdapterPosition());
            String username = messageData.getTitle().replace("我对 ", "")
                    .replace("说:", "")
                    .replace(" 对我", "")
                    .replace(" 回复了我", "");
            UserDetailActivity.openWithAnimation(
                    activity, username, articleUserImage, dataList.get(getAdapterPosition()).getauthorImage());
        }
    }

}