package com.example.audiobook_for_kids.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        
        if (msg.isUser()) {
            // Hiển thị bên phải (User)
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutAi.setVisibility(View.GONE);
            holder.tvUserText.setText(msg.getText());
        } else {
            // Hiển thị bên trái (AI)
            holder.layoutAi.setVisibility(View.VISIBLE);
            holder.layoutUser.setVisibility(View.GONE);
            holder.tvAiText.setText(msg.getText());
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutAi, layoutUser;
        TextView tvAiText, tvUserText;

        ChatViewHolder(View v) {
            super(v);
            layoutAi = v.findViewById(R.id.layout_ai_msg);
            layoutUser = v.findViewById(R.id.layout_user_msg);
            tvAiText = v.findViewById(R.id.tv_ai_text);
            tvUserText = v.findViewById(R.id.tv_user_text);
        }
    }
}
