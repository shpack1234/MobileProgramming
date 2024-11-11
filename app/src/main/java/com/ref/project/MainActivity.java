package com.ref.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.ref.project.data.RequestJWTAdapter;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그인된 ID 토큰 가져오기
        SharedPreferences prefs = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String idToken = prefs.getString("idToken", null);
        if (idToken != null) {
            Log.d("MainActivity", "User is logged in with ID Token: " + idToken);
        } else {
            Log.d("MainActivity", "No user is logged in");
        }

        Button jwtButton=findViewById(R.id.btn_jwt);
        jwtButton.setOnClickListener(v -> getjwt(idToken));

        // 로그아웃 버튼 클릭 이벤트
        //Button logoutButton = findViewById(R.id.btn_logout);
        //logoutButton.setOnClickListener(v -> logout());
    }

    private void getjwt(String idToken) {
        Log.d("IDTOKEN", idToken);
        RequestJWTAdapter jwtAdapter=new RequestJWTAdapter(MainActivity.this);
        String jwtToken=jwtAdapter.requestJWTToken(idToken);
        if(jwtToken!=null) {
            Log.d("Token Request", jwtToken);
        } else {
            Log.e("Token Err", "failed to get JWTToken");
        }
    }

    // 로그아웃 처리 메서드
    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // SharedPreferences에서 로그인 정보 삭제
            SharedPreferences prefs = getSharedPreferences("appPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("idToken");
            editor.remove("jwtToken");
            editor.apply();

            // LoginActivity로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // 현재 MainActivity 종료
        });
    }
}
