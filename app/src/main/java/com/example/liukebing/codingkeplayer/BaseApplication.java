package com.example.liukebing.codingkeplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.liukebing.codingkeplayer.utils.Constant;
import com.lidroid.xutils.DbUtils;

/**
 * Created by Nick on 2016/4/12.
 */
public class BaseApplication extends Application {

    public static SharedPreferences sp;//定义为静态，可从全局引用
    public static DbUtils dbUtils;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);//实例化SharedPreferences对象
        //实例化操作数据库的工具类dbUtils，并创建数据库
        dbUtils = DbUtils.create(getApplicationContext(), Constant.DB_NAME);
        context = getApplicationContext();
    }
}
