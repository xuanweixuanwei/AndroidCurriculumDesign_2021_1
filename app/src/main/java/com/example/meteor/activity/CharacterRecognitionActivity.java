package com.example.meteor.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.meteor.AppConstant;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.ui.IosPopupWindow;
import com.example.myapplication.R;
import com.google.gson.Gson;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * 1、通用文字识别
 * 2、支持中英文,支持手写和印刷文字。
 * 3、在倾斜文字上效果有提升，同时支持部分生僻字的识别。
 * 4、图片格式支持jpg格式、jpeg格式、png格式、bmp格式，且需保证图像文件大小base64编码后不超过10MB
 */
public class CharacterRecognitionActivity extends AppCompatActivity {
    //https://api.xf-yun.com/v1/private/sf8e6aca1?
    // authorization
    // =YXBpX2tleT0iYXBpa2V5WFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFgiLCBhbGdvcml0aG09ImhtYWMtc2hhMjU2IiwgaGVhZGVycz0iaG9zdCBkYXRlIHJlcXVlc3QtbGluZSIsIHNpZ25hdHVyZT0iL21nMmg5QkNrZXNwaWxaOTRIVUJhUVZQcTJ2N1B4WUY5MHRlVEJsYXhkOD0i
    // &host=api.xf-yun.com
    // &date=Wed%2C+11+Aug+2021+06%3A55%3A18+GMT
        /*
• date参数生成规则：
date必须是UTC+0或GMT时区，RFC1123格式(Wed, 11 Aug 2021 06:55:18 GMT)。
服务端会对date进行时钟偏移检查，最大允许300秒的偏差，超出偏差的请求都将被拒绝。

• authorization参数生成格式：
1）获取接口密钥APIKey 和 APISecret。
在讯飞开放平台控制台，创建一个应用后打开OCR中英文字识别页面可以获取，均为32位字符串。
2）参数authorization base64编码前（authorization_origin）的格式如下。
api_key="$api_key",algorithm="hmac-sha256",
headers="host date request-line",signature="$signature"
其中 api_key 是在控制台获取的APIKey，
algorithm 是加密算法（仅支持hmac-sha256），
headers是参与签名的参数，请注意是固定的参数名（"host date request-line"），而非这些参数的值
signature 是使用加密算法对参与签名的参数签名后并使用base64编码的字符串

APIKey b02aa25c9c52fdf36cf809d300959d7c
APISecret MWQ0MWIyNGUxMzhhOWQxYWI5NjlhOTJj
APPID f1d1cefd
*/


    private static final String TAG = CharacterRecognitionActivity.class.getSimpleName();
    private Bitmap photoBitmap;

    //控制台获取以下信息
    private static String APPID = "b5d063a5";
    private static String apiSecret = "MTVkMjAwNDNjZDBjOWRhMmI5YjlkODNm";
    private static String apiKey = "857529ad654b3aed232dc9bb525b65c3";

    //解析json
    private static Gson gson = new Gson();

    private ImageButton ib_pick_picture, copy_text_result;
    private EditText et_text_result;
    private ImageView iv_picture;

    public static String mImageName = "";

    private String photoBase64str;
    private String userEmail;
    private AccountDao accountDao;
    private Account currentUser;

    private IosPopupWindow choosePicturePopupWindow;
    private String textBase64Decode = null;

