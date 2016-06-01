package com.example.liukebing.codingkeplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.liukebing.codingkeplayer.R;
import com.example.liukebing.codingkeplayer.vo.SearchResult;

import java.util.ArrayList;


/**
 * Created by Nick on 2016/4/13.
 */
public class NetMusicListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SearchResult> searchResults;

    public NetMusicListAdapter(Context context, ArrayList<SearchResult> searchResults) {
        this.context=context;
        this.searchResults = searchResults;
    }

    public ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.net_item_music_list, null);
            vh = new ViewHolder();
            vh.textView2_singer = (TextView) convertView.findViewById(R.id.textView2_singer);
            vh.textView2_title = (TextView) convertView.findViewById(R.id.textView2_title);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        SearchResult searchResult = searchResults.get(position);
        vh.textView2_title.setText(searchResult.getMusicName());
        vh.textView2_singer.setText(searchResult.getArtist());
        return convertView;
    }
    static class ViewHolder{
        TextView textView2_title;
        TextView textView2_singer;
    }
}
