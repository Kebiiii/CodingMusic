package com.example.liukebing.codingkeplayer;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.liukebing.codingkeplayer.adapter.MyMusicListAdapter;
import com.example.liukebing.codingkeplayer.utils.MediaUtils;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;

import java.util.ArrayList;

/**
 * Created by Liukebing on 2016/4/21.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener ,View.OnClickListener{

    private ListView listView_my_music;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;
    private TextView textView_songName,textView2_singer;
    private ImageView imageView_album,imageView2_play_pause,imageView3_next;

    private int position = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        listView_my_music = (ListView) view.findViewById(R.id.listView_mymusic);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView2_play_pause = (ImageView) view.findViewById(R.id.imageView2_play_pause);
        imageView3_next = (ImageView) view.findViewById(R.id.imageView3_next);
        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);

        listView_my_music.setOnItemClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView3_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);

        //loadData();
        //绑定播放服务

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定服务
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //解除播放服务
//        mainActivity.unbindPlayService();
    }

    /**
     * 加载本地音乐列表
     */
    public void loadData() {
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);
        //mp3Infos = mainActivity.playService.getMp3Infos();
        myMusicListAdapter = new MyMusicListAdapter(mainActivity, mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList() != PlayService.MY_MUSIC_LIST) {
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        mainActivity.playService.play(position);

        //保存播放的时间
        savePlayRecord();
    }

    private void savePlayRecord() {
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = BaseApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
            if (playRecordMp3Info == null) {
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                BaseApplication.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                BaseApplication.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //切换播放状态下的UI设置
    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position < mainActivity.playService.getMp3Infos().size()) {
            Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView2_singer.setText(mp3Info.getArtist());
            if (mainActivity.playService.isPlaying()) {
                imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
            } else {
                imageView2_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
            }

            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);
            this.position = position;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView2_play_pause:
                if (mainActivity.playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();
                } else {

                    if (mainActivity.playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.imageView3_next:
                mainActivity.playService.next();
                break;
            case R.id.imageView_album:
                Intent intent = new Intent(mainActivity, MusicPlayActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
