/*
    ServerAdapter - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Services;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerAdapter{
    private static final String TAG = "ServerAdapter";

    private final String endpoint;
    private final OkHttpClient client;

    // Constructor
    public ServerAdapter(Context context) throws IllegalArgumentException {
        Log.d(TAG, "init..");

        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            endpoint = info.metaData.getString("com.ref.project.API_ENDPOINT");
        }
        catch (PackageManager.NameNotFoundException e){
            Log.e(TAG, "API_ENDPOINT was not found in META_DATA!");
            throw new IllegalArgumentException();
        }

        client = new OkHttpClient();
    }

    public interface ITokenSignInCallback {
        public void onSuccess();
        public void onFailure();
    }

    public void TokenSignIn(String token, @NonNull ITokenSignInCallback callback){
        FormBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(endpoint + "/api/auth/tokenSignIn")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG,"TokenSignIn Exception!\n" + e);
                callback.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() != 200){
                    onFailure(call, new IOException("Server has returned statusCode " + response.code()));
                    return;
                }

                Log.d(TAG, "Server Authenticated.");
                callback.onSuccess();
            }
        });
    }
}