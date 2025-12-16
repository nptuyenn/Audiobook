package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        // Xử lý nút quay lại - về trang chủ với hiệu ứng slide down
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Hiệu ứng: MainActivity giữ nguyên, LoginActivity trượt xuống (smooth version)
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
            finish();
        });

        findViewById(R.id.btn_login).setOnClickListener(v -> handleLogin());
        findViewById(R.id.tv_register).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            // Hiệu ứng: SignUpActivity trượt lên từ dưới, LoginActivity giữ nguyên (smooth version)
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        });

        // Open ForgotPasswordActivity when clicking "Quên mật khẩu?"
        findViewById(R.id.tv_forgot_password).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:5000/auth/signin");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true); // ĐẶT TRƯỚC headers
                conn.setRequestProperty("Content-Type", "application/json"); // ✅ Bỏ charset
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                // Tạo JSON
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                //  Log để debug
                System.out.println("=== LOGIN REQUEST ===");
                System.out.println("URL: " + url);
                System.out.println("JSON: " + json.toString());

                // Gửi data
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush(); //  THÊM flush
                }

                // Đọc response
                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                br.close();
                conn.disconnect();

                String serverResponse = response.toString();
                System.out.println("Server Response: " + serverResponse);

                runOnUiThread(() -> {
                    try {
                        JSONObject respJson = new JSONObject(serverResponse);
                        if (responseCode == 200) {
                            //  Lưu token nếu cần
                            String accessToken = respJson.optString("accessToken", "");
                            String refreshToken = respJson.optString("refreshToken", "");

                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            // Hiệu ứng: MainActivity giữ nguyên, LoginActivity trượt xuống (smooth version)
                            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
                            finish();
                        } else {
                            String message = respJson.has("message")
                                    ? respJson.getString("message")
                                    : "Đăng nhập thất bại";
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}