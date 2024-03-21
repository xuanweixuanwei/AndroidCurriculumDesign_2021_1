package com.example.meteor.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.meteor.aop.SingleClick;
import com.example.meteor.app.AppActivity;
import com.example.meteor.dialog.InputDialog;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.AppConstant;
import com.example.myapplication.R;

import com.example.meteor.dialog.SelectDialog;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.ui.IosPopupWindow;
import com.hjq.base.BaseDialog;

import com.hjq.widget.layout.SettingBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class InfoActivity extends AppActivity {
    private static final int REQUEST_TAKEPHOTO_CODE = 1;
    private ViewGroup mAvatarLayout;
    private ImageView mAvatarView;
    private SettingBar mIdView;
    private SettingBar mNameView;
    private SettingBar mBirthdayView;
    private SettingBar mSexView;
    private SettingBar mTtsDailyUsageView;
    private SettingBar mOcrDailyUsageView;
    private SettingBar mAsrDailyUsageView;

    private SettingBar mTtsCountUsageView;
    private SettingBar mOcrCountUsageView;
    private SettingBar mAsrCountUsageView;
    private IosPopupWindow chooseAvatarPopupWindow;

    private SettingBar mApplicationView;
    private SettingBar mFeedBackView;
    private String[] sexEntries;
    private String[] sexValues;
    private String mImagePath;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static int REQUEST_PERMISSION_CODE = 1;

    private String userEmail;
    private AppDatabase db;
    private AccountDao accountDao;
    private Account currentUser;

    /** 头像地址 */

    private Toast mToast;
    private boolean createSuccess=true;
    private String mImageName;
    private Bitmap mAvatarBitmap;
    private String photoBase64str;
    private final ActivityResultLauncher pickPhotoFromAlbum = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {

//                    TODO 解析
                    if (result != null) {
                        mAvatarView.setImageURI(result);
                        try {
                            mAvatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            int quality = 70;
                            mAvatarBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                            byte[] byteArray = outputStream.toByteArray();
                            photoBase64str = Base64.getEncoder().encodeToString(byteArray);

                            while (base64FileSize(photoBase64str) > 4194304) {
                                mAvatarBitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10, outputStream);
                                byteArray = outputStream.toByteArray();
                                photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                            }
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    currentUser.setAvatarStr(photoBase64str);
                                    accountDao.updateAccount(currentUser);
                                }
                            });
                            executor.shutdown();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showTip("请选择图片~");
                    }
                }
            }
    );

    @Override
    protected int getLayoutId() {
        return R.layout.activity_info;
    }

    @Override
    protected void initView() {


        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("个人信息");
        actionBar.setDisplayHomeAsUpEnabled(true);

        chooseAvatarPopupWindow = new IosPopupWindow( InfoActivity.this, new IosPopupWindow.OnClickListener() {

            @Override
            public void cameraOnClick() {

                if (createSuccess) {
                    pickPhotoByCamera();
                } else {
                    showTip("功能无法使用，可能是手机机型问题，请向开发者反馈~");
                }

            }

            @Override
            public void albumOnClick() {
                pickPhotoFromAlbum.launch("image/*");
            }

            @Override
            public void cancel() {
                chooseAvatarPopupWindow.dismiss();
                showTip("取消了");
            }
        });

        mAvatarLayout = findViewById(R.id.fl_person_data_avatar);
        mAvatarView = findViewById(R.id.iv_person_data_avatar);
        mNameView = findViewById(R.id.sb_person_data_name);
        mBirthdayView = findViewById(R.id.sb_person_data_birthday);
        mSexView = findViewById(R.id.sb_person_data_sex);

        mApplicationView = findViewById(R.id.sb_person_application);
        mFeedBackView = findViewById(R.id.sb_person_feedback);

        mIdView = findViewById(R.id.sb_person_data_id);
        mTtsDailyUsageView = findViewById(R.id.sb_person_data_tts_usage);
        mOcrDailyUsageView = findViewById(R.id.sb_person_data_ocr_usage);
        mAsrDailyUsageView = findViewById(R.id.sb_person_data_asr_usage);
        mTtsCountUsageView = findViewById(R.id.sb_person_data_tts_usage_count);
        mOcrCountUsageView = findViewById(R.id.sb_person_data_ocr_usage_count);
        mAsrCountUsageView = findViewById(R.id.sb_person_data_asr_usage_count);

        createFileDirectory();

        setOnClickListener(mAvatarLayout,mNameView,mSexView,mAvatarView,mBirthdayView);
    }

    @Override
    protected void initData() {
        sexEntries = getResources().getStringArray(R.array.sex_entries);
        sexValues = getResources().getStringArray(R.array.sex_values);

        userEmail=getSharedPreferences(AppConstant.preferenceFileName, Context.MODE_PRIVATE)
                .getString(AppConstant.userEmail,"");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                db = AppDatabase.getInstance(getApplicationContext());
                accountDao = db.AccountDao();


                if ((currentUser=accountDao.findAccountByEmail(userEmail))!=null) {
                    Log.e(TAG, "run: sexEntries[currentUser.getSex()]:"+sexEntries[currentUser.getSex()] );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSexView.setRightText(sexEntries[currentUser.getSex()]);
                            mNameView.setRightText(currentUser.getName()==null?"昵称还未设置哦":currentUser.getName());
                            mBirthdayView.setRightText(currentUser.getBirthday()==null?"来设置生日吧":currentUser.getBirthday());
                            mAsrCountUsageView.setRightText(currentUser.getAsrUsageCount()+"");
                            mOcrCountUsageView.setRightText(currentUser.getOcrUsageCount()+"");
                            mTtsCountUsageView.setRightText(currentUser.getTtsUsageCount()+"");
                        }
                    });

                    if (currentUser.getAvatarStr()!=null) {

                        byte[] avatarStr =  Base64.getDecoder().decode(currentUser.getAvatarStr());
                        Timber.e("run: avatarStr:%s", avatarStr);
                        if (avatarStr.length!=0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAvatarBitmap=BitmapFactory.decodeByteArray(avatarStr,0,avatarStr.length);
                                    mAvatarView.setImageBitmap(mAvatarBitmap);
                                    Log.e(TAG, "run: currentUser.getName().isEmpty()："+ currentUser.getName().isEmpty());
                                }
                            });

                        }
                    }else {
                        setAvatar();
                    }

                }else {
                    showTip("登陆信息出错，请重新登陆");
                    postDelayed(()->{finish();},1000);
                }
            }
        });
        executor.shutdown();
    }
