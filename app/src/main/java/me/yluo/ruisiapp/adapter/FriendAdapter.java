package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.ChatActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.listener.ListItemLongClickListener;
import me.yluo.ruisiapp.model.FriendData;
import me.yluo.ruisiapp.widget.CircleImageView;

/**
 * Created by yang on 16-4-12.
 * 好友列表
 */
public class FriendAdapter extends BaseAdapter {

    private final List<FriendData> datas;
    private final Context context;
    private final ListItemLongClickListener listener;

    public FriendAdapter(Context context, List<FriendData> datas, ListItemLongClickListener l) {
        this.datas = datas;
        this.context = context;
        this.listener = l;
    }

    @Override
    protected int getDataCount() {
        return datas.size();
    }

    @Override
    protected int getItemType(int pos) {
        return 0;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new FriendViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false));
    }

    private class FriendViewHolder extends BaseViewHolder {
        CircleImageView userImage;
        TextView userName, userInfo, isOnline;
        View container;

        FriendViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.logo);
            userName = itemView.findViewById(R.id.user_name);
            userInfo = itemView.findViewById(R.id.user_info);
            isOnline = itemView.findViewById(R.id.is_online);
            container = itemView.findViewById(R.id.list_item);

            userImage.setOnClickListener(v -> userImageClick());
            container.setOnClickListener(v -> itemClick());
        }

        @Override
        void setData(final int position) {
            FriendData single = datas.get(position);
            userName.setText(single.userName);
            userName.setTextColor(single.usernameColor);
            userInfo.setText(single.info);
            isOnline.setVisibility(single.isOnline() ? View.VISIBLE : View.GONE);
            Picasso.get().load(single.imgUrl).placeholder(R.drawable.image_placeholder).into(userImage);
            container.setOnLongClickListener(view -> {
                if (listener != null) {
                    listener.onItemLongClick(container, position);
                    return true;
                }
                return false;
            });
        }

        void userImageClick() {
            FriendData single = datas.get(getAdapterPosition());
            String username = single.userName;
            UserDetailActivity.openWithAnimation((Activity) context, username, userImage, single.uid);
        }

        void itemClick() {
            String uid = datas.get(getAdapterPosition()).uid;
            String username = datas.get(getAdapterPosition()).userName;
            String url = "home.php?mod=space&do=pm&subop=view&touid=" + uid + "&mobile=2";
            ChatActivity.open(context, username, url);
        }

    }
}
