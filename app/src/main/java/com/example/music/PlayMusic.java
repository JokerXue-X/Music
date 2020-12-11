package com.example.music;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PlayMusic extends AppCompatActivity implements View.OnClickListener {

    TextView tv_song, MusicEndTime, MusicBeginTime, SongName, Singer;
    ImageButton BeforeSong, MusicPlay, NextSong, ib_back;
    SeekBar MusicProgress;
    ImageView Album;

    private String url1;//这首歌的URL
    private String songName;//
    private String singerName;
    private String FileName;
    private int position;
    private int songId;
    private boolean isLike;

    ArrayList<Integer> id_list = new ArrayList<>();
    ArrayList<String> songName_list = new ArrayList<String>();
    ArrayList<String> singer_list = new ArrayList<String>();


    private MediaPlayer mediaPlayer = new MediaPlayer();//实例化mediaplayer，系统自带的类，用来播放音频文件
    private DownLoadService.DownloadBinder downloadBinder;//服务与活动间的通信，用来进行下载
    private ServiceConnection connection = new ServiceConnection() {//ServiceConnection匿名类，
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownLoadService.DownloadBinder) service;//获取downloadBinder实例，用于在活动中调用服务提供的各种方法
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    Timer timer;
    TimerTask timerTask;
    Boolean isChanged = false;
    private long currentPosition = 0;


    ImageButton MusicDownLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        SongName = findViewById(R.id.song_name);
        Singer = findViewById(R.id.artist);
        MusicEndTime = (TextView) findViewById(R.id.MusicEnd);
        MusicBeginTime = (TextView) findViewById(R.id.MusicBegin);
        BeforeSong = (ImageButton) findViewById(R.id.MusicPrevious);
        MusicPlay = (ImageButton) findViewById(R.id.MusicPlay);
        MusicPlay.setBackgroundResource(R.drawable.play);
        NextSong = (ImageButton) findViewById(R.id.ib_next);


        MusicProgress = (SeekBar) findViewById(R.id.MusicProgress);
        MusicDownLoad = (ImageButton) findViewById(R.id.MusicDownLoad);
        Album = (ImageView) findViewById(R.id.iv_disk);

        //设置点击事件
        BeforeSong.setOnClickListener(this);
        MusicPlay.setOnClickListener(this);
        NextSong.setOnClickListener(this);
        MusicDownLoad.setOnClickListener(this);
        //seekebar的监听事件
        MusicProgress.setOnSeekBarChangeListener(new MySeekBar());

        //得到下载链接
        Intent intent = this.getIntent();
        songName = intent.getStringExtra("songName");//设置播放界面的音乐名
        singerName = intent.getStringExtra("singerName");
        songId = intent.getIntExtra("songId", 0);
        FileName = songId + ".mp3";//文件就是歌曲id+.mp3
        position = intent.getIntExtra("position", 0);
        songName_list = intent.getStringArrayListExtra("songs_nameList");
        singer_list=intent.getStringArrayListExtra("singer_list");



        id_list = intent.getIntegerArrayListExtra("songs_idList");

        //打印验证

        try {
            url1 = getUrlById(songId);//通过歌曲的id利用getUrlById()此方法去得到该歌曲的URL，因为下载歌曲到本地需要URL
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        InitData();
        Intent intent1 = new Intent(PlayMusic.this, DownLoadService.class);
        startService(intent1);//启动服务
        bindService(intent1, connection, BIND_AUTO_CREATE);//绑定服务
        if (ContextCompat.checkSelfPermission(PlayMusic.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlayMusic.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();//初始化MediaPlayer

        }


        MusicEndTime.setText(formatime(mediaPlayer.getDuration()));//总时间
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isChanged) {
                    return;
                }
                currentPosition = mediaPlayer.getCurrentPosition();
                MusicProgress.setProgress(mediaPlayer.getCurrentPosition());//设置进度
                showCurrentTime();
            }
        };
        timer.schedule(timerTask, 0, 10);

    }

    @Override
    public void onClick(View v) {
        int id = 0;
        if (downloadBinder == null) {
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FileName);//通过歌名去本地文件查找是否有该歌曲的文件
        Log.d("DDD", file.getPath() + "\n" + url1 + "\n" + FileName);
        switch (v.getId()) {
            case R.id.MusicDownLoad:
                if (!file.exists()) {//如果本地没有该歌曲的文件，则去下载该歌曲
                    Log.d("DDD", "kaishixiazai");
                    downloadBinder.startDownload(url1, FileName);
                } else {
                    Toast.makeText(PlayMusic.this, "该歌曲已经下载！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.MusicPlay://播放按钮的点击事件
                if (file.exists()) {//文件存在，则可以播放
                    if (!mediaPlayer.isPlaying()) {//判断当前是否在播放
                        initMediaPlayer();//刷新界面
                        mediaPlayer.start();//开始播放
                        MusicPlay.setBackgroundResource(R.drawable.pause);
                        Toast.makeText(PlayMusic.this, "开始播放！", Toast.LENGTH_SHORT).show();
                    } else {
                        mediaPlayer.pause();//如果正在播放，在点击播放按钮时，则暂停播放
                        MusicPlay.setBackgroundResource(R.drawable.play);
                        Toast.makeText(PlayMusic.this, "暂停播放！", Toast.LENGTH_SHORT).show();
                    }
                } else {//文件不存在，则提示
                    Toast.makeText(PlayMusic.this, "该歌曲未下载！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.MusicPrevious:
                mediaPlayer.reset();//音乐重置
                MusicPlay.setBackgroundResource(R.drawable.play);

                position = songName_list.indexOf(songName);//得到该歌曲的位置
                //得到音乐文件名
                try {
                    if (position == 0) {//判断该歌是否为第一首歌
                        Toast.makeText(PlayMusic.this, "该歌是第一首歌,没有上一首了", Toast.LENGTH_LONG).show();
                    } else {
                        songName = songName_list.get(position - 1);//将歌名设置为上一首歌的歌名
                        singerName = singer_list.get(position - 1);//歌手同理
                        id = id_list.get(position - 1);//还要将id变为上一首的id
                        url1 = getUrlById(id);//通过id去获得上一首歌曲的URL
                        //Log.d("PPP2", song1 + ":" + id + ":" + url1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                FileName = id + ".mp3";
                InitData();//修改UI界面的信息
                initMediaPlayer();//初始化MediaPlayer
                break;
            case R.id.ib_next://下一首歌曲同上一首，只不过position应该加1
                mediaPlayer.reset();
                MusicPlay.setBackgroundResource(R.drawable.play);

                position = songName_list.indexOf(songName);
                try {
                    if (position == songName_list.size() - 1) {//判断是否为最后一首
                        Toast.makeText(PlayMusic.this, "该歌是最后一首,没有下一首了", Toast.LENGTH_LONG).show();
                    } else {
                        songName = songName_list.get(position + 1);
                        singerName = singer_list.get(position + 1);
                        id = id_list.get(position + 1);
                        url1 = getUrlById(id);
                        //Log.d("PP4", songName + ":" + id + ":" + url1);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                FileName = id + ".mp3";
                InitData();
                initMediaPlayer();//初始化MediaPlayer
                break;
        }
    }

    //设置UI界面的显示数据
    private void InitData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SongName.setText(songName);//修改歌曲名
                Singer.setText(singerName);//修改歌手


                MusicBeginTime.setText("00:00");
            }
        });
    }

    //初始化音乐播放器
    private void initMediaPlayer() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FileName);
            Log.d("MMM", file.getPath());
            mediaPlayer.setDataSource(file.getPath());//指定音频文件的路径
            mediaPlayer.prepare();//让mediaPlayer进入到准备状态
            MusicProgress.setMax(mediaPlayer.getDuration());//设置进度条的最大值
            MusicEndTime.setText(formatime(mediaPlayer.getDuration()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUrlById(int id) throws InterruptedException {
        UrlThread urlThread = new UrlThread(id);//不能把获得URL的操作代码放在main里(主线程),应该新建一个获得URL的进程，并将代码写在此URL进程里面，不然无法得到URL
        Thread thread = new Thread(urlThread);//通过此获取URL进程去获取URL
        thread.start();//让进程运行
        thread.join();

        return urlThread.getUrl();
    }

    //更新播放的时间
    private void showCurrentTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MusicBeginTime.setText(formatime(currentPosition));
            }
        });
    }

    //时间转换类，将得到的音乐时间毫秒转换为时分秒格式
    private String formatime(long length) {
        Date date = new Date(length);
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String totaltime = sdf.format(date);
        return totaltime;
    }

    class MySeekBar implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanged = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            isChanged = false;
        }
    }

    //权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(PlayMusic.this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    initMediaPlayer();
                }
                break;
            default:
        }
    }

    //若活动销毁则对服务进行解绑
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
        }

    }

}


class UrlThread implements Runnable {//URL进程，用来获取歌曲的URL
    private final int songId;
    private String url;

    public UrlThread(int songId) {
        this.songId = songId;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void run() {//当进程运行时即start()方法，自动执行run方法
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://autumnfish.cn/song/url?id=" + songId)
                    .build();//通过歌曲id去访问服务器并获得服务器的响应request

            Response response = client.newCall(request).execute();
            String responseData = Objects.requireNonNull(response.body()).string();//将服务器的响应转化为JSON格式的字符串
            SongURL songURL = JSON.parseObject(responseData, SongURL.class);//然后借助SongURL类去解析JSON格式的字符串
            ArrayList<SongURL.Data> dataArrayList = songURL.getData();
            url = dataArrayList.get(0).getUrl();//获得解析数据中的URL
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}