    /*
    * onCreate生命周期函数
    --设置布局
    --初始化UI控件
    --初始化数据
    --创建应用文件夹
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_recognition);

        initView();
        initData();
        createFileDirectory();
    }

    private void initData() {
        userEmail = getSharedPreferences(AppConstant.preferenceFileName, Context.MODE_PRIVATE)
                .getString(AppConstant.userEmail, "");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                accountDao = AppDatabase.getInstance(getApplicationContext()).AccountDao();
                currentUser = accountDao.findAccountByEmail(userEmail);
            }
        });
        executor.shutdown();

    }

    /*进行完整图片识文请求的方法
    --增加OCR使用次数
    -- doRequest，进行网络请求
    --解析resp
    --将resp中的words衔接到result_string_buffer，当当前lines的words读取完毕就将字符串写入EditText中

    --如果登录信息有误将无法进行网络请求
    */
    private void request() {

        //增加OCR使用次数
        if (currentUser != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                accountDao.updateAccount(currentUser.useOcr());

                try {

                    String resp = doRequest();
                    Timber.tag(TAG).w("request: " + "resp=>" + resp);
                    JsonResult myJsonResult = gson.fromJson(resp, JsonResult.class);
                    //修改了UTF-8的标准，未测试
                    textBase64Decode =
                            new String(Base64.getDecoder().decode(myJsonResult.payload.result.text),
                                    StandardCharsets.UTF_8);

                    JSONObject jsonObject = JSON.parseObject(textBase64Decode);
                    Timber.tag(TAG).w("request: " + "text字段Base64解码后=>" + jsonObject);

                    ArrayList<Page> pages = gson.fromJson(textBase64Decode, Text.class).pages;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            et_text_result.setText("");
                            StringBuilder result_string_buffer = new StringBuilder();
                            for (Page page :
                                    pages) {
                                for (Line line :
                                        page.lines) {
                                    if (line.words == null) {
                                        result_string_buffer.append("\n");
                                    } else {
                                        for (Word word :
                                                line.words) {
                                            result_string_buffer.append(word.content);
                                        }
                                        result_string_buffer.append("\n");
                                    }
                                }
                            }
                            et_text_result.setText(result_string_buffer.toString().trim());
                            changeEditTextHeight(et_text_result);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executor.shutdown();
        } else {
            showTip("登陆信息失效，请重新登陆后使用");
        }

    }

    /*初始化UI控件
    --设置ActionBar
    --绑定视图
    --初始化choosePicturePopupWindow，实现IosPopupWindow的三个接口
    --为按钮、图片设置点击事件监听器
    --为et_text_result添加TextChangedListener监听器，在开始编辑时根据编辑前文字内容调整EditText的高度
    */
    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("文字识别");
        actionBar.setDisplayHomeAsUpEnabled(true);//在ActionBar最左边显示返回箭头按钮

        ib_pick_picture = findViewById(R.id.ib_pick_picture);
        copy_text_result = findViewById(R.id.copy_text_result);
        et_text_result = findViewById(R.id.et_text_result);
        iv_picture = findViewById(R.id.iv_picture);

        choosePicturePopupWindow =
                new IosPopupWindow((Activity) CharacterRecognitionActivity.this,
                        new IosPopupWindow.OnClickListener() {

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
                                choosePicturePopupWindow.dismiss();
                                showTip("取消了");
                            }
                        });

        ib_pick_picture.setOnClickListener(listener);
        iv_picture.setOnClickListener(listener);
        copy_text_result.setOnClickListener(listener);
        et_text_result.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeEditTextHeight(et_text_result);
                Log.w(TAG, "beforeTextChanged: change height");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void changeEditTextHeight(EditText editText) {
        ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
        layoutParams.height = getTextHeight(editText);
        editText.setLayoutParams(layoutParams);

    }

    /*根据EditText中文本的宽高修改EditText的高度*/
    private int getTextHeight(EditText editText) {
        Layout layout = editText.getLayout();
        if (layout != null) {
            int totalLineCount = layout.getLineCount();
            int paddingTop = editText.getPaddingTop();
            int paddingBottom = editText.getPaddingBottom();

            int lineHeight = layout.getLineBottom(totalLineCount - 1) - layout.getLineTop(0);
            Log.w(TAG, "getTextHeight: " + paddingBottom + "   " + paddingTop + "  " + lineHeight);
            return lineHeight + paddingTop + paddingBottom;
        }
        return 0;
    }

    public static final int REQUEST_TAKEPHOTO_CODE = 1;

