package com.example.wangyao.myweather.util;

import android.text.TextUtils;

import com.example.wangyao.myweather.db.City;
import com.example.wangyao.myweather.db.County;
import com.example.wangyao.myweather.db.Province;
import com.example.wangyao.myweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 工具类
 * 解析从服务器传回的数据
 * Created by wangyao on 2017/5/17.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);//创建json对象，将返回数据传入
                for(int i = 0; i < allProvinces.length(); i++){//遍历返回数据生成的json对象
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();//创建数据库对象
                    province.setProvinceName(provinceObject.getString("name"));//在返回数据中找到带有"name"的字符串数据，并传入数据库Province
                    province.setProvinceCode(provinceObject.getInt("id"));//在返回数据中找到带有"id"的整型数据，并传入数据库Province
                    province.save();

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }




    public static boolean handleCountyResponse(String response,int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);//把返回数据列为一个为所有县的json数组
                for(int i = 0; i < allCounties.length(); i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);//遍历得到所有县的json对象
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));//通过一个json对象的getString方法得到含"name"的字符串信息传入county数据库
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);//传入选中city的id
                    county.save();

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);//将返回的json形式数据生成一个json对象

            //将json对象里的"HeWeather"的数组生成数组对象（一个HeWeather里包含很多个status，basic，aqi……）
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");

            //取出HeWeather数组里的第一个status，basic，aqi，now，suggestion，daily_forecast,并转换成字符串
            String weatherContent = jsonArray.getJSONObject(0).toString();

            //fromJson需要的是字符串数据,将之解析成实体对象(即:将数据一一传入Weather类中，转换成Weather对象)
            return new Gson().fromJson(weatherContent,Weather.class);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}