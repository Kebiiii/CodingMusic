package com.example.liukebing.codingkeplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liukebing.codingkeplayer.adapter.MyPagerAdapter;
import com.example.liukebing.codingkeplayer.utils.Constant;
import com.example.liukebing.codingkeplayer.utils.DownloadUtils;
import com.example.liukebing.codingkeplayer.utils.MediaUtils;
import com.example.liukebing.codingkeplayer.utils.SearchMusicUtils;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;
import com.example.liukebing.codingkeplayer.vo.SearchResult;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.view.DefaultLrcBuilder;
import com.view.ILrcBuilder;
import com.view.ILrcView;
import com.view.LrcRow;
import com.view.LrcView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,ViewPager.OnPageChangeListener {

    private TextView textView1_title, textView1_start_time, textView1_end_time;
    private ImageView imageView_favorite,imageView1_album, imageView_play_mode, imageView1_next, imageView_play_pause, imageView_prev;
    private SeekBar seekBar;
    private ViewPager viewPager;
    private LrcView lrc_view;

    private ArrayList<View> views = new ArrayList<>();
    //private ArrayList<Mp3Info> mp3Infos;

    private static final int UPDATE_TIME = 0x3;//更新播放时间的标记
    private static final int UPDATE_LRC = 0x4;//设置更新时间的标记

    private BaseApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (BaseApplication) getApplication();

        textView1_start_time = (TextView) findViewById(R.id.textView1_start_time);
        textView1_end_time = (TextView) findViewById(R.id.textView1_end_time);

        imageView_favorite = (ImageView) findViewById(R.id.imageView_favorite);
        imageView_play_mode = (ImageView) findViewById(R.id.imageView_play_mode);
        imageView1_next = (ImageView) findViewById(R.id.imageView1_next);
        imageView_play_pause = (ImageView) findViewById(R.id.imageView_play_pause);
        imageView_prev = (ImageView) findViewById(R.id.imageView_prev);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        initViewPager();

        //注册事件
        imageView_play_mode.setOnClickListener(this);
        imageView1_next.setOnClickListener(this);
        imageView_play_pause.setOnClickListener(this);
        imageView_prev.setOnClickListener(this);
        imageView_favorite.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        viewPager.setOnPageChangeListener(this);


        //mp3Infos = MediaUtils.getMp3Infos(this);
        //bindPlayService();

        myHandler = new MyHandler(this);

//        isPause = getIntent().getBooleanExtra("isPause", false);
//        position = getIntent().getIntExtra("position",0);
//        change(position);
    }

    //把两个viewPager页面添加到集合里
    private void initViewPager() {
        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        imageView1_album = (ImageView) album_image_layout.findViewById(R.id.imageView1_album);
        textView1_title = (TextView) album_image_layout.findViewById(R.id.textView1_title);
        views.add(album_image_layout);
        View lrc_layout = getLayoutInflater().inflate(R.layout.lrc_layout, null);
        lrc_view = (LrcView) lrc_layout.findViewById(R.id.lrc_view);//初始化歌词组件
        //设置滚动事件
        lrc_view.setListener(new ILrcView.LrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playService.isPlaying()) {
                    playService.seekTo((int) row.time);//seekTo方法的参数是msec毫秒时间
                }
            }
        });
        lrc_view.setLoadingTipText("正在加载歌词");
        lrc_view.setBackgroundResource(R.mipmap.jb_bg);//设置背景图
        lrc_view.getBackground().setAlpha(150);//设置透明度
        views.add(lrc_layout);
        viewPager.setAdapter(new MyPagerAdapter(views));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();//绑定服务，是一个异步的过程，绑定未成功之前，不能调用change()方法
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindPlayService();
    }

    private static MyHandler myHandler;//先声明再赋值，原因静态变量

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (playService.isPlaying()) {
                playService.pause();//先暂停
                playService.seekTo(progress);
                playService.start();//继续播放
            } else {
                playService.seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    static class MyHandler extends Handler {

        private MusicPlayActivity playActivity;

        public MyHandler(MusicPlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView1_start_time.setText(MediaUtils.formatTime((int)msg.obj));
                        break;
                    case UPDATE_LRC:
                        playActivity.lrc_view.seekLrcToTime((int) msg.obj);
                        break;

                    case DownloadUtils.SUCCESS_LRC://下载成功就加载歌词
                        playActivity.loadLRC(new File((String) msg.obj));
                        break;
                    case DownloadUtils.FAILED_LRC://下载失败弹出提示
                        Toast.makeText(playActivity, "歌词下载失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void publish(int progress) {
        seekBar.setProgress(progress);//更新进度，seekBar可以，内部组件已经做过处理
//        Message msg = myHandler.obtainMessage(UPDATE_TIME);
//        msg.arg1 = progress;
//        myHandler.sendMessage(msg);
        myHandler.obtainMessage(UPDATE_TIME,progress).sendToTarget();//发送携带progress的msg，更新显示时间
        myHandler.obtainMessage(UPDATE_LRC,progress).sendToTarget();//更新歌词
        //textView1_start_time.setText(MediaUtils.formatTime(progress));
    }


    //更新本Activity的UI界面，在绑定服务的时候会执行此方法
    @Override
    public void change(int position) {
//        if (this.playService.isPlaying()) {
        Mp3Info mp3Info = playService.getMp3Infos().get(position);
        textView1_title.setText(mp3Info.getTitle());
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        imageView1_album.setImageBitmap(albumBitmap);
        textView1_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar.setProgress(0);
        seekBar.setMax((int) mp3Info.getDuration());
        //按钮根据播放状态确定
        if (playService.isPlaying()) {
            imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
        } else {
            imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
        }
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.order);
                imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.random);
                imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.single);
                imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;
        }

        //初始化收藏状态
        try {
            Mp3Info likeMp3Info;
            if (playService.getChangePlayList() == PlayService.MY_MUSIC_LIST) {
                likeMp3Info = BaseApplication.dbUtils.
                        findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
            } else {
                likeMp3Info = BaseApplication.dbUtils.
                        findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            }
            if (likeMp3Info != null) {
                if (likeMp3Info.getIsLike() == 1) {
                    imageView_favorite.setImageResource(R.mipmap.xin_hong);
                } else {
                    imageView_favorite.setImageResource(R.mipmap.xin_bai);
                }
            } else {
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
//        }

        //歌词界面的更新，滑动到此界面时界面就会更新么？为什么会调用change()方法
        String songName = mp3Info.getTitle();//获取歌名
        String artist = mp3Info.getArtist();//获取歌手
        Log.i("lrc", songName + " " + artist);
        String lrcPath = Environment.getExternalStorageDirectory() + Constant.DIR_LRC + "/" + songName + ".lrc";//拼接本地路径
        File lrcFile = new File(lrcPath);
        Log.i("lrc", String.valueOf(lrcFile));//查看是否创建了file
        if (!lrcFile.exists()) {
            //如果歌词文件不存在，下载歌词
            SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
                @Override
                public void onSearchResult(ArrayList<SearchResult> searchResults) {
                    if (searchResults.size() > 0) {
                        SearchResult searchResult = searchResults.get(0);//获取了search的结果
                        String url = Constant.BAIDU_URL + searchResult.getUrl();//拼接下载歌词的URL，好像多了一个"/";
                        Log.i("lrc", url);
                        //调用下载歌词的方法
                        DownloadUtils.getInstance().downloadLRC(url, searchResult.getMusicName(), myHandler);
                    }
                }
            }).search(songName + " " + artist, 1);//用歌名+空格+歌手发起搜索，搜到这首歌
            //执行完下载之后怎么调用到这个方法呢？已经通过handler发送了，然后就执行了。
        } else {
            loadLRC(lrcFile);//如果歌词存在，加载；
        }
    }

    private void loadLRC(File lrcFile) {
        StringBuffer buf = new StringBuffer(1024 * 10);//用于存储歌词
        char[] chars = new char[1024];//用于缓存读取流读到的歌词
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile)));
            int len = -1;
            while ((len = in.read(chars)) != -1) {
                buf.append(chars, 0, len);//把读取的歌词添加到buf中
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ILrcBuilder builder = new DefaultLrcBuilder();//获取构建歌词的builder实例
        List<LrcRow> rows = builder.getLrcRows(buf.toString());//把歌词按行存入集合
        lrc_view.setLrc(rows);//给lrcView初始化数据
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageView_play_pause:
                if (playService.isPlaying()) {
                    imageView_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        //因为play方法已经通过回调onChange()方法调用了changeUIStatusOnPlay()方法，所以只需在调用start时更新按钮即可
                        imageView_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();
                    } else {
                        //暂定从头播放（因为这种情况只有第一次打开播放器会发生，后续需要存储播放状态）,已经修改为从保存的曲目播放
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.imageView1_next:
                playService.next();
                break;
            case R.id.imageView_prev:
                playService.prev();
                break;
            //给play_mode图标添加点击事件，既要更新图标，又要更改playService中的播放模式
            case R.id.imageView_play_mode:
                int mode = (int) imageView_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.random);
                        imageView_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(MusicPlayActivity.this,getString(R.string.random_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.single);
                        imageView_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(MusicPlayActivity.this,getString(R.string.single_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.order);
                        imageView_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(MusicPlayActivity.this,getString(R.string.order_play),Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            /**
            case R.id.imageView_favorite:
                Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
                try {
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3Info","=",getId(mp3Info)));
                    System.out.println("likeMp3Info"+likeMp3Info);
                    if (likeMp3Info == null) {
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        app.dbUtils.save(mp3Info);
                        System.out.println("save");

                        //改变按钮的状态
                        imageView_favorite.setImageResource(R.mipmap.xin_hong);
                    } else {
                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            imageView_favorite.setImageResource(R.mipmap.xin_bai);
                        } else {
                            likeMp3Info.setIsLike(1);
                            imageView_favorite.setImageResource(R.mipmap.xin_hong);
                        }
                        //app.dbUtils.deleteById(Mp3Info.class, likeMp3Info.getId());
                        System.out.println("update");

                        app.dbUtils.update(likeMp3Info,"isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;*/
            case R.id.imageView_favorite:
                //判断是否存在在收藏列表中，是，取消，否添加；又收藏功能在其他Activity中可能用到，声明为全局变量。
                Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());//获取当前播放音乐的信息
                Log.i("favorite0", mp3Info.toString());
                try {
                    //根据原id==mp3InfoId查询此歌曲是否已收藏（收藏后id字段会被自动覆盖，查询时把原id保存到mp3InfoId中）
                    //为了同步收藏列表和原始播放列表，使收藏列表在播放时能够同步UI，目前改为用mp3InfoId查询
                    //当从收藏列表查询时，自然没有问题，因为歌曲的mp3InfoId已经重新赋值；
                    //当对未存入列表的歌查询时，mp3InfoID目前是空，一定查不到，也是正常的
                    //另外之前匹配时是根据ID查看当前播放的音乐是否已存入收藏列表，已存入的id=mp3InfoId，未存入的遍历后id!=mp3InfoId
                    // 而目前改为mp3InfoID查询也能达到这效果，因为已存入的mp3InfoID自然有这个字段且已经赋值，未存入的就是0
                    Mp3Info likeMp3Info = BaseApplication.dbUtils.
                            findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
                    Log.i("favorite1", String.valueOf(likeMp3Info));
                    //每次点击图标时，如果实在播放收藏列表中的歌曲，应该更新playService中的播放列表，修改获取id的方法后
//                    if (likeMp3Info == null) {
//                        mp3Info.setMp3InfoId(mp3Info.getId());//未查询到，把原id保存到mp3InfoId中
//                        mp3Info.setIsLike(1);//标记已收藏
//                        BaseApplication.dbUtils.save(mp3Info);
//                        Log.i("favorite2", "save");
//                        imageView1_favorite.setImageResource(R.mipmap.xin_hong);
//                    } else {
                    //老师的代码，感觉有点多余
                    if (likeMp3Info != null) {
                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            imageView_favorite.setImageResource(R.mipmap.xin_bai);
                            //查询到，根据id删除likeMp3Info。老师的代码删掉了这行
//                            BaseApplication.dbUtils.deleteById(Mp3Info.class, likeMp3Info.getId());
                            Log.i("favorite3", "delete");
                        } else {
                            likeMp3Info.setIsLike(1);
                            imageView_favorite.setImageResource(R.mipmap.xin_hong);
                        }
                        BaseApplication.dbUtils.update(likeMp3Info, "isLike");//更新字段，作用？能够更新列表么？
                        //如果当前播放列表是收藏列表，更新播放列表，但是这样做了之后，再从本地点击音乐，又出问题。
                        if (playService.getChangePlayList() == PlayService.LIKE_MUSIC_LIST) {
                            List<Mp3Info> list = BaseApplication.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", "1"));
                            if (list == null || list.size() == 0) {
                                return;
                            }
                            playService.setMp3Infos((ArrayList<Mp3Info>) list);
                        }
                    }
//                        mp3Info.setIsLike(0);//感觉就在这设为0就可以
//                        imageView1_favorite.setImageResource(R.mipmap.xin_bai);

                    //这句代码还是有问题，如果直接更新列表的话，播放完这一首，再播放下一首时，回跳到第三首，因为列表改变了，对应的2成了3
//                        if (playService.getChangePlayList() == PlayService.FAVORTIE_MUSIC_LIST) {
//                            //既要更新播放列表，又要更新position还有Adapter
//                            playService.setMp3Infos((ArrayList<Mp3Info>) BaseApplication.dbUtils.findAll(Mp3Info.class));
//                        }
//                        playService.getMp3Infos().notify();

                } catch (DbException e) {
                    Log.i("favorite4", e.toString());
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }
    }

    private long getId(Mp3Info mp3Info) {
        //初始收藏状态
        long id = 0;
        switch (playService.getChangePlayList()) {
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }
}
