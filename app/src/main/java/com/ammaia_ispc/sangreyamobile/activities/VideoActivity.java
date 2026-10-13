package com.ammaia_ispc.sangreyamobile.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ammaia_ispc.sangreyamobile.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class VideoActivity extends AppCompatActivity {
    private static final String VIDEO_ID = "B8e_Qyk6cd0";
    // A real HTTPS origin supplies the Referer required by YouTube (avoids error 153).
    private static final String PLAYER_ORIGIN = "https://sangreyaispc.pythonanywhere.com";
    private static final String STATE_POSITION = "video_position";
    private static final long LOAD_TIMEOUT_MS = 20000;
    private static final int[] PLAYBACK_BUTTONS = {
            R.id.video_play, R.id.video_pause, R.id.video_stop
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private LinearLayout videoCard;
    private LinearLayout videoPanel;
    private FrameLayout videoFrame;
    private FrameLayout fullscreenContainer;
    private View content;
    private TextView status;
    private ImageButton fullscreenButton;
    private boolean resumed;
    private boolean playerReady;
    private boolean loadFailed;
    private boolean fullscreen;
    private int previousOrientation;
    private double playbackPosition;
    private long loadStartedAt;
    private int pollingGeneration;

    private final Runnable pollPlayback = new Runnable() {
        @Override
        public void run() {
            if (!resumed || loadFailed) return;
            int generation = pollingGeneration;
            webView.evaluateJavascript(
                    "window.getPlaybackStatus ? getPlaybackStatus() : null;", result -> {
                        if (!resumed || isDestroyed() || loadFailed || generation != pollingGeneration) return;
                        updatePlaybackStatus(result);
                        if (!loadFailed) handler.postDelayed(this, 500);
                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        webView = findViewById(R.id.video_web_view);
        videoCard = findViewById(R.id.video_card);
        videoPanel = findViewById(R.id.video_panel);
        videoFrame = findViewById(R.id.video_frame);
        fullscreenContainer = findViewById(R.id.video_fullscreen_container);
        content = findViewById(R.id.video_content);
        status = findViewById(R.id.video_status);
        fullscreenButton = findViewById(R.id.video_fullscreen);

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getDouble(STATE_POSITION, 0);
        }

        findViewById(R.id.btnBack).setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fullscreen) setFullscreen(false);
                else finish();
            }
        });

        findViewById(R.id.video_play).setOnClickListener(view -> controlVideo("play"));
        findViewById(R.id.video_pause).setOnClickListener(view -> controlVideo("pause"));
        findViewById(R.id.video_stop).setOnClickListener(view -> {
            playbackPosition = 0;
            controlVideo("stop");
        });
        fullscreenButton.setOnClickListener(view -> setFullscreen(!fullscreen));
        findViewById(R.id.video_retry).setOnClickListener(view -> loadVideo());

        videoFrame.addOnLayoutChangeListener((view, left, top, right, bottom,
                                             oldLeft, oldTop, oldRight, oldBottom) -> {
            if (!fullscreen) resizeInlineVideo();
        });
        configureWebView();
        loadVideo();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        // JavaScript is needed by the official iframe player. No JavaScript-to-Java bridge is exposed.
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setBackgroundColor(getColor(R.color.black));
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Keep the fixed player document in place; iframe HTTPS resources can still load.
                return request.isForMainFrame() || !"https".equals(request.getUrl().getScheme());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showError(R.string.video_load_error);
            }
        });
    }

    private void loadVideo() {
        pollingGeneration++;
        handler.removeCallbacks(pollPlayback);
        playerReady = false;
        loadFailed = false;
        setPlaybackButtonsEnabled(false);
        status.setText(R.string.video_loading);
        status.setVisibility(View.VISIBLE);
        findViewById(R.id.video_retry).setVisibility(View.GONE);
        loadStartedAt = SystemClock.elapsedRealtime();
        try (InputStream input = getAssets().open("donation_video.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            String html = new String(output.toByteArray(), StandardCharsets.UTF_8)
                    .replace("__VIDEO_ID__", VIDEO_ID)
                    .replace("__ORIGIN__", PLAYER_ORIGIN)
                    .replace("__START_SECONDS__", Long.toString((long) playbackPosition));
            webView.loadDataWithBaseURL(PLAYER_ORIGIN + "/", html, "text/html", "UTF-8", null);
            if (resumed) handler.post(pollPlayback);
        } catch (IOException exception) {
            showError(R.string.video_load_error);
        }
    }

    private void updatePlaybackStatus(String result) {
        try {
            if (!"null".equals(result)) {
                JSONObject data = new JSONObject(result);
                int error = data.optInt("error");
                if (error != 0) {
                    showError(error == 100 || error == 101 || error == 150
                            ? R.string.video_unavailable : R.string.video_load_error);
                    return;
                }
                if (data.optBoolean("ready")) {
                    if (!playerReady) {
                        playerReady = true;
                        status.setVisibility(View.GONE);
                        setPlaybackButtonsEnabled(true);
                    }
                    playbackPosition = data.optDouble("time", playbackPosition);
                    if (data.optBoolean("playing")) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            }
        } catch (JSONException ignored) {
            // The document may be transitioning while loading/retrying; poll again.
        }
        if (!playerReady && SystemClock.elapsedRealtime() - loadStartedAt >= LOAD_TIMEOUT_MS) {
            showError(R.string.video_load_error);
        }
    }

    private void showError(int message) {
        loadFailed = true;
        playerReady = false;
        handler.removeCallbacks(pollPlayback);
        setPlaybackButtonsEnabled(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        status.setText(message);
        status.setVisibility(View.VISIBLE);
        findViewById(R.id.video_retry).setVisibility(View.VISIBLE);
    }

    private void setPlaybackButtonsEnabled(boolean enabled) {
        for (int id : PLAYBACK_BUTTONS) {
            View button = findViewById(id);
            button.setEnabled(enabled);
            button.setAlpha(enabled ? 1f : 0.4f);
        }
    }

    private void controlVideo(String action) {
        webView.evaluateJavascript("window.controlVideo && controlVideo('" + action + "');", null);
    }

    private void resizeInlineVideo() {
        int width = videoFrame.getWidth();
        if (width == 0) return;
        int height = Math.max((int) (200 * getResources().getDisplayMetrics().density), width * 9 / 16);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) videoFrame.getLayoutParams();
        if (params.height != height || params.weight != 0) {
            params.height = height;
            params.weight = 0;
            videoFrame.setLayoutParams(params);
        }
    }

    private void setFullscreen(boolean enabled) {
        fullscreen = enabled;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        ((ViewGroup) videoPanel.getParent()).removeView(videoPanel);
        if (enabled) {
            previousOrientation = getRequestedOrientation();
            content.setVisibility(View.GONE);
            fullscreenContainer.setVisibility(View.VISIBLE);
            fullscreenContainer.addView(videoPanel, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            videoFrame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            fullscreenButton.setImageResource(R.drawable.ic_video_fullscreen_exit);
            fullscreenButton.setContentDescription(getString(R.string.video_exit_fullscreen));
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            fullscreenContainer.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            videoCard.addView(videoPanel, 1, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            resizeInlineVideo();
            fullscreenButton.setImageResource(R.drawable.ic_video_fullscreen);
            fullscreenButton.setContentDescription(getString(R.string.video_fullscreen));
            controller.show(WindowInsetsCompat.Type.systemBars());
            setRequestedOrientation(previousOrientation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        pollingGeneration++;
        webView.onResume();
        loadStartedAt = SystemClock.elapsedRealtime();
        handler.post(pollPlayback);
    }

    @Override
    protected void onPause() {
        resumed = false;
        pollingGeneration++;
        handler.removeCallbacks(pollPlayback);
        controlVideo("pause");
        webView.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(STATE_POSITION, playbackPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        webView.stopLoading();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        super.onDestroy();
    }
}
