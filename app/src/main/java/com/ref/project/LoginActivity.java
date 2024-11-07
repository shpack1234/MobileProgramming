package com.ref.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // GoogleSignInOptions 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_CLIENT_ID")  // 클라이언트 ID 입력
                .requestEmail()
                .build();

        // GoogleSignInClient 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ActivityResultLauncher 초기화
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }
        );

        // 로그인 버튼 클릭 이벤트
        Button signInButton = findViewById(R.id.btn_google_sign_in);
        signInButton.setOnClickListener(v -> signIn());
    }

    // 로그인 시작 메서드
    private void signIn() {
        Log.d("Notion", "Login Start...");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent); // ActivityResultLauncher 사용
    }

    // 로그인 결과 처리 메서드
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            Log.d("Token", idToken);
            // 서버로 ID 토큰을 보내고 JWT 토큰을 요청
            //requestJWTToken(idToken);

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // 서버에 ID 토큰을 보내고 JWT 토큰을 요청하는 메서드
    private void requestJWTToken(String idToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("")  // 서버의 URL을 입력
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        // 네트워크 작업은 별도의 스레드에서 처리해야 함
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        // 서버에서 반환된 JWT 토큰을 파싱
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        String jwtToken = json.getString("jwtToken");

                        Log.d(TAG, "JWT Token: " + jwtToken);

                        // 필요에 따라 SharedPreferences에 JWT 토큰을 저장하거나 사용 가능
                        // 예: SharedPreferences에 저장

                    } else {
                        Log.e(TAG, "JWT 요청 실패");
                    }
                } catch (IOException | org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
