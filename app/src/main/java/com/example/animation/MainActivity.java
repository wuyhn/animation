//package com.example.animation;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.ValueAnimator;
//import android.os.Bundle;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//public class MainActivity extends AppCompatActivity {
//
//    private LinearLayout viewsContainer;
//    private Button btnBadAnimation;
//    private Button btnGoodAnimation;
//    private TextView explanationText;
//
//    private ValueAnimator badAnimator;
//
//    private int originalWidth = 0;
//    private int originalHeight = 0;
//    private int originalMargin = 8; // Lưu lại margin gốc
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        viewsContainer = findViewById(R.id.views_container);
//        btnBadAnimation = findViewById(R.id.btn_bad_animation);
//        btnGoodAnimation = findViewById(R.id.btn_good_animation);
//        explanationText = findViewById(R.id.explanation_text);
//
//        int size = (int) (getResources().getDisplayMetrics().density * 36);
//        // *** 1. TĂNG TẢI LÊN MỨC CỰC ĐOAN (200 VIEWS) ***
//        for (int i = 1; i <= 200; i++) {
//            TextView tv = new TextView(this);
//            tv.setText(String.valueOf(i));
//            tv.setTextSize(16f);
//            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
//            tv.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
//            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
//            lp.setMargins(originalMargin, originalMargin, originalMargin, originalMargin);
//            tv.setLayoutParams(lp);
//
//            viewsContainer.addView(tv);
//        }
//
//        viewsContainer.post(() -> {
//            View firstChild = viewsContainer.getChildAt(0);
//            if (firstChild != null) {
//                originalWidth = firstChild.getWidth();
//                originalHeight = firstChild.getHeight();
//            }
//        });
//
//        btnBadAnimation.setOnClickListener(v -> runBadAnimation());
//        btnGoodAnimation.setOnClickListener(v -> runGoodAnimation());
//    }
//
//    private void runBadAnimation() {
//        explanationText.setText(
//                "🚫 CÁCH CHƯA TỐI ƯU (ĐẢM BẢO > 16ms) 🚫\n" +
//                        "Thêm 10ms delay và thay đổi Margin + Width/Height cho 200 View.\n" +
//                        "👉 Thời gian frame = 10ms (delay) + Thời gian tính toán layout.\n" +
//                        "Kết quả: Cột xanh dương luôn vượt ngưỡng một cách rõ rệt."
//        );
//
//        resetViewState();
//
//        if (originalWidth == 0) return;
//
//        badAnimator = ValueAnimator.ofInt(0, 100);
//        badAnimator.setDuration(1000);
//        badAnimator.addUpdateListener(animator -> {
//            // *** "ĐÓNG BĂNG" UI THREAD ĐỂ ĐẢM BẢO VƯỢT NGƯỠNG ***
//            // (LƯU Ý: KHÔNG BAO GIỜ DÙNG TRONG CODE THỰC TẾ)
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            int offset = (int) animator.getAnimatedValue();
//
//            for (int i = 0; i < viewsContainer.getChildCount(); i++) {
//                View view = viewsContainer.getChildAt(i);
//                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
//                lp.width = originalWidth + offset;
//                lp.height = originalHeight + offset;
//                lp.setMargins(originalMargin + offset / 2, originalMargin + offset / 2, originalMargin + offset / 2, originalMargin + offset / 2);
//                view.setLayoutParams(lp);
//            }
//
//            viewsContainer.requestLayout();
//        });
//
//        badAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                resetViewState();
//            }
//        });
//        badAnimator.start();
//    }
//
//    private void runGoodAnimation() {
//        explanationText.setText(
//                "✅ CÁCH TỐI ƯU HOÀN HẢO ✅\n" +
//                        "Animate CẢ CONTAINER bằng scale. Đặt pivotY = 0 để chuyển động giống hệt.\n" +
//                        "👉 Hiệu ứng hình ảnh giống hệt, nhưng hiệu năng vượt trội vì do GPU xử lý.\n" +
//                        "Kết quả: Animation mượt mà, biểu đồ phẳng."
//        );
//
//        resetViewState();
//
//        viewsContainer.setPivotY(0f);
//
//        viewsContainer.animate()
//                .scaleX(2.0f)
//                .scaleY(2.0f)
//                .setDuration(1000)
//                .withLayer()
//                .withEndAction(() -> resetViewState())
//                .start();
//    }
//
//    private void resetViewState() {
//        if (badAnimator != null) {
//            badAnimator.cancel();
//        }
//
//        viewsContainer.animate().cancel();
//        viewsContainer.setScaleX(1.0f);
//        viewsContainer.setScaleY(1.0f);
//        viewsContainer.setPivotX(viewsContainer.getWidth() / 2f);
//        viewsContainer.setPivotY(viewsContainer.getHeight() / 2f);
//
//        if (viewsContainer.getChildCount() > 0 && viewsContainer.getChildAt(0).getWidth() != originalWidth) {
//            for (int i = 0; i < viewsContainer.getChildCount(); i++) {
//                View view = viewsContainer.getChildAt(i);
//                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
//                layoutParams.width = originalWidth;
//                layoutParams.height = originalHeight;
//                // *** SỬA LỖI: RESET LẠI MARGIN ***
//                layoutParams.setMargins(originalMargin, originalMargin, originalMargin, originalMargin);
//                view.setLayoutParams(layoutParams);
//            }
//        }
//    }
//}
package com.example.animation;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout viewsContainer;
    private Button btnBadAnimation, btnGoodAnimation;
    private TextView explanationText;
    private Handler handler = new Handler();
    private Runnable[] barRunnables; // "bad"
    private static final int BAR_COUNT = 140;
    private static final int BAR_WIDTH_DP = 18;
    private static final int BAR_HEIGHT_MIN_DP = 52; // min height bar
    private static final int BAR_HEIGHT_MAX_DP = 180; // max height bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewsContainer   = findViewById(R.id.views_container);
        btnBadAnimation  = findViewById(R.id.btn_bad_animation);
        btnGoodAnimation = findViewById(R.id.btn_good_animation);
        explanationText  = findViewById(R.id.explanation_text);

        // Tạo nhiều "bar" dựng thẳng đứng
        int barWidthPx     = (int) (getResources().getDisplayMetrics().density * BAR_WIDTH_DP);
        int barHeightMinPx = (int) (getResources().getDisplayMetrics().density * BAR_HEIGHT_MIN_DP);
        for (int i = 0; i < BAR_COUNT; i++) {
            View bar = new View(this);
            bar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(barWidthPx, barHeightMinPx);
            lp.setMargins(7, 0, 7, 0);
            bar.setLayoutParams(lp);
            bar.setScaleY(1f);
            viewsContainer.addView(bar);
        }

        btnBadAnimation.setOnClickListener(v -> runBadAnimation());
        btnGoodAnimation.setOnClickListener(v -> runGoodAnimation());
    }

    // Chưa tối ưu: đổi layoutParams.height từng bar
    private void runBadAnimation() {
        explanationText.setText(
                "🚫 Animation CHƯA tối ưu: mỗi cột đổi chiều cao (layoutParams.height) liên tục.\n"
                        + "→ Hệ thống phải layout lại nên drop frame khi đủ nhiều BAR."
        );
        resetViewState();

        int barHeightMinPx = (int) (getResources().getDisplayMetrics().density * BAR_HEIGHT_MIN_DP);
        int barHeightMaxPx = (int) (getResources().getDisplayMetrics().density * BAR_HEIGHT_MAX_DP);
        int n = viewsContainer.getChildCount();
        barRunnables = new Runnable[n];

        for (int i = 0; i < n; i++) {
            View bar = viewsContainer.getChildAt(i);
            barRunnables[i] = createBadBarAnimator(bar, barHeightMinPx, barHeightMaxPx, i);
            bar.postDelayed(barRunnables[i], i * 15); // lệch nhịp sóng
        }
    }
    private Runnable createBadBarAnimator(final View bar, final int minHeight, final int maxHeight, int index) {
        final int[] heightVal = {minHeight};
        final boolean[] increasing = {true};
        return new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams lp = bar.getLayoutParams();
                lp.height = heightVal[0];
                bar.setLayoutParams(lp);

                if (increasing[0]) heightVal[0] += 6;
                else heightVal[0] -= 6;
                if (heightVal[0] >= maxHeight) increasing[0] = false;
                if (heightVal[0] <= minHeight) increasing[0] = true;

                bar.postDelayed(this, 16);
            }
        };
    }

    // Tối ưu: scaleY property animation
    private void runGoodAnimation() {
        explanationText.setText(
                "✅ Animation TỐI ƯU: mỗi cột chỉ scaleY với property animator.\n"
                        + "→ Hoạt ảnh cực kỳ mượt, tuyệt đối không drop frame."
        );
        resetViewState();

        int n = viewsContainer.getChildCount();
        for (int i = 0; i < n; i++) {
            View bar = viewsContainer.getChildAt(i);
            long delay = i * 15;
            animateBarProperty(bar, delay);
        }
    }
    private void animateBarProperty(View bar, long delay) {
        bar.animate()
                .scaleY(3.0f)
                .setDuration(340)
                .setStartDelay(delay)
                .withEndAction(() -> bar.animate()
                        .scaleY(1.0f)
                        .setDuration(340)
                        .withEndAction(() -> animateBarProperty(bar, 0))
                        .start())
                .start();
    }

    // RESET
    private void resetViewState() {
        if (barRunnables != null) {
            for (int i = 0; i < barRunnables.length; i++) {
                View bar = viewsContainer.getChildAt(i);
                if (barRunnables[i] != null) bar.removeCallbacks(barRunnables[i]);
            }
        }
        for (int i = 0; i < viewsContainer.getChildCount(); i++) {
            View bar = viewsContainer.getChildAt(i);
            bar.clearAnimation();
            bar.animate().cancel();
            bar.setScaleY(1f);
            // đặt chiều cao lại min
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.height = (int) (getResources().getDisplayMetrics().density * BAR_HEIGHT_MIN_DP);
            bar.setLayoutParams(lp);
        }
    }
}