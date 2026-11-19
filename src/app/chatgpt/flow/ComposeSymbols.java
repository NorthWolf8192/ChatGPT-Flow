package app.chatgpt.flow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Compose names and call signatures for supported ChatGPT builds. */
final class ComposeSymbols {
    static final int BUILD_WITH_SHARED_GRADIENT = 2631421;
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

    private static final Map<String, String> BUILD_2630718 = aliases(
            "g5i0 i2i0", "a1q a0q", "k771 aa71", "pib0 rgb0", "akf djf", "yjf cjf",
            "f5i0 h2i0", "b600 u400", "wjf ajf", "ec60 k960", "qwj0 jtj0",
            "ol2 il2", "ew2 rv2", "sw2 ew2", "y6x k6x", "p550 y250", "vox0 vpx0",
            "x3t0 h5t0", "isv0#a fo00#k", "mut wtt", "cja0#x p4t#w",
            "o500 h400", "io.sentry.compose.e io.sentry.compose.d",
            "bl7#F q381#j0", "bl7#q t1g#p", "kha0#a0 yso#u0",
            "qwj0#e1 jtj0#d1", "androidx.compose.ui.node.LayoutNode.N1 P1",
            "qod1#f d3e1#h", "qod1#d d3e1#f", "xwa wwa",
            "t0u b0u", "tzt dzt", "yd01 eg01", "tpb0 snb0", "ijn0 xjn0", "j3c1 r5c1",
            "nst urt", "dt51 hw51", "lqf0#J fo00#w", "ppe pu1", "grf1#e jwf1#a", "x6z0 y7z0",
            "bu00 ks00", "au00 js00", "au00#W0 js00#X0",
            "vb6 ob6", "p5y0 b6y0", "r5y0 d6y0",
            "nje#d mbd1#b", "z9q q8q", "a181#H jq41#d", "sz2#l i9x0#x",
            "kff0 fdf0", "yau mau", "nt51 rw51", "ot51 sw51", "pt51 tw51",
            "h3w0#a kaz0#b", "xa01 yc01", "kfi0 fci0", "nei0 kbi0",
            "aq00#h jot0#e", "fy91#f q381#d", "nbf1#c ajf1#a", "nqe1 qve1",
            "vof1#b zsf1#c", "umt0#m z8e0#z", "umt0#n z8e0#A");

    private static final Map<String, String> BUILD_2631421 = aliases(
            "g5i0 tvj0", "a1q hwq", "k771 zy91", "pib0 r2d0", "akf iyf", "yjf gyf",
            "f5i0 svj0", "b600 se10", "wjf eyf", "ec60 bl70", "qwj0 bol0",
            "ol2 mt2", "ol2#s mt2#r", "ew2 t43", "sw2 f53", "y6x mdy", "p550 xd60", "vox0 lb01",
            "x3t0#c mv70#K", "isv0#a bz10#l", "mut#f fvu#g", "cja0#x mnp#t", "o500 ge10",
            "io.sentry.compose.e io.sentry.compose.d", "bl7#F l701#v", "kha0#a0 q6b#T",
            "qwj0#e1 bol0#d1", "androidx.compose.ui.node.LayoutNode.N1 R1",
            "qod1#f aii1#d", "qod1#d aii1#b", "xwa x4b",
            "t0u q1v", "yd01 q231", "tpb0 r9d0", "ijn0 eop0", "j3c1 d0f1",
            "nst itu", "dt51 zk81", "lqf0#J c901#F", "grf1#e ubi1#d", "x6z0 vu11",
            "bu00 g320", "au00 f320", "vb6#z ggk#x", "p5y0 xs01", "r5y0 zs01",
            "sz2#l r9a#f", "yau dcv", "nt51 il81", "ot51 jl81",
            "xa01#f h031#e", "kfi0 b7k0", "nei0 g6k0", "aq00#h t761#d", "fy91#f mm11#e",
            "nbf1#c f9h1#f", "nqe1 gxh1", "vof1#b q6i1#h");

