package com.example.music;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowMusic extends AppCompatActivity {
    ArrayList<String>musicName, singerName;
    ArrayList<Integer> musicId;
    private List<SingerNameandSongName> songs = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_music);
        sendRequestWithOkHttp();

        ListView listView = (ListView) findViewById(R.id.list_view2);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置省份城市的点击事件，当点击一个省份城市时，会进入到它的下一级城市列表
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent =new Intent(ShowMusic.this,PlayMusic.class);
                int songId =songs.get(position).getSongId();
                String songName=songs.get(position).getSongName();
                String singgerName=songs.get(position).getSingerName();
                intent.putExtra("position",position);
                intent.putExtra("songId",songId);//将点击的省份城市的adcode信息传过去
                intent.putExtra("songName",songName);
                intent.putExtra("singerName",singgerName);
                intent.putStringArrayListExtra("songs_nameList",musicName);
                intent.putStringArrayListExtra("singer_list",singerName);
                intent.putIntegerArrayListExtra("songs_idList",musicId);
                startActivity(intent);
            }
        });


        Button ShowMusic_return = (Button) findViewById(R.id.ShowMusic_return);
        ShowMusic_return.setOnClickListener(new View.OnClickListener() {//取消关注按钮的点击事件
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }


    //当点击查询按钮时会调用该方法
    public void sendRequestWithOkHttp() {//通过OkHttp用来从服务器端获取数据
        Intent intent = getIntent();
        String music = intent.getStringExtra("music");
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://autumnfish.cn/search?keywords=" + music).build();
                //利用传递过来的关键词music，让服务器定位去搜索该关键词对应音乐信息，得到这个关键词所对应的音乐信息
                Response response = client.newCall(request).execute();//用response对象接受服务器的响应，服务器传来的数据是JSON格式
                String responseData = Objects.requireNonNull(response.body()).string();//将JSON格式的数据转化为字符串
                //Log.d("abc", responseData);
                //Log.d("abc1", changdu);
                handleResponse(responseData);//去解析然后装入数据
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleResponse(String responseData) {
        MusicInformation musicInformation = JSON.parseObject(responseData, MusicInformation.class);//把服务器传来的JOSN数据存入到MusicInformation这个类里面，然后通过该类去解析数据
        Result musicResult = musicInformation.getResult();//通过MusicInformation类去创建MusicInformation类中的内部类
        ArrayList<Song> musicSong = musicResult.getSongs();//该列表表示歌手的歌曲，一个歌手会有多首歌曲
        musicId = new ArrayList<>();
        musicName = new ArrayList<>();
        singerName = new ArrayList<>();
        for (int i = 0; i < musicSong.size(); i++) {//由于JSON数据中"songs"是一个数组，数组里面是每一首歌，通过for循环去获得每首歌的信息
            int id = musicSong.get(i).getId();//获得每首歌但的id，将所有歌的id放在一个列表中
            String songName = musicSong.get(i).getName();//获得每首歌的歌名，将所有歌的歌名放在一个列表中
            ArrayList<Artist> artists = musicSong.get(i).getArtists();//artists是"songs"数组里面一个元素里面的一个内容，artists又是一个数组，其里面又包含歌手名字和其他内容
            String sname = artists.get(0).getName();//歌手名
            //Log.d("abc1", songName);
            //Log.d("abc", sname);
            SingerNameandSongName singerandName = new SingerNameandSongName(sname, songName,id);


            songs.add(singerandName);

            //Log.d("abc", singerandName.getSingerName());
            musicId.add(id);
            musicName.add(songName);
            singerName.add(sname);
            showMusic();
        }

    }


    public void showMusic() {//通过得到的数据去界面显示数据
        runOnUiThread(new Runnable() {//之前在是把配置适配器的内容放在onCreate方法中，但是界面一直没有显示ListView的内容，即为ListView添加的数据为空。
            @Override//通过该方法将更新UI的操作代码放在此方法里面，ListView就可以正常显示数据了，这样就可以解决上述问题。就是将更新UI的操作放在UI进程上，这样运行UI进程时，会修改对面的UI
            public void run() {
                SongAdapter adapter = new SongAdapter(ShowMusic.this, R.layout.song_view, songs);
                //Log.d("abc", "aaaa");
                ListView listView = (ListView) findViewById(R.id.list_view2);
                listView.setAdapter(adapter);//配置适配器
            }
        });
    }



}


class SingerNameandSongName {
    String singerName, songName;
    int songId;

    public SingerNameandSongName(String singerName, String songName, int songId) {
        this.singerName = singerName;
        this.songName = songName;
        this.songId = songId;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }
}


class SongAdapter extends ArrayAdapter<SingerNameandSongName> {//创建一个自定义适配器，其类型为SingerNameandSongName，此适配器用来显示所搜索到的歌曲
    private int resourceId;

    public SongAdapter(Context context, int textViewResourceId, List<SingerNameandSongName> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {//重新getView方法用来返回布局
        SingerNameandSongName s = getItem(position);//获得当前滚到屏幕类的实例，然后将这个实例加载到界面上
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView songsinger = (TextView) view.findViewById(R.id.song_singer);
        songsinger.setText(s.getSingerName());
        TextView songname = (TextView) view.findViewById(R.id.song_name);
        songname.setText(s.getSongName());
        return view;
    }
}

