package com.example.liukebing.codingkeplayer.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.liukebing.codingkeplayer.vo.SearchResult;
import com.lidroid.xutils.HttpUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created by Nick on 2016/4/14.
 */
public class DownloadUtils {

    private static final String DOWNLOAD_URL = "/download?_o=%2Fsearch%2Fsong";//拼接下载功能的URL
    public static final int SUCCESS_LRC = 1;//下载歌词成功
    public static final int FAILED_LRC = 2;//下载歌词失败
    public static final int SUCCESS_MP3 = 3;//下载MP3成功
    public static final int FAILED_MP3 = 4;//下载MP3失败
    private static final int GET_MP3_URL = 5;//获取MP3的URL成功
    private static final int GET_FAILED_MP3_URL = 6;//获取MP3的URL失败——收费/音乐盒等情况
    private static final int MUSIC_EXISTS = 7;//保存时，发现音乐已存在

    private static DownloadUtils sInstance;
    private onDownloadListener mListener;

    private ExecutorService mThreadPool;

    //设置监听器
    public DownloadUtils setListener(onDownloadListener mListener) {
        this.mListener = mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownloadUtils getInstance() {
        if (sInstance == null) {
             sInstance = new DownloadUtils();//单例设计模式的一个问题。。老师的代码排了一个ParserConfigurationException
        }
        return sInstance;
    }

    //老师的代码也抛了异常，ParserConfigurationException
    private DownloadUtils() {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    //自定义的下载事件监听器
    public interface onDownloadListener {

        public void onDownloadSuccess(String mp3Url);
        public void onFailed(String error);

    }

    /**
     * 下载的具体方法
     * @param searchResult
     */
    public void download(final SearchResult searchResult) {
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mListener != null) {
                            mListener.onDownloadSuccess("歌词下载成功");//Listener的回调方法
                        }
                        break;
                    case FAILED_LRC:
                        if (mListener != null) {
                            mListener.onFailed("歌词下载失败");//Listener的回调方法
                        }
                        break;
                    case GET_MP3_URL:
                        Log.i("download", String.valueOf(msg.obj));//打印了Mp3下载的地址
                        //获取URL后执行下载音乐的方法，msg.obj就是链接，通过执行getDownloadMusicURL获取到URL
                        downloadMusic(searchResult, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null) {
                            mListener.onFailed("下载失败，该歌曲为收费或VIP类型");//Listener的回调方法
                        }
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null) {
                            mListener.onDownloadSuccess(searchResult.getMusicName() + "已下载");//Listener的回调方法
                        }
                        String url = Constant.BAIDU_URL + searchResult.getUrl();//拼接下载歌词的URL
                        Log.i("download", "download lrc:" + url);
                        //音乐下载成功后下载歌词
                        downloadLRC(url, searchResult.getMusicName(), this);//下载歌词的方法
                        break;
                    case FAILED_MP3:
                        if (mListener != null) {
                            mListener.onFailed(searchResult.getMusicName() + "下载失败");//Listener的回调方法
                        }
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) {
                            mListener.onFailed("音乐已存在");//Listener的回调方法
                        }
                        break;
                }
            }
        };

        //点击歌曲时获取的URL不是能够下载的URL
        getDownloadMusicURL(searchResult, handler);
    }

    private void getDownloadMusicURL(final SearchResult searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //拼接URL,http://music.baidu.com/+song/+截取(/song/247041101)中的id+/download?_o=%2Fsearch%2Fsong
                    String url = Constant.BAIDU_URL + "/song/" +
                            searchResult.getUrl().substring(searchResult.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();

                    Elements targetElements = doc.select("a[data-btndata]");//按标签获取集合
                    if (targetElements.size() < 0) {//代表获取URL失败
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    for (Element e : targetElements) {
                        //包含mp3代表获取了能够下载的mp3链接URL
                        // 这个地方有点问题好像，遍历到第一个.mp3时，就已经结束循环了，下面的代码重复了。
                        if (e.attr("href").contains(".mp3")) {
                            String result = e.attr("href");
                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
                            msg.sendToTarget();
                            return;
                        }
                        //代表是vip歌曲，删除此项？？？这里之前搜索已经搜过了，应该已经过滤过了，为什么还要设置呢？？
                        if (e.attr("href").startsWith("/vip")) {
                            targetElements.remove(e);
                        }
                    }
                    //如果删除后集合的数目已经小于0了，就结束循环
                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    //如果有mp3的下载URL，获取第一个的链接(有标准版和高品质版本），利用handler发送
                    String result = targetElements.get(0).attr("href");
                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
                    msg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }

            }
        });
    }

    //下载音乐的具体方法
    public void downloadMusic(final SearchResult searchResult, final String url, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //创建存放mp3文件的目录
                File musicDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdirs();
                }
                String mp3Url = Constant.BAIDU_URL + url;
                String target = musicDirFile + "/" + searchResult.getMusicName() + ".mp3";//拼接文件名
                File fileTarget = new File(target);
                if (fileTarget.exists()) {//查看要下载的文件是否已存在，按照音乐名查询
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    //OkHttpClient组件，XUtils中的HttpUtils中的download()方法老师测试的文件数是0，暂时弃用
                    /*HttpUtils httpUtils = new HttpUtils();
                    httpUtils.download();*/
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3Url).build();//创建请求
                    try {
                        //发送请求，获取response
                        Response response = client.newCall(request).execute();
//                        请求成功
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();//获取mp3的所有字节
                            ps.write(bytes, 0, bytes.length);//直接写入文件
                            ps.close();//关闭打印流
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();//发送下载成功的标记
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }

    public void downloadLRC(final String url, final String musicName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements lrcTag = doc.select("div.lyric-content");//存入集合
                    //从集合中获取LRC的下载地址，有些还是没有这个地址，但没有判断。已判断。
                    String lrcURL = lrcTag.attr("data-lrclink");
                    //创建存放歌词的目录
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);
                    if (!lrcDirFile.exists()) {
                        lrcDirFile.mkdirs();//创建多级目录，因为我的方法里，之前没创建codingke_music目录
                        Log.i("lrc", String.valueOf(lrcDirFile));//查看是否创建了目录，执行了
                    }
                    if (!TextUtils.isEmpty(lrcURL)) {
                        lrcURL = Constant.BAIDU_URL + lrcURL;
                    } else {
                        handler.obtainMessage(FAILED_LRC).sendToTarget();
                        return;
                    }
                    Log.i("lrc", "lrcURL=" + lrcURL);//打印真正的下载地址
                    String target = lrcDirFile + "/" + musicName + ".lrc";//设置文件名
                    File file = new File(target);
//                    if (!file.exists()) {
//                        boolean isExist = file.createNewFile();
//                        Log.i("lrc", String.valueOf(isExist));
//                    }
                    Log.i("lrc", String.valueOf(file));//打印路径
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();
                    Response response = client.newCall(request).execute();//没有抛异常是因为使用Elements时就抛了异常
                    if (response.isSuccessful()) {
                        PrintStream ps = new PrintStream(file);//在这里创建了目录
                        byte[] bytes = response.body().bytes();
                        ps.write(bytes, 0, bytes.length);
                        ps.close();
                        handler.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                        Log.i("lrc", "response is successful");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(FAILED_LRC).sendToTarget();
                    Log.i("lrc", e.toString());//打印异常
                }
            }
        });
    }



}
