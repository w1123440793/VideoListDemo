package cn.demo.videolist;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import cn.demo.videolist.media.IjkVideoView;
import cn.demo.videolist.media.VideoAdapter;
//import io.vov.vitamio.Vitamio;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {

    private RecyclerView videoList;
    private LinearLayoutManager mLayoutManager;
    private VideoAdapter adapter;
    private FrameLayout videoLayout;

    private int postion = -1;
    private int lastPostion=-1;
    private Context context;
    private VideoPlayView videoItemView;

    private FrameLayout fullScreen;
    private VideoListData listData;
    private RelativeLayout smallLayout;
    private ImageView close;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        /*if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) {
            Log.e("tag", "checkVitamioLibs");
            return;
        }*/

        context=this;
//        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_main);
        mLayoutManager = new LinearLayoutManager(this);
        videoList= (RecyclerView) findViewById(R.id.video_list);
        videoList.setLayoutManager(mLayoutManager);
        adapter=new VideoAdapter(this);
        videoList.setAdapter(adapter);
        fullScreen= (FrameLayout) findViewById(R.id.full_screen);
        videoLayout = (FrameLayout) findViewById(R.id.layout_video);

        videoItemView = new VideoPlayView(context);
        String data = readTextFileFromRawResourceId(this, R.raw.video_list);
        listData = new Gson().fromJson(data, VideoListData.class);
        adapter.refresh(listData.getList());
        smallLayout= (RelativeLayout) findViewById(R.id.small_layout);

        close= (ImageView) findViewById(R.id.close);
        initActions();
    }

    private void initActions(){

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoItemView.isPlay()){
                    videoItemView.pause();
                    smallLayout.setVisibility(View.GONE);
                }
            }
        });

        smallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smallLayout.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });
        videoItemView.setCompletionListener(new VideoPlayView.CompletionListener() {
            @Override
            public void completion(IMediaPlayer mp) {

                //播放完还原播放界面
                if (smallLayout.getVisibility() == View.VISIBLE) {
                    videoLayout.removeAllViews();
                    smallLayout.setVisibility(View.GONE);
                    videoItemView.setShowContoller(true);
                }

                FrameLayout frameLayout = (FrameLayout) videoItemView.getParent();
                videoItemView.release();
                if (frameLayout != null && frameLayout.getChildCount() > 0) {
                    frameLayout.removeAllViews();
                    View itemView = (View) frameLayout.getParent();

                    if (itemView != null) {
                        itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }
                }
                lastPostion = -1;
            }
        });

        adapter.setClick(new VideoAdapter.onClick() {
            @Override
            public void onclick(int position) {
                MainActivity.this.postion = position;

                if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED){
                if (position!=lastPostion) {

                    videoItemView.stop();
                    videoItemView.release();
                }
            }

            if(smallLayout.getVisibility()==View.VISIBLE)

            {
                smallLayout.setVisibility(View.GONE);
                videoLayout.removeAllViews();
                videoItemView.setShowContoller(true);
            }

            if(lastPostion!=-1)

            {
                ViewGroup last = (ViewGroup) videoItemView.getParent();//找到videoitemview的父类，然后remove
                if (last != null) {
                    last.removeAllViews();
                    View itemView = (View) last.getParent();
                    if (itemView != null) {
                        itemView.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                    }
                }
            }

            View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
            frameLayout.removeAllViews();
            frameLayout.addView(videoItemView);
            videoItemView.start(listData.getList().get(position).getMp4_url());
            lastPostion=position;
        }
    });

        videoList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int index = videoList.getChildAdapterPosition(view);
                view.findViewById(R.id.showview).setVisibility(View.VISIBLE);
                if (index == postion) {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    if (videoItemView != null &&
                            ((videoItemView.isPlay())||videoItemView.VideoStatus()==IjkVideoView.STATE_PAUSED)) {
                        view.findViewById(R.id.showview).setVisibility(View.GONE);
                    }

                    if (videoItemView.VideoStatus()==IjkVideoView.STATE_PAUSED){
                        if (videoItemView.getParent()!=null)
                            ((ViewGroup)videoItemView.getParent()).removeAllViews();
                        frameLayout.addView(videoItemView);
                        return;
                    }

                    if (smallLayout.getVisibility() == View.VISIBLE && videoItemView != null && videoItemView.isPlay()) {
                        smallLayout.setVisibility(View.GONE);
                        videoLayout.removeAllViews();
                        videoItemView.setShowContoller(true);
                        frameLayout.addView(videoItemView);
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                int index = videoList.getChildAdapterPosition(view);
                if (index == postion) {
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    if (smallLayout.getVisibility() == View.GONE && videoItemView != null
                            && videoItemView.isPlay()) {
                        smallLayout.setVisibility(View.VISIBLE);
                        videoLayout.removeAllViews();
                        videoItemView.setShowContoller(false);
                        videoLayout.addView(videoItemView);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoItemView==null){
            videoItemView=new VideoPlayView(context);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(videoLayout==null)
            return;
        if (smallLayout.getVisibility()== View.VISIBLE){
            smallLayout.setVisibility(View.GONE);
            videoLayout.removeAllViews();
        }

        if (postion!=-1){
            ViewGroup view= (ViewGroup) videoItemView.getParent();
            if (view!=null){
                view.removeAllViews();
            }
        }
        videoItemView.stop();
        videoItemView.release();
        videoItemView.onDestroy();
        videoItemView=null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoItemView!=null){
            videoItemView.stop();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoItemView!=null){
            videoItemView.onChanged(newConfig);
            if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
                fullScreen.setVisibility(View.GONE);
                videoList.setVisibility(View.VISIBLE);
                fullScreen.removeAllViews();
                if (postion<=mLayoutManager.findLastVisibleItemPosition()
                        &&postion>=mLayoutManager.findFirstVisibleItemPosition()) {
                    View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    frameLayout.addView(videoItemView);
                    videoItemView.setShowContoller(true);
                }else {
                    videoLayout.removeAllViews();
                    videoLayout.addView(videoItemView);
                    videoItemView.setShowContoller(false);
                    smallLayout.setVisibility(View.VISIBLE);
                }
                videoItemView.setContorllerVisiable();
            }else {
                ViewGroup viewGroup= (ViewGroup) videoItemView.getParent();
                if (viewGroup==null)
                    return;
                viewGroup.removeAllViews();
                fullScreen.addView(videoItemView);
                smallLayout.setVisibility(View.GONE);
                videoList.setVisibility(View.GONE);
                fullScreen.setVisibility(View.VISIBLE);
            }
        }else {
            adapter.notifyDataSetChanged();
            videoList.setVisibility(View.VISIBLE);
            fullScreen.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public String readTextFileFromRawResourceId(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(
                resourceId)));

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return builder.toString();
    }
}
