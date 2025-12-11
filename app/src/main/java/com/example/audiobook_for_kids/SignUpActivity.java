package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignUp = findViewById(R.id.btn_signup);

        btnSignUp.setOnClickListener(v -> handleSignUp());
        findViewById(R.id.tv_login).setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });
        // Xử lý nút quay lại - về trang chủ với hiệu ứng slide down
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up_enter, R.anim.slide_down_exit);
            finish();
        });
    }

    private void handleSignUp() {
        String name = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Sending signup request: name=" + name + ", email=" + email + ", password=" + password);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:5000/auth/signup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("email", email);
                json.put("password", password);

                // Ghi dữ liệu JSON vào output stream
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

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
                Log.d(TAG, "Server response: " + serverResponse);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Phản hồi: " + serverResponse, Toast.LENGTH_LONG).show();
                    // Nếu thành công, chuyển sang VerifyOtpActivity
                    if (responseCode == 200 || responseCode == 201) {
                        Intent intent = new Intent(this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                });

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi: " + e.toString(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
