package com.ref.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그아웃 버튼 클릭 이벤트
        Button logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> logout());
    }

    // 로그아웃 처리 메서드
    private void logout() {

    }
}
