// File: utils/Constants.java
package com.example.audiobook_for_kids.utils;

public class Constants {

    private static final String LOCAL_BASE_URL = "http://10.0.2.2:5000/";
    private static final String NGROK_BASE_URL = "https://advised-emmett-biogenetically.ngrok-free.dev/";

    public static final String BASE_URL =
            DeviceUtils.isEmulator()
                    ? LOCAL_BASE_URL
                    : NGROK_BASE_URL;
}
