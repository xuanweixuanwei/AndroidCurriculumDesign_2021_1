package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 1、通用文字识别
 * 2、支持中英文,支持手写和印刷文字。
 * 3、在倾斜文字上效果有提升，同时支持部分生僻字的识别。
 * 4、图片格式支持jpg格式、jpeg格式、png格式、bmp格式，且需保证图像文件大小base64编码后不超过10MB
 */
public class CharacterRecognitionActivity extends AppCompatActivity {
    private static final String TAG = CharacterRecognitionActivity.class.getSimpleName();
    private String requestUrl = "https://api.xf-yun.com/v1/private/sf8e6aca1";
//https://api.xf-yun.com/v1/private/sf8e6aca1?
// authorization=YXBpX2tleT0iYXBpa2V5WFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFgiLCBhbGdvcml0aG09ImhtYWMtc2hhMjU2IiwgaGVhZGVycz0iaG9zdCBkYXRlIHJlcXVlc3QtbGluZSIsIHNpZ25hdHVyZT0iL21nMmg5QkNrZXNwaWxaOTRIVUJhUVZQcTJ2N1B4WUY5MHRlVEJsYXhkOD0i
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

    //控制台获取以下信息
    private static String APPID = "";
    private static String apiSecret = "";
    private static String apiKey = "";

    //文件存放位置
    private static String IMAGE_PATH = "example/1.jpg";

    //解析json
    private static Gson gson=new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_recognition);
        
        initView();
//        UniversalCharacterRecognition demo = new UniversalCharacterRecognition();
        try {
            String resp = doRequest();
            System.out.println("resp=>" + resp);
            JsonParse myJsonParse = gson.fromJson(resp, JsonParse.class);
            String textBase64Decode=new String(Base64.getDecoder().decode(myJsonParse.payload.result.text), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(textBase64Decode);
            System.out.println("text字段Base64解码后=>"+jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("文字识别");
        actionBar.setDisplayHomeAsUpEnabled(true);//在ActionBar最左边显示返回箭头按钮

    }

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

    /**
     * 请求主方法
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
        URL realUrl = new URL(buildRequetUrl());
//    根据url获取HttpUrlConnection对象
        HttpURLConnection  httpURLConnection = (HttpURLConnection) realUrl.openConnection();
//    表示允许输入流和输出流，即允许从服务器接收数据
        httpURLConnection.setDoInput(true);
//    和向服务器发送数据
        httpURLConnection.setDoOutput(true);
//    设置请求方法为post方法
        httpURLConnection.setRequestMethod("POST");
//    设置请求头参数，表示请求体中的数据类型为JSON格式
        httpURLConnection.setRequestProperty("Content-type","application/json");
//    获取HttpUrlConnection的输出流
        OutputStream out = httpURLConnection.getOutputStream();
//    获取参数并打印
        String params = buildParam();
        Log.w(TAG, "doRequest: "+"params=>"+params);
//    out.write(params.getBytes())将请求参数写入请求体中，并使用getBytes方法将参数转换为字节数组
        out.write(params.getBytes());
//    out.flush()刷新输出流，将写入的数据发送到服务器
        out.flush();

        InputStream is = null;
        try{
//    获取连接从服务器获取的输入流
            is = httpURLConnection.getInputStream();
        }catch (Exception e){
            is = httpURLConnection.getErrorStream();
            throw new Exception("make request error:"+"code is "+httpURLConnection.getResponseMessage()+readAllBytes(is));
        }
        return readAllBytes(is);
    }
    /**
     * 处理请求URl
     * 封装鉴权参数等
     * @return 处理后的URl
     */
    public String buildRequetUrl(){

        URL url = null;
        // 替换掉schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String  httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://","https://" );

        try {
            url = new URL(httpRequestUrl);

            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.CHINA);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());

            String host = url.getHost();

            if (url.getPort()!=80 && url.getPort() !=443){
                host = host +":"+String.valueOf(url.getPort());
            }

            StringBuilder signatureOriginalFiled = new StringBuilder("host: ").append(host).append("\n").//host: $host\n
                    append("date: ").append(date).append("\n").//date: $date\n
                    append("POST ").append(url.getPath()).append(" HTTP/1.1");//$request-line 请求行，请求报文的第一行，包括请求方法字段、URL字段和HTTP协议版本
            //System.err.println(builder.toString());
            Charset charset = Charset.forName("UTF-8");
            Mac hmac_sha256 = Mac.getInstance("hmac_sha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmac_sha256");
            hmac_sha256.init(spec);
            byte[] signature_sha = hmac_sha256.doFinal(signatureOriginalFiled.toString().getBytes(charset));
            String signature = Base64.getEncoder().encodeToString(signature_sha);
            String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", signature);
            String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(charset));
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authorization,"utf-8"), URLEncoder.encode(host,"utf-8"), URLEncoder.encode(date,"utf-8"));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:"+e.getMessage());
        }
    }

    /**
     * 组装请求参数
     * 直接使用示例参数，
     * 替换部分值
     * @return 参数字符串
     */
    private String  buildParam() throws IOException {
        String param = "{"+
                "    \"header\": {"+
                "        \"app_id\": \""+APPID+"\","+
                "        \"status\": 3"+
                "    },"+
                "    \"parameter\": {"+
                "        \"sf8e6aca1\": {"+
                "            \"category\": \"ch_en_public_cloud\","+
                "            \"result\": {"+
                "                \"encoding\": \"utf8\","+
                "                \"compress\": \"raw\","+
                "                \"format\": \"json\""+
                "            }"+
                "        }"+
                "    },"+
                "    \"payload\": {"+
                "        \"sf8e6aca1_data_1\": {"+
                "            \"encoding\": \"jpg\","+
                "            \"status\": " + 3 + "," +
                "            \"image\": \""+Base64.getEncoder().encodeToString(read(IMAGE_PATH))+"\""+
                "        }"+
                "    }"+
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
        while ((len = is.read(b)) != -1){
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
class JsonParse{
    Header header;
    Payload payload;
}
class Header{
    int code;
    String message;
    String sid;
}
class Payload{
    Result result;
}
class Result{
    String text;
}