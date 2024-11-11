/*
    GoogleSignInManager - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Services;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class GoogleSignInManager {
    private static final String TAG = "GoogleSignInManager";

    private final Context context;
    private final String clientId;

    public GoogleSignInManager(Context c){
        Log.d(TAG, "init..");
        context = c;

        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            clientId = info.metaData.getString("com.ref.project.GOOGLE_CLIENT_ID");
            if(clientId == null) throw new Exception();

        }
        catch (Exception e) {
            Log.e(TAG, "GOOGLE_CLIENT_ID was not found in META_DATA!");
            throw new IllegalArgumentException();
        }
    }

    public boolean GetAutoSignIn(){
        SharedPreferences prefs = context.getSharedPreferences("appPrefs", MODE_PRIVATE);
        boolean state = prefs.getBoolean("autoSignIn", false);

        Log.d(TAG, "autoSignIn=" + state);
        return state;
    }

    public void SetAutoSignIn(boolean state){
        SharedPreferences prefs = context.getSharedPreferences("appPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("autoSignIn", state).apply();
        Log.d(TAG, "Set autoSignIn=" + state);
    }

    public void SignInRequestAsync(Context c, boolean autoSelect, CredentialManagerCallback<GetCredentialResponse, GetCredentialException> callback){
        GetGoogleIdOption option = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(autoSelect)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        // Google Sign-in using CredentialManager API.
        CredentialManager credentialManager = CredentialManager.create(c);
        credentialManager.getCredentialAsync(c, request, null, c.getMainExecutor(), new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                String token = GoogleIdTokenCredential.createFrom(getCredentialResponse.getCredential().getData()).getIdToken();
                Log.d(TAG, "Authenticated. (Token: " + token + ")");
                callback.onResult(getCredentialResponse);
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                Log.e(TAG, "Authentication failed!", e);
                callback.onError(e);
            }

        });
    }
}