    private static final Map<String, String> BUILD_2631424 = aliases(
            "g5i0 jwj0", "a1q ivq", "k771 p0a1", "pib0 t3d0", "akf mxf", "yjf kxf",
            "f5i0 iwj0", "b600 gd10", "wjf ixf", "ec60 kk70", "qwj0 hpl0",
            "ol2 mt2", "ol2#s mt2#r", "ew2 u43", "sw2 h53", "y6x lby", "p550 tc60", "vox0 eb01",
            "x3t0#c hrw0#w", "isv0#a emp#f", "mut#f fuu#g", "cja0#x h701#p", "o500 vc10",
            "io.sentry.compose.e io.sentry.compose.d", "bl7#F sza1#u", "kha0#a0 y6b#a0",
            "qwj0#e1 hpl0#d1", "androidx.compose.ui.node.LayoutNode.N1 R1",
            "qod1#f hli1#h", "qod1#d hli1#f", "xwa g5b", "ewj0 uwj0", "p4b z4b",
            "t0u i0v", "yd01 u131", "tpb0 sad0", "ijn0 opp0", "j3c1 l2f1",
            "nst hsu", "dt51 nm81", "lqf0#J mm91#q0", "grf1#e yfi1#a", "x6z0 iu11",
            "bu00 d220", "au00 c220", "au00#W0 c220#X0", "vb6#z ndg#w", "p5y0 ls01", "r5y0 ns01",
            "sz2#l fnj0#c", "yau yav", "nt51 wm81", "ot51 xm81", "xa01#f iz21#e",
            "kfi0 w7k0", "nei0 a7k0", "nbf1#c bfh1#b",
            "v0v lzu", "wnd0 zs11", "g780 dcw", "yz6 zod0", "zsa0 dta0", "om50 ml50", "c1c0#o yu70#k");

    private static final Map<String, String> BUILD_2631428 = aliases(
            "g5i0 dvj0", "a1q mvq", "k771 h1a1", "pib0 a3d0", "akf qxf", "yjf oxf",
            "f5i0 cvj0", "b600 sc10", "wjf mxf", "ec60 uj70", "qwj0 vnl0",
            "ol2 ct2", "ol2#s ct2#r", "ew2 m43", "sw2 z43", "y6x hby", "p550 fc60", "vox0 lb01",
            "x3t0#c fn91#L", "isv0#a rwa1#w", "mut#f auu#g", "cja0#x lg71#r", "o500 hc10",
            "io.sentry.compose.e io.sentry.compose.d", "bl7#F nrw0#w", "kha0#a0 o8b1#k0",
            "qwj0#e1 vnl0#d1", "androidx.compose.ui.node.LayoutNode.N1 R1",
            "qod1#f mmi1#c", "qod1#d mmi1#a", "xwa r5b", "ewj0 ovj0", "p4b i5b",
            "t0u b0v", "yd01 i331", "tpb0 aad0", "ijn0 kop0", "j3c1 t4f1",
            "nst esu", "dt51 fn81", "lqf0#J o8b1#S", "grf1#e fei1#b", "x6z0 rv11",
            "bu00 q120", "au00 p120", "au00#W0 p120#X0", "vb6#z ndg#w", "p5y0 bt01", "r5y0 dt01",
            "sz2#l xwq#f", "yau oav", "nt51 on81", "ot51 pn81", "xa01#f u031#e",
            "kfi0 m6k0", "nei0 p5k0", "fy91#f rwa1#c", "nbf1#c u6h1#a",
            "v0v gzu", "wnd0 mu11", "g780 cbd0", "yz6#invoke q8g1#I",
            "zsa0 psa0", "om50 wk50", "c1c0#o fn91#r");