private void setAvatar(){
    if (currentUser.getAvatarStr()==null) {
        //             添加如果设定了性别，就根据性别来设定头像
        if (currentUser.getSex()== AppConstant.female) {
            mAvatarView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avatar_girl));
        }else if (currentUser.getSex()== AppConstant.male){
            mAvatarView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avator_boy));
        }else {
            mAvatarView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avatar));
        }
    }

}
/*
ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {

                                }
                                });
                         executor.shutdown();
                         */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view == mAvatarLayout) {
            chooseAvatarPopupWindow.show(LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_info, null));
        }else
            if (view == mNameView) {
            new InputDialog.Builder(this)
                    // 标题可以不用填写
                    .setTitle(getString(R.string.personal_data_name_hint))
                    .setContent(mNameView.getRightText())
                    //.setHint(getString(R.string.personal_data_name_hint))
                    .setConfirm("确定")
                    // 设置 null 表示不显示取消按钮
                    .setCancel("取消")
                    // 设置点击按钮后不关闭对话框
                    //.setAutoDismiss(false)
                    .setListener((dialog, content) -> {
                        if (!mNameView.getRightText().equals(content)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNameView.setRightText(content);
                                }
                            });

                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    currentUser.setName(content);
                                    accountDao.updateAccount(currentUser);
                                }
                                });
                         executor.shutdown();

                        }
                    })
                    .show();
        }else
            if(view == mAvatarView){

            final Dialog dialog = new Dialog(InfoActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen); // 系统全屏样式

            ImageView target_picture = getImageView();
            dialog.setContentView(target_picture);
            dialog.show();
            target_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }else
            if(view ==mSexView){
            // 单选对话框
            new SelectDialog.Builder(this)
                    .setTitle("请选择你的性别")
                    .setList(sexEntries)
                    // 设置单选模式
                    .setSingleSelect()
                    // 设置默认选中
                    .setSelect(currentUser.getSex())
                    .setListener(new SelectDialog.OnListener<String>() {

                        @Override
                        public void onSelected(BaseDialog dialog, HashMap<Integer, String> data) {
                            Integer selectedIndex = data.keySet().toArray(new Integer[1])[0];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSexView.setRightText(sexEntries[selectedIndex]);
                                }
                            });

                            showTip("确定了：" + sexEntries[selectedIndex]);
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    currentUser.setSex(selectedIndex);
                                    accountDao.updateAccount(currentUser);
                                    setAvatar();
                                }
                            });
                            executor.shutdown();
                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {
                            showTip("取消了");
                        }
                    })
                    .show();
        }else if(view == mBirthdayView){
            getDate();
        }
    }
    private ImageView getImageView(){
        ImageView imageView = new ImageView(InfoActivity.this);
        imageView.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT  ));
        if (mAvatarBitmap!=null) {
            imageView.setImageBitmap(mAvatarBitmap);
        }else {
            //             添加如果设定了性别，就根据性别来设定头像
            if (currentUser.getSex()== AppConstant.female) {
                imageView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avatar_girl));
            }else if (currentUser.getSex()== AppConstant.male){
                imageView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avator_boy));
            }else {
                imageView.setImageDrawable(getDrawable(R.drawable.vector_drawable_avatar));
            }
        }
        return imageView;
    }

    /**
     * 点击进行选取头像并在选择后进行裁剪*/
    /*ImageSelectActivity.start(this, data -> {
                // 裁剪头像
                cropImageFile(new File(data.get(0)));
            });*/
    /**
     * 裁剪图片
     */
    /*
    private void cropImageFile(File sourceFile) {
        ImageCropActivity.start(this, sourceFile, 1, 1, new ImageCropActivity.OnCropListener() {

            @Override
            public void onSucceed(Uri fileUri, String fileName) {
                File outputFile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outputFile = new FileContentResolver(getActivity(), fileUri, fileName);
                } else {
                    try {
                        outputFile = new File(new URI(fileUri.toString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        outputFile = new File(fileUri.toString());
                    }
                }
                updateCropImage(outputFile, true);
            }

            @Override
            public void onError(String details) {
                // 没有的话就不裁剪，直接上传原图片
                // 但是这种情况极其少见，可以忽略不计
                updateCropImage(sourceFile, false);
            }
        });
    }
*/
    /**
 *上传裁剪后的图片
 */
    /*    private void updateCropImage(File file, boolean deleteFile) {
        if (true) {
            if (file instanceof FileContentResolver) {
                mAvatarUrl = ((FileContentResolver) file).getContentUri();
            } else {
                mAvatarUrl = Uri.fromFile(file);
            }
//            GlideApp.with(getActivity())
//                    .load(mAvatarUrl)
//                    .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
//                    .into(mAvatarView);
            return;
        }

        EasyHttp.post(this)
                .api(new UpdateImageApi()
                        .setImage(file))
                .request(new HttpCallback<HttpData<String>>(this) {

                    @Override
                    public void onSucceed(HttpData<String> data) {
                        mAvatarUrl = Uri.parse(data.getData());
//                        GlideApp.with(getActivity())
//                                .load(mAvatarUrl)
//                                .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
//                                .into(mAvatarView);
                        if (deleteFile) {
                            file.delete();
                        }
                    }
                });
    }*/

    /**
     * 创建保存图片的文件夹
     */
    public void createFileDirectory() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        mImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/meteor/";//指定保存路径
        File f = new File(mImagePath);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Log.e(TAG, "createFileDirectory: 文件夹创建失败");
                showTip("无法创建文件夹，通过拍照返回照片的功能无法使用！");
                createSuccess = false;
            }
        }
    }

    public void getDate() {
        Calendar data = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(InfoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String str;
                str = String.valueOf(year) + "年" + String.valueOf(month+1) + "月" + String.valueOf(day)+"日";
                mBirthdayView.setRightText(str);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        currentUser.setBirthday(str);
                        accountDao.updateAccount(currentUser);
                    }
                });
                executor.shutdown();
            }
        }, data.get(Calendar.YEAR), data.get(Calendar.MONTH), data.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void pickPhotoByCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        mImageName = System.currentTimeMillis() + ".jpeg";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath + mImageName)));
        Uri imageUri = FileProvider.getUriForFile(
                InfoActivity.this,
                "com.example.myapplication.provider", //(use your app signature + ".provider" )
                new File(mImagePath + mImageName));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        Log.e(TAG, "pickPhotoByCamera: " + imageUri + "    " + mImagePath + mImageName);

        startActivityForResult(intent, REQUEST_TAKEPHOTO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKEPHOTO_CODE) {
            if (resultCode == -1) {
                mAvatarBitmap = BitmapFactory.decodeFile(mImagePath + mImageName);
//            Bitmap take = ResizeBitmap(bitmap,iv_picture.getMaxWidth());
                mAvatarView.setImageBitmap(mAvatarBitmap);
//            bitmap.recycle();
//            bitmap = compressBitmap(bitmap);
                int quality = 40;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                mAvatarBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                //Use your Base64 String as you wish
                photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                while (base64FileSize(photoBase64str) > 4194304) {
                    mAvatarBitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10, outputStream);
                    byteArray = outputStream.toByteArray();
                    photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            currentUser.setAvatarStr(photoBase64str);
                            accountDao.updateAccount(currentUser);

                        }
                    });
                    executor.shutdown();
                }


            } else {
                showTip("未完成拍照~");
            }


        }
    }

    public static double base64FileSize(String base64String) {
        /**检测是否含有base64,文件头)*/
        if (base64String.lastIndexOf(",") > 0) {
            base64String = base64String.substring(base64String.lastIndexOf(",") + 1);
        }
        /** 获取base64字符串长度(不含data:audio/wav;base64,文件头) */
        int size0 = base64String.length();
        /** 获取字符串的尾巴的最后10个字符，用于判断尾巴是否有等号，正常生成的base64文件'等号'不会超过4个 */
        String tail = base64String.substring(size0 - 10);
        /** 找到等号，把等号也去掉,(等号其实是空的意思,不能算在文件大小里面) */
        int equalIndex = tail.indexOf("=");
        if (equalIndex > 0) {
            size0 = size0 - (10 - equalIndex);
        }
        /** 计算后得到的文件流大小，单位为字节 */
        return size0 - ((double) size0 / 8) * 2;
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