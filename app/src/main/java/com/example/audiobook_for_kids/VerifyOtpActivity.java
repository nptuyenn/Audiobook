package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        etOtp = findViewById(R.id.et_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);

        // Lấy email từ Intent
        String email = getIntent().getStringExtra("email");

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty() || otp.length() != 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2:5000/auth/verify-otp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setDoOutput(true);

                    JSONObject json = new JSONObject();
                    json.put("email", email);
                    json.put("otp", otp);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = json.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }

                    int responseCode = conn.getResponseCode();
                    InputStream is = (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream() : conn.getErrorStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    br.close();

                    String serverResponse = response.toString();
                    JSONObject resJson = new JSONObject(serverResponse);

                    runOnUiThread(() -> {
                        if (responseCode == 200) {
                            Toast.makeText(this, resJson.optString("message", "Xác minh thành công!"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, resJson.optString("message", "OTP không hợp lệ hoặc hết hạn"), Toast.LENGTH_SHORT).show();
                        }
                    });

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }
}
