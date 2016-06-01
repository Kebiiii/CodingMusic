package com.example.liukebing.codingkeplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.liukebing.codingkeplayer.R;
import com.example.liukebing.codingkeplayer.utils.MediaUtils;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;


/**
 * Created by Liukebing on 2016/4/21.
 */
public class MyMusicListAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<Mp3Info> mp3Infos;
    public MyMusicListAdapter(Context ctx, ArrayList<Mp3Info> mp3Infos){
        this.ctx = ctx;
        this.mp3Infos = mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_music_list, null);
            vh = new ViewHolder();
            vh.textView_title = (TextView) convertView.findViewById(R.id.textView_title);
            vh.textView2_singer = (TextView) convertView.findViewById(R.id.textView2_singer);
            vh.textView3_time = (TextView) convertView.findViewById(R.id.textView3_time);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        vh.textView_title.setText(mp3Info.getTitle());
        vh.textView2_singer.setText(mp3Info.getArtist());
        vh.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        return convertView;
    }

    static class ViewHolder{
        TextView textView_title;
        TextView textView2_singer;
        TextView textView3_time;
    }
}