    private static final Map<String, String> BUILD_2631431 = aliases(
            "g5i0 yuj0", "a1q qvq", "k771 i1a1", "pib0 o1d0", "akf kxf", "yjf ixf",
            "f5i0 xuj0", "b600 id10", "wjf gxf", "ec60 hj70", "qwj0 rnl0",
            "ol2 et2", "ol2#s et2#r", "ew2 n43", "sw2 z43", "y6x gcy", "p550 ec60", "vox0 pc01",
            "x3t0#c qvc1#B", "isv0#a d0b1#k", "mut#f cvu#g", "cja0#x y1w0#a0", "o500 wc10",
            "io.sentry.compose.e io.sentry.compose.d", "bl7#F sy80#x", "kha0#a0 hf71#V",
            "qwj0#e1 rnl0#d1", "androidx.compose.ui.node.LayoutNode.N1 R1",
            "qod1#f jmi1#h", "qod1#d jmi1#f", "xwa p3b", "ewj0 kvj0", "p4b j3b",
            "t0u p1v", "yd01 c331", "tpb0 l8d0", "ijn0 znp0", "j3c1 w2f1",
            "nst etu", "dt51 bm81", "lqf0#J ga01#t", "grf1#e qhi1#c", "x6z0 nv11",
            "bu00 v120", "au00 u120", "au00#W0 u120#W0", "vb6#z mfk#x", "p5y0 tt01", "r5y0 vt01",
            "sz2#l wwq#c", "yau zbv", "nt51 km81", "ot51 lm81", "xa01#f q031#e",
            "kfi0 i6k0", "nei0 p5k0", "nbf1#c qgh1#k",
            "v0v t0v", "wnd0 rrl", "g780 xpf", "yz6#invoke zt31#I",
            "zsa0 csa0", "om50 cl50", "c1c0#o b5b#v");

    private static final Map<String, String> BUILD_2631432 = aliases(
            "g5i0 j0k0", "a1q pyq", "k771 j9a1", "pib0 c7d0", "akf o0g", "yjf m0g",
            "f5i0 i0k0", "b600 wf10", "wjf k0g", "ec60 ro70", "qwj0 dtl0",
            "ol2 kt2", "ol2#s kt2#r", "ew2 p43", "sw2 c53", "y6x mey", "p550 gh60", "vox0 bi01",
            "x3t0#c dg61#B", "isv0#a qm71#i", "mut#f gxu#g", "cja0#x qm71#x", "o500 lf10",
            "io.sentry.compose.e io.sentry.compose.d", "bl7#F hsk0#x", "kha0#a0 t4b1#V",
            "qwj0#e1 dtl0#d1", "androidx.compose.ui.node.LayoutNode.N1 R1",
            "qod1#f zti1#c", "qod1#d zti1#a", "xwa r6b", "ewj0 u0k0", "p4b k6b",
            "t0u h3v", "yd01 f931", "tpb0 ged0", "ijn0 vtp0", "j3c1 ncf1",
            "nst hvu", "dt51 ct81", "lqf0#J sgb1#W", "grf1#e nli1#c", "x6z0 i121",
            "bu00 m520", "au00 l520", "au00#W0 l520#X0", "vb6#z qgg#w", "p5y0 oz01", "r5y0 qz01",
            "sz2#l zzq#q", "yau tdv", "nt51 lt81", "ot51 mt81", "xa01#f r631#e",
            "kfi0 mbk0", "nei0 sak0", "nbf1#c pdh1#b",
            "v0v m2v", "wnd0 usu0", "g780 b41", "yz6#invoke vfg1#I",
            "zsa0 zwa0", "om50 dq50", "c1c0#o qm71#o");

