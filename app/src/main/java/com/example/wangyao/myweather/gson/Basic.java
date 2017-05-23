package com.example.wangyao.myweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wangyao on 2017/5/20.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
