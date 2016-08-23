package cn.coolbhu.snailgo.activities.cars;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.coolbhu.snailgo.MyApplication;
import cn.coolbhu.snailgo.R;
import cn.coolbhu.snailgo.beans.Brand;
import cn.coolbhu.snailgo.beans.Car;
import cn.coolbhu.snailgo.beans.Model;
import cn.coolbhu.snailgo.utils.NotificationUtil;
import de.hdodenhof.circleimageview.CircleImageView;

public class CarsInfoActivity extends AppCompatActivity {

    public static final String Action = "Action";
    public static final String Num = "num";

    //View
    private CircleImageView mBrand;
    private TextView mCarNum;
    private TextView mCarNick;
    private TextView mCarEngineNum;
    private TextView mCarRackNum;
    private TextView mCarMileage;
    private TextView mCarType;
    private TextView mCarGas;
    private TextView mCarEngineStatus;
    private TextView mCarSpeedStatus;
    private TextView mCarLightStatus;
    private TextView mCarMaintain;
    private TextView mBrandName;
    private TextView mModelType;
    private LinearLayout linearLayout;

    private LinearLayout layout;
    private ProgressBar progressBar;

    private int action = 0;

    //maintain
    private String num = null;
    private String content = null;
    private Car mCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_info);

        initToolbar();

        findView();

        initData();
    }

    //initData
    private void initData() {

        Intent mIntent = getIntent();

        Bundle mBundle = mIntent.getExtras();

        action = mBundle.getInt(Action, 0);

        switch (action) {

            case 0:

                num = mBundle.getString(Num);

                if (num != null) {

                    showBar();

                    findCar();
                }

                break;

            case 1:

                num = mBundle.getString(NotificationUtil.NOTIFICATION_NUM);
                content = mBundle.getString(NotificationUtil.NOTIFICATION_STR);

                //加载数据
                if (num != null) {

                    showBar();
                    findCar();
                }
                break;
        }
    }

    //findCar
    private void findCar() {

        BmobQuery<Car> query = new BmobQuery<>("Car");

        query.addWhereMatches("Car_Num", num);

        if (MyApplication.isLoginSucceed) {

            query.addWhereMatches("User_Tel", MyApplication.mUser.getUser_Tel());
        }

        query.findObjects(CarsInfoActivity.this, new FindListener<Car>() {
            @Override
            public void onSuccess(List<Car> list) {

                if (list.size() > 0) {

                    mCar = list.get(0);

                    initViewAfterFind();

                    hideBar();
                }
            }

            @Override
            public void onError(int i, String s) {

                Toast.makeText(CarsInfoActivity.this, "汽车信息加载失败，请稍后再试！", Toast.LENGTH_SHORT).show();

                hideBar();
            }
        });
    }

    private void initViewAfterFind() {

        if (mCar != null) {

            this.mCarNum.setText(mCar.getCar_Num());
            this.mCarNick.setText(mCar.getCar_Nick());
            this.mCarRackNum.setText(mCar.getCar_RackNum());
            this.mCarEngineNum.setText(mCar.getCar_EngineNum());

            this.mCarMileage.setText(mCar.getCar_Mileage().intValue() + "");
            this.mCarType.setText(mCar.getCar_ModelType());
            this.mCarGas.setText(mCar.getCar_Gas().shortValue() + "");
            this.mCarEngineStatus.setText((mCar.getCar_EngineStatus() > 0 ? "正常" : "异常"));
            this.mCarSpeedStatus.setText((mCar.getCar_SpeedStatus() > 0 ? "正常" : "异常"));
            this.mCarLightStatus.setText((mCar.getCar_LightStatus() > 0 ? "好" : "坏"));

            //加载图片

            final BmobQuery<Model> query = new BmobQuery<>("Model");

            query.addWhereMatches("Model_Name", mCar.getCar_ModelType());

            query.findObjects(CarsInfoActivity.this, new FindListener<Model>() {
                @Override
                public void onSuccess(List<Model> list) {

                    if (list.size() > 0) {


                        Model model = list.get(0);

                        mBrandName.setText(model.getBrand_Name());
                        mModelType.setText(model.getModel_Type());

                        BmobQuery<Brand> query1 = new BmobQuery<Brand>("Brand");

                        query1.addWhereMatches("Brand_Name", model.getBrand_Name());

                        query1.findObjects(CarsInfoActivity.this, new FindListener<Brand>() {
                            @Override
                            public void onSuccess(List<Brand> list) {

                                if (list.size() > 0) {

                                    Brand brand = list.get(0);

                                    if (brand != null) {

                                        brand.getBrand_Sign().download(CarsInfoActivity.this, new DownloadFileListener() {
                                            @Override
                                            public void onSuccess(String s) {

                                                Bitmap bt = BitmapFactory.decodeFile(s);//图片地址

                                                mBrand.setImageBitmap(bt);
                                            }

                                            @Override
                                            public void onFailure(int i, String s) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onError(int i, String s) {

                            }
                        });
                    }
                }

                @Override
                public void onError(int i, String s) {

                }
            });
        }

        if (content != null) {

            mCarMaintain.setText(content);

            linearLayout.setVisibility(View.VISIBLE);
        }

    }

    //findView
    private void findView() {

        mBrand = (CircleImageView) findViewById(R.id.activity_cars_info_img);
        mCarNum = (TextView) findViewById(R.id.activity_cars_info_num);
        mCarNick = (TextView) findViewById(R.id.activity_cars_info_nick);
        mCarEngineNum = (TextView) findViewById(R.id.activity_cars_info_engine);
        mCarRackNum = (TextView) findViewById(R.id.activity_cars_info_rack);
        mCarMileage = (TextView) findViewById(R.id.activity_cars_info_mileage);
        mCarType = (TextView) findViewById(R.id.activity_cars_info_type);
        mCarGas = (TextView) findViewById(R.id.activity_cars_info_gas);
        mCarEngineStatus = (TextView) findViewById(R.id.activity_cars_info_engine_status);
        mCarSpeedStatus = (TextView) findViewById(R.id.activity_cars_info_speed);
        mCarLightStatus = (TextView) findViewById(R.id.activity_cars_info_light);
        mCarMaintain = (TextView) findViewById(R.id.activity_cars_info_mantain);
        linearLayout = (LinearLayout) findViewById(R.id.activity_cars_info_layout_mantain);
        mBrandName = (TextView) findViewById(R.id.brand_name);
        mModelType = (TextView) findViewById(R.id.model_type);

        layout = (LinearLayout) findViewById(R.id.activity_cars_info_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    //initToolbar
    private void initToolbar() {

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(mToolbar);

        ActionBar mActionBar = getSupportActionBar();

        if (mActionBar != null) {

            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //showbar
    private void showBar() {

        layout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    //hidebar
    private void hideBar() {

        layout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}
