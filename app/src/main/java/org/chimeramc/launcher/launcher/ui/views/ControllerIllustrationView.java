package org.chimeramc.launcher.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.chimeramc.launcher.launcher.controller.ControllerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerIllustrationView extends View {
    private ControllerType type = ControllerType.XBOX;
    private final List<Region> regions = new ArrayList<>();
    private final Map<String, Region> regionById = new HashMap<>();
    private final Map<String, Float> glow = new HashMap<>();
    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float cx;
    private float cy;
    private float scale;

    public ControllerIllustrationView(Context context) {
        super(context);
        init();
    }

    public ControllerIllustrationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        basePaint.setColor(Color.rgb(38, 42, 50));
        outlinePaint.setColor(Color.rgb(120, 128, 140));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3f);
        buttonPaint.setColor(Color.rgb(72, 78, 90));
        glowPaint.setColor(Color.rgb(78, 226, 162));
        textPaint.setColor(Color.rgb(210, 218, 228));
        textPaint.setTextAlign(Paint.Align.CENTER);
        rebuild();
    }

    public void setType(ControllerType type) {
        this.type = type;
        rebuild();
    }

    public void setRegionGlow(String id, boolean on) {
        glow.put(id, on ? 1f : 0f);
        invalidate();
    }

    private void rebuild() {
        regions.clear();
        regionById.clear();
        addRegion("ls", 0.30f, 0.70f,  0.05f, "LS");
        addRegion("rs", 0.60f,  0.72f,  0.05f, "RS");
        addRegion("lx", 0.30f,  0.52f,  0.045f, "LX");
        addRegion("ly", 0.30f,  0.58f,  0.045f, "LY");
        if (type == ControllerType.XBOX) {
            addRegion("a", 0.62f,  0.42f,  0.045f, "A");
            addRegion("b", 0.72f,  0.30f,  0.045f, "B");
            addRegion("x", 0.52f,  0.30f,  0.045f, "X");
            addRegion("y", 0.62f,  0.18f,  0.045f, "Y");
            addRegion("lb", 0.34f,  0.10f,  0.035f, "LB");
            addRegion("rb", 0.66f,  0.10f,  0.035f, "RB");
        } else {
            addRegion("a", 0.64f,  0.40f,  0.045f, "A");
            addRegion("b", 0.76f,  0.28f,  0.045f, "B");
            addRegion("x", 0.52f,  0.28f,  0.045f, "X");
            addRegion("y", 0.64f,  0.16f,  0.045f, "Y");
            addRegion("lb", 0.34f,  0.08f,  0.035f, "LB");
            addRegion("rb", 0.66f,  0.08f,  0.035f, "RB");
        }
        addRegion("dpad_up", 0.22f,  0.34f,  0.035f, "");
        addRegion("dpad_down", 0.22f,  0.54f,  0.035f, "");
        addRegion("dpad_left", 0.14f,  0.44f,  0.035f, "");
        addRegion("dpad_right", 0.30f,  0.44f,  0.035f, "");
        if (type != ControllerType.XBOX && type != null) {
            addRegion("touch", 0.50f,  0.07f,  0.06f, "TP");
        }
        for (Region r : regions) regionById.put(r.id, r);
        invalidate();
    }

    private void addRegion(String id, float x, float y, float r, String label) {
        Region region = new Region();
        region.id = id;
        region.x = x;
        region.y = y;
        region.radius = r;
        region.label = label;
        regions.add(region);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;
        cx = getWidth() / 2f;
        cy = getHeight() / 2f;
        scale = Math.min(getWidth(), getHeight() * 0.45f);
        drawBody(canvas);
        for (Region r : regions) drawRegion(canvas, r);
    }

    private void drawBody(Canvas canvas) {
        float w = scale *  1.5f;
        float h = scale *  0.9f;
        RectF body = new RectF(cx - w / 2f, cy - h /  2f, cx + w /  2f, cy + h /  2f);
        canvas.drawRoundRect(body, scale * 0.25f, scale * 0.25f, basePaint);
        canvas.drawRoundRect(body, scale * 0.25f, scale * 0.25f, outlinePaint);
        RectF lt = new RectF(cx - w * 0.75f, cy - h *  0.45f, cx - w * 0.32f, cy + h *  0.45f);
        RectF rt = new RectF(cx + w * 0.32f, cy - h *  0.45f, cx + w *  0.75f, cy + h *  0.45f);
        canvas.drawRoundRect(lt, scale * 0.20f, scale *  0.20f, basePaint);
        canvas.drawRoundRect(rt, scale *  0.20f, scale *  0.20f, basePaint);
        canvas.drawRoundRect(lt, scale *  0.20f, scale *  0.20f, outlinePaint);
        canvas.drawRoundRect(rt, scale *  0.20f, scale *  0.20f, outlinePaint);
    }

    private void drawRegion(Canvas canvas, Region r) {
        float px = cx + (r.x -  0.5f *	2f * scale);
        float py = cy + (r.y -  0.5f * 2f * scale);
        float pr = r.radius * 	2f * scale;
        Float strength = glow.get(r.id);
        float g = strength == null ? 0f : strength;
        if (g >  0.05f) {
            glowPaint.setAlpha((int) (150 * g));
            canvas.drawCircle(px, py, pr * (1f +  0.3f * g), glowPaint);
        }
        canvas.drawCircle(px, py, pr, buttonPaint);
        textPaint.setTextSize(pr * 0.7f);
        canvas.drawText(r.label, px, py - (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent / 2f), textPaint);
    }

    public void handleKeyEvent(int keyCode, boolean down) {
        String id = mapKey(keyCode);
        if (id != null) setRegionGlow(id, down);
    }

    public void handleMotionEvent(MotionEvent event) {
        float lx = event.getAxisValue(MotionEvent.AXIS_X);
        float ly = event.getAxisValue(MotionEvent.AXIS_Y);
        float rx = event.getAxisValue(MotionEvent.AXIS_Z);
        float rz = event.getAxisValue(MotionEvent.AXIS_RZ);
        if (Math.abs(lx) >  0.35f || Math.abs(ly) >  0.35f) setRegionGlow("ls", true);
        if (Math.abs(rx) >  0.35f || Math.abs(rz) >  0.35f) setRegionGlow("rs", true);
        float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (hatY >  0.4f) setRegionGlow("dpad_up", true);
        if (hatY < -0.4f) setRegionGlow("dpad_down", true);
        if (hatX < -0.4f) setRegionGlow("dpad_left", true);
        if (hatX >  0.4f) setRegionGlow("dpad_right", true);
        if (Math.abs(lx) >  0.9f || Math.abs(ly) >  0.9f) setRegionGlow("ls", true);
        if (Math.abs(rx) >  0.9f || Math.abs(rz) >  0.9f) setRegionGlow("rs", true);
    }

    private String mapKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A: return "a";
            case KeyEvent.KEYCODE_BUTTON_B: return "b";
            case KeyEvent.KEYCODE_BUTTON_X: return "x";
            case KeyEvent.KEYCODE_BUTTON_Y: return "y";
            case KeyEvent.KEYCODE_BUTTON_L1: return "lb";
            case KeyEvent.KEYCODE_BUTTON_R1: return "rb";
            case KeyEvent.KEYCODE_BUTTON_THUMBL: return "ls";
            case KeyEvent.KEYCODE_BUTTON_THUMBR: return "rs";
            case KeyEvent.KEYCODE_DPAD_UP: return "dpad_up";
            case KeyEvent.KEYCODE_DPAD_DOWN: return "dpad_down";
            case KeyEvent.KEYCODE_DPAD_LEFT: return "dpad_left";
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "dpad_right";
            default: return null;
        }
    }

    private static final class Region {
        String id;
        float x;
        float y;
        float radius;
        String label;
    }
}

