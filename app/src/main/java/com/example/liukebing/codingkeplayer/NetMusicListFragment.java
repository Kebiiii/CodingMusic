package com.example.liukebing.codingkeplayer;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.liukebing.codingkeplayer.adapter.NetMusicListAdapter;
import com.example.liukebing.codingkeplayer.utils.AppUtils;
import com.example.liukebing.codingkeplayer.utils.Constant;
import com.example.liukebing.codingkeplayer.utils.SearchMusicUtils;
import com.example.liukebing.codingkeplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Liukebing on 2016/4/21.
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView listView_net_music;
    private LinearLayout load_layout,ll_search_btn_container,ll_search_container;
    private ImageButton ib_search_btn;
    private EditText et_search_content;

    private ArrayList<SearchResult> searchResults = new ArrayList<>();
    private NetMusicListAdapter netMusicListAdapter;
    private int page=1;//搜索音乐的页码,只搜第一页

    private MainActivity mainActivity;

    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;//获取上下文
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //初始化UI组件
        View view = inflater.inflate(R.layout.net_music_list_layout, null);
        listView_net_music = (ListView) view.findViewById(R.id.listView_net_music);
        load_layout = (LinearLayout) view.findViewById(R.id.load_layout);
        ll_search_btn_container = (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container = (LinearLayout) view.findViewById(R.id.ll_search_container);
        et_search_content = (EditText) view.findViewById(R.id.et_search_content);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);

        listView_net_music.setOnItemClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);

        loadNetData();//加载网络音乐
        return view;
    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);//显示界面
        //执行异步加载网络音乐的任务,传入URL作为参数
        new LoadNetDataTask().execute(Constant.BAIDU_URL + Constant.BAIDU_DAYHOT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击搜索的layout，隐藏自己，显示输入的layout
            case R.id.ll_search_btn_container:
                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //处理搜索事件
                searchMusic();
                break;
            default:
                break;
        }
    }

    //搜索音乐
    private void searchMusic() {
        //隐藏输入键盘
        AppUtils.hideInputMethod(et_search_content);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        String key = et_search_content.getText().toString();//获取文本框中的信息
        if (TextUtils.isEmpty(key)) {
            //如果为空，弹出提示，可以搜歌名，也可以搜歌手
            Toast.makeText(mainActivity, "请输入关键字", Toast.LENGTH_SHORT).show();
            return;
        }
        load_layout.setVisibility(View.VISIBLE);
        //执行搜索，异步过程，观察者设计模式，自定义
        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
            @Override
            public void onSearchResult(ArrayList<SearchResult> searchResults) {
//                System.out.println(searchResults);
                ArrayList<SearchResult> sr = netMusicListAdapter.getSearchResults();
                sr.clear();
                sr.addAll(searchResults);
                netMusicListAdapter.notifyDataSetChanged();//更新数据
                load_layout.setVisibility(View.GONE);
            }
        }).search(key, page);
    }

    private class LoadNetDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //界面加载已经做过了啊
            load_layout.setVisibility(View.VISIBLE);
            listView_net_music.setVisibility(View.GONE);
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                //使用Jsoup组件请求网络，并解析音乐数据,connect(地址),USER_AGENT——操作系统及浏览器等，timeout(超时时间），get请求
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();
//                Log.i("Jsoup", String.valueOf(doc));
                //根据标签<span>查询class为song-title查询歌曲，存到集合Elements中
//              <span class="song-title " style='width: 240px;'>
//              <a href="/song/121353608" target="_blank" title="刘珂矣半壶纱" data-film="null">半壶纱</a></span>
//              <span class="singer"  style="width: 240px;" >
//              <span class="author_list" title="刘珂矣">
//              <a hidefocus="true" href="/artist/132632388">刘珂矣</a></span>
                Elements songTitles = doc.select("span.song-title");//获取所有song-title标签对应的信息，存入songTitle集合
//               <span class="author_list" title="刘珂矣">
//               <a hidefocus="true" href="/artist/132632388">刘珂矣</a></span>
                Elements artists = doc.select("span.author_list");//获取所有author_list标签对应的信息，存入artists集合
                for (int i = 0; i < songTitles.size(); i++) {
                    SearchResult searchResult = new SearchResult();
                    //查询标签为"a"的元素存入集合
                    Elements urls = songTitles.get(i).getElementsByTag("a");
                    //从urls集合中的0位置取出键为href对应的值，即url——"/song/121353608"，设置url
                    searchResult.setUrl(urls.get(0).attr("href"));
                    //获取0位置对应的text信息——半壶纱，设置歌名
                    searchResult.setMusicName(urls.get(0).text());
                    //获取第i首歌的a标签对应信息
                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    //获取第i首歌的a标签下的artist信息——刘珂矣
                    searchResult.setArtist(artistElements.get(0).text());
                    //设置专辑为热歌榜
                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);//添加到列表中
                }
                Log.i("Jsoup", String.valueOf(searchResults));
                Log.i("Jsoup", String.valueOf(searchResults.size()));//查询到92首歌。。。
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                netMusicListAdapter = new NetMusicListAdapter(mainActivity, searchResults);
                listView_net_music.setAdapter(netMusicListAdapter);
                listView_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout, null));
            } else {
                Toast.makeText(mainActivity, "查询失败", Toast.LENGTH_SHORT).show();
            }
            load_layout.setVisibility(View.GONE);//把进度条隐藏
            listView_net_music.setVisibility(View.VISIBLE);//显示ListView
        }
    }

    //列表项的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= netMusicListAdapter.getSearchResults().size() || position < 0) {
            return;
        }
        //弹出对话框
        showDownloadDialog(position);
    }

    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(searchResults.get(position));
        downloadDialogFragment.show(getFragmentManager(), "download");
    }
}
