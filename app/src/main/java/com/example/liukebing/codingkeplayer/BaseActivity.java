package com.example.liukebing.codingkeplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;


/**
 * Created by Liukebing on 2016/4/22.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected PlayService playService;
    private boolean isBound = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //bindPlayService();
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            playService = playBinder.getPlayService();//通过binder对象获取service对象，进行实例化
            playService.setMusicUpdateListener(musicUpdateListener);//给服务设置监听器
            musicUpdateListener.onChange(playService.getCurrentPosition());//在绑定成功时执行onChange方法，会回调子类方法。
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService=null;
            isBound = false;
        }
    };

    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int position);

    //绑定服务
    public void bindPlayService(){
        if (!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    //解除绑定服务
    public void unbindPlayService(){
        if (isBound) {
            unbindService(conn);
            isBound = false;
        }
    }
}
