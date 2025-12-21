package com.example.audiobook_for_kids.utils;

public class DeviceUtils {

    public static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK")
                || android.os.Build.MANUFACTURER.contains("Google")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.HARDWARE.contains("goldfish");
    }
}