    /*
    * 通过相册选取图片
    --通过FileProvider获取APP创建的文件位置
    --生成目标文件uri，并新建文件
    --设置Intent的参数为文件的目标存储路径
    --传入正确的请求码，startActivityForResult
    * */
    private void pickPhotoByCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        mImageName = System.currentTimeMillis() + ".jpeg";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath + mImageName)));
        Uri imageUri = FileProvider.getUriForFile(
                CharacterRecognitionActivity.this,
                "com.example.myapplication.provider", //(use your app signature + ".provider" )
                new File(mImagePath + mImageName));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        Timber.e("pickPhotoByCamera: " + imageUri + "    " + mImagePath + mImageName);
        startActivityForResult(intent, REQUEST_TAKEPHOTO_CODE);
    }

    /*onActivityResult回调
    --判断请求码是否为REQUEST_TAKEPHOTO_CODE
         ---判断请求结果是否为-1（拍照成功）
         通过文件名（根据mImagePath，mImageName获取照片的文件路径、文件名）编码为Bitmap格式photoBitmap
         将iv_picture的Bitmap设置为photoBitmap
        将照片进行压缩直到满足请求接口要求（小于4M）
        进行网络请求
     ---若请求结果不为-1则弹窗提示用户
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKEPHOTO_CODE) {
            if (resultCode == -1) {
                photoBitmap = BitmapFactory.decodeFile(mImagePath + mImageName);
                iv_picture.setImageBitmap(photoBitmap);
                int quality = 40;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                photoBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                //Use your Base64 String as you wish
                photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                while (base64FileSize(photoBase64str) > 4194304) {
                    photoBitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10, outputStream);
                    byteArray = outputStream.toByteArray();
                    photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                }
                request();
            } else {
                showTip("未完成拍照~");
            }
        }
    }

//    public static Bitmap compressBitmap(Bitmap bitmap) {//拍照的图片太大，设置格式大小
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newWidth = width/2;
//        float temp = ((float) height) / ((float) width);
//        int newHeight = (int) ((newWidth) * temp);
////        float scaleWidth = ((float) newWidth) / width;
////        float scaleHeight = ((float) newHeight) / height;
////        Matrix matrix = new Matrix();
////        // resize the bit map
////        matrix.postScale(scaleWidth, scaleHeight);
////        // matrix.postRotate(45);
//        Log.w(TAG, "ResizeBitmap: "+width+"  "+height );
////       TODO createBitmap方法报错
////        但是由于已经做过NestedScrollView，不需要压缩图片，可以忽略
//        //        bitmap.recycle();
//        return Bitmap.createScaledBitmap(bitmap,  width, height,  true);
//    }

    private Toast mToast;

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

    /*监听器，Activity的成员变量，实现一下UI控件的点击事件
    --图片识文按钮ib_pick_picture
    --图片识文的目标图片iv_picture
    --复制文字按钮copy_text_result
    */
    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ib_pick_picture:
                    choosePicturePopupWindow.show(LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_character_recognition, null));
                    break;
                case R.id.iv_picture:
                    final Dialog dialog = new Dialog(CharacterRecognitionActivity.this,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen); // 系统全屏样式

                    ImageView target_picture = getImageView();
                    dialog.setContentView(target_picture);
                    dialog.show();
                    target_picture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    break;
                case R.id.copy_text_result:
                    copyText(et_text_result);
                    break;
            }
        }
    };

    /**
     * 将识别结果复制到手机剪切板中
     * 写入剪切板不需要动态获取权限
     */
    private void copyText(EditText et_result) {

        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("recognition result", et_result.getText());

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);
        // Only show a toast for Android 12 and lower.
        //        因为测试过程发现即使真机是安卓13，在调用剪切板复制以后也没有弹窗，所以统一不做判断直接弹窗复制的信息
        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){  }

        String str = et_result.getText().toString();
        if (str.length() < 20)
            showTip("复制了“" + str + "”");
        else {
            //            在复制的文本过多时，做一个字符串的截取和拼接
            String limitedStr = String.format("%s...%s", str.substring(0, 7),
                    str.substring(str.length() - 7));
            showTip("复制了“" + limitedStr + "”");
        }

    }

    /*获取ImageView，当photoBitmap为空时返回应用图标，不为空时将图片的Bitmap设置为photoBitmap*/
    private ImageView getImageView() {
        ImageView imageView = new ImageView(CharacterRecognitionActivity.this);
        imageView.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
        if (photoBitmap != null) {
            imageView.setImageBitmap(photoBitmap);
        } else {
            imageView.setImageDrawable(getDrawable(R.drawable.meteor));
        }
        return imageView;
    }

    /*设置ActionBar返回箭头的点击事件*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*从相册选择图片的ActivityResultLauncher的实现
        --根据返回数据设置iv_picture的图片uri
        --根据图片uri查找对应文件并转换为Bitmap
        --将图片压缩并转换为字节数组，编码为base64字符串
        --如果图片大小大于4M，继续压缩直到符合API要求
        --进行图文识字请求

        --如果没有从相册选择图片，弹窗提示用户选择图片
    */
    private final ActivityResultLauncher pickPhotoFromAlbum = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {

                    if (result != null) {
                        iv_picture.setImageURI(result);
                        try {
                            photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                    result);
//                            bitmap = compressBitmap(bitmap);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            int quality = 70;
                            photoBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                            byte[] byteArray = outputStream.toByteArray();
                            photoBase64str = Base64.getEncoder().encodeToString(byteArray);

                            while (base64FileSize(photoBase64str) > 4194304) {
                                photoBitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10,
                                        outputStream);
                                byteArray = outputStream.toByteArray();
                                photoBase64str = Base64.getEncoder().encodeToString(byteArray);
                            }

                            request();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showTip("请选择图片~");
                    }
                }
            }
    );

    /*计算base文件的字节大小*/
    public static double base64FileSize(String base64String) {
        /*检测是否含有base64,文件头)*/
        if (base64String.lastIndexOf(",") > 0) {
            base64String = base64String.substring(base64String.lastIndexOf(",") + 1);
        }
        /* 获取base64字符串长度(不含data:audio/wav;base64,文件头) */
        int size0 = base64String.length();
        /* 获取字符串的尾巴的最后10个字符，用于判断尾巴是否有等号，正常生成的base64文件'等号'不会超过4个 */
        String tail = base64String.substring(size0 - 10);
        /* 找到等号，把等号也去掉,(等号其实是空的意思,不能算在文件大小里面) */
        int equalIndex = tail.indexOf("=");
        if (equalIndex > 0) {
            size0 = size0 - (10 - equalIndex);
        }
        /* 计算后得到的文件流大小，单位为字节 */
        return size0 - ((double) size0 / 8) * 2;
    }


    private String mImagePath;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static int REQUEST_PERMISSION_CODE = 2;
    private boolean createSuccess = true;

    /**
     * 申请权限，创建保存图片的文件夹，设置mImagePath的值
     */
    public void createFileDirectory() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
                        REQUEST_PERMISSION_CODE);
            }
        }
        mImagePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/meteor/";//指定保存路径
        File f = new File(mImagePath);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Log.e(TAG, "createFileDirectory: 文件夹创建失败");
                showTip("无法创建文件夹，通过拍照返回照片的功能无法使用！");
                createSuccess = false;
            }
        }
    }


    /**
     * 请求主方法
     *
     * @return 返回服务结果
     * @throws Exception 异常
     */
