package app.chatgpt.flow;

/** Color and timing math shared by the hook and the settings preview. */
public final class Palette {
    private static final int[] RAINBOW = {
        0xffff4f9c, 0xffff9455, 0xffffdf65, 0xff57e3ac,
        0xff53c7ff, 0xff8b7aff, 0xffe66bff, 0xffff4f9c
    };
    private static final int[] BLUR_WEIGHTS = {1, 6, 15, 20, 15, 6, 1};
    private Palette() {}
    public static int clamp(int n, int low, int high) {
        return Math.max(low, Math.min(high, n));
    }
    public static boolean isDark(int color) {
        return (((color >> 16) & 255) * 299 + ((color >> 8) & 255) * 587
                + (color & 255) * 114) < 128000;
    }
    public static int blend(int a, int b, float f) {
        f = Math.max(0f, Math.min(1f, f));
        int r = Math.round(((a >> 16) & 255) * (1-f) + ((b >> 16) & 255) * f);
        int g = Math.round(((a >> 8) & 255) * (1-f) + ((b >> 8) & 255) * f);
        int bl = Math.round((a & 255) * (1-f) + (b & 255) * f);
        return 0xff000000 | (r << 16) | (g << 8) | bl;
    }
    public static int[] colors(int base, int strength) {
        boolean dark = isDark(base);
        int anchor = dark ? blend(base, 0xff10121c, .58f) : blend(base, 0xffffffff, .58f);
        float amount = (dark ? .40f : .36f) * clamp(strength, 0, 100) / 100f;
        int[] result = new int[RAINBOW.length];
        for (int i=0; i<result.length; i++) result[i] = blend(anchor, RAINBOW[i], amount);
        return result;
    }
    /** Smooth a repeating linear gradient in one dimension, with a small Gaussian approximation. */
    public static int[] blurColors(int[] colors, float radiusInPeriods) {
        if (radiusInPeriods <= 0) return colors;
        int intervals = colors.length - 1;
        int samples = intervals * 12;
        float step = radiusInPeriods * intervals * .5f;
        int[] blurred = new int[samples + 1];
        for (int i = 0; i < samples; i++) {
            float red = 0, green = 0, blue = 0;
            for (int tap = 0; tap < BLUR_WEIGHTS.length; tap++) {
                double position = i / 12f + (tap - 3) * step;
                position -= Math.floor(position / intervals) * intervals;
                int index = Math.min((int)position, intervals - 1);
                int color = blend(colors[index], colors[index + 1], (float)(position - index));
                int weight = BLUR_WEIGHTS[tap];
                red += weight * ((color >> 16) & 255);
                green += weight * ((color >> 8) & 255);
                blue += weight * (color & 255);
            }
            blurred[i] = 0xff000000 | (Math.round(red / 64) << 16)
                    | (Math.round(green / 64) << 8) | Math.round(blue / 64);
        }
        blurred[samples] = blurred[0];
        return blurred;
    }
    public static long durationMs(int speed) {
        return Math.round(60000.0 / (.5 + clamp(speed, 1, 100) * .055));
    }
    public static float phase(long now, long start, int speed) {
        long duration = durationMs(speed);
        return Math.floorMod(now-start, duration) / (float)duration;
    }
}
