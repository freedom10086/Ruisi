package me.yluo.ruisiapp.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.listener.ListItemClickListener;

/**
 * Created by yang on 16-5-1.
 * 表情adapter
 */
public class SmileyAdapter extends RecyclerView.Adapter<SmileyAdapter.SmileyViewHolder> {

    private List<Pair<String, String>> smileys = new ArrayList<>();
    private final ListItemClickListener itemListener;
    private final Context context;

    public SmileyAdapter(Context context,ListItemClickListener itemListener, List<Pair<String, String>> smileys) {
        this.smileys = smileys;
        this.itemListener = itemListener;
        this.context = context;
    }


    @Override
    public SmileyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SmileyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smiley, parent, false));
    }

    @Override
    public void onBindViewHolder(SmileyViewHolder holder, int position) {
        holder.setSmiley(position);
    }


    @Override
    public int getItemCount() {
        return smileys.size();
    }

    class SmileyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;

        SmileyViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.smiley);
            image.setOnClickListener(view -> itemListener.onListItemClick(image, getAdapterPosition()));
        }


        private void setSmiley(int position) {
            Picasso.get().load(smileys.get(position).first).into(image);
        }


    }


}
