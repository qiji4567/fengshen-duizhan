package com.example.duizhan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.example.duizhan.R;

/**
 * Bottom-right fan layout: attack at the anchor, skills on a spaced arc above-left.
 */
public class SkillFanPanel extends FrameLayout {
    private static final class FanSlot {
        final float angleFromLeftDeg;
        final float radiusDp;
        final float sizeDp;

        FanSlot(float angleFromLeftDeg, float radiusDp, float sizeDp) {
            this.angleFromLeftDeg = angleFromLeftDeg;
            this.radiusDp = radiusDp;
            this.sizeDp = sizeDp;
        }
    }

    private static final FanSlot ATTACK_SLOT = new FanSlot(0f, 0f, 72f);
    private static final FanSlot PRIMARY_SLOT = new FanSlot(16f, 138f, 58f);
    private static final FanSlot SECONDARY_SLOT = new FanSlot(38f, 146f, 58f);
    private static final FanSlot ULTIMATE_SLOT = new FanSlot(62f, 158f, 68f);
    private static final FanSlot TALENT_SLOT = new FanSlot(86f, 136f, 54f);

    private final float anchorMarginDp;
    private final float panelWidthDp;
    private final float panelHeightDp;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SkillFanPanel(Context context) {
        this(context, null);
    }

    public SkillFanPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
        setClipToPadding(false);
        setWillNotDraw(false);
        anchorMarginDp = getResources().getDimension(R.dimen.battle_fan_anchor_margin)
                / getResources().getDisplayMetrics().density;
        panelWidthDp = getResources().getDimension(R.dimen.battle_fan_panel_width)
                / getResources().getDisplayMetrics().density;
        panelHeightDp = getResources().getDimension(R.dimen.battle_fan_panel_height)
                / getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float anchorX = getWidth() - dp(anchorMarginDp) - dp(ATTACK_SLOT.sizeDp) / 2f;
        float anchorY = getHeight() - dp(anchorMarginDp) - dp(ATTACK_SLOT.sizeDp) / 2f;
        float radius = dp(172f);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(anchorX, anchorY, radius,
                new int[]{Color.argb(115, 8, 13, 28), Color.argb(52, 15, 23, 42), Color.TRANSPARENT},
                new float[]{0f, 0.58f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(anchorX, anchorY, radius, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(Color.argb(90, 250, 204, 21));
        for (FanSlot slot : new FanSlot[]{PRIMARY_SLOT, SECONDARY_SLOT, ULTIMATE_SLOT, TALENT_SLOT}) {
            float rad = (float) Math.toRadians(slot.angleFromLeftDeg);
            float x = anchorX - (float) Math.cos(rad) * dp(slot.radiusDp);
            float y = anchorY - (float) Math.sin(rad) * dp(slot.radiusDp);
            canvas.drawLine(anchorX, anchorY, x, y, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = dp(panelWidthDp);
        int height = dp(panelHeightDp);
        setMeasuredDimension(width, height);
        int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        for (int i = 0; i < getChildCount(); i++) {
            measureChild(getChildAt(i), childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float anchorX = (right - left) - dp(anchorMarginDp) - dp(ATTACK_SLOT.sizeDp) / 2f;
        float anchorY = (bottom - top) - dp(anchorMarginDp) - dp(ATTACK_SLOT.sizeDp) / 2f;
        layoutById(R.id.btnAttack, anchorX, anchorY, ATTACK_SLOT);
        layoutById(R.id.btnSkillPrimary, anchorX, anchorY, PRIMARY_SLOT);
        layoutById(R.id.btnSkillSecondary, anchorX, anchorY, SECONDARY_SLOT);
        layoutById(R.id.btnSkillUltimate, anchorX, anchorY, ULTIMATE_SLOT);
        layoutById(R.id.btnTalent, anchorX, anchorY, TALENT_SLOT);
    }

    private void layoutById(int viewId, float anchorX, float anchorY, FanSlot slot) {
        View child = findViewById(viewId);
        if (child == null || child.getVisibility() == GONE) {
            return;
        }
        if (slot.radiusDp <= 0f) {
            layoutCentered(child, anchorX, anchorY, slot.sizeDp);
            return;
        }
        layoutFanChild(child, anchorX, anchorY, slot);
    }

    private void layoutCentered(View child, float centerX, float centerY, float sizeDp) {
        float size = dp(sizeDp);
        int childLeft = Math.round(centerX - size / 2f);
        int childTop = Math.round(centerY - size / 2f);
        child.layout(childLeft, childTop, childLeft + Math.round(size), childTop + Math.round(size));
    }

    private void layoutFanChild(View child, float anchorX, float anchorY, FanSlot slot) {
        float rad = (float) Math.toRadians(slot.angleFromLeftDeg);
        float radius = dp(slot.radiusDp);
        float centerX = anchorX - (float) Math.cos(rad) * radius;
        float centerY = anchorY - (float) Math.sin(rad) * radius;
        layoutCentered(child, centerX, centerY, slot.sizeDp);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != VISIBLE) {
                continue;
            }
            if (x >= child.getLeft() && x < child.getRight()
                    && y >= child.getTop() && y < child.getBottom()) {
                return super.dispatchTouchEvent(ev);
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