    private static final Map<String, String> BUILD_2632100 = aliases(
            "g5i0 dtn0", "g5i0#C dtn0#A", "a1q jlr", "k771 n4c1", "pib0 nvg0",
            "akf w7g", "yjf v7g", "f5i0 ctn0", "b600 h150", "wjf t7g",
            "ec60 uab0", "qwj0 bnp0", "qwj0#R bnp0#Q", "ol2 sv2", "ol2#s sv2#q", "ol2#o sv2#n",
            "ew2 o63", "sw2 c73", "y6x q7z", "p550 y3a0", "vox0 pc21",
            "x3t0#c qtf0#v", "isv0#a jx11#a", "mut#f nov#g", "cja0#x ytp#K", "o500 w050",
            "bl7#F kf81#F", "kha0#a0 z5d1#y0", "qwj0#e1 bnp0#c1", "androidx.compose.ui.node.LayoutNode.N1 R1", "qod1#f v0l1#f",
            "qod1#d ptn0#invoke", "xwa hcb", "ewj0 qtn0", "p4b wbb", "t0u vuv",
            "t0u#l vuv#m", "yd01 f751", "tpb0 j2h0", "ijn0 tcr0", "j3c1 kfh1",
            "nst pmv", "dt51 uqa1", "lqf0#J orx#A", "grf1#e bzj1#i", "x6z0 xx31",
            "bu00 sp50", "au00 rp50", "au00#W0 rp50#W0", "au00#w1 rp50#v1", "vb6#z eog#v",
            "p5y0 gv21", "r5y0 iv21", "sz2#l rqj1#g", "yau i5w", "nt51 dra1",
            "ot51 era1", "xa01#f s451#f", "kfi0 w4o0", "fy91#f lvx0#f", "nbf1#c ouk1#b",
            "v0v auv", "wnd0 if60", "g780 r51", "yz6#invoke t1z#I", "zsa0 ole0",
            "om50 qc90", "c1c0#o mqj0#l");

    final int build;
    private final ClassLoader loader;
    private final Map<String, String> names;

    private ComposeSymbols(ClassLoader loader, int build, Map<String, String> names) {
        this.loader = loader;
        this.build = build;
        this.names = names;
    }

    static ComposeSymbols select(ClassLoader loader) throws ReflectiveOperationException {
        ComposeSymbols build2632100 = new ComposeSymbols(loader, 2632100, BUILD_2632100);
        if (build2632100.matches()) return build2632100;
        ComposeSymbols build2631432 = new ComposeSymbols(loader, 2631432, BUILD_2631432);
        if (build2631432.matches()) return build2631432;
        ComposeSymbols build2631431 = new ComposeSymbols(loader, 2631431, BUILD_2631431);
        if (build2631431.matches()) return build2631431;
        ComposeSymbols build2631428 = new ComposeSymbols(loader, 2631428, BUILD_2631428);
        if (build2631428.matches()) return build2631428;
        ComposeSymbols build2631424 = new ComposeSymbols(loader, 2631424, BUILD_2631424);
        if (build2631424.matches()) return build2631424;
        ComposeSymbols build2631421 = new ComposeSymbols(loader, BUILD_WITH_SHARED_GRADIENT, BUILD_2631421);
        if (build2631421.matches()) return build2631421;
        ComposeSymbols legacy = new ComposeSymbols(loader, 2630709, Collections.emptyMap());
        if (legacy.matches()) return legacy;
        ComposeSymbols build2630717 = new ComposeSymbols(loader, 2630717, BUILD_2630717);
        if (build2630717.matches()) return build2630717;
        ComposeSymbols build2630718 = new ComposeSymbols(loader, 2630718, BUILD_2630718);
        if (build2630718.matches()) return build2630718;
        throw new ClassNotFoundException("No Compose mapping matched builds 2630709, 2630717, 2630718, 2631421, 2631424, 2631428, 2631431, 2631432 or 2632100");
    }

    boolean usesSharedGradient() {
        return build == BUILD_WITH_SHARED_GRADIENT || build == 2631424 || build == 2631428 || build == 2631431 || build == 2631432 || build == 2632100;
    }

    boolean hasAlias(String owner, String member) {
        return names.containsKey(owner) || names.containsKey(owner + "#" + member);
    }

    private boolean matches() {
        try {
            Class<?> modifier = type("g5i0");
            return modifier.isInterface()
                    && modifier.isAssignableFrom(type("o500"))
                    && modifier.getMethod(methodName("g5i0", "C"), modifier).getReturnType() == modifier;
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
