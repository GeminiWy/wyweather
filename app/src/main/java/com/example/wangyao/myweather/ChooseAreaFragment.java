package com.example.wangyao.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangyao.myweather.db.City;
import com.example.wangyao.myweather.db.County;
import com.example.wangyao.myweather.db.Province;
import com.example.wangyao.myweather.util.HttpUtil;
import com.example.wangyao.myweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *碎片，用来遍历省市县三层列表
 * Created by wangyao on 2017/5/17.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView titleText;
    private Button backButton;
    private ListView chooseListView;
    private int currentLevel;
    private Province selectedProvince;
    private City selectedCity;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private ProgressDialog progressDialog;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);//碎片布局传入
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        chooseListView = (ListView)view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);//数组adapter传入listview布局，还有数据
        chooseListView.setAdapter(adapter);//listview设置adapter
        return view;
    }

    /**
     * 在此设置各种点击事件
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chooseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            /**
             *
             * 设置listview的点击事件
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * 选中的是province等级则将cities数据加载出来，并将点击哪个province放入到selectedProvince中
                 */
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                    /**
                     * 选中的是city等级则将counties数据加载出来，并将点击哪个city放入到selectedCity中
                     */

                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        /**
         * 设置返回按钮的点击事件
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){//如果是在county页面点击，则返回加载city页面
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){//如果是在city页面点击，则返回加载province页面
                    queryProvinces();
                }
            }
        });
        queryProvinces();//一开始加载的是province页面
    }

    /**
     * 加载province页面方法
     */
    private void queryProvinces(){
        titleText.setText("中国");//设置标题为中国
        backButton.setVisibility(View.GONE);//隐藏返回按钮
        provinceList = DataSupport.findAll(Province.class);//在数据库查找province信息，传入provincelist
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){//遍历provincelist并传入province
                dataList.add(province.getProvinceName());//将遍历的province通过getprovincename方法得到所有省的名字并加入datalist
            }
            adapter.notifyDataSetChanged();
            chooseListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;//设置此时级别为province级别
        }else {
            String address = "http://guolin.tech/api/china";//否则通过网络传输从服务器得到数据
            queryFromServer(address,"province");
        }
    }

    /**
     * 加载city页面方法
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//必须有这一项，显示出返回按钮
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);//通过where找到点击的provinceid，再通过find在此province中找到city数据库信息
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            chooseListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 加载county页面
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            chooseListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode+ "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 发送网络请求并从服务器返回数据并解析传入数据库
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT);
                    }
                });

            }

            /**
             * 成功时的回调，将服务器返回的数据解析后传入数据库供queryprovince等加载方法调用
             * @param call
             * @param response
             * @throws IOException
             */

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
