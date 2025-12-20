// File: auth/SessionManager.java
package com.example.audiobook_for_kids.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "audiobook_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private static SessionManager instance;
    private SharedPreferences prefs;

    private SessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }

    public void clear() { prefs.edit().remove(KEY_TOKEN).apply(); }
}

