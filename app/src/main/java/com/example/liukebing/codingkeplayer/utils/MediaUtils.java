package com.example.liukebing.codingkeplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.example.liukebing.codingkeplayer.R;
import com.example.liukebing.codingkeplayer.vo.Mp3Info;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Liukebing on 2016/4/22.
 */
public class MediaUtils {

    private static List<String> lstFile = new ArrayList<String>();
    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    /**
     * 根据歌曲id查询歌曲信息
     * @param context
     * @param _id
     * @return
     */
    public static Mp3Info getMp3Info(Context context, long _id) {
//        System.out.println(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media._ID + "=" + _id, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        Mp3Info mp3Info = null;
        if (cursor.moveToNext()) {
            mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {//只把音乐添加到集合当中
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
            }
        }
        cursor.close();
        return mp3Info;
    }

    /**
     * 从数据库中查询歌曲的id，保存在数组当中
     * @param context
     * @return
     */
    public static long[] getMp3InfoIds(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DURATION+">=180000",null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        long[] ids = null;
        if (cursor != null) {
            ids = new long[cursor.getCount()];
            for (int i =0;i<cursor.getCount();i++) {
                cursor.moveToNext();
                ids[i] = cursor.getLong(0);
            }
        }
        cursor.close();
        return ids;
    }

    /**
     * 从数据库中查询歌曲的信息，保存在List当中
     * @param context
     * @return
     */
    public static ArrayList<Mp3Info> getMp3Infos(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION+">=180000",null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
        for (int i = 0;i<cursor.getCount();i++) {
            cursor.moveToNext();
            Mp3Info mp3Info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {//只把音乐添加到集合当中
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
            }
            mp3Infos.add(mp3Info);
        }
        cursor.close();
        return mp3Infos;
    }

    public static List<HashMap<String, String>> getMusicMaps(List<Mp3Info> mp3Infos) {
        List<HashMap<String, String>> mp3list = new ArrayList<>();
        for (Iterator iterator = mp3Infos.iterator();iterator.hasNext();) {
            Mp3Info mp3Info = (Mp3Info) iterator.next();
            HashMap<String, String> map = new HashMap<>();
            map.put("title", mp3Info.getTitle());
            map.put("Artist", mp3Info.getArtist());
            map.put("album", mp3Info.getAlbum());
            map.put("albumId", String.valueOf(mp3Info.getAlbumId()));
            map.put("duration", formatTime(mp3Info.getDuration()));
            map.put("size", String.valueOf(mp3Info.getSize()));
            map.put("url", mp3Info.getUrl());
            mp3list.add(map);
        }
        return mp3list;
    }

    /**
     * 格式化时间，将毫秒转换为分：秒格式
     * @param duration
     * @return
     */
    public static String formatTime(long duration) {
        /**long s = duration/1000;
        String min = s / 60 + "";
        String sec = (s % 60) + "";
        //System.out.println(min+"----"+sec);
        if (min.length() < 2) {
            //System.out.println("min.length():"+min.length());
            min = "0" + min;
        } else {
            min = duration / (1000 * 60) + "";
        }
        if (sec.length() < 2) {
            //System.out.println("sec.length():"+sec.length());
            sec = "0" + sec+"";
        } else {
            //System.out.println("sec.length():"+sec.length());
            sec = sec + "";
        }
        return min+":"+sec;*/

        String min = duration / (1000 * 60) + "";
        String sec = duration % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + min;
        } else {
            min = duration / (1000 * 60) + "";
        }
        if ((sec.length() == 4)) {
            sec = "0" + (duration % (1000 * 60)) + "";
        } else if(sec.length()==3){
            sec = "00" + (duration % (1000 * 60)) + "";
        } else if(sec.length()==2){
            sec = "000" + (duration % (1000 * 60)) + "";
        } else if(sec.length()==1){
            sec = "0000" + (duration % (1000 * 60)) + "";
        }
        return min+":"+sec.trim().substring(0,2);

    }

    /**
     * 获取默认专辑图片
     * @param context
     * @param small
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {
            return BitmapFactory.decodeStream(context.getResources()
                    .openRawResource(R.mipmap.music_album), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources()
                .openRawResource(R.mipmap.music_album), null, opts);
    }

    /**
     * 从文件中获取专辑封面位图
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            //只进行大小判断
            options.inJustDecodeBounds = true;
            //调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            //我们的目标是在800pixel的画面上显示
            //所以需要调用computeSampleSize得到图片缩放比例
            options.inSampleSize = 100;
            //我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑位图
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefault
     * @param small
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id,
                                    long album_id, boolean allowdefault, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先定制原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /**
                 * 我们的目的是在你N pixel的画面上显示，所以需要调用computeSampleSize得到图片的缩放比例
                 * 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合
                 */
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                //我们得到了缩放的比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            }finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     * @param options
     * @param target
     * @return
     */
    private static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }

    /**
     * 计算文件的大小，返回相关的m字符串
     *
     * @param fileS
     * @return
     */
    public static String getFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 获取歌曲长度
     *
     * @param trackLengthAsString
     * @return
     */
    public static long getTrackLength(String trackLengthAsString) {

        if (trackLengthAsString.contains(":")) {
            String temp[] = trackLengthAsString.split(":");
            if (temp.length == 2) {
                int m = Integer.parseInt(temp[0]);// 分
                int s = Integer.parseInt(temp[1]);// 秒
                int currTime = (m * 60 + s) * 1000;
                return currTime;
            }
        }
        return 0;
    }
}
