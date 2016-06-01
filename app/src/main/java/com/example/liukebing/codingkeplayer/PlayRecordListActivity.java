package com.example.liukebing.codingkeplayer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.liukebing.codingkeplayer.adapter.MyMusicListAdapter;
import com.example.liukebing.codingkeplayer.utils.Constant;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private TextView textView2_no_data;
    private MyMusicListAdapter adapter;
    private ListView listView_play_record;
    private ArrayList<Mp3Info> playRecordList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);
        textView2_no_data = (TextView) findViewById(R.id.textView2_no_data);
        listView_play_record = (ListView) findViewById(R.id.listView_play_record);
        initData();
        listView_play_record.setOnItemClickListener(this);
    }

    //初始化“最近播放”数据
    private void initData() {
        try {
            //查询“最近播放”的记录，true表示倒序，desc，false代表正序，sec
            List<Mp3Info> list = BaseApplication.dbUtils.findAll(Selector.from(Mp3Info.class).
                    where("playTime", "!=", 0).orderBy("playTime", true).limit(Constant.PLAY_RECORD_NUM));
            if (list == null || list.size() == 0) {
                textView2_no_data.setVisibility(View.VISIBLE);
                listView_play_record.setVisibility(View.GONE);
            } else {
                textView2_no_data.setVisibility(View.GONE);
                listView_play_record.setVisibility(View.VISIBLE);
                playRecordList = (ArrayList<Mp3Info>) list;
                adapter = new MyMusicListAdapter(this, playRecordList);
                listView_play_record.setAdapter(adapter);
            }
            Log.i("playRecordList", String.valueOf(playRecordList));
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
        if (playService.getChangePlayList() != PlayService.PLAY_RECORD_MUSIC_LIST) {
            playService.setMp3Infos(playRecordList);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        playService.play(position);
    }
}
