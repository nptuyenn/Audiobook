package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private EditText etSearch;
    private ImageView ivBack;
    private ImageView ivClearSearch;
    private RecyclerView rvSearchResults;
    private TextView searchSuggest;
    private TextView recentSearch;

    // Topic CardViews
    private View cardTopic1, cardTopic2, cardTopic3, cardTopic4, cardTopic5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        try {
            setupTopicClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }

       }
    private void setupTopicClickListeners() {
        findViewById(R.id.card_cotich).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));

        findViewById(R.id.card_nuoc_ngoai).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));

        findViewById(R.id.card_ngu_ngon).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));

        findViewById(R.id.card_giao_duc).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));

        findViewById(R.id.card_phieu_luu).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        try {
            Intent intent = new Intent(this, TopicActivity.class);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TYPE, topicType);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TITLE, topicTitle);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
