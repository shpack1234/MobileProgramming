package com.ref.project.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class RequestJWTAdapter {

    private static final String TAG = "RequestJWTAdapter";
    private static final String JWT_PREFS = "appPrefs";
    private static final String JWT_TOKEN_KEY = "jwtToken";
    private static final String URL = "https://dev.cloudinteractive.net";

    private final OkHttpClient client;
    private final SharedPreferences prefs;

    public RequestJWTAdapter(Context context) {
        this.client = new OkHttpClient();
        this.prefs = context.getSharedPreferences(JWT_PREFS, Context.MODE_PRIVATE);
    }

    // JWT 토큰 요청 메서드
    public String requestJWTToken(String idToken) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 요청 생성
                Request request = new Request.Builder()
                        .url(URL)
                        .addHeader("Authorization", "Bearer " + idToken)
                        .build();

                // 네트워크 요청을 수행하는 스레드 생성
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        String jwtToken = json.getString("jwtToken");

                        // JWT 토큰을 SharedPreferences에 저장
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(JWT_TOKEN_KEY, jwtToken);
                        editor.apply();

                        Log.d(TAG, "JWT Token retrieved and saved successfully: " + jwtToken);
                    } else {
                        Log.e(TAG, "Failed to retrieve JWT Token. Response Code: " + response.code());
                    }
                } catch (IOException | org.json.JSONException e) {
                    Log.e(TAG, "Exception in requestJWTToken", e);
                }
            }
        }).start();  // 새 스레드에서 실행
        return idToken;
    }

    // 저장된 JWT 토큰 가져오기
    public String getStoredJWTToken() {
        return prefs.getString(JWT_TOKEN_KEY, null);
    }

    // JWT 토큰 삭제 메서드
    public void clearJWTToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(JWT_TOKEN_KEY);
        editor.apply();
        Log.d(TAG, "JWT Token cleared from SharedPreferences.");
    }
}