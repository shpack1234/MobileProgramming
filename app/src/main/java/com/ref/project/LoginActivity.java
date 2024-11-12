package com.ref.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.gms.common.SignInButton;
import com.ref.project.Services.GoogleSignInManager;
import com.ref.project.Services.ServerAdapter;

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
        SignInButton signInButton = findViewById(R.id.login_google_signIn_btn);
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
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {

            }
        });
    }

}
