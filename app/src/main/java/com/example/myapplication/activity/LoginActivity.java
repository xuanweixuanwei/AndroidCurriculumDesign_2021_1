package com.example.myapplication.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.hjq.widget.view.ClearEditText;
import com.hjq.widget.view.PasswordEditText;
import com.hjq.widget.view.ScaleImageView;
import com.hjq.widget.view.SubmitButton;

public class LoginActivity extends AppCompatActivity {

   private ClearEditText et_login_email ;
    private PasswordEditText et_login_password;
    private AppCompatTextView tv_register;
    private AppCompatTextView tv_login_forget;
    private SubmitButton btn_login_commit;
    private ScaleImageView iv_login_qq;
    private ScaleImageView  iv_login_wechat;
    private View.OnClickListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initListener();
    }

    private ActivityResultLauncher registerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent resultData = result.getData();
                    if (resultData!=null) {
//                        et_login_email.setText(resultData.getExtras().getString("email"));
//                        TODO 注册成功后，填写注册时填写的邮箱，密码不填写
                    }
                }
            });


    private void initView() {
        et_login_email = findViewById(R.id.et_login_email);
        et_login_password = findViewById(R.id.et_login_password);
        tv_register = findViewById(R.id.tv_register);
        tv_login_forget = findViewById(R.id.tv_login_forget);
        btn_login_commit = findViewById(R.id.btn_login_commit);
        iv_login_qq = findViewById(R.id.iv_login_qq);
         iv_login_wechat = findViewById(R.id.iv_login_wechat);
    }

    private void initListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_register:
                        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                        registerLauncher.launch(registerIntent);
                        break;
                }
            }
        };
    }
}