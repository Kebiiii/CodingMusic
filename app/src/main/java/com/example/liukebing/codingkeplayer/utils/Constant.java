package com.example.liukebing.codingkeplayer.utils;

/**
 * Created by Liukebing on 2016/4/24.
 */
public class Constant {

    public static String SP_NAME = "codingMusic";//SharedPreference名称
    public static final String DB_NAME = "CodingPlayer.db";//数据库名
    public static final int PLAY_RECORD_NUM = 5;//播放记录的最大值

    //百度音乐网页地址URL
    public static final String BAIDU_URL = "http://music.baidu.com" ;
    //热歌榜
    public static final String BAIDU_DAYHOT ="/top/dayhot/?pst=shouyeTop" ;
    //搜索的地址
    public static final String BAIDU_SEARCH = "/search/song";
    //利用tools中的userAgent工具获取当前系统版本、浏览器等信息。
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36";

    //成功标记
    public static final int SUCCESS=1;
    //失败标记
    public static final int FAILED = 2;

    public static final String DIR_MUSIC = "/codingke_music";
    public static final String DIR_LRC = "/codingke_music/lrc";
}
