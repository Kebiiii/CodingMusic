package com.example.liukebing.codingkeplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.liukebing.codingkeplayer.BaseActivity;
import com.example.liukebing.codingkeplayer.BaseApplication;
import com.example.liukebing.codingkeplayer.R;
import com.example.liukebing.codingkeplayer.adapter.MyMusicListAdapter;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liukebing on 2016/4/25.
 */
public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private ListView listView_favorite;
    BaseApplication app;
    private ArrayList<Mp3Info> likemp3Infos;
    private MyMusicListAdapter adapter;
    private boolean isChange = false;//表示当前播放列表是否为收藏列表

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (BaseApplication) getApplication();
        setContentView(R.layout.activtiy_like_music_list);
        listView_favorite = (ListView) findViewById(R.id.listView_favorite);
        listView_favorite.setOnItemClickListener(this);
        initData();
    }

    private void initData() {
        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", 1));
            if (list == null || list.size() == 0) {
                Toast.makeText(this,"您还没有收藏音乐",Toast.LENGTH_SHORT).show();
                return;
            }
            likemp3Infos = (ArrayList<Mp3Info>) list;
            adapter = new MyMusicListAdapter(this, likemp3Infos);
            listView_favorite.setAdapter(adapter);
            
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList()!=playService.LIKE_MUSIC_LIST) {
            playService.setMp3Infos(likemp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);

        //保存播放的时间
        savePlayRecord();
    }

    private void savePlayRecord() {
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (playRecordMp3Info == null) {
                mp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
