package com.example.liukebing.codingkeplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.example.liukebing.codingkeplayer.utils.MediaUtils;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现的功能：
 * 1，播放
 * 2，暂停
 * 3，下一首
 * 4，上一首
 * 5，获取当前歌曲播放的进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener{
    private MediaPlayer mPlayer;
    private int currentPosition;//表示当前正在播放的位置
    private ArrayList<Mp3Info> mp3Infos;

    private MusicUpdateListener musicUpdateListener;

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isPause = false;

    //播放模式
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    //切换播放列表
    public static  final int MY_MUSIC_LIST = 1;//表示所有歌曲列表
    public static  final int LIKE_MUSIC_LIST = 2;//表示收藏歌曲列表
    public static final int PLAY_RECORD_MUSIC_LIST = 3;//表示“最近播放”列表
    private int ChangePlayList = MY_MUSIC_LIST;

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public boolean isPause(){
        return isPause;
    }

    public int getChangePlayList() {
        return ChangePlayList;
    }

    public void setChangePlayList(int isChangePlayList) {
        this.ChangePlayList = isChangePlayList;
    }

    public PlayService() {
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public int getCurrentPosition(){
        if (mPlayer.isPlaying()) {
            return currentPosition;
        }
        return 0;
    }

    private Random random = new Random();
    @Override
    public void onCompletion(MediaPlayer mp) {
        //判断播放模式
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    class PlayBinder extends Binder{
        public PlayService getPlayService(){
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication app = (BaseApplication) getApplication();
        currentPosition = BaseApplication.sp.getInt("currentPosition", 0);
        play_mode = BaseApplication.sp.getInt("play_mode", 0);

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        es.execute(updateStatusRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
        mPlayer = null;
        mp3Infos = null;
        musicUpdateListener = null;
    }

    //更新状态的Runnable实例——updateStatusRunnable
    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                //不断调用
                if (musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());//更新进度值，具体方法在各自的Activity中实现
                }
                try {
                    Thread.sleep(500);//每0.5s更新一次进度
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //播放
    public void play(int position){
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
        mPlayer.reset();
            try {
                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mPlayer.prepare();
                mPlayer.start();
                currentPosition = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (musicUpdateListener != null) {
                musicUpdateListener.onChange(currentPosition);
            }


    }

    //暂停
    public void pause(){

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next(){

        if (currentPosition >= mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev(){
        if (currentPosition-1 < 0) {
            currentPosition = mp3Infos.size()-1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //
    public void start(){

        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();

        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentProgress(){
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration(){
        return mPlayer.getDuration();
    }

    public void seekTo(int msec){
        mPlayer.seekTo(msec);
    }

    //更新状态的接口
    public interface MusicUpdateListener{
        public void onPublish(int progress);
        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
