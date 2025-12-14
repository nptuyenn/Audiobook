package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView iv_back = findViewById(R.id.iv_back);
        if (iv_back != null) {
            iv_back.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            });
        }

        try {
            setupTopicClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupTopicClickListeners() {
        // Cổ tích
        View cardCoTich = findViewById(R.id.card_cotich);
        cardCoTich.setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));

        // Nước ngoài
        View cardNuocNgoai = findViewById(R.id.card_nuoc_ngoai);
        cardNuocNgoai.setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));

        // Ngụ ngôn
        View cardNguNgon = findViewById(R.id.card_ngu_ngon);
        cardNguNgon.setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));

        // Giáo dục
        View cardGiaoDuc = findViewById(R.id.card_giao_duc);
        cardGiaoDuc.setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));

        // Phiêu lưu
        View cardPhieuLuu = findViewById(R.id.card_phieu_luu);
        cardPhieuLuu.setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        try {
            Intent intent = new Intent(this, TopicActivity.class);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TYPE, topicType);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TITLE, topicTitle);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
