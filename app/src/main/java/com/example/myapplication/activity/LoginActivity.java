package com.example.myapplication.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.myapplication.AppConstant;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.roomDatabase.database.AppDatabase;
import com.example.myapplication.roomDatabase.entity.Account;
import com.example.myapplication.util.DateUtil;
import com.example.myapplication.util.HandlerAction;
import com.hjq.widget.view.ClearEditText;
import com.hjq.widget.view.PasswordEditText;
import com.hjq.widget.view.ScaleImageView;
import com.hjq.widget.view.SubmitButton;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okio.ByteString;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements HandlerAction {

    private ClearEditText et_login_email;
    private PasswordEditText et_login_password;
    private AppCompatTextView tv_register;
    private AppCompatTextView tv_login_forget;
    private SubmitButton btn_login_commit;
    private ScaleImageView iv_login_qq;
    private ScaleImageView iv_login_wechat;
    private View.OnClickListener listener;
    private AppDatabase db;
    private Toast mToast;
    public static final String emailPattern = "\\w+@\\w+\\.\\w+";
    public static final String TAG = LoginActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = AppDatabase.getInstance(this);
        sharedPreferences = getSharedPreferences(AppConstant.preferenceFileName,MODE_PRIVATE);
        initView();
        initListener();
    }

    private ActivityResultLauncher registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent resultData = result.getData();
                        if (resultData != null) {
//                        et_login_email.setText(resultData.getExtras().getString("email"));
//                        TODO 注册成功后，填写注册时填写的邮箱，密码不填写
                            et_login_email.setText( resultData.getStringExtra("email"));
                            et_login_password.setText( resultData.getStringExtra("password"));
                        }
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
                        Intent registerIntent = new Intent(LoginActivity.this,
                                RegisterActivity.class);
                        registerLauncher.launch(registerIntent);
                        break;
                    case R.id.btn_login_commit:
                        btn_login_commit.showProgress();
                        String email = Objects.requireNonNull(et_login_email.getText()).toString().trim();
                        String password = Objects.requireNonNull(et_login_password.getText()).toString().trim();
                        if (email.matches(emailPattern)&&
                                password.length()>=8) {
                            if (Login(email,password)) {
                                    success();//跳转
                            }else         btn_login_commit.showError(3000);
                        }
                }
            }
        };
        tv_register.setOnClickListener(listener);
        btn_login_commit.setOnClickListener(listener);
    }

    private void success() {
        postDelayed(() -> {
            btn_login_commit.showSucceed();
            postDelayed(() -> {

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }, 1000);
        }, 2000);
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

    @Override
    protected void onRestart() {
        super.onRestart();
        btn_login_commit.reset();
    }

    private boolean Login(String email, String password){

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Account accountByEmail=db.AccountDao().findAccountByEmail(email);
                Account account = db.AccountDao().findAccountByPassword(email,ByteString.encodeUtf8(password).sha256().toString());//通过email和password查询
                if (account!=null) {//账号和密码正确，判断账号是否处于封锁状态：注销5天内或者某日密码错误超过次数还处于封禁日期内（也是5天）
                    if (account.isLocked()) {//账号被锁
                        if (account.getLogoutTime()==0) {//未被注销，说明可能在封锁期间
                            if (account.getErrorTimes()>4) {//今日错误次数超过4次
                                accountStateError();
                                showTip("账户由于输入密码错误次数超过4次已被禁用，解锁时间为"+ DateUtil.MillisToStr(account.getLockedTime()));
                            }else {
                                showTip("系统出错，请稍后再试或联系管理员");
                                Timber.e("Login:account.getLockedTime(): %s", account.getLockedTime());
                                Timber.e("Login:account.getLastErrorTime() %s", account.getLastErrorTime());
                                Timber.e("Login:account.getEmail() %s", account.getEmail());
                                Timber.e("Login:account.getPasswordSHA() %s", account.getPasswordSHA());
                                Timber.e("Login:account.isLocked() %s", account.isLocked());
                                Timber.e("Login: account.getErrorTimes() %s", account.getErrorTimes());
                            }
                        }else {
                            if (account.getLogoutTime()> Calendar.getInstance().getTimeInMillis()) {//如果注销时间未到
                                accountStateError();
                                showTip("当前账号于五日内被注销，如有疑问请联系管理员");
                            }else {//已注销，删除用户信息
//                        TODO 为确保表的onDelete = CASCADE 生效，应该再实现其他表的删除
                                db.AccountDao().deleteAccount(account);
                                loginError();
                            }
                        }

                    }else {
                        sharedPreferences.edit()
                                .putString(AppConstant.userEmail,et_login_email.getText().toString().trim())
                                .putString(AppConstant.userPasswordSHA,ByteString.encodeUtf8(et_login_password.getText().toString().trim()).sha256().toString())
                                .putBoolean(AppConstant.loginState,true)
                                .apply();
                    }

                }else {
//            账号、密码错误
                    loginError();

//            如果存在对应账号，记录错误次数
                    if (accountByEmail!=null) {
                        accountByEmail.passwordError();
                        db.AccountDao().updateAccount(accountByEmail);
                    }
                }

            }
        });
        executor.shutdown();

        return sharedPreferences.getBoolean(AppConstant.loginState,false);
    }

    private void loginError(){
        et_login_password.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
        et_login_email.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));
        showTip("账号或密码错误,一天内密码连续错误超过4次会封锁账号");
    }

    private void accountStateError(){
        et_login_email.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_anim));

    }

}