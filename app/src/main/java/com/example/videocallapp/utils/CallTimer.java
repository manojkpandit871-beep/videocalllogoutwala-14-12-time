package com.example.videocallapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/**
 * Manages a call duration timer that updates a TextView in HH:MM:SS format.
 */
public class CallTimer {

    private final Handler   handler   = new Handler(Looper.getMainLooper());
    private long            startTime;
    private boolean         running   = false;
    private TextView        display;
    private OnTickListener  listener;
    private long            elapsedSeconds = 0;

    public interface OnTickListener {
        void onTick(long elapsedSeconds);
    }

    public CallTimer(TextView display) {
        this.display = display;
    }

    public CallTimer(OnTickListener listener) {
        this.listener = listener;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running   = true;
        tick();
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    /** Returns elapsed seconds since start(). */
    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            String formatted = format(elapsedSeconds);
            if (display != null) display.setText(formatted);
            if (listener != null) listener.onTick(elapsedSeconds);
            handler.postDelayed(this, 1000);
        }
    };

    private void tick() {
        handler.post(ticker);
    }

    /** Format seconds as HH:MM:SS */
    public static String format(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
