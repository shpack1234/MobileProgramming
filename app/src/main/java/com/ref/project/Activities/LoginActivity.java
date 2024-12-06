/*
    LoginActivity - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.ref.project.R;
import com.ref.project.Services.GoogleSignInManager;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    GoogleSignInManager signInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 로그인 버튼 클릭 이벤트
        ImageButton signInButton = findViewById(R.id.login_google_signIn_btn);
        signInButton.setOnClickListener(v -> onClickSignInBtn());

        // ToS 텍스트 클릭 이벤트
        findViewById(R.id.login_tos_btn).setOnClickListener(v -> onClickTosText());
    }

    // Terms of Services page
    private void onClickTosText(){
        Intent intent = new Intent(this, TosActivity.class);
        this.startActivity(intent);
    }

    // Google Sign-in button click event
    private void onClickSignInBtn() {
        signInManager.SignInRequestAsync(this, false, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                signInManager.SetAutoSignIn(true);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                if(e instanceof NoCredentialException){
                    Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[] {"com.google"});
                    Toast.makeText(LoginActivity.this, getText(R.string.app_no_credential_error), Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
                else if(!(e instanceof GetCredentialCancellationException)){
                    Toast.makeText(LoginActivity.this, getText(R.string.app_unknown_login_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
