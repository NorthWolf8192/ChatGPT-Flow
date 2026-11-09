package app.chatgpt.flow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Compose names differ between the two APKs labeled 1.2026.307. */
final class ComposeSymbols {
    private static final Map<String, String> BUILD_2630717 = aliases(
            "g5i0 u2i0", "a1q c0q", "k771 r971", "pib0 lhb0", "akf kjf", "yjf jjf",
            "f5i0 t2i0", "b600 y400", "wjf hjf", "ec60 x960", "qwj0 guj0",
            "ew2 dw2", "sw2 rw2", "y6x v6x", "p550 e350", "vox0 rqx0",
            "x3t0 i5t0", "isv0 tbx0", "mut cut", "cja0 x5w", "cja0#x x5w#h",
            "o500 m400", "io.sentry.compose.e io.sentry.compose.d",
            "bl7#F kga0#E", "bl7#q vla0#i", "kha0#a0 rp41#U",
            "qwj0#e1 guj0#d1", "androidx.compose.ui.node.LayoutNode.N1 P1",
            "qod1 o2e1", "xwa jxa", "t0u f0u", "tzt hzt", "yd01 dg01",
            "tpb0 nob0", "ijn0 jkn0", "j3c1 z5c1", "nst xrt", "dt51 kv51",
            "lqf0 bia0", "ppe jbv", "grf1#e fvf1#h", "x6z0 h9z0",
            "bu00 ms00", "au00 ls00", "au00#W0 ls00#X0",
            "vb6 gc6", "p5y0 n7y0", "r5y0 p7y0",
            "nje#d ij70#a", "z9q y8q", "a181#H cwq#b", "sz2#l g1q#x",
            "kff0 ydf0", "yau tau", "nt51 uv51", "ot51 vv51", "pt51 wv51",
            "h3w0#a gex0#b", "xa01 dd01", "kfi0 xci0", "nei0 eci0",
            "aq00#h bia0#n", "fy91#f r7u#d", "nbf1 cif1", "nqe1 tve1", "vof1#b qsf1#p",
            "umt0#m eo2#A", "umt0#n eo2#B");

    final int build;
    private final ClassLoader loader;
    private final Map<String, String> names;

    private ComposeSymbols(ClassLoader loader, int build, Map<String, String> names) {
        this.loader = loader;
        this.build = build;
        this.names = names;
    }

    static ComposeSymbols select(ClassLoader loader) throws ReflectiveOperationException {
        ComposeSymbols legacy = new ComposeSymbols(loader, 2630709, Collections.emptyMap());
        ComposeSymbols current = new ComposeSymbols(loader, 2630717, BUILD_2630717);
        if (legacy.matches()) return legacy;
        if (current.matches()) return current;
        throw new ClassNotFoundException("No Compose mapping matched builds 2630709 or 2630717");
    }

    private boolean matches() {
        try {
            Class<?> modifier = type("g5i0");
            return modifier.isInterface()
                    && modifier.isAssignableFrom(type("o500"))
                    && modifier.getMethod("C", modifier).getReturnType() == modifier;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(names.getOrDefault(name, name), false, loader);
    }

    Method method(String owner, String name, Class<?>... parameters) throws ReflectiveOperationException {
        String mapped = methodAddress(owner, name);
        int separator = mapped.indexOf('#');
        return type(mapped.substring(0, separator)).getDeclaredMethod(mapped.substring(separator + 1), parameters);
    }

    Class<?> methodOwner(String owner, String name) throws ClassNotFoundException {
        String mapped = methodAddress(owner, name);
        return type(mapped.substring(0, mapped.indexOf('#')));
    }

    String methodName(String owner, String name) {
        String mapped = methodAddress(owner, name);
        return mapped.substring(mapped.indexOf('#') + 1);
    }

    private String methodAddress(String owner, String name) {
        String key = owner + "#" + name;
        return names.getOrDefault(key, key);
    }

    Field field(String owner, String name) throws ReflectiveOperationException {
        Field field = type(owner).getDeclaredField(names.getOrDefault(owner + "." + name, name));
        field.setAccessible(true);
        return field;
    }

    private static Map<String, String> aliases(String... entries) {
        Map<String, String> result = new HashMap<>();
        for (String entry : entries) {
            int split = entry.indexOf(' ');
            result.put(entry.substring(0, split), entry.substring(split + 1));
        }
        return Collections.unmodifiableMap(result);
    }
}