//    这段Java代码实现了一个请求主方法，
//    通过构造请求URL、设置请求头、设置请求方法为POST、设置请求参数、发送请求、接收响应流数据并将其转换为字符串并返回。
//
//    其中，buildRequetUrl方法用于构造请求URL，封装了鉴权参数等；
//    buildParam方法用于组装请求参数，其中包含了请求头、请求参数、请求体等信息；
//    doRequest方法则是请求主方法，通过调用buildRequetUrl和buildParam方法构造请求参数，
//    并通过HttpURLConnection发送POST请求，接收响应流数据，将其转换为字符串并返回。
//    在构造请求URL时，需要注意将调用方提供的requestUrl替换为ws://和wss://前缀，
//    并根据请求URL生成日期、请求方法、请求路径等信息构造请求行，最终使用Base64编码生成请求头。
//    在组装请求参数时，需要注意将示例参数替换为调用方提供的参数，
//    并使用Base64编码生成请求头中的api_key、algorithm、headers和signature等信息。
//    在发送POST请求时，需要设置请求头、请求参数、请求方法为POST，
//    并将请求参数写入请求体中。
//    在接收响应流数据时，需要使用HttpURLConnection的输入流读取响应数据，并将其转换为字符串并返回。
    public String doRequest() throws Exception {
//    构建url
        URL realUrl = new URL(buildRequestUrl());
//    根据url获取HttpUrlConnection对象
        HttpURLConnection httpURLConnection = (HttpURLConnection) realUrl.openConnection();
//    表示允许输入流和输出流，即允许从服务器接收数据
        httpURLConnection.setDoInput(true);
//    和向服务器发送数据
        httpURLConnection.setDoOutput(true);
//    设置请求方法为post方法
        httpURLConnection.setRequestMethod("POST");
//    设置请求头参数，表示请求体中的数据类型为JSON格式
        httpURLConnection.setRequestProperty("Content-type", "application/json");
//    获取HttpUrlConnection的输出流
        OutputStream out = httpURLConnection.getOutputStream();
//    获取参数并打印
        String params = buildParam();
        Log.w(TAG, "doRequest: " + "params=>" + params);
//    out.write(params.getBytes())将请求参数写入请求体中，并使用getBytes方法将参数转换为字节数组
        out.write(params.getBytes());
//    out.flush()刷新输出流，将写入的数据发送到服务器
        out.flush();

        InputStream is = null;
        try {
//    获取连接从服务器获取的输入流
            is = httpURLConnection.getInputStream();
        } catch (Exception e) {
            is = httpURLConnection.getErrorStream();
            throw new Exception("make request error:" + "code is " + httpURLConnection.getResponseMessage() + readAllBytes(is));
        }

        return readAllBytes(is);
    }

    /**
     * 处理请求URl
     * 封装鉴权参数等
     *
     * @return 处理后的URl
     */
    public String buildRequestUrl() {

        URL url = null;
        // 替换掉schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String requestUrl = "https://api.xf-yun.com/v1/private/sf8e6aca1";
        String httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://", "https" +
                "://");

        try {
            url = new URL(httpRequestUrl);

            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
                    Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());

            String host = url.getHost();

