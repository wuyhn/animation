package com.example.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout viewsContainer;
    private Button btnBadAnimation;
    private Button btnGoodAnimation;
    private TextView explanationText;

    private ValueAnimator badAnimator;

    private int originalWidth = 0;
    private int originalHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewsContainer = findViewById(R.id.views_container);
        btnBadAnimation = findViewById(R.id.btn_bad_animation);
        btnGoodAnimation = findViewById(R.id.btn_good_animation);
        explanationText = findViewById(R.id.explanation_text);

        int size = (int) (getResources().getDisplayMetrics().density * 36);
        for (int i = 1; i <= 40; i++) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(i));
            tv.setTextSize(16f);
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            tv.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(lp);

            viewsContainer.addView(tv);
        }

        viewsContainer.post(() -> {
            View firstChild = viewsContainer.getChildAt(0);
            if (firstChild != null) {
                originalWidth = firstChild.getWidth();
                originalHeight = firstChild.getHeight();
            }
        });

        btnBadAnimation.setOnClickListener(v -> runBadAnimation());
        btnGoodAnimation.setOnClickListener(v -> runGoodAnimation());
    }

    private void runBadAnimation() {
        explanationText.setText(
                "🚫 CÁCH CHƯA TỐI ƯU 🚫\n" +
                "Thay đổi LayoutParams cho nhiều View cùng lúc.\n" +
                "👉 UI Thread phải requestLayout + measure lại TẤT CẢ View mỗi frame.\n" +
                "Kết quả: Animation bị giật/lag rõ rệt."
        );

        resetViewState();

        if (originalWidth == 0) return;

        badAnimator = ValueAnimator.ofInt(originalWidth, originalWidth * 2);
        badAnimator.setDuration(1000);
        badAnimator.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            for (int i = 0; i < viewsContainer.getChildCount(); i++) {
                View view = viewsContainer.getChildAt(i);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = animatedValue;
                layoutParams.height = animatedValue;
                view.setLayoutParams(layoutParams);
            }
        });
        badAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetViewState();
            }
        });
        badAnimator.start();
    }

    private void runGoodAnimation() {
        explanationText.setText(
                "✅ CÁCH ĐÃ TỐI ƯU ✅\n" +
                "Dùng ViewPropertyAnimator.scaleX/scaleY, nhưng thay đổi Pivot Point để có hiệu ứng hình ảnh giống hệt.\n" +
                "👉 Vẫn do GPU xử lý, KHÔNG requestLayout/measure. Hiệu năng vượt trội.\n" +
                "Kết quả: Animation mượt mà, dù hiệu ứng hình ảnh giống hệt cách chưa tối ưu."
        );

        resetViewState();

        int childCount = viewsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewsContainer.getChildAt(i);

            // Thay đổi điểm neo (pivot) về góc trên-trái (0,0)
            view.setPivotX(0f);
            view.setPivotY(0f);

            Runnable endAction = null;
            if (i == childCount - 1) {
                endAction = () -> resetViewState();
            }

            view.animate()
                .scaleX(2.0f)
                .scaleY(2.0f)
                .setDuration(1000)
                .withLayer()
                .withEndAction(endAction)
                .start();
        }
    }

    private void resetViewState() {
        if (badAnimator != null) {
            badAnimator.cancel();
        }

        for (int i = 0; i < viewsContainer.getChildCount(); i++) {
            View view = viewsContainer.getChildAt(i);
            view.animate().cancel();
            view.setScaleX(1.0f);
            view.setScaleY(1.0f);

            // Reset pivot về lại trung tâm (mặc định)
            view.setPivotX(view.getWidth() / 2f);
            view.setPivotY(view.getHeight() / 2f);

            if (view.getWidth() != originalWidth && originalWidth > 0) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = originalWidth;
                layoutParams.height = originalHeight;
                view.setLayoutParams(layoutParams);
            }
        }
    }
}
