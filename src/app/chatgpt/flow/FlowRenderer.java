package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;

public final class FlowRenderer {
    private static final float[] FALLOFF = {0f, .48f, .78f, 1f};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Matrix translation = new Matrix();
    private final long start = SystemClock.uptimeMillis();
    private final Glow[] glows = {
        new Glow(.25f, .16f, .38f, .16f, .56f, .83f, .1f),
        new Glow(.81f, .38f, .37f, .24f, .50f, 1.07f, .3f),
        new Glow(.57f, .76f, .42f, .24f, .58f, 1.24f, .55f),
        new Glow(.10f, .90f, .37f, .17f, .51f, 1.49f, .76f)
    };
    private LinearGradient background;
    private BackdropBlur blur;
    private boolean blurUnavailable;
    private float width, height;
    private int base, strength = -1;
    private long phaseTime = Long.MIN_VALUE;
    private int phaseSpeed;

    public void draw(Canvas canvas, float w, float h, int base, boolean enabled,
                     int speed, int intensity) {
        draw(canvas, w, h, base, base, enabled, speed, intensity);
    }

    public void draw(Canvas canvas, float w, float h, int base, int original, boolean enabled,
                     int speed, int intensity) {
        drawAt(canvas, w, h, base, original, enabled, speed, intensity, SystemClock.uptimeMillis());
    }

    public void drawAt(Canvas canvas, float w, float h, int base, int original, boolean enabled,
                       int speed, int intensity, long time) {
        if (w <= 0 || h <= 0) return;
        if (!enabled || intensity == 0) {
            fill(canvas, 0, 0, w, h, original);
            return;
        }
        prepare(w, h, base, intensity, time, speed);
        drawScene(canvas, 0, 0, w, h);
    }

    // The canvas is already clipped to the surface and translated to backdrop coordinates.
    void drawGlass(Canvas canvas, RectF bounds, float w, float h, int base, int intensity,
                   long time, int speed, float radius) {
        if (intensity == 0) {
            fill(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom, base);
            return;
        }
        prepare(w, h, base, intensity, time, speed);
        if (radius > 0 && !blurUnavailable && canvas.isHardwareAccelerated()) {
            try {
                if (blur == null) blur = new BackdropBlur();
                blur.draw(canvas, this, w, h, radius, bounds);
                return;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
                blurUnavailable = true;
                blur = null;
            }
        }
        drawScene(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    void drawScene(Canvas canvas, float left, float top, float right, float bottom) {
        paint.setColor(0xffffffff);
        paint.setShader(background);
        canvas.drawRect(left, top, right, bottom, paint);
        for (Glow glow : glows) {
            if (glow.x + glow.radius < left || glow.x - glow.radius > right
                    || glow.y + glow.radius < top || glow.y - glow.radius > bottom) continue;
            paint.setShader(glow.shader);
            canvas.drawCircle(glow.x, glow.y, glow.radius, paint);
        }
    }

    private void fill(Canvas canvas, float left, float top, float right, float bottom, int color) {
        paint.setShader(null);
        paint.setColor(color);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void prepare(float w, float h, int base, int intensity, long time, int speed) {
        if (background == null || width != w || height != h || this.base != base || strength != intensity) {
            if (width != w || height != h) blur = null;
            width = w;
            height = h;
            this.base = base;
            strength = intensity;
            background = new LinearGradient(0, 0, w * .34f, h, Palette.meshBase(base, intensity),
                    null, Shader.TileMode.CLAMP);
            float extent = Math.max(w, h);
            int[] colors = Palette.meshGlows(base, intensity);
            for (int i = 0; i < glows.length; i++) glows[i].updateShader(extent, colors[i]);
            phaseTime = Long.MIN_VALUE;
        }
        if (phaseTime == time && phaseSpeed == speed) return;
        long duration = Palette.durationMs(speed);
        for (Glow glow : glows) glow.move(w, h, time - start, duration, translation);
        phaseTime = time;
        phaseSpeed = speed;
        if (blur != null) blur.invalidate();
    }

    private static final class Glow {
        private final float centerX, centerY, travelX, travelY, size, period, offset;
        private float x, y, radius;
        private RadialGradient shader;

        Glow(float centerX, float centerY, float travelX, float travelY, float size,
             float period, float offset) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.travelX = travelX;
            this.travelY = travelY;
            this.size = size;
            this.period = period;
            this.offset = offset;
        }

        void updateShader(float extent, int color) {
            radius = extent * size;
            int rgb = color & 0xffffff;
            int alpha = color >>> 24;
            shader = new RadialGradient(0, 0, radius, new int[]{
                    color, (Math.round(alpha * .51f) << 24) | rgb,
                    (Math.round(alpha * .12f) << 24) | rgb, rgb
            }, FALLOFF, Shader.TileMode.CLAMP);
        }

        void move(float width, float height, long elapsed, long duration, Matrix translation) {
            long cycle = Math.round(duration * period);
            double angle = (Math.floorMod(elapsed, cycle) / (double) cycle + offset) * 2 * Math.PI;
            x = width * (centerX + travelX * (float) Math.sin(angle));
            y = height * (centerY + travelY * (float) Math.cos(angle));
            translation.setTranslate(x, y);
            shader.setLocalMatrix(translation);
        }
    }
}