//            if (url.getPort() != 80 && url.getPort() != 443) {
//                host = host + ":" + String.valueOf(url.getPort());
//            }

            StringBuilder signatureOriginalFiled =
                    new StringBuilder("host: ").append(host).append("\n").//host: $host\n
                            append("date: ").append(date).append("\n").//date: $date\n
                            append("POST ").append(url.getPath()).append(" HTTP/1.1");//$request
            // -line
            // 请求行，请求报文的第一行，包括请求方法字段、URL字段和HTTP协议版本
            //System.err.println(builder.toString());
            Charset charset = Charset.forName("UTF-8");
            Mac hmac_sha256 = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmac_sha256");
            hmac_sha256.init(spec);
            byte[] signature_sha =
                    hmac_sha256.doFinal(signatureOriginalFiled.toString().getBytes(charset));
            String signature = Base64.getEncoder().encodeToString(signature_sha);
            String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", " +
                    "headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date " +
                    "request-line", signature);
            String authorization =
                    Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(charset));
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl,
                    URLEncoder.encode(authorization, "utf-8"), URLEncoder.encode(host, "utf-8"),
                    URLEncoder.encode(date, "utf-8"));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
        }
    }

    /**
     * 组装请求参数
     * 直接使用示例参数，
     * 替换部分值
     *
     * @return 参数字符串
     */
    private String buildParam() throws IOException {
        String param = "{" +
                "    \"header\": {" +
                "        \"app_id\": \"" + APPID + "\"," +
                "        \"status\": 3" +
                "    }," +
                "    \"parameter\": {" +
                "        \"sf8e6aca1\": {" +
                "            \"category\": \"ch_en_public_cloud\"," +
                "            \"result\": {" +
                "                \"encoding\": \"utf8\"," +
                "                \"compress\": \"raw\"," +
                "                \"format\": \"json\"" +
                "            }" +
                "        }" +
                "    }," +
                "    \"payload\": {" +
                "        \"sf8e6aca1_data_1\": {" +
                "            \"encoding\": \"jpg\"," +
                "            \"status\": " + 3 + "," +
                "            \"image\": \"" + photoBase64str + "\"" +
                "        }" +
                "    }" +
                "}";
        return param;
    }

    /**
     * 读取流数据
     *
     * @param is 流
     * @return 字符串
     * @throws IOException 异常
     */
    private String readAllBytes(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len = 0;
//        is.read(b))的作用是从输入流中读取若干个字节，并将其存储到b数组中
//        is.read(b))的返回值代表读取到的字节数量
//          如果读取到的字节数量为-1，则表示输入流中已经没有更多的字节可以读取了
//          因此，这个方法的返回值可以用来判断输入流中是否还有数据可以读取
        while ((len = is.read(b)) != -1) {
//      偏移量offset为0，每次将读取到的内容【恰好是len个字节】连接到stringBuilder的尾部
            sb.append(new String(b, 0, len, "utf-8"));
        }
//      转换为字符串
        return sb.toString();
    }

    //    根据文件路径，读取文件流并调用inputStream2ByteArray转换为字节数组
    public static byte[] read(String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        byte[] data = inputStream2ByteArray(in);
        in.close();
        return data;
    }

    //    将输入流转换为字节数组
    private static byte[] inputStream2ByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}

//解析json
class JsonResult {
    Header header;
    Payload payload;
}

class Header {
    int code;
    String message;
    String sid;
}

class Payload {
    Result result;
}

class Result {
    String text;
//    String compress;
//    String encoding;
//    String format;
}

class Text {
    ArrayList<Page> pages;
//    String category;
//    String version;
}

class Page {
    //    int exception;
    //    float width;
    //    float angle;
    //    float height;
    ArrayList<Line> lines;

}

class Line {
    ArrayList<Word> words;
}

class Word {
    String content;
}