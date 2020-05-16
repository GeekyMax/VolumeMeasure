package com.geekymax.volumemeasure.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.entity.Record;
import com.geekymax.volumemeasure.manager.HistoryManager;
import com.meetic.marypopup.MaryPopup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistorySwipeAdapter extends RecyclerSwipeAdapter<HistorySwipeAdapter.SimpleViewHolder> {
    private Activity mContext;
    private List<Record> recordList = new ArrayList<>();

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        TextView textName;
        TextView textSize;
        TextView textId;
        ImageView imageView;
        View trash;
        View share;
        Record record;
        View surfaceView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            textSize = itemView.findViewById(R.id.item_size);
            textId = itemView.findViewById(R.id.item_id);
            textName = itemView.findViewById(R.id.item_name);
            imageView = itemView.findViewById(R.id.item_image);
            trash = itemView.findViewById(R.id.trash);
            share = itemView.findViewById(R.id.share);
            surfaceView = itemView.findViewById(R.id.surface_wrapper);
            View popView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.size_popup, null);

        }

        public void setRecord(Record record) {
            this.record = record;
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm:ss");
            textName.setText(sdf.format(record.date));
            textSize.setText(String.format("%.2f * %.2f * %.2f = %.6f", record.x, record.y, record.z, record.x * record.y * record.z));
            textId.setText(record.uid);
            imageView.setImageBitmap(record.bitmap);

        }
    }

    public HistorySwipeAdapter(Activity activity) {
        this.mContext = activity;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList.clear();
        this.recordList.addAll(recordList);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_layout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder viewHolder, int position) {
        Record item = recordList.get(position);
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
//                Toast.makeText(mContext, "swipe", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.surfaceView.setOnClickListener(v -> {

            View popView = LayoutInflater.from(mContext).inflate(R.layout.history_detail_popup, null);
            ImageView i = popView.findViewById(R.id.image);
            if (i != null) {
                i.setImageBitmap(viewHolder.record.bitmap);
            }
            MaryPopup sizePopup = MaryPopup.with(mContext)
                    .cancellable(true)
                    .blackOverlayColor(Color.parseColor("#DD444444"))
                    .backgroundColor(Color.parseColor("#EFF4F5"))
                    .content(popView)
                    .draggable(true)
                    .center(true)
                    .scaleDownDragging(true)
                    .from(viewHolder.imageView);
            sizePopup.show();
        });
        viewHolder.setRecord(item);
        viewHolder.trash.setOnClickListener(v -> {
            removeRecord(item);
        });
        viewHolder.share.setOnClickListener(v -> {
            HistoryManager.getInstance().shareHistory(item);
        });

    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeRecord(Record record) {
        HistoryManager.getInstance().deleteRecordById(record.uid);
        int pos = recordList.indexOf(record);
        recordList.remove(record);
        notifyItemRemoved(pos);
    }

    public void clearRecord() {
        recordList.clear();
        notifyDataSetChanged();

    }
}
