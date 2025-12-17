package com.example.audiobook_for_kids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AccountActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnEditProfile;
    private MaterialButton btnSettings;
    private MaterialButton btnHelp;
    private MaterialButton btnLogout;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "AudiobookPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Ánh xạ views
        initViews();

        // Thiết lập dữ liệu người dùng
        setupUserData();

        // Thiết lập listeners
        setupClickListeners();

        // Xử lý Bottom Navigation
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo tab Account được chọn
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_account);

        // Cập nhật lại dữ liệu người dùng khi quay lại
        setupUserData();
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnSettings = findViewById(R.id.btn_settings);
        btnHelp = findViewById(R.id.btn_help);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupUserData() {
        // Load dữ liệu người dùng từ SharedPreferences
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Người dùng");
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "user@kidobook.com");

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnSettings.setOnClickListener(v -> showSettingsMenu());

        btnHelp.setOnClickListener(v -> showHelpDialog());

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Click listener cho avatar để thay đổi ảnh đại diện
        ivProfilePicture.setOnClickListener(v -> changeProfilePicture());
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        // Ánh xạ views trong dialog
        com.google.android.material.textfield.TextInputEditText etName =
            dialogView.findViewById(R.id.et_dialog_name);
        com.google.android.material.textfield.TextInputEditText etEmail =
            dialogView.findViewById(R.id.et_dialog_email);
        MaterialButton btnChangePhoto = dialogView.findViewById(R.id.btn_change_photo);

        // Set current values
        etName.setText(sharedPreferences.getString(KEY_USER_NAME, "Người dùng"));
        etEmail.setText(sharedPreferences.getString(KEY_USER_EMAIL, "user@kidobook.com"));

        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        // Handle change photo button
        btnChangePhoto.setOnClickListener(v -> changeProfilePicture());

        // Handle save button
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_dialog_save);
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên hiển thị", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USER_NAME, newName);
            editor.putString(KEY_USER_EMAIL, newEmail);
            editor.apply();

            // Update UI
            setupUserData();

            Toast.makeText(this, "Đã cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Handle cancel button
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSettingsMenu() {
        String[] settingsOptions = {
            "Cài đặt thông báo",
            "Chất lượng âm thanh",
            "Tự động phát tiếp",
            "Chế độ tối",
            "Ngôn ngữ"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cài đặt")
                .setItems(settingsOptions, (dialog, which) -> {
                    handleSettingsSelection(which);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleSettingsSelection(int position) {
        switch (position) {
            case 0:
                showNotificationSettings();
                break;
            case 1:
                showAudioQualitySettings();
                break;
            case 2:
                showAutoPlaySettings();
                break;
            case 3:
                showDarkModeSettings();
                break;
            case 4:
                showLanguageSettings();
                break;
        }
    }

    private void showNotificationSettings() {
        boolean isEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cài đặt thông báo")
                .setSingleChoiceItems(new String[]{"Bật", "Tắt"}, isEnabled ? 0 : 1,
                    (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("notifications_enabled", which == 0);
                        editor.apply();

                        Toast.makeText(this,
                            which == 0 ? "Đã bật thông báo" : "Đã tắt thông báo",
                            Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAudioQualitySettings() {
        String[] qualities = {"Chất lượng thấp", "Chất lượng trung bình", "Chất lượng cao"};
        int currentQuality = sharedPreferences.getInt("audio_quality", 1);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chất lượng âm thanh")
                .setSingleChoiceItems(qualities, currentQuality,
                    (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("audio_quality", which);
                        editor.apply();

                        Toast.makeText(this, "Đã cập nhật chất lượng âm thanh", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAutoPlaySettings() {
        boolean isAutoPlay = sharedPreferences.getBoolean("auto_play", true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tự động phát tiếp")
                .setSingleChoiceItems(new String[]{"Bật", "Tắt"}, isAutoPlay ? 0 : 1,
                    (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("auto_play", which == 0);
                        editor.apply();

                        Toast.makeText(this,
                            which == 0 ? "Đã bật tự động phát" : "Đã tắt tự động phát",
                            Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDarkModeSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chế độ tối")
                .setSingleChoiceItems(new String[]{"Sáng", "Tối", "Theo hệ thống"},
                    isDarkMode ? 1 : 0,
                    (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("dark_mode", which == 1);
                        editor.apply();

                        Toast.makeText(this, "Đã cập nhật chế độ hiển thị", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        // TODO: Apply theme change
                    })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLanguageSettings() {
        String[] languages = {"Tiếng Việt", "English"};
        int currentLang = sharedPreferences.getInt("language", 0);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ngôn ngữ")
                .setSingleChoiceItems(languages, currentLang,
                    (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("language", which);
                        editor.apply();

                        Toast.makeText(this, "Đã cập nhật ngôn ngữ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        // TODO: Apply language change
                    })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showHelpDialog() {
        String helpContent = "📱 Ứng dụng Audiobook cho Trẻ em\n\n" +
                "🎵 Cách sử dụng:\n" +
                "• Chọn truyện từ trang chủ\n" +
                "• Dùng AI để tạo truyện mới\n" +
                "• Tìm kiếm truyện yêu thích\n" +
                "• Lưu truyện vào thư viện\n\n" +
                "📞 Hỗ trợ:\n" +
                "• Email: support@kidobook.com\n" +
                "• Hotline: 1900-1234\n\n" +
                "📋 Phiên bản: 1.0.0";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Trợ giúp & Hỗ trợ")
                .setMessage(helpContent)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Liên hệ", (dialog, which) -> {
                    // TODO: Open contact activity or email intent
                    Toast.makeText(this, "Mở ứng dụng email...", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        // Xóa dữ liệu đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        // clear stored tokens and user info
        editor.remove("refresh_token");
        editor.remove("user_email");
        editor.remove("user_name");
        editor.apply();

        // Clear SessionManager token
        com.example.audiobook_for_kids.auth.SessionManager.getInstance(this).clear();

        // Hiển thị thông báo
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Hiệu ứng: LoginActivity trượt lên từ dưới, AccountActivity giữ nguyên (smooth version)
        overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        finish();
    }

    private void changeProfilePicture() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện", "Sử dụng avatar mặc định"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thay đổi ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // TODO: Open camera
                            Toast.makeText(this, "Chức năng chụp ảnh đang phát triển", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // TODO: Open gallery
                            Toast.makeText(this, "Chức năng chọn ảnh đang phát triển", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            // Reset to default avatar
                            Toast.makeText(this, "Đã đặt lại ảnh đại diện mặc định", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(AccountActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(AccountActivity.this, AIStoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                Intent intent = new Intent(AccountActivity.this, LibraryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_account) {
                // Đã ở trang tài khoản
                return true;
            }

            return false;
        });
    }
}
