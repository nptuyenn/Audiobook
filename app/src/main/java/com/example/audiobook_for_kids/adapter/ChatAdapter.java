package com.example.audiobook_for_kids.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiobook_for_kids.R;
import com.example.audiobook_for_kids.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 1 : 0;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We reuse some simple system layouts or create small ones
        // For brevity, I'll assume we use a simple layout for chat
        int layout = (viewType == 1) ? android.R.layout.simple_list_item_1 : android.R.layout.simple_list_item_1;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.tvText.setText(msg.getText());
        if (msg.isUser()) {
            holder.tvText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.tvText.setTextColor(0xFF2196F3);
        } else {
            holder.tvText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.tvText.setTextColor(0xFF333333);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        ChatViewHolder(View v) {
            super(v);
            tvText = v.findViewById(android.R.id.text1);
        }
    }
}
