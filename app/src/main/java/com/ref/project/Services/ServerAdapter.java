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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ref.project.Models.AccountInfoModel;
import com.ref.project.Models.AddItemListModel;
import com.ref.project.Models.CategoryListModel;
import com.ref.project.Models.ItemListModel;
import com.ref.project.Models.ReceiptModel;

import java.io.IOException;
import java.net.CookieManager;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerAdapter{
    // Interfaces
    public interface ITokenSignInCallback {
        void onSuccess();
        void onFailure();
    }

    public interface IServerRequestCallback<T> {
        void onSuccess(T result);
        void onFailure();
    }

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


        client = new OkHttpClient().newBuilder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .readTimeout(80, TimeUnit.SECONDS)
                .build();
    }

    private <T> void requestAsync(String taskName, Request request, Class<T> type, IServerRequestCallback<T> callback){
        Log.d(TAG, "RequestAsync (" + taskName + "): Begin.");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "RequestAsync (" + taskName + ") Exception during request!\n" + e);
                callback.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if(response.code() != 200){
                    onFailure(call, new IOException("Server has returned statusCode "  + response.code()));
                    return;
                }
                else if(response.body() == null){
                    onFailure(call, new IOException("body was null."));
                    return;
                }

                try{
                    byte[] body = response.body().bytes();

                    if(body == null || body.length == 0) {
                        Log.d(TAG, "RequestAsync (" + taskName + "): Ok [body.length=0]");
                        callback.onSuccess(null);
                        return;
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    T model = mapper.readValue(body, type);
                    Log.d(TAG, "RequestAsync (" + taskName + "): " + new String(body, StandardCharsets.UTF_8));
                    callback.onSuccess(model);
                }
                catch (Exception e){
                    Log.e(TAG, "RequestAsync (" + taskName + ") Exception during parsing!\n" + e);
                    callback.onFailure();
                }
            }
        });
    }

    public void TokenSignInAsync(String token, @NonNull ITokenSignInCallback callback){
        requestAsync("TokenSignInAsync",
                new Request.Builder()
                        .url(endpoint + "/api/auth/tokenSignIn")
                        .post(new FormBody.Builder()
                                .add("token", token)
                                .build())
                        .build(),
                Void.class, new IServerRequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Server authenticated.");
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure() {
                        Log.w(TAG, "Server authentication failed!");
                        callback.onFailure();
                    }
                });
    }

    public void GetAccountInfoAsync(IServerRequestCallback<AccountInfoModel> callback){
        requestAsync("GetAccountInfoAsync",
                new Request.Builder()
                        .url(endpoint + "/api/auth/accountInfo")
                        .get()
                        .build(),
                AccountInfoModel.class, callback);
    }

    public void GetCategoryListAsync(IServerRequestCallback<CategoryListModel> callback){
        requestAsync("GetCategoryListAsync",
                new Request.Builder()
                        .url(endpoint + "/api/fridge/categoryList")
                        .get()
                        .build(),
                CategoryListModel.class, callback);
    }

    public void GetItemListAsync(IServerRequestCallback<ItemListModel> callback){
        requestAsync("GetCategoryListAsync",
                new Request.Builder()
                        .url(endpoint + "/api/fridge/itemList")
                        .get()
                        .build(),
                ItemListModel.class, callback);
    }

    public void AddItemsAsync(AddItemListModel model, IServerRequestCallback<Void> callback) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String json;

        try{
            json = mapper.writeValueAsString(model);
        }
        catch (Exception e){
            Log.e(TAG, "Exception in AddItemsAsync() writeValueAsString\n" + e);
            callback.onFailure();
            return;
        }

        requestAsync("AddItemsAsync",
                new Request.Builder()
                        .url(endpoint + "/api/fridge/addItems")
                        .post(RequestBody.create(json, MediaType.parse("application/json; charset=utf-8")))
                        .build(), Void.class, callback);
    }

    public void ImportFromReceiptAsync(byte[] image, IServerRequestCallback<ReceiptModel> callback){
        requestAsync("ImportFromReceiptAsync",
                new Request.Builder()
                        .url(endpoint + "/api/intelligence/importFromReceipt")
                        .post(new MultipartBody.Builder()
                                .addFormDataPart("image", "image.jpg",
                                        RequestBody.create(image)).setType(MultipartBody.FORM).build())
                        .build(),
                ReceiptModel.class, callback);
    }
}