package com.example.wangyao.myweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 工具类
 * 利用okhttp发送网络请求
 * Created by wangyao on 2017/5/17.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
