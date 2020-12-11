package com.example.music;

public interface DownLoadListener {//用于对下载过程中的各种状态进行监听和回调

    void onProgress(int Progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
