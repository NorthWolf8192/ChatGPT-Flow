package app.chatgpt.flow;

/** Color and timing math shared by the hook and the settings preview. */
public final class Palette {
    private static final int[] DARK_GLOWS = {0xff345994, 0xff246b64, 0xff703e76, 0xff74553c};
    private static final int[] LIGHT_GLOWS = {0xffaad0fa, 0xffacecdd, 0xffe9bdf5, 0xffffd2b4};
    private static final float[] GLOW_OPACITY = {.86f, .80f, .83f, .76f};
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
    public static int[] meshBase(int base, int strength) {
        boolean dark = isDark(base);
        float amount = clamp(strength, 0, 100) / 100f;
        int top = dark ? 0xff1a2944 : 0xffd7e7ff;
        int middle = dark ? 0xff161c31 : 0xfff2f5ff;
        int bottom = dark ? 0xff171526 : 0xfffcf7ff;
        return new int[]{blend(base, top, amount), blend(base, middle, amount), blend(base, bottom, amount)};
    }
    public static int[] meshGlows(int base, int strength) {
        int[] colors = isDark(base) ? DARK_GLOWS : LIGHT_GLOWS;
        float amount = clamp(strength, 0, 100) / 100f;
        int[] glows = new int[colors.length];
        for (int i = 0; i < glows.length; i++) {
            int alpha = Math.round(255 * GLOW_OPACITY[i] * amount);
            glows[i] = (alpha << 24) | (colors[i] & 0xffffff);
        }
        return glows;
    }
    public static long durationMs(int speed) {
        return Math.round(60000.0 / (.5 + clamp(speed, 1, 100) * .055));
    }
}
