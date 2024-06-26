package com.example.meteor.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.meteor.AppConstant;
import com.example.meteor.base.BaseActivity;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.util.HandlerAction;
import com.example.meteor.util.enumType.Role;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityAddUserBinding;
import com.hjq.widget.view.ClearEditText;
import com.hjq.widget.view.PasswordEditText;
import com.hjq.widget.view.SubmitButton;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddUserActivity extends BaseActivity<ActivityAddUserBinding> implements HandlerAction {

    private ClearEditText et_register_email, et_register_question, et_register_answer;
    private PasswordEditText et_register_password1, et_register_password2;
    private SubmitButton btn_register_commit;
    private View.OnClickListener listener;

    private AppDatabase db;
    private Toast mToast;
    public static final String emailPattern = "\\w+@\\w+\\.\\w+";
    public static final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(this);
                initView();
        initListener();
    }
    private void initListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_add_user_commit:
                        register();
                        break;
                }
            }
        };
        et_register_question.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                et_register_answer.setEnabled(Objects.requireNonNull(et_register_question.getText()).toString().length() > 0);
            }
        });

        et_register_password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void afterTextChanged(Editable editable) {
                if (Objects.requireNonNull(et_register_password2.getText()).toString().trim()
                        .equals(Objects.requireNonNull(et_register_password1.getText()).toString().trim())) {
                    et_register_password1.setBackground(null);
                    et_register_password2.setBackground(null);
                } else {
                    et_register_password1.setBackgroundColor(getColor(R.color.powder_blue));
                    et_register_password2.setBackgroundColor(getColor(R.color.powder_blue));
                }
            }
        });

        et_register_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    if(!et_register_email.getText().toString().matches(emailPattern)) {
                        Editable editable =
                                new SpannableStringBuilder(et_register_email.getEditableText());
                        ForegroundColorSpan blueSpan =
                                new ForegroundColorSpan(getColor(R.color.common_confirm_text_color));
                        editable.setSpan(blueSpan, 0, editable.length(),
                                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        et_register_email.setText(editable);
                    }else {
                        et_register_email.setText(et_register_email.getText().toString().trim());
                    }
                }
            }
        });
        btn_register_commit.setOnClickListener(listener);
    }
    private void initView() {
        et_register_email = binding.etAddUserEmail;
        et_register_question = binding.etAddUserQuestion;
        et_register_answer = binding.etAddUserAnswer;
        et_register_password1 = binding.etAddUserPassword1;
        et_register_password2 = binding.etAddUserPassword2;
        btn_register_commit = binding.btnAddUserCommit;
    }

    private void register() {
        String email = Objects.requireNonNull(et_register_email.getText()).toString().trim();
        String password1 =
                Objects.requireNonNull(et_register_password1.getText()).toString().trim();
        String password2 =
                Objects.requireNonNull(et_register_password2.getText()).toString().trim();
        boolean isAdmin = binding.switchWidget.isChecked();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (db.AccountDao().findAccountByEmail(email)!=null) {
                    et_register_email.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                    btn_register_commit.showError(3000);
                    showTip("该邮箱已被注册");
                    return;
                }

                if (email.matches(emailPattern)) {
                    if (password1.equals(password2)) {
                        if (password1.length()>=8&&password1.length()<18) {
                            if(Objects.requireNonNull(et_register_question.getText()).length()==0){
                                btn_register_commit.showProgress();
                                db.AccountDao().insert(new Account(email,password1,isAdmin));

                            }else {
                                String question = et_register_question.getText().toString().trim();
                                String answer = Objects.requireNonNull(et_register_answer.getText()).toString().trim();
                                db.AccountDao().insert(new Account(email,password1,question,answer,isAdmin));
                            }
                            showTip("新增成功");
                            postDelayed(() -> {
                                btn_register_commit.showSucceed();
                                postDelayed(() -> {
                                    btn_register_commit.reset();
//                                    et_register_email.setText("");
//                                    et_register_answer.setText("");
//                                    et_register_password1.setText("");
//                                    et_register_password2.setText("");
//                                    et_register_question.setText("");
                                }, 1000);
                            }, 2000);
                        }else {
                            et_register_password1.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                            et_register_password2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                            btn_register_commit.showError(3000);
                            showTip("密码需要包含8-18位的英文字母或阿拉伯数字");
                            return;
                        }
                    }else {
                        et_register_password1.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                        et_register_password2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                        btn_register_commit.showError(3000);
                        showTip("请检查两次输入的密码是否一致");
                        return;
                    }
                }else {
                    et_register_email.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
                    btn_register_commit.showError(3000);
                    showTip("请检查邮箱格式");
                    return;
                }

            }
        });
        executor.shutdown();


    }

    /**
     * 封装弹窗功能
     */
    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }
}