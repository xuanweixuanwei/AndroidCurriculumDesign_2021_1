package com.example.meteor.activity;

import static com.hjq.http.EasyUtils.postDelayed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meteor.AppConstant;
import com.example.myapplication.R;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.hjq.widget.view.PasswordEditText;
import com.hjq.widget.view.RegexEditText;
import com.hjq.widget.view.SubmitButton;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okio.ByteString;

public class RestPasswordActivity extends AppCompatActivity {
    LinearLayout reset_view;
    RegexEditText et_password_forget_email;
    TextView question;
    AppCompatEditText et_password_forget_answer;
    SubmitButton btn_password_forget_commit;
    boolean submitted = false;
    boolean rightAnswer = false;
    View.OnClickListener listener;
    LinearLayout reset_password_view;
    PasswordEditText et_reset_password1, et_reset_password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_password);
        initView();
        initListener();
    }

    private void initListener() {
        et_password_forget_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_password_forget_commit.setClickable(s.length() != 0);
            }
        });
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == btn_password_forget_commit) {

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (submitted&&!rightAnswer) {
                                if (checkAnswer()) {
                                    rightAnswer = true;
                                    success();
                                    resetPasswordView();
                                }else {
                                    showTip("密保答案错误");
                                    dataError();
                                }
                            } else {
                                if (rightAnswer) {
                                    resetPassword();
                                }else{
                                    email = Objects.requireNonNull(et_password_forget_email.getText()).toString().trim();
                                    setAnswer(email);
                                }

                            }
                        }
                    });
                    executorService.shutdown();

                }
            }
        };
        btn_password_forget_commit.setOnClickListener(listener);

    }

    private void resetPassword() {
        String password_new = Objects.requireNonNull(et_reset_password1.getText()).toString().trim();
        if (password_new.equals(Objects.requireNonNull(et_reset_password2.getText()).toString().trim())) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    AccountDao accountDao =
                            AppDatabase.getInstance(getApplicationContext()).AccountDao();
                    Account account = accountDao.findAccountByEmail(email);
                    accountDao.updateAccount(account.setPassword(password_new));
                    showTip("密码已经重置完成");
                    postDelayed(()->{
                        setResult(RESULT_OK, new Intent()
                                .putExtra(AppConstant.INTENT_KEY_EMAIL, email)
                                .putExtra(AppConstant.INTENT_KEY_PASSWORD, password_new));
                        finish();},1000);
                }
            });
            executorService.shutdown();

        }else {
            showTip("两次输入密码不一致");
            btn_password_forget_commit.showError(1000);
        }
    }

    private void resetPasswordView() {
/*
        showTip("已将密码重置为邮箱号");*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_password_forget_commit.reset();
                reset_view.setVisibility(View.GONE);
                reset_password_view.setVisibility(View.VISIBLE);
            }
        });

    }

    private boolean checkAnswer() {
        return ByteString.encodeUtf8(et_password_forget_answer.getText().toString().trim()).sha256().toString().equals(answerSHA);
    }

    private void initView() {
        reset_view = findViewById(R.id.reset_Q_A);
        et_password_forget_email = findViewById(R.id.et_password_forget_email);
        question = findViewById(R.id.question);
        et_password_forget_answer = findViewById(R.id.et_password_forget_answer);
        btn_password_forget_commit = findViewById(R.id.btn_password_forget_commit);
        reset_password_view = findViewById(R.id.reset_password_view);
        et_reset_password1 = findViewById(R.id.et_reset_password1);
        et_reset_password2 = findViewById(R.id.et_reset_password2);
    }

    private String answerSHA;
    private String email;

    private void setAnswer(String email) {
        btn_password_forget_commit.showProgress();
        if (emailCheck(email)) {
            Account currentAccount = AppDatabase.getInstance(getApplicationContext()).AccountDao()
                    .findAccountByEmail(email);
            if (currentAccount.getQuestion() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_password_forget_commit.showSucceed();
                        reset_view.setVisibility(View.VISIBLE);
                        question.setText(currentAccount.getQuestion());
                        postDelayed(()->{
                            btn_password_forget_commit.reset();
                        },1000);
                    }
                });

                answerSHA = currentAccount.getAnswerSHA();
                submitted = true;
                success();
            } else {
                dataError();
                showTip("当前邮箱可能未设置密保问题，如需重置密码请联系管理员");
            }
        } else {
            dataError();
            showTip("邮箱格式错误");
        }
    }

    private boolean emailCheck(String email) {
        return email.matches("\\w+@\\w+\\.\\w+");
    }

    private Toast toast;

    /**
     * 封装弹窗功能
     */
    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    private void dataError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_password_forget_commit.showError(1000);
            }
        });
    }

    private void success() {
        postDelayed(() -> {
            btn_password_forget_commit.showSucceed();
        }, 1000);
    }
}