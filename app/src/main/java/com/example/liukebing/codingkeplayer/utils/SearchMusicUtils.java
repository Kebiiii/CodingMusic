package com.example.liukebing.codingkeplayer.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.liukebing.codingkeplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 2016/4/13.
 */
public class SearchMusicUtils {
    private static final int SIZE=20;//只显示查询结果的前20条
    private static final String URL = Constant.BAIDU_URL + Constant.BAIDU_SEARCH;
    private static SearchMusicUtils sInstance;
    private OnSearchResultListener mListener;

    private ExecutorService mThreadPool= Executors.newSingleThreadExecutor();//线程池

    //单例设计模式
    public synchronized static SearchMusicUtils getInstance() {
        if (sInstance == null) {
            sInstance = new SearchMusicUtils();
        }
        return sInstance;
    }

    public SearchMusicUtils setListener(OnSearchResultListener listener) {
        mListener = listener;
        return this;//返回自身对象
    }

    //查询的具体方法
    public void search(final String key, final int page) {
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SUCCESS:
                        if (mListener != null) {
                            //事件方法回调
                            mListener.onSearchResult((ArrayList<SearchResult>) msg.obj);
                        }
                        break;
                    case Constant.FAILED:
                        if (mListener != null) {
                            mListener.onSearchResult(null);
                        }
                        break;
                }
            }
        };
        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {
                //调用查询方法
                ArrayList<SearchResult> results = getMusicList(key, page);//之前写过的搜索方法，这个执行到了
                Log.i("lrc", String.valueOf(results));//打印搜索结果
                if (results == null) {
                    //查询失败。。
                    handler.sendEmptyMessage(Constant.FAILED);
                    return;
                }
                handler.obtainMessage(Constant.SUCCESS,results).sendToTarget();
            }
        });
    }

    //使用Jsoup请求网络解析数据，获取搜索音乐的结果
    private ArrayList<SearchResult> getMusicList(String key, int page) {
        //设置查询结果的起始位置，page=1，从0开始
        final String start = String.valueOf((page - 1) * SIZE);
        try {
            Document doc = Jsoup.connect(URL)
                    .data("key", key, "start", start, "size", String.valueOf(SIZE))//三个参数，设定了查询结果
                    .userAgent(Constant.USER_AGENT)
                    .timeout(60 * 1000).get();
//            Log.i("Jsoup", String.valueOf(doc));
//          <div class="song-item clearfix "><span class="checkbox-item">
//          <input type="checkbox"  class="checkbox-item-hook"  /></span>
//          <span class="index-num index-hook"  style="width: 33px;" >01</span>——id
//           <span class="song-title"  style="width: 170px;" >
//          <a href="/song/7316935" target=_blank class="" data-provider=""data-songdata='{ "id": "7316935" }'
//          title="周杰伦《钢琴恋曲101》龙卷风" data-film='null' data-info=''>龙卷风</a>——歌曲名称
            Elements songTitles = doc.select("div.song-item.clearfix");
            Elements songInfos;
            ArrayList<SearchResult> searchResults = new ArrayList<>();

            TAG:
            for (Element song : songTitles) {
                //去a连接存入songInfos
                songInfos = song.getElementsByTag("a");
                SearchResult searchResult = new SearchResult();
                for (Element info : songInfos) {
                    //收费的歌曲
                    if (info.attr("href").startsWith("http://y.baidu.com/song")) {
                        continue TAG;//收费歌曲就跳过
                    }

                    //跳转到百度音乐盒的歌曲
                    if (info.attr("href").equals("#") && !TextUtils.isEmpty(info.attr("data-songdata"))) {
                        continue TAG;//也跳过（貌似现在都是音乐盒的额。。)
                    }

                    //歌曲链接(一个都没有）。。。
                    if (info.attr("href").startsWith("/song")) {
                        searchResult.setMusicName(info.text());
                        searchResult.setUrl(info.attr("href"));
                    }

                    //歌手链接
                    if (info.attr("href").startsWith("/data")) {
                        searchResult.setArtist(info.text());
                    }

                    //专辑链接
                    if (info.attr("href").startsWith("/album")) {
                        searchResult.setAlbum(info.text().replace("《|》", ""));//如果有《|》替换为null
                    }
                }
                searchResults.add(searchResult);
            }
            System.out.println(searchResults);
            return searchResults;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnSearchResultListener {
        public void onSearchResult(ArrayList<SearchResult> searchResults);
    }

}
