package cn.demo.videolist;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import cn.demo.videolist.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Description 播放view
 */
public class VideoPlayView extends RelativeLayout implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {


    private CustomMediaContoller mediaController;
    private View player_btn, view;
    private IjkVideoView mVideoView;
    private Handler handler = new Handler();
    private boolean isPause;

    private View rView;
    private Context mContext;
    private boolean portrait;
//    private OrientationEventListener orientationEventListener;

    public VideoPlayView(Context context) {
        super(context);
        mContext = context;
        initData();
        initViews();
        initActions();
    }

    private void initData() {

    }

    private void initViews() {

        rView = LayoutInflater.from(mContext).inflate(R.layout.view_video_item, this, true);
        view = findViewById(R.id.media_contoller);
        mVideoView = (IjkVideoView) findViewById(R.id.main_video);
        mediaController = new CustomMediaContoller(mContext, rView);
        mVideoView.setMediaController(mediaController);

        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                view.setVisibility(View.GONE);
                if (mediaController.getScreenOrientation((Activity) mContext)
                        == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    //横屏播放完毕，重置
                    ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    setLayoutParams(layoutParams);
                }
                if (completionListener != null)
                    completionListener.completion(mp);
            }
        });

    }

    private void initActions() {

        /*orientationEventListener = new OrientationEventListener(mContext) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.e("onOrientationChanged", "orientation");
                if (orientation >= 0 && orientation <= 30 || orientation >= 330 || (orientation >= 150 && orientation <= 210)) {
                    //竖屏
                    if (portrait) {
                        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                } else if ((orientation >= 90 && orientation <= 120) || (orientation >= 240 && orientation <= 300)) {
                    if (!portrait) {
                        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                }
            }
        };*/
    }

    public boolean isPlay() {
        return mVideoView.isPlaying();
    }

    public void pause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
    }

    public void start(String path) {
        Uri uri = Uri.parse(path);
        if (mediaController != null)
            mediaController.start();
        if (!mVideoView.isPlaying()) {
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        } else {
            mVideoView.stopPlayback();
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
    }

    public void start(){
        if (mVideoView.isPlaying()){
            mVideoView.start();
        }
    }

    public void setContorllerVisiable(){
        mediaController.setVisiable();
    }

    public void seekTo(int msec){
        mVideoView.seekTo(msec);
    }

    public void onChanged(Configuration configuration) {
        portrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    public void doOnConfigurationChanged(final boolean portrait) {
        if (mVideoView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setFullScreen(!portrait);
                    if (portrait) {
                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        Log.e("handler", "400");
                        setLayoutParams(layoutParams);
                        requestLayout();
                    } else {
                        int heightPixels = ((Activity) mContext).getResources().getDisplayMetrics().heightPixels;
                        int widthPixels = ((Activity) mContext).getResources().getDisplayMetrics().widthPixels;
                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.height = heightPixels;
                        layoutParams.width = widthPixels;
                        Log.e("handler", "height==" + heightPixels + "\nwidth==" + widthPixels);
                        setLayoutParams(layoutParams);
                    }
                }
            });
//            orientationEventListener.enable();
        }
    }

    public void stop() {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
//        orientationEventListener.disable();
    }

    private void setFullScreen(boolean fullScreen) {
        if (mContext != null && mContext instanceof Activity) {
            WindowManager.LayoutParams attrs = ((Activity) mContext).getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                ((Activity) mContext).getWindow().setAttributes(attrs);
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }

    public void setShowContoller(boolean isShowContoller) {
        mediaController.setShowContoller(isShowContoller);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public long getPalyPostion() {
        return mVideoView.getCurrentPosition();
    }

    public void release() {
        mVideoView.release(true);
    }

    public int VideoStatus() {
        return mVideoView.getCurrentStatue();
    }

    private CompletionListener completionListener;

    public void setCompletionListener(CompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    public interface CompletionListener {
        void completion(IMediaPlayer mp);
    }
}
