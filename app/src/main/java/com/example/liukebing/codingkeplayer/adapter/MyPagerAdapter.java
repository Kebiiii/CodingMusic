package com.example.liukebing.codingkeplayer.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Liukebing on 2016/4/24.
 */
public class MyPagerAdapter extends PagerAdapter {
    private ArrayList<View> views;
    public MyPagerAdapter(ArrayList<View> views) {
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    //判断当前视图是否是返回的对象
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    //删除选项卡
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));//删除视图
    }


    //实例化选项卡
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v=views.get(position);
        container.addView(v);//添加视图
        return v;
    }
}
