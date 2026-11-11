package app.chatgpt.flow;

class PaletteTest {
    private static double luminance(int color) {
        double[] weights = {.2126, .7152, .0722};
        double sum = 0;
        for (int channel = 0; channel < weights.length; channel++) {
            double component = ((color >>> (16 - channel * 8)) & 255) / 255.;
            sum += weights[channel] * (component <= .04045
                    ? component / 12.92 : Math.pow((component + .055) / 1.055, 2.4));
        }
        return sum;
    }

    public static void main(String[] args) {
        int tested = 0;
        for (int base : new int[]{0xff000000, 0xff171717, 0xff212121, 0xfff9f9f9, 0xffffffff}) {
            for (int strength = 0; strength <= 100; strength++) {
                int[] stops = Palette.meshBase(base, strength);
                int[] glows = Palette.meshGlows(base, strength);
                for (int stop : stops) {
                    // Each glow may be absent or at maximum alpha at a given pixel.
                    for (int mask = 0; mask < 1 << glows.length; mask++) {
                        int color = stop;
                        for (int i = 0; i < glows.length; i++) {
                            if ((mask & (1 << i)) != 0) {
                                color = Palette.blend(color, glows[i], (glows[i] >>> 24) / 255f);
                            }
                        }
                        double light = luminance(color);
                        double contrast = Palette.isDark(base) ? 1.05 / (light + .05) : (light + .05) / .05;
                        if (contrast < 4.5) throw new AssertionError("Contrast: " + contrast);
                        if ((color >>> 24) != 255) throw new AssertionError("Background must be opaque");
                        if (strength == 0 && color != base) throw new AssertionError("Zero strength");
                        tested++;
                    }
                }
            }
        }
        for (int speed = 2; speed <= 100; speed++) {
            if (Palette.durationMs(speed) >= Palette.durationMs(speed - 1)) {
                throw new AssertionError("Increasing speed must shorten the cycle");
            }
        }
        System.out.println("PASS: " + tested + " mesh palette samples, text contrast and speed settings");
    }
}
