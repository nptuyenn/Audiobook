// File: utils/Constants.java
package com.example.audiobook_for_kids.utils;

public class Constants {
    public static final boolean IS_EMULATOR = false;

    public static final String BASE_URL = IS_EMULATOR
            ? "http://10.0.2.2:5000/"
            : "https://advised-emmett-biogenetically.ngrok-free.dev/";
}