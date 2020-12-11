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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    Button button_show;
    EditText text;
    private List<SingerNameandSongName> Localsongs = new Vector<>();
    ArrayList<String> LocalmusicName =new ArrayList<>();
    ArrayList<String> Localsinger =new ArrayList<>();
    ArrayList<Integer> LocalmusicId=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button_show = (Button) findViewById(R.id.button_show);
        text = (EditText) findViewById(R.id.text);
        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String music = text.getText().toString();//获得输入的城市id值
                Intent intent = new Intent(MainActivity.this, ShowMusic.class);
                intent.putExtra("music", music);//将输入的城市id传递到下一个活动
                startActivity(intent);
                //点击添加按钮之后就会跳转到AddActivi活动来填写作者和日记内容
            }
        });

        SingerNameandSongName singerandName1 = new SingerNameandSongName("陈奕迅", "明年今日 (2007 Live)", 65312);
        SingerNameandSongName singerandName2 = new SingerNameandSongName("陈奕迅", "是但求其爱", 1496602290);
        SingerNameandSongName singerandName3 = new SingerNameandSongName("陈奕迅", "陀飞轮(Live)", 64496);
        SingerNameandSongName singerandName4 = new SingerNameandSongName("TaylorSwift", "LoveStory", 26896129);
        SingerNameandSongName singerandName5 = new SingerNameandSongName("TaylorSwift", "Welcome To New York", 33337002);
        SingerNameandSongName singerandName6 = new SingerNameandSongName("薛之谦", "天外来物", 1463165983);
        SingerNameandSongName singerandName7 = new SingerNameandSongName("薛之谦", "刚刚好", 415792881);
        SingerNameandSongName singerandName8 = new SingerNameandSongName("薛之谦", "我好像在哪见过你", 417859631);
        Localsongs.add(singerandName1);
        Localsongs.add(singerandName2);
        Localsongs.add(singerandName3);
        Localsongs.add(singerandName4);
        Localsongs.add(singerandName5);
        Localsongs.add(singerandName6);
        Localsongs.add(singerandName7);
        Localsongs.add(singerandName8);

        LocalmusicId.add(singerandName1.getSongId());
        LocalmusicId.add(singerandName2.getSongId());
        LocalmusicId.add(singerandName3.getSongId());
        LocalmusicId.add(singerandName4.getSongId());
        LocalmusicId.add(singerandName5.getSongId());
        LocalmusicId.add(singerandName6.getSongId());
        LocalmusicId.add(singerandName7.getSongId());
        LocalmusicId.add(singerandName8.getSongId());

        LocalmusicName.add(singerandName1.getSongName());
        LocalmusicName.add(singerandName2.getSongName());
        LocalmusicName.add(singerandName3.getSongName());
        LocalmusicName.add(singerandName4.getSongName());
        LocalmusicName.add(singerandName5.getSongName());
        LocalmusicName.add(singerandName6.getSongName());
        LocalmusicName.add(singerandName7.getSongName());
        LocalmusicName.add(singerandName8.getSongName());

        Localsinger.add(singerandName1.getSingerName());
        Localsinger.add(singerandName2.getSingerName());
        Localsinger.add(singerandName3.getSingerName());
        Localsinger.add(singerandName4.getSingerName());
        Localsinger.add(singerandName5.getSingerName());
        Localsinger.add(singerandName6.getSingerName());
        Localsinger.add(singerandName7.getSingerName());
        Localsinger.add(singerandName8.getSingerName());




        SongAdapter adapter = new SongAdapter(MainActivity.this, R.layout.song_view, Localsongs);//创建自定义的适配器，其中ListView的子布局为city_view，其中的数据为city列表的数据
        //Log.d("abc", "aaaa");
        ListView listView = (ListView) findViewById(R.id.list_view1);
        listView.setAdapter(adapter);//配置适配器

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置省份城市的点击事件，当点击一个省份城市时，会进入到它的下一级城市列表
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlayMusic.class);
                int songId = Localsongs.get(position).getSongId();
                String songName = Localsongs.get(position).getSongName();
                String singgerName = Localsongs.get(position).getSingerName();
                intent.putExtra("position", position);
                intent.putExtra("songId", songId);//将点击的省份城市的adcode信息传过去
                intent.putExtra("songName", songName);
                intent.putExtra("singerName", singgerName);
                intent.putStringArrayListExtra("songs_nameList", LocalmusicName);
                intent.putStringArrayListExtra("singer_list", Localsinger);
                intent.putIntegerArrayListExtra("songs_idList", LocalmusicId);
                startActivity(intent);
            }
        });


    }

}


class LocalSingerNameandSongName {
    String singerName, songName;
    int songId;

    public LocalSingerNameandSongName(String singerName, String songName, int songId) {
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


class LocalSongAdapter extends ArrayAdapter<LocalSingerNameandSongName> {//创建一个自定义适配器，其类型为SingerNameandSongName，此适配器用来显示所搜索到的歌曲
    private int resourceId;

    public LocalSongAdapter(Context context, int textViewResourceId, List<LocalSingerNameandSongName> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {//重新getView方法用来返回布局
        LocalSingerNameandSongName s = getItem(position);//获得当前滚到屏幕类的实例，然后将这个实例加载到界面上
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView songsinger = (TextView) view.findViewById(R.id.song_singer);
        songsinger.setText(s.getSingerName());
        TextView songname = (TextView) view.findViewById(R.id.song_name);
        songname.setText(s.getSongName());
        return view;
    }
}
