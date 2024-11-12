package com.ref.project.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerAdapter {
    private static final String TAG = "ServerAdapter";
    private static final String URL = "https://dev.cloudinteractive.net"; // 올바른 경로 확인 필요
    private static final String PREFS_NAME = "appPrefs";
    private static final String JWT_TOKEN_KEY = "jwtToken";

    private static ServerAdapter instance;
    private final OkHttpClient client;
    private final SharedPreferences prefs;

    private ServerAdapter(Context context) {
        client = new OkHttpClient();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ServerAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new ServerAdapter(context.getApplicationContext());
        }
        return instance;
    }

    // JWT 토큰 요청 메서드
    public void requestJWTToken(String idToken, JWTCallback callback) {
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Authorization", "Bearer " + idToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    String jwtToken = json.getString("jwtToken");

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(JWT_TOKEN_KEY, jwtToken);
                    editor.apply();

                    Log.d(TAG, "JWT Token: " + jwtToken);
                    callback.onSuccess(jwtToken); // 성공 콜백 호출
                } else {
                    Log.e(TAG, "JWT 요청 실패 - 코드: " + response.code());
                    callback.onFailure("JWT 요청 실패 - 코드: " + response.code());
                }
            } catch (IOException | org.json.JSONException e) {
                Log.e(TAG, "Exception in requestJWTToken", e);
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    public String getStoredJWTToken() {
        return prefs.getString(JWT_TOKEN_KEY, null);
    }

    public interface JWTCallback {
        void onSuccess(String jwtToken);
        void onFailure(String errorMessage);
    }
}
