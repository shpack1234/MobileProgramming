/*
    IntroActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.ref.project.R;
import com.ref.project.Services.GoogleSignInManager;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

@AndroidEntryPoint
public class IntroActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        boolean autoSignIn = signInManager.GetAutoSignIn();
        if(autoSignIn) {
            signInManager.SignInRequestAsync(this,true, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse getCredentialResponse) {
                    endIntro(true);
                }

                @Override
                public void onError(@NonNull GetCredentialException e) {
                    endIntro(false);
                }
            });
        }
        else {
            new Handler().postDelayed(() -> endIntro(false), 1800);

        }
    }

    private void endIntro(boolean state){
        Intent intent;
        if (!state) intent = new Intent(getApplicationContext(), LoginActivity.class);
        else intent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(intent);
        finish();
    }
}