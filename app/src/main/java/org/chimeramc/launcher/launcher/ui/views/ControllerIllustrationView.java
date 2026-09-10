package org.chimeramc.launcher.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int accentColor = -1;
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
        applyThemeColors();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3f);
        accentPaint.setStyle(Paint.Style.STROKE);
        accentPaint.setStrokeWidth(2.5f);
        glowPaint.setColor((int) 0xFF4AE0A0L);
        textPaint.setTextAlign(Paint.Align.CENTER);
        rebuild();
    }

    private boolean isDarkMode() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private void applyThemeColors() {
        if (isDarkMode()) {
            basePaint.setColor(Color.rgb(38, 42, 50));
            outlinePaint.setColor(Color.rgb(120, 128, 140));
            buttonPaint.setColor(Color.rgb(72, 78, 90));
            textPaint.setColor(Color.rgb(210, 218, 228));
        } else {
            basePaint.setColor(Color.rgb(232, 235, 240));
            outlinePaint.setColor(Color.rgb(110, 120, 130));
            buttonPaint.setColor(Color.rgb(190, 196, 202));
            textPaint.setColor(Color.rgb(60, 64, 72));
        }
    }

    public void setType(ControllerType type) {
        this.type = type;
        rebuild();
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;

        invalidate();
    }

    @Override
    protected void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyThemeColors();
        invalidate();
    }

    public void setRegionGlow(String id, boolean on) {
        glow.put(id, on ? 1f : 0f);
        invalidate();
    }

    private void rebuild() {
        regions.clear();
        regionById.clear();
        if (type == ControllerType.XBOX) {
            addRegion("ls",  0.335f,  0.66f,  0.05f, "LS", Shape.CIRCLE);
            addRegion("rs",  0.665f,  0.70f,  0.05f, "RS", Shape.CIRCLE);
            addRegion("lsRing",  0.335f,  0.66f,  0.055f, "", Shape.STICK_RING);
            addRegion("rsRing",  0.665f,  0.70f,  0.055f, "", Shape.STICK_RING);
            addRegion("dp",  0.25f,  0.48f,  0.042f, "", Shape.DPAD);
            addRegion("a",  0.65f,  0.34f,  0.043f, "A", Shape.FACE_XBOX);
            addRegion("b",  0.75f,  0.27f,  0.043f, "B", Shape.FACE_XBOX);
            addRegion("x",  0.58f,  0.27f,  0.043f, "X", Shape.FACE_XBOX);
            addRegion("y",  0.65f,  0.20f,  0.043f, "Y", Shape.FACE_XBOX);
            addRegion("lb",  0.30f,  0.115f,  0.030f, "LB", Shape.BUMPER);
            addRegion("rb",  0.70f,  0.115f,  0.030f, "RB", Shape.BUMPER);
            addRegion("lt",  0.17f,  0.06f,  0.030f, "", Shape.TRIGGER);
            addRegion("rt",  0.83f,  0.06f,  0.030f, "", Shape.TRIGGER);
            addRegion("menu",  0.50f,  0.465f,  0.024f, "", Shape.CENTER_BUTTON);
            addRegion("view",  0.44f,  0.465f,  0.024f, "", Shape.CENTER_BUTTON);
        } else if (type == ControllerType.DS4) {

            addRegion("ls",  0.335f,  0.62f,  0.05f, "LS", Shape.CIRCLE);
            addRegion("rs",  0.665f,  0.62f,  0.05f, "RS", Shape.CIRCLE);
            addRegion("lsRing",  0.335f,  0.62f,  0.055f, "", Shape.STICK_RING);
            addRegion("rsRing",  0.665f,  0.62f,  0.055f, "", Shape.STICK_RING);
            addRegion("dp",  0.26f,  0.42f,  0.040f, "", Shape.DPAD);
            addRegion("a",  0.76f,  0.30f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("b",  0.70f,  0.38f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("x",  0.64f,  0.30f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("y",  0.70f,  0.22f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("lb",  0.34f,  0.135f,  0.030f, "L1", Shape.BUMPER);
            addRegion("rb",  0.66f,  0.135f,  0.030f, "R1", Shape.BUMPER);
            addRegion("lt",  0.20f,  0.075f,  0.030f, "", Shape.TRIGGER);
            addRegion("rt",  0.80f,  0.075f,  0.030f, "", Shape.TRIGGER);
            addRegion("touch",  0.50f,  0.06f,  0.050f, "", Shape.TOUCHPAD);
            addRegion("share",  0.36f,  0.42f,  0.020f, "", Shape.CENTER_BUTTON);
            addRegion("options",  0.44f,  0.42f,  0.020f, "", Shape.CENTER_BUTTON);
            addRegion("ps",  0.50f,  0.465f,  0.022f, "", Shape.PS_LOGO);
        } else {

            addRegion("ls",  0.335f,  0.62f,  0.05f, "LS", Shape.CIRCLE);
            addRegion("rs",  0.665f,  0.62f,  0.05f, "RS", Shape.CIRCLE);
            addRegion("lsRing",  0.335f,  0.62f,  0.055f, "", Shape.STICK_RING);
            addRegion("rsRing",  0.665f,  0.62f,  0.055f, "", Shape.STICK_RING);
            addRegion("dp",  0.26f,  0.42f,  0.040f, "", Shape.DPAD);
            addRegion("a",  0.76f,  0.30f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("b",  0.70f,  0.38f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("x",  0.64f,  0.30f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("y",  0.70f,  0.22f,  0.040f, "", Shape.FACE_DUAL);
            addRegion("lb",  0.34f,  0.135f,  0.030f, "L1", Shape.BUMPER);
            addRegion("rb",  0.66f,  0.135f,  0.030f, "R1", Shape.BUMPER);
            addRegion("lt",  0.20f,  0.075f,  0.030f, "", Shape.TRIGGER);
            addRegion("rt",  0.80f,  0.075f,  0.030f, "", Shape.TRIGGER);
            addRegion("touch",  0.50f,  0.06f,  0.050f, "", Shape.TOUCHPAD);
            addRegion("share",  0.42f,  0.465f,  0.020f, "", Shape.CENTER_BUTTON);
            addRegion("options",  0.58f,  0.465f,  0.020f, "", Shape.CENTER_BUTTON);
            addRegion("ps",  0.50f,  0.42f,  0.022f, "", Shape.PS_LOGO);
        }
    }
    private void addRegion(String id, float x, float y, float radius, String label, Shape shape) {
        Region region = new Region();
        region.id = id;
        region.x = x;
        region.y = y;
        region.radius = radius;
        region.label = label;
        region.shape = shape;
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
        if (type == ControllerType.XBOX) {
            drawBodyXbox(canvas);
        } else {
            drawBodyPlayStation(canvas);
        }
    }

    private void drawBodyXbox(Canvas canvas) {
        float aw = scale * 0.72f;
        float ah = scale * 0.42f;
        Path p = new Path();
        p.moveTo(cx - aw, cy - ah - scale * 0.10f);
        p.lineTo(cx + aw, cy - ah - scale * 0.10f);
        p.lineTo(cx + aw * 0.80f, cy - ah * 0.30f);
        p.lineTo(cx + aw * 1.30f, cy + ah * 0.60f);
        p.lineTo(cx + aw * 1.05f, cy + ah * 1.35f);
        p.lineTo(cx + aw * 0.60f, cy + ah * 1.10f);
        p.lineTo(cx - aw *  0.60f, cy + ah *  1.10f);
        p.lineTo(cx - aw *  1.05f, cy + ah *  1.35f);
        p.lineTo(cx - aw *  1.30f, cy + ah *  0.60f);
        p.lineTo(cx - aw *  0.80f, cy - ah *  0.30f);
        p.close();
        canvas.drawPath(p, basePaint);
        canvas.drawPath(p, outlinePaint);
    }

    private void drawBodyPlayStation(Canvas canvas) {
        float aw = scale * 0.70f;
        float ah = scale * 0.40f;
        Path p = new Path();
        p.moveTo(cx - aw, cy - ah);
        p.lineTo(cx + aw, cy - ah);
        p.lineTo(cx + aw, cy + ah * 0.55f);
        p.lineTo(cx + aw * 0.90f, cy + ah * 1.20f);
        p.lineTo(cx + aw * 0.55f, cy + ah * 0.95f);
        p.lineTo(cx - aw * 0.55f, cy + ah * 0.95f);
        p.lineTo(cx - aw * 0.90f, cy + ah * 1.20f);
        p.lineTo(cx - aw, cy + ah * 0.55f);
        p.close();
        canvas.drawPath(p, basePaint);
        canvas.drawPath(p, outlinePaint);
        float tx = scale * 0.34f;
        RectF touch = new RectF(cx - tx, cy - ah - scale * 0.16f, cx + tx, cy - ah);
        canvas.drawRoundRect(touch, scale * 0.08f, scale * 0.08f, basePaint);
        canvas.drawRoundRect(touch, scale * 0.08f, scale *  0.08f, outlinePaint);
    }
    private void drawRegion(Canvas canvas, Region r) {
        float px = cx + (r.x -  0.5f *  2f * scale);
        float py = cy + (r.y -  0.5f * 2f * scale);
        float pr = r.radius *   2f * scale;
        Float strength = glow.get(r.id);
        float g = strength == null ? 0f : strength;
        if (g >  0.05f) {
            glowPaint.setColor(accentColor != -1 ? accentColor : (int) 0xFF4AE0A0L);
            glowPaint.setAlpha((int) (150 * g));
            drawShape(canvas, r, px, py, pr * (1f +  0.3f * g), true);
        }
        drawShape(canvas, r, px, py, pr, false);
        if (r.label != null && !r.label.isEmpty()) {
            textPaint.setTextSize(pr * 0.55f);
            float ty = py - (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent / 2f);
            canvas.drawText(r.label, px, ty, textPaint);
        }
    }
    private void drawShape(Canvas canvas, Region r, float px, float py, float pr, boolean glowMode) {
        switch (r.shape) {
            case CIRCLE: drawStick(canvas, px, py, pr, glowMode); break;
            case STICK_RING: drawRing(canvas, px, py, pr, glowMode); break;
            case DPAD: drawDPad(canvas, px, py, pr, glowMode); break;
            case FACE_XBOX: drawFaceXbox(canvas, r, px, py, pr, glowMode); break;
            case FACE_DUAL: drawFaceDual(canvas, r, px, py, pr, glowMode); break;
            case BUMPER: drawBumper(canvas, px, py, pr, glowMode); break;
            case TRIGGER: drawTrigger(canvas, px, py, pr, glowMode); break;
            case CENTER_BUTTON: drawCenterButton(canvas, px, py, pr, glowMode); break;
            case TOUCHPAD: drawTouchpad(canvas, px, py, pr, glowMode); break;
            case PS_LOGO: drawPsLogo(canvas, px, py, pr, glowMode); break;
        }
    }

    private void drawStick(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        canvas.drawCircle(px, py, pr, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawCircle(px, py, pr, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
        }
    }

    private void drawRing(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : outlinePaint);
        canvas.drawCircle(px, py, pr, p);
    }

    private void drawDPad(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        float arm = pr *  0.8f;
        RectF v = new RectF(px - pr *  0.28f, py - arm, px + pr *  0.28f, py + arm);
        RectF h = new RectF(px - arm, py - pr *  0.28f, px + arm, py + pr *  0.28f);
        canvas.drawRoundRect(v, pr *  0.15f, pr *  0.15f, p);
        canvas.drawRoundRect(h, pr *  0.15f, pr *  0.15f, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawRoundRect(v, pr *  0.15f, pr *  0.15f, outlinePaint);
            canvas.drawRoundRect(h, pr *  0.15f, pr *  0.15f, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
        }
    }
    private void drawFaceXbox(Canvas canvas, Region r, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : accentPaint);
        canvas.drawCircle(px, py, pr, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawCircle(px, py, pr, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
            textPaint.setTextSize(pr * 0.8f);
            canvas.drawText(r.label, px, py + pr * 0.1f, textPaint);
        }
    }

    private void drawFaceDual(Canvas canvas, Region r, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : accentPaint);
        canvas.drawCircle(px, py, pr, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawCircle(px, py, pr, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
            accentPaint.setColor(faceColor(r.id));
            accentPaint.setStrokeWidth(pr * 0.16f);
            drawDualSymbol(canvas, r.id, px, py, pr);
            accentPaint.setStrokeWidth(2.5f);
        }
    }

    private int faceColor(String id) {
        if (id.equals("y")) return (int) 0xFFE60012L;
        if (id.equals("b")) return (int) 0xFFF3C518L;
        if (id.equals("a")) return (int) 0xFF00A2E8L;
        return (int) 0xFF00A651L;

    }
    private void drawDualSymbol(Canvas canvas, String id, float px, float py, float pr) {
        Paint p = accentPaint);
        float q = pr * 0.45f;
        if (id.equals("a"))) {
            canvas.drawLine(px - q, py - q, px + q, py + q, p);
            canvas.drawLine(px - q, py + q, px + q, py - q, p);
        } else if (id.equals("b"))) {
            canvas.drawCircle(px, py, q, p);
        } else if (id.equals("x"))) {
            RectF sq = new RectF(px - q, py - q, px + q, py + q);
            canvas.drawRect(sq, p);
        } else if (id.equals("y"))) {
            Path t = new Path();
            t.moveTo(px, py - q);
            t.lineTo(px + q, py + q);
            t.lineTo(px - q, py + q);
            t.close();
            canvas.drawPath(t, p);
        }
    }

    private void drawBumper(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        RectF b = new RectF(px - pr * 2.0f, py - pr * 0.6f, px + pr * 2.0f, py + pr * 0.6f);
        canvas.drawRoundRect(b, pr * 0.4f, pr * 0.4f, p);
    }
    private void drawTrigger(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        RectF t = new RectF(px - pr * 1.6f, py - pr * 0.5f, px + pr * 1.6f, py + pr * 0.5f);
        canvas.drawRoundRect(t, pr * 0.5f, pr * 0.5f, p);
    }

    private void drawCenterButton(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        canvas.drawCircle(px, py, pr, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawCircle(px, py, pr, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
            canvas.drawCircle(px, py, pr * 0.4f, textPaint);
        }
    }

    private void drawTouchpad(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : buttonPaint);
        RectF tp = new RectF(px - pr * 2.2f, py - pr * 0.7f, px + pr * 2.2f, py + pr * 0.7f);
        canvas.drawRoundRect(tp, pr * 0.3f, pr *  0.3f, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawRoundRect(tp, pr *  0.3f, pr *  0.3f, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
            canvas.drawLine(px - pr * 0.5f, py, px + pr * 0.5f, py, textPaint);
        }
    }
    private void drawPsLogo(Canvas canvas, float px, float py, float pr, boolean glowMode) {
        Paint p = glowMode ? glowPaint : accentPaint);
        canvas.drawCircle(px, py, pr, p);
        if (!glowMode) {
            outlinePaint.setStrokeWidth(2f);
            canvas.drawCircle(px, py, pr, outlinePaint);
            outlinePaint.setStrokeWidth(3f);
            textPaint.setTextSize(pr * 0.7f);
            canvas.drawText("PS", px, py + pr * 0.1f, textPaint);
        }
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
        if (hatY >  0.4f) setRegionGlow("dp", true);
        if (hatY < -0.4f) setRegionGlow("dp", true);
        if (hatX < -0.4f) setRegionGlow("dp", true);
        if (hatX >  0.4f) setRegionGlow("dp", true);
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
            case KeyEvent.KEYCODE_DPAD_UP: return "dp";
            case KeyEvent.KEYCODE_DPAD_DOWN: return "dp";
            case KeyEvent.KEYCODE_DPAD_LEFT: return "dp";
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "dp";
            default: return null;
        }
    }

    private enum Shape {
        CIRCLE, STICK_RING, DPAD, FACE_XBOX, FACE_DUAL, BUMPER, TRIGGER, CENTER_BUTTON, TOUCHPAD, PS_LOGO;
    }
    private static final class Region {
        String id;
        float x;
        float y;
        float radius;
        String label;
        Shape shape;
    }
}
