
package com.example.animation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private LinearLayout viewsContainer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int BAR_COUNT = 1000;
    private static final int BAR_HEIGHT_DP = 8;
    private static final int BAR_MIN_WIDTH_PX = 60;
    // Đồng bộ mục tiêu: Gấp 10 lần chiều rộng cơ bản
    private static final float SCALE_TARGET = 10.0f;

    private boolean isAnimationRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewsContainer = findViewById(R.id.views_container);
        setupBars();

        findViewById(R.id.btn_bad).setOnClickListener(v -> runBadAnimation());
        findViewById(R.id.btn_good).setOnClickListener(v -> runGoodAnimation());
        findViewById(R.id.btn_stop).setOnClickListener(v -> stopAllAnimations());
    }

    private void setupBars() {
        float density = getResources().getDisplayMetrics().density;
        int barHeightPx = (int) (density * BAR_HEIGHT_DP);

        viewsContainer.removeAllViews();
        for (int i = 0; i < BAR_COUNT; i++) {
            // Cấu trúc wrapper lồng nhau để tạo gánh nặng cho CPU [cite: 178]
            LinearLayout wrapper = new LinearLayout(this);
            wrapper.setPadding(2, 2, 2, 2);

            View bar = new View(this);
            bar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(BAR_MIN_WIDTH_PX, barHeightPx);
            wrapper.addView(bar, lp);
            viewsContainer.addView(wrapper);
        }
    }

    /**
     * ❌ CHẾ ĐỘ BAD: VẼ LẠI LAYOUT (CPU) [cite: 580]
     * Sử dụng chu trình Measure -> Layout -> Draw ép CPU hoạt động nặng
     */
    private void runBadAnimation() {
        stopAllAnimations();
        isAnimationRunning = true;
        int targetWidth = (int) (BAR_MIN_WIDTH_PX * SCALE_TARGET);

        for (int i = 0; i < viewsContainer.getChildCount(); i++) {
            ViewGroup wrapper = (ViewGroup) viewsContainer.getChildAt(i);
            View bar = wrapper.getChildAt(0);
            startBadLoop(bar, targetWidth, i);
        }
    }

    private void startBadLoop(final View bar, final int targetW, int index) {
        final int[] width = {BAR_MIN_WIDTH_PX};
        final boolean[] inc = {true};

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!isAnimationRunning) return;

                // Thay đổi layoutParams trực tiếp [cite: 547, 992]
                ViewGroup.LayoutParams lp = bar.getLayoutParams();
                lp.width = width[0];
                bar.setLayoutParams(lp);

                // Ép buộc hệ thống tính toán lại bố cục (Measure/Layout pass)
                viewsContainer.requestLayout();

                // Logic tăng giảm mượt mà để giống với Property Animation
                int step = 30;
                if (inc[0]) {
                    width[0] += step;
                    if (width[0] >= targetW) inc[0] = false;
                } else {
                    width[0] -= step;
                    if (width[0] <= BAR_MIN_WIDTH_PX) inc[0] = true;
                }

                mainHandler.postDelayed(this, 16); // ~60 FPS [cite: 551]
            }
        };
        mainHandler.postDelayed(r, (index % 100) * 10);
    }

    /**
     * ✅ CHẾ ĐỘ GOOD: PROPERTY ANIMATION (GPU) [cite: 531, 584]
     * Sử dụng scaleX biến đổi ma trận ảnh, không gây layout lại [cite: 577, 1007]
     */
    private void runGoodAnimation() {
        stopAllAnimations();
        isAnimationRunning = true;

        for (int i = 0; i < viewsContainer.getChildCount(); i++) {
            ViewGroup wrapper = (ViewGroup) viewsContainer.getChildAt(i);
            View bar = wrapper.getChildAt(0);
            animateBarOptimized(bar, (i % 100) * 10);
        }
    }

private void animateBarOptimized(final View bar, long delay) {
    if (!isAnimationRunning) return;

    bar.setPivotX(0f); // Giữ gốc tọa độ bên trái

    // ta giữ layer này cố định trong suốt quá trình chạy để GPU xử lý texture mượt nhất.
    if (bar.getLayerType() != View.LAYER_TYPE_HARDWARE) {
        bar.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    bar.animate()
            .scaleX(SCALE_TARGET)
            .setDuration(1500)
            .setStartDelay(delay)
            .setInterpolator(new android.view.animation.LinearInterpolator())
            .withEndAction(() -> {
                if (isAnimationRunning) {
                    bar.animate()
                            .scaleX(1.0f)
                            .setDuration(1500)
                            .setInterpolator(new android.view.animation.LinearInterpolator())
                            // ✅ CHIẾN THUẬT 2: Loại bỏ StartDelay ở các vòng lặp sau
                            // Chỉ delay vòng đầu, các vòng sau chạy nối đuôi nhau để tránh sụt giảm frame
                            .withEndAction(() -> animateBarOptimized(bar, 0))
                            .start();
                }
            }).start();
}

    private void stopAllAnimations() {
        isAnimationRunning = false;
        // Xóa sạch hàng đợi Handler ngay lập tức để giải phóng UI Thread
        mainHandler.removeCallbacksAndMessages(null);

        for (int i = 0; i < viewsContainer.getChildCount(); i++) {
            ViewGroup wrapper = (ViewGroup) viewsContainer.getChildAt(i);
            View bar = wrapper.getChildAt(0);

            bar.animate().cancel(); // Hủy mọi animation đang chạy

            // Quan trọng: Tắt Hardware Layer để trả lại VRAM cho GPU
            bar.setLayerType(View.LAYER_TYPE_NONE, null);

            bar.setScaleX(1f);

            // Chỉ cập nhật LayoutParams nếu thực sự cần thiết để tránh ép CPU layout lại
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            if (lp.width != BAR_MIN_WIDTH_PX) {
                lp.width = BAR_MIN_WIDTH_PX;
                bar.setLayoutParams(lp);
            }
        }
    }
}