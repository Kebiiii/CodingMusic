package com.example.liukebing.codingkeplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.example.liukebing.codingkeplayer.utils.DownloadUtils;
import com.example.liukebing.codingkeplayer.vo.SearchResult;

import java.io.File;

/**
 * Created by Nick on 2016/4/14.
 */
public class DownloadDialogFragment extends DialogFragment {

    private SearchResult searchResult;//表示当前要下载的歌曲对象。
    private MainActivity mainActivity;

    //实例化方法
    public static DownloadDialogFragment newInstance(SearchResult searchResult) {
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.searchResult = searchResult;
        return downloadDialogFragment;
    }

    private String[] items;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) getActivity();
        //设置列表项
        items = new String[]{getString(R.string.download), getString(R.string.cancel)};
    }

    //创建对话框的方法
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //执行下载
                        downloadMusic();
                        break;
                    case 1://取消
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
        return builder.show();

    }

    //下载音乐
    private void downloadMusic() {
        Toast.makeText(mainActivity, "正在下载：" + searchResult.getMusicName(), Toast.LENGTH_SHORT).show();
        DownloadUtils.getInstance().setListener(new DownloadUtils.onDownloadListener() {

            @Override
            public void onDownloadSuccess(String mp3Url) {//下载成功的回调方法
                Toast.makeText(mainActivity, mp3Url, Toast.LENGTH_SHORT).show();
                //扫描新下载的歌曲
                Uri contentUri = Uri.fromFile(new File(mp3Url));
            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(mainActivity, error, Toast.LENGTH_SHORT).show();
            }
        }).download(searchResult);

    }
}
