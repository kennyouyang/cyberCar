package com.fat246.cybercar.activities.regulations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cheshouye.api.client.WeizhangClient;
import com.cheshouye.api.client.json.CarInfo;
import com.cheshouye.api.client.json.CityInfoJson;
import com.cheshouye.api.client.json.InputConfigJson;
import com.cheshouye.api.client.json.ProvinceInfoJson;
import com.fat246.cybercar.R;
import com.fat246.cybercar.utils.PreferencesUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRegulationActivity extends AppCompatActivity {


    private String defaultChepai = "粤"; // 粤=广东

    private TextView short_name;
    private TextView query_city;
    private View btn_cpsz;
    private Button btn_query;

    private EditText chepai_number;
    private EditText chejia_number;
    private EditText engine_number;

    //定位
    LocationClient mLocationClient;

    //只需要一次定位成功就行
    public int isFirstSucceed = 0;

    // 行驶证图示
    private View popXSZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regulation_custom);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.csy_titlebar);

        setToolbar();

        //判断是否需要定位
        if (PreferencesUtility.getInstance(this).isSettingsRegulationLocation()) {

            initLoacation();
        }

        // 选择省份缩写
        query_city = (TextView) findViewById(R.id.cx_city);
        chepai_number = (EditText) findViewById(R.id.chepai_number);
        chejia_number = (EditText) findViewById(R.id.chejia_number);
        engine_number = (EditText) findViewById(R.id.engine_number);
        short_name = (TextView) findViewById(R.id.chepai_sz);

        // ----------------------------------------------

        btn_cpsz = (View) findViewById(R.id.btn_cpsz);
        btn_query = (Button) findViewById(R.id.btn_query);

        btn_cpsz.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(CustomRegulationActivity.this, ShortProvinceGridActivity.class);
                intent.putExtra("select_short_name", short_name.getText());
                startActivityForResult(intent, 0);
            }
        });

        query_city.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(CustomRegulationActivity.this, ProvinceActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        btn_query.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 获取违章信息
                CarInfo car = new CarInfo();
                String quertCityStr = null;
                String quertCityIdStr = null;

                final String shortNameStr = short_name.getText().toString()
                        .trim();
                final String chepaiNumberStr = chepai_number.getText()
                        .toString().trim();
                if (query_city.getText() != null
                        && !query_city.getText().equals("")) {
                    quertCityStr = query_city.getText().toString().trim();

                }

                if (query_city.getTag() != null
                        && !query_city.getTag().equals("")) {
                    quertCityIdStr = query_city.getTag().toString().trim();
                    car.setCity_id(Integer.parseInt(quertCityIdStr));
                }
                final String chejiaNumberStr = chejia_number.getText()
                        .toString().trim();
                final String engineNumberStr = engine_number.getText()
                        .toString().trim();

                Intent intent = new Intent();

                car.setChejia_no(chejiaNumberStr);
                car.setChepai_no(shortNameStr + chepaiNumberStr);

                car.setEngine_no(engineNumberStr);

                Bundle bundle = new Bundle();

                bundle.putString("carInfo", car.toJSONObject().toString());
                intent.putExtras(bundle);

                boolean result = checkQueryItem(car);

                if (result) {
                    intent.setClass(CustomRegulationActivity.this, RegulationResultActivity.class);
                    startActivity(intent);

                }
            }
        });

        // 根据默认查询地城市id, 初始化查询项目
        // setQueryItem(defaultCityId, defaultCityName);
        short_name.setText(defaultChepai);

        // 显示隐藏行驶证图示
        popXSZ = (View) findViewById(R.id.popXSZ);
        popXSZ.setOnTouchListener(new popOnTouchListener());
        hideShowXSZ();
    }

    //开始定位
    private void initLoacation() {

        //初始化
        mLocationClient = new LocationClient(this);

        //注册监听事件
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {

                //最多定位10次，放弃定位
                if (isFirstSucceed > 10) {

                    mLocationClient.stop();
                } else {

                    isFirstSucceed++;

                    String province = bdLocation.getProvince();

                    String city = bdLocation.getCity();

                    province = province.substring(0, 2);

                    city = city.substring(0, 2);


                    Log.e("here_>>>>", city + province);

                    Log.e("city_code", bdLocation.getCityCode());

                    int cityId = getCityId(province, city);

                    if (cityId < 0) return;

                    setQueryItem(cityId);

                    //改变UI
                    short_name.setText(getShortProvince(province));

                    query_city.setText(city);

                    //成功就停止定位
                    mLocationClient.stop();
                }
            }
        });

        //设置
        LocationClientOption option = new LocationClientOption();

        option.setOpenGps(true); //打开GPS

        option.setCoorType("gcj02"); //设置坐标类型
        option.setScanSpan(1000);   //定位间隙

        option.setIsNeedAddress(true);

        //绑定设置
        mLocationClient.setLocOption(option);

        //开始定位
        mLocationClient.start();
    }

    //setToolbar
    private void setToolbar() {

        View rootView = findViewById(R.id.activity_regulation_custom_toolbar);

        if (rootView != null) {

            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

            toolbar.setTitle("自定义查询");

            setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    CustomRegulationActivity.this.finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        switch (requestCode) {
            case 0:
                Bundle bundle = data.getExtras();
                String ShortName = bundle.getString("short_name");
                short_name.setText(ShortName);
                break;
            case 1:
                Bundle bundle1 = data.getExtras();
                // String cityName = bundle1.getString("city_name");
                String cityId = bundle1.getString("city_id");
                // query_city.setText(cityName);
                // query_city.setTag(cityId);
                // InputConfigJson inputConfig =
                // WeizhangClient.getInputConfig(Integer.parseInt(cityId));
                // System.out.println(inputConfig.toJson());
                setQueryItem(Integer.parseInt(cityId));

                break;
        }
    }

    // 根据城市的配置设置查询项目
    private void setQueryItem(int cityId) {

        InputConfigJson cityConfig = WeizhangClient.getInputConfig(cityId);

        // 没有初始化完成的时候;
        if (cityConfig != null) {
            CityInfoJson city = WeizhangClient.getCity(cityId);

            query_city.setText(city.getCity_name());
            query_city.setTag(cityId);

            int len_chejia = cityConfig.getClassno();
            int len_engine = cityConfig.getEngineno();

            View row_chejia = (View) findViewById(R.id.row_chejia);
            View row_engine = (View) findViewById(R.id.row_engine);

            // 车架号
            if (len_chejia == 0) {
                row_chejia.setVisibility(View.GONE);
            } else {
                row_chejia.setVisibility(View.VISIBLE);
                setMaxlength(chejia_number, len_chejia);
                if (len_chejia == -1) {
                    chejia_number.setHint("请输入完整车架号");
                } else if (len_chejia > 0) {
                    chejia_number.setHint("请输入车架号后" + len_chejia + "位");
                }
            }

            // 发动机号
            if (len_engine == 0) {
                row_engine.setVisibility(View.GONE);
            } else {
                row_engine.setVisibility(View.VISIBLE);
                setMaxlength(engine_number, len_engine);
                if (len_engine == -1) {
                    engine_number.setHint("请输入完整车发动机号");
                } else if (len_engine > 0) {
                    engine_number.setHint("请输入发动机后" + len_engine + "位");
                }
            }
        }
    }

    // 提交表单检测
    private boolean checkQueryItem(CarInfo car) {
        if (car.getCity_id() == 0) {
            Toast.makeText(CustomRegulationActivity.this, "请选择查询地", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (car.getChepai_no().length() != 7) {
            Toast.makeText(CustomRegulationActivity.this, "您输入的车牌号有误", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (car.getCity_id() > 0) {
            InputConfigJson inputConfig = WeizhangClient.getInputConfig(car
                    .getCity_id());
            int engineno = inputConfig.getEngineno();
            int registno = inputConfig.getRegistno();
            int classno = inputConfig.getClassno();

            // 车架号
            if (classno > 0) {
                if (car.getChejia_no().equals("")) {
                    Toast.makeText(CustomRegulationActivity.this, "输入车架号不为空", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (car.getChejia_no().length() != classno) {
                    Toast.makeText(CustomRegulationActivity.this, "输入车架号后" + classno + "位",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (classno < 0) {
                if (car.getChejia_no().length() == 0) {
                    Toast.makeText(CustomRegulationActivity.this, "输入全部车架号", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            //发动机
            if (engineno > 0) {
                if (car.getEngine_no().equals("")) {
                    Toast.makeText(CustomRegulationActivity.this, "输入发动机号不为空", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (car.getEngine_no().length() != engineno) {
                    Toast.makeText(CustomRegulationActivity.this,
                            "输入发动机号后" + engineno + "位", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (engineno < 0) {
                if (car.getEngine_no().length() == 0) {
                    Toast.makeText(CustomRegulationActivity.this, "输入全部发动机号", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            // 注册证书编号
            if (registno > 0) {
                if (car.getRegister_no().equals("")) {
                    Toast.makeText(CustomRegulationActivity.this, "输入证书编号不为空", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (car.getRegister_no().length() != registno) {
                    Toast.makeText(CustomRegulationActivity.this,
                            "输入证书编号后" + registno + "位", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (registno < 0) {
                if (car.getRegister_no().length() == 0) {
                    Toast.makeText(CustomRegulationActivity.this, "输入全部证书编号", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    // 设置/取消最大长度限制
    private void setMaxlength(EditText et, int maxLength) {
        if (maxLength > 0) {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                    maxLength)});
        } else { // 不限制
            et.setFilters(new InputFilter[]{});
        }
    }

    // 显示隐藏行驶证图示
    private void hideShowXSZ() {
        View btn_help1 = (View) findViewById(R.id.ico_chejia);
        View btn_help2 = (View) findViewById(R.id.ico_engine);
        Button btn_closeXSZ = (Button) findViewById(R.id.btn_closeXSZ);

        btn_help1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.VISIBLE);
            }
        });
        btn_help2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.VISIBLE);
            }
        });
        btn_closeXSZ.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                popXSZ.setVisibility(View.GONE);
            }
        });
    }

    // 避免穿透导致表单元素取得焦点
    private class popOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            popXSZ.setVisibility(View.GONE);
            return true;
        }
    }


    /***********************************************************************************************
     * 用map 解决所在地和车牌号第一位的信息
     */
    public static Map<String, String> SHORT_PROVINCE = null;

    public static String getShortProvince(String longProvince) {

        if (SHORT_PROVINCE == null) {

            initProvince();
        }

        return SHORT_PROVINCE.get(longProvince);
    }

    public static void initProvince() {

        SHORT_PROVINCE = new HashMap<>();

        SHORT_PROVINCE.put("北京", "京");
        SHORT_PROVINCE.put("天津", "津");
        SHORT_PROVINCE.put("上海", "沪");
        SHORT_PROVINCE.put("四川", "川");
        SHORT_PROVINCE.put("武汉", "鄂");
        SHORT_PROVINCE.put("甘肃", "甘");
        SHORT_PROVINCE.put("江西", "赣");
        SHORT_PROVINCE.put("广西", "桂");
        SHORT_PROVINCE.put("贵州", "贵");
        SHORT_PROVINCE.put("黑龙", "黑");
        SHORT_PROVINCE.put("吉林", "吉");
        SHORT_PROVINCE.put("河北", "翼");
        SHORT_PROVINCE.put("山西", "晋");
        SHORT_PROVINCE.put("辽宁", "辽");
        SHORT_PROVINCE.put("山东", "鲁");
        SHORT_PROVINCE.put("内蒙", "蒙");
        SHORT_PROVINCE.put("福建", "闽");
        SHORT_PROVINCE.put("宁夏", "宁");
        SHORT_PROVINCE.put("青海", "青");
        SHORT_PROVINCE.put("海南", "琼");
        SHORT_PROVINCE.put("陕西", "陕");
        SHORT_PROVINCE.put("江苏", "苏");
        SHORT_PROVINCE.put("安徽", "皖");
        SHORT_PROVINCE.put("湖南", "湘");
        SHORT_PROVINCE.put("新疆", "新");
        SHORT_PROVINCE.put("重庆", "渝");
        SHORT_PROVINCE.put("河南", "豫");
        SHORT_PROVINCE.put("广东", "粤");
        SHORT_PROVINCE.put("云南", "云");
        SHORT_PROVINCE.put("西藏", "藏");
        SHORT_PROVINCE.put("浙江", "浙");
    }

    public static int getCityId(String province, String city) {

        List<ProvinceInfoJson> provinceInfoJsons = WeizhangClient.getAllProvince();

        int pid = -1;

        if (provinceInfoJsons == null) {

            return -1;
        }

        for (ProvinceInfoJson pj : provinceInfoJsons) {

            String str = pj.getProvinceName();

            if (str.equals(province)) {
                pid = pj.getProvinceId();

                break;
            }
        }

        Log.e("pid", pid + "");
        if (pid != -1) {

            List<CityInfoJson> cityInfoJsons = WeizhangClient.getCitys(pid);

            if (cityInfoJsons == null) {

                return -1;
            }

            for (CityInfoJson cj : cityInfoJsons) {

                String str = cj.getCity_name();

                if (str.equals(city)) {

                    Log.e("city_id_my", cj.getCity_id() + "");
                    return cj.getCity_id();
                }
            }
        }

        return -1;
    }
    /***********************************************************************************************
     */
}
