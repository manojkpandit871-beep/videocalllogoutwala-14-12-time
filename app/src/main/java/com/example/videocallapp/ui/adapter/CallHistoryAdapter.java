package com.example.videocallapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocallapp.R;
import com.example.videocallapp.model.CallRecord;
import com.example.videocallapp.utils.CallTimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView adapter for call history items.
 */
public class CallHistoryAdapter extends ListAdapter<CallRecord, CallHistoryAdapter.VH> {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public CallHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_call_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CallRecord record = getItem(position);

        holder.tvUserName.setText(record.userName);
        holder.tvRoomId.setText("Room: " + record.roomId);
        holder.tvDate.setText(DATE_FMT.format(new Date(record.timestamp)));
        holder.tvDuration.setText(CallTimer.format(record.durationSec));

        // Set icon based on call type
        switch (record.callType) {
            case "INCOMING":
                holder.ivCallType.setImageResource(R.drawable.ic_call_incoming);
                holder.ivCallType.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.call_incoming));
                break;
            case "MISSED":
                holder.ivCallType.setImageResource(R.drawable.ic_call_missed);
                holder.ivCallType.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.call_missed));
                holder.tvDuration.setText("Missed");
                break;
            default: // OUTGOING
                holder.ivCallType.setImageResource(R.drawable.ic_call_outgoing);
                holder.ivCallType.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.call_outgoing));
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCallType;
        TextView  tvUserName, tvRoomId, tvDate, tvDuration;

        VH(View v) {
            super(v);
            ivCallType  = v.findViewById(R.id.iv_call_type);
            tvUserName  = v.findViewById(R.id.tv_user_name);
            tvRoomId    = v.findViewById(R.id.tv_room_id);
            tvDate      = v.findViewById(R.id.tv_date);
            tvDuration  = v.findViewById(R.id.tv_duration);
        }
    }

    private static final DiffUtil.ItemCallback<CallRecord> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CallRecord>() {
                @Override
                public boolean areItemsTheSame(@NonNull CallRecord a, @NonNull CallRecord b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull CallRecord a, @NonNull CallRecord b) {
                    return a.timestamp == b.timestamp && a.callType.equals(b.callType);
                }
            };
}
