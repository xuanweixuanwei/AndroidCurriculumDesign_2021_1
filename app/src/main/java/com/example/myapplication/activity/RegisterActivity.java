package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.hjq.widget.view.ClearEditText;
import com.hjq.widget.view.PasswordEditText;
import com.hjq.widget.view.SubmitButton;

public class RegisterActivity extends AppCompatActivity {
    private ClearEditText et_register_email,et_register_question,et_register_answer;
    private PasswordEditText et_register_password1,et_register_password2;
    private SubmitButton btn_register_commit;
    private View.OnClickListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        initListener();
    }

    private void initListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_register_commit:
                        break;
//                        todo 注册
                }
            }
        };

    }

    private void initView() {
        et_register_email = findViewById(R.id.et_register_email);
        et_register_question = findViewById(R.id.et_register_question);
        et_register_answer = findViewById(R.id.et_register_answer);
        et_register_password1 = findViewById(R.id.et_register_password1);
        et_register_password2 = findViewById(R.id.et_register_password2);
        btn_register_commit = findViewById(R.id.btn_register_commit);
    }
}