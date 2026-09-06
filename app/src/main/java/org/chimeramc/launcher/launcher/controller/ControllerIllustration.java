package org.chimeramc.launcher.launcher.controller;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class ControllerIllustration {
    public enum Shape { CIRCLE, ROUND_RECT }

    public static class Region {
        public final String tag;
        public final float cx, cy, rx, ry;
        public final Shape shape;
        public Region(String tag, float cx, float cy, float rx, float ry, Shape shape){
            this.tag = tag;
            this.cx = cx;
            this.cy = cy;
            this.rx = rx;
            this.ry = ry;
            this.shape = shape;
        }
    }

    private static final int VIEWPORT_W = 300;
    private static final int VIEWPORT_H = 200;

    private final Context context;
    private final FrameLayout holder;
    private final float scale;
    private final Map<String, View> overlays = new HashMap<>();
    private final Map<String, TextView> labels = new HashMap<>();

    public ControllerIllustration(Context context, FrameLayout holder, int baseRes){
        this.context = context;
        this.holder = holder;
        holder.removeAllViews();
        float w = holder.getWidth();
        if(w <= 0){
            int maxW = context.getResources().getDisplayMetrics().widthPixels;
            w = Math.min(maxW - dp(48), dp(400));
        }
        float h = w * VIEWPORT_H / (float) VIEWPORT_W;
        scale = w / VIEWPORT_W;
        ImageView base = new ImageView(context);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams((int)w, (int)h);
        blp.gravity = Gravity.CENTER;
        base.setLayoutParams(blp);
        base.setImageResource(baseRes);
        base.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.addView(base);
    }

    private int dp(float v){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, context.getResources().getDisplayMetrics());
    }

    public void addRegion(Region r){
        float pw = 2 * r.rx * scale;
        float ph = 2 * r.ry * scale;
        ImageView v = new ImageView(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)pw, (int)ph);
        lp.leftMargin = (int)((r.cx - r.rx) * scale);
        lp.topMargin = (int)((r.cy - r.ry) * scale);
        v.setLayoutParams(lp);
        v.setBackground(shapeDrawable(r));
        v.setAlpha(0f);
        v.setClickable(true);
        v.setOnClickListener(ev -> {
            Toast.makeText(context, labelFor(r.tag), Toast.LENGTH_SHORT).show();
        });
        holder.addView(v);
        overlays.put(r.tag, v);
    }

    public void addLabel(String text, float cx, float cy, boolean above){
        TextView t = new TextView(context);
        t.setText(text);
        t.setTextColor(Color.GRAY);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        t.setIncludeFontPadding(false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        float labelW = t.getPaint().measureText(text);
        float labelH = dp(12);
        lp.leftMargin = (int)(cx * scale - labelW / 2);
        lp.topMargin = (int)((above ? cy - 12 : cy + 4) * scale);
        t.setLayoutParams(lp);
        holder.addView(t);
        labels.put(text, t);
    }

    public void highlight(String tag){
        View v = overlays.get(tag);
        if(v != null){
            v.animate().alpha(1f).setDuration(60).start();
        }
    }

    public void clear(String tag){
        View v = overlays.get(tag);
        if(v != null){
            v.animate().alpha(0f).setDuration(60).start();
        }
    }

    public void clearAll(){
        for ( View v : overlays.values()){
            v.animate().alpha(0f).setDuration(60).start();
        }
    }

    public String labelFor(String tag){
        return tag;
    }

    private GradientDrawable shapeDrawable(Region r){
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x33FFD740);
        d.setStroke(dp(2), 0xFFFFD740);
        if(r.shape == Shape.CIRCLE){
            d.setShape(GradientDrawable.OVAL);
        }else{
            d.setCornerRadius(dp(6));
        }
        return d;
    }
}
