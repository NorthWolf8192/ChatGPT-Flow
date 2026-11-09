package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;

public final class FlowRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final long start = SystemClock.uptimeMillis();
    private LinearGradient gradient, blurred;
    private int[] colors;
    private float width, height;
    private float blurRadius;
    private int base, strength = -1;
    private long phaseTime = Long.MIN_VALUE;
    private int phaseSpeed;

    public void draw(Canvas canvas, float w, float h, int background, boolean enabled,
                     int speed, int intensity) {
        draw(canvas, w, h, background, background, enabled, speed, intensity);
    }

    public void draw(Canvas canvas, float w, float h, int background, int original, boolean enabled,
                     int speed, int intensity) {
        drawAt(canvas,w,h,background,original,enabled,speed,intensity,SystemClock.uptimeMillis());
    }

    public void drawAt(Canvas canvas, float w, float h, int background, int original, boolean enabled,
                       int speed, int intensity, long time) {
        if (w <= 0 || h <= 0) return;
        if (!enabled || intensity == 0) {
            paint.setShader(null);
            paint.setColor(original);
        } else {
            prepare(w,h,background,intensity,time,speed);
            paint.setColor(0xffffffff);
            if (paint.getShader() != gradient) paint.setShader(gradient);
        }
        canvas.drawRect(0,0,w,h,paint);
    }

    Shader glassShader(float w,float h,int background,int intensity,long time,int speed,float radius) {
        if (intensity == 0) return null;
        prepare(w,h,background,intensity,time,speed);
        if (radius <= 0) return gradient;
        if (blurred == null || radius != blurRadius) {
            float period = (float)Math.hypot(w * 1.6f, h * .8f);
            blurred = new LinearGradient(0,0,w * 1.6f,h * .8f,
                    Palette.blurColors(colors,radius / period),null,Shader.TileMode.REPEAT);
            blurred.setLocalMatrix(matrix);
            blurRadius = radius;
        }
        return blurred;
    }

    private void prepare(float w,float h,int background,int intensity,long time,int speed) {
        if (gradient == null || width != w || height != h || base != background || strength != intensity) {
            width = w; height = h; base = background; strength = intensity;
            colors = Palette.colors(background, intensity);
            gradient = new LinearGradient(0,0,w * 1.6f,h * .8f,colors,null,Shader.TileMode.REPEAT);
            blurred = null;
            phaseTime = Long.MIN_VALUE;
        }
        if (phaseTime == time && phaseSpeed == speed) return;
        float phase = Palette.phase(time, start, speed);
        matrix.setTranslate(-phase * w * 1.6f, -phase * h * .8f);
        gradient.setLocalMatrix(matrix);
        if (blurred != null) blurred.setLocalMatrix(matrix);
        phaseTime = time;
        phaseSpeed = speed;
    }
}
