package cn.demo.videolist;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import cn.demo.videolist.media.IjkVideoView;
import cn.demo.videolist.media.VideoAdapter;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Author  wangchenchen
 * CreateDate 2016/8/23.
 * Email wcc@jusfoun.com
 * Description
 */
public class VideoListLayout extends RelativeLayout {

    private RecyclerView videoList;
    private LinearLayoutManager mLayoutManager;
    private VideoAdapter adapter;
    private FrameLayout videoLayout;

    private int postion = -1;
    private int lastPostion = -1;
    private Context context;
    private VideoPlayView videoItemView;

    private FrameLayout fullScreen;
    private VideoListData listData;
    private RelativeLayout smallLayout;
    private ImageView close;

    public VideoListLayout(Context context) {
        super(context);
        initView(context);
        initActions();
    }

    public VideoListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initActions();
    }

    public VideoListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initActions();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoListLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
        initActions();
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_video_list,this,true);
        this.context = context;
        mLayoutManager = new LinearLayoutManager(context);
        videoList = (RecyclerView) findViewById(R.id.video_list);
        videoList.setLayoutManager(mLayoutManager);

        adapter = new VideoAdapter(context);
        videoList.setAdapter(adapter);
        fullScreen = (FrameLayout) findViewById(R.id.full_screen);
        videoLayout = (FrameLayout) findViewById(R.id.layout_video);
        videoItemView = new VideoPlayView(context);

        String data = readTextFileFromRawResourceId(context, R.raw.video_list);
        listData = new Gson().fromJson(data, VideoListData.class);
        adapter.refresh(listData.getList());
        smallLayout = (RelativeLayout) findViewById(R.id.small_layout);
        close = (ImageView) findViewById(R.id.close);

    }

    private void initActions() {

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoItemView.isPlay()) {
                    videoItemView.stop();
                    postion = -1;
                    lastPostion = -1;
                    videoLayout.removeAllViews();
                    smallLayout.setVisibility(View.GONE);
                }
            }
        });

        smallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smallLayout.setVisibility(View.GONE);
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
                VideoListLayout.this.postion = position;

                if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED) {
                    if (position != lastPostion) {

                        videoItemView.stop();
                        videoItemView.release();
                    }
                }

                if (smallLayout.getVisibility() == View.VISIBLE)

                {
                    smallLayout.setVisibility(View.GONE);
                    videoLayout.removeAllViews();
                    videoItemView.setShowContoller(true);
                }

                if (lastPostion != -1)

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

                if (videoItemView.getParent() != null) {
                    ((ViewGroup) videoItemView.getParent()).removeAllViews();
                }

                View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                frameLayout.removeAllViews();
                frameLayout.addView(videoItemView);
                videoItemView.start(listData.getList().get(position).getMp4_url());
                lastPostion = position;
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
                            ((videoItemView.isPlay()) || videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED)) {
                        view.findViewById(R.id.showview).setVisibility(View.GONE);
                    }

                    if (videoItemView.VideoStatus() == IjkVideoView.STATE_PAUSED) {
                        if (videoItemView.getParent() != null)
                            ((ViewGroup) videoItemView.getParent()).removeAllViews();
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
                        videoLayout.removeAllViews();
                        videoItemView.setShowContoller(false);
                        videoLayout.addView(videoItemView);
                        smallLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (videoItemView != null) {
            videoItemView.onChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                fullScreen.setVisibility(View.GONE);
                videoList.setVisibility(View.VISIBLE);
                fullScreen.removeAllViews();
                if (postion <= mLayoutManager.findLastVisibleItemPosition()
                        && postion >= mLayoutManager.findFirstVisibleItemPosition()) {
                    View view = videoList.findViewHolderForAdapterPosition(postion).itemView;
                    FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.layout_video);
                    frameLayout.removeAllViews();
                    frameLayout.addView(videoItemView);
                    videoItemView.setShowContoller(true);
                } else {
                    videoLayout.removeAllViews();
                    videoLayout.addView(videoItemView);
                    videoItemView.setShowContoller(false);
                    smallLayout.setVisibility(View.VISIBLE);
                }
                videoItemView.setContorllerVisiable();
            } else {
                ViewGroup viewGroup = (ViewGroup) videoItemView.getParent();
                if (viewGroup == null)
                    return;
                viewGroup.removeAllViews();
                fullScreen.addView(videoItemView);
                smallLayout.setVisibility(View.GONE);
                videoList.setVisibility(View.GONE);
                fullScreen.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.notifyDataSetChanged();
            videoList.setVisibility(View.VISIBLE);
            fullScreen.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (videoItemView==null)
            videoItemView=new VideoPlayView(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (videoLayout == null)
            return;
        if (smallLayout.getVisibility() == View.VISIBLE) {
            smallLayout.setVisibility(View.GONE);
            videoLayout.removeAllViews();
        }

        if (postion != -1) {
            ViewGroup view = (ViewGroup) videoItemView.getParent();
            if (view != null) {
                view.removeAllViews();
            }
        }
        videoItemView.stop();
        videoItemView.release();
        videoItemView.onDestroy();
        videoItemView = null;
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
