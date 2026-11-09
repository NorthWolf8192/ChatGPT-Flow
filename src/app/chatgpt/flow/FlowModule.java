package app.chatgpt.flow;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/** Adapter for the supported builds of com.openai.chatgpt 1.2026.307. */
public final class FlowModule extends XposedModule {
    private static final String TAG = "ChatGPTFlow";
    private static final String CHAT_TAG = "ConversationScreenContentScaffold";
    private final AtomicBoolean installed = new AtomicBoolean();
    private final java.util.Set<String> errorsLogged = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Node> nodes = new ArrayList<>();
    private final RenderSession renderSession=new RenderSession();
    private long animationTime=SystemClock.uptimeMillis();
    private boolean scheduled, observerRegistered, lifecycleRegistered, versionOk = true;
    private volatile boolean enabled = true;
    private volatile boolean glassEnabled = true;
    private volatile boolean footerFrosted = true;
    private volatile int glassBlur=22,glassTint=38;
    private GlassHooks glass;
    private volatile boolean imeCompat;
    private boolean imeVisible, imeUpdateScheduled;
    private int latestChatBase = 0xff212121;
    private final ImeWindowCompat imeWindow = new ImeWindowCompat();
    private WeakReference<View> observedDecor = new WeakReference<>(null);
    private ViewTreeObserver.OnGlobalLayoutListener imeListener;
    private volatile int speed = 55, strength = 70;
    private WeakReference<Activity> activity = new WeakReference<>(null);
    private ComposeSymbols symbols;
    private DrawBehindAdapter drawBehind;
    private Method fold, invalidate, themeColors;
    private Object foldCallback, unit;
    private Class<?> function1;
    private Field scopeDraw, scopeNode, drawParams, paramsCanvas, paramsSize, themeGroup, themeBackground;
    private Field nodeOwner, nodeAttached;
    private final java.util.Map<Class<?>, Field> canvasFields = new java.util.HashMap<>();
    private final java.util.Map<Class<?>, Field> tagFields = new java.util.HashMap<>();
    private final java.util.Map<Object, DrawCallback> callbacks = new java.util.WeakHashMap<>();

    private static final class Node {
        final WeakReference<Object> ref;
        long drawn;
        Node(Object node, long now) { ref = new WeakReference<>(node); drawn = now; }
    }

    @Override public void onPackageReady(XposedModuleInterface.PackageReadyParam param) {
        if (!"com.openai.chatgpt".equals(param.getPackageName()) || !installed.compareAndSet(false, true)) return;
        try {
            ClassLoader loader = param.getClassLoader();
            symbols = ComposeSymbols.select(loader);
            Class<?> modifier = symbols.type("g5i0");
            function1 = symbols.type("kotlin.jvm.functions.Function1");
            Class<?> function2 = symbols.type("kotlin.jvm.functions.Function2");
            unit = symbols.type("k771").getField("a").get(null);
            drawBehind = DrawBehindAdapter.resolve(symbols, modifier, function1);
            fold = modifier.getMethod("a", Object.class, function2);
            fold.setAccessible(true);
            invalidate = symbols.method("bl7", "F", symbols.type("b600"));
            invalidate.setAccessible(true);
            scopeDraw = symbols.field("pib0", "a"); scopeNode = symbols.field("pib0", "b");
            drawParams = symbols.field("akf", "a");
            paramsCanvas = symbols.field("yjf", "c"); paramsSize = symbols.field("yjf", "d");
            nodeOwner = symbols.field("f5i0", "a");
            nodeAttached = symbols.field("f5i0", "n");
            themeColors = symbols.method("x3t0", "c", symbols.type("a1q"));
            themeColors.setAccessible(true);
            themeGroup = accessible(themeColors.getReturnType(), "c");
            themeBackground = accessible(themeGroup.getType(), "g");
            foldCallback = Proxy.newProxyInstance(loader, new Class<?>[]{function2}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) return objectMethod(proxy, method, args);
                return Boolean.TRUE.equals(args[0]) || hasChatTag(args[1]);
            });
            Method scaffold = null;
            for (Method m : symbols.type("isv0").getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getName().equals("a") && p.length == 13 && p[0] == modifier && p[5] == long.class) scaffold = m;
            }
            if (scaffold == null) throw new NoSuchMethodException("Conversation scaffold");
            scaffold.setAccessible(true);
            hook(scaffold).intercept(chain -> {
                Object[] args = null;
                try { args = adaptScaffold(chain); }
                catch (Throwable error) { logOnce("Cannot attach conversation background", error); }
                return args == null ? chain.proceed() : chain.proceed(args);
            });
            // Prevent an already compiled conversation caller from bypassing the hooked scaffold.
            for (Method m : symbols.type("mut").getDeclaredMethods()) {
                if (m.getName().equals("f") && m.getParameterTypes().length == 35) deoptimize(m);
            }
            installImeScrimHooks(modifier);
            try { glass=new GlassHooks(this,loader,symbols); }
            catch(Throwable error) { logOnce("Glass adapter unavailable",error); }
            Method resume = Activity.class.getDeclaredMethod("onResume");
            hook(resume).intercept(chain -> {
                Object result = chain.proceed();
                if (isMainActivity(chain.getThisObject())) onResume((Activity)chain.getThisObject());
                return result;
            });
            Method pause = Activity.class.getDeclaredMethod("onPause");
            hook(pause).intercept(chain -> {
                if (isMainActivity(chain.getThisObject()) && chain.getThisObject()==activity.get()) {
                    pauseRendering();
                }
                return chain.proceed();
            });
            hook(Activity.class.getDeclaredMethod("onDestroy")).intercept(chain -> {
                if (isMainActivity(chain.getThisObject())) destroyRendering((Activity)chain.getThisObject());
                return chain.proceed();
            });
            installLifecycleRecovery(loader);
            log(4, TAG, "ChatGPT Flow 1.0.19 initialized for ChatGPT build " + symbols.build + " (API 102).");
        } catch (Throwable error) {
            log(6, TAG, "Unsupported ChatGPT build or missing Compose adapter", error);
        }
    }

    private void installLifecycleRecovery(ClassLoader loader) {
        try {
            Class<?> main=Class.forName("com.openai.chatgpt.MainActivity",false,loader);
            Method create=main.getDeclaredMethod("onCreate",Bundle.class);
            create.setAccessible(true);
            hook(create).intercept(chain->{
                Object result=chain.proceed();
                registerLifecycle((Activity)chain.getThisObject());
                return result;
            });
            Class<?> owner=main; Method focus=null;
            while(owner!=null && focus==null){
                try{focus=owner.getDeclaredMethod("onWindowFocusChanged",boolean.class);}
                catch(NoSuchMethodException missing){owner=owner.getSuperclass();}
            }
            if(focus!=null){
                focus.setAccessible(true);
                hook(focus).intercept(chain->{
                    Object result=chain.proceed();
                    if(isMainActivity(chain.getThisObject()) && Boolean.TRUE.equals(chain.getArg(0))
                            && renderSession.isResumed()) recoverForeground(true);
                    return result;
                });
            }
        } catch(Throwable error){logOnce("Cannot install foreground recovery",error);}
    }

    private void registerLifecycle(Activity a) {
        if(lifecycleRegistered)return;
        a.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks(){
            public void onActivityCreated(Activity activity,Bundle state){}
            public void onActivityStarted(Activity activity){}
            public void onActivityResumed(Activity activity){if(isMainActivity(activity))onResume(activity);}
            public void onActivityPaused(Activity a){if(isMainActivity(a) && activity.get()==a)pauseRendering();}
            public void onActivityStopped(Activity activity){}
            public void onActivitySaveInstanceState(Activity activity,Bundle state){}
            public void onActivityDestroyed(Activity activity){if(isMainActivity(activity))destroyRendering(activity);}
        });
        lifecycleRegistered=true;
    }

    private void pauseRendering(){
        renderSession.pause();scheduled=false;handler.removeCallbacks(tick);imeWindow.restore();
    }

    private void destroyRendering(Activity a){
        if(activity.get()!=a)return;
        pauseRendering();detachImeObserver();nodes.clear();activity.clear();renderSession.destroy();
        if(glass!=null)glass.destroyed();
    }

    private void recoverForeground(boolean resetResources){
        if(!renderSession.isResumed())return;
        if(glass!=null && resetResources)glass.foreground();
        long ticket=renderSession.newRefresh();
        for(int delay:new int[]{0,40,140,350})handler.postDelayed(()->{
            if(!renderSession.current(ticket))return;
            Activity a=activity.get();if(a==null)return;
            redraw();
            if(glass!=null)glass.changed();
            a.getWindow().getDecorView().invalidate();
            requestImeUpdate();schedule();
        },delay);
    }

    private void installImeScrimHooks(Class<?> modifier) {
        try {
            Class<?> composer = symbols.type("a1q");
            Method overlay = symbols.method("qod1", "f", modifier, float.class, composer, int.class, int.class);
            overlay.setAccessible(true);
            hook(overlay).intercept(chain -> {
                if (!imeCompatActive()) return chain.proceed();
                Object[] args = chain.getArgs().toArray();
                args[1] = 0f;
                args[3] = ((Integer)args[3] & ~112) | 32;
                args[4] = (Integer)args[4] & ~2;
                return chain.proceed(args);
            });
            Class<?> function0 = symbols.type("kotlin.jvm.functions.Function0");
            Class<?> bottomSheet = symbols.type("xwa");
            Method peek = bottomSheet.getDeclaredMethod("i", int.class, long.class,
                    composer, function0, boolean.class, boolean.class);
            peek.setAccessible(true);
            hook(peek).intercept(chain -> {
                if (!imeCompatActive()) return chain.proceed();
                Object[] args = chain.getArgs().toArray();
                args[1] = 0L;
                args[0] = ((Integer)args[0] & ~14) | 4;
                return chain.proceed(args);
            });
            for (Method m : symbols.type("qod1").getDeclaredMethods()) {
                if (m.getName().equals("d")) deoptimize(m);
            }
            for (Method m : bottomSheet.getDeclaredMethods()) if (m.getName().equals("h")) deoptimize(m);
            log(4, TAG, "Optional IME OverlayScrim and PeekHeightScrim hooks installed.");
        } catch (Throwable error) { logOnce("Optional IME scrim adapter unavailable", error); }
    }

    private boolean chatAttached() {
        try {
            for (Node n : nodes) {
                Object node = n.ref.get();
                if (node != null && nodeAttached.getBoolean(nodeOwner.get(node))) return true;
            }
        } catch (Throwable error) { logOnce("Cannot inspect chat draw node", error); }
        return false;
    }

    private boolean imeCompatActive() {
        Activity a = activity.get();
        return imeCompat && enabled && versionOk && renderSession.isResumed() && imeVisible
                && a != null && a.hasWindowFocus() && chatAttached();
    }

    private void observeIme(Activity a) {
        View decor = a.getWindow().getDecorView();
        if (observedDecor.get() != decor) {
            detachImeObserver();
            observedDecor = new WeakReference<>(decor);
            imeListener = () -> {
                View view = observedDecor.get();
                if (view == null) return;
                boolean visible = ImeWindowCompat.isImeVisible(view);
                if (imeVisible != visible) { imeVisible = visible; redraw(); }
                requestImeUpdate();
            };
            decor.getViewTreeObserver().addOnGlobalLayoutListener(imeListener);
        }
        imeVisible = ImeWindowCompat.isImeVisible(decor);
        requestImeUpdate();
    }

    private void detachImeObserver() {
        View view = observedDecor.get();
        if (view != null && imeListener != null && view.getViewTreeObserver().isAlive())
            view.getViewTreeObserver().removeOnGlobalLayoutListener(imeListener);
        observedDecor.clear(); imeListener = null;
        imeWindow.restore();
    }

    private void requestImeUpdate() {
        if (imeUpdateScheduled) return;
        imeUpdateScheduled = true;
        handler.post(() -> {
            imeUpdateScheduled = false;
            try {
                Activity a = activity.get();
                imeWindow.update(a == null ? null : a.getWindow(), imeCompatActive(), latestChatBase, speed, strength);
            } catch (Throwable error) { logOnce("Cannot extend IME backing background", error); }
        });
    }

    private Object[] adaptScaffold(io.github.libxposed.api.XposedInterface.Chain chain) throws Throwable {
        if (!versionOk || !Boolean.TRUE.equals(fold.invoke(chain.getArg(0), Boolean.FALSE, foldCallback))) return null;
        int defaults = (Integer) chain.getArg(12);
        long color = (Long) chain.getArg(5);
        // This exact chat call supplies an sRGB color explicitly.
        if ((defaults & 32) != 0 || (color & 63L) != 0) return null;
        Object original = chain.getArg(0);
        DrawCallback callback = callbacks.get(original);
        if (callback == null) {
            callback = new DrawCallback(original.getClass().getClassLoader());
            callbacks.put(original, callback);
        }
        callback.background = (int) (color >>> 32);
        if ((callback.background >>> 24) == 255) callback.base = callback.background;
        else {
            Object theme = themeColors.invoke(null, chain.getArg(10));
            long packed = themeBackground.getLong(themeGroup.get(theme));
            callback.base = (int) (packed >>> 32);
        }
        Object[] args = chain.getArgs().toArray();
        args[0] = appendDrawBehind(original, callback.proxy);
        args[5] = 0L;
        args[11] = ((Integer) args[11] & ~196608) | 131072;
        args[12] = defaults & ~32;
        return args;
    }

    private static Field accessible(Class<?> c, String name) throws Exception {
        Field f = c.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private static boolean isMainActivity(Object obj) {
        return obj != null && obj.getClass().getName().equals("com.openai.chatgpt.MainActivity");
    }
    private boolean hasChatTag(Object element) throws Exception {
        if (!element.getClass().getName().equals("io.sentry.compose.SentryModifier$SentryTagModifierNodeElement")) return false;
        Field f = tagFields.get(element.getClass());
        if (f == null) {
            for (Field candidate : element.getClass().getDeclaredFields()) {
                if (candidate.getType() == String.class && !java.lang.reflect.Modifier.isStatic(candidate.getModifiers())) {
                    candidate.setAccessible(true); f = candidate; break;
                }
            }
            if (f == null) return false;
            tagFields.put(element.getClass(), f);
        }
        return CHAT_TAG.equals(f.get(element));
    }
    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("equals")) return proxy == args[0];
        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
        return "ChatGPTFlowCallback";
    }
    private final class DrawCallback {
        final FlowRenderer renderer = new FlowRenderer();
        final Object proxy;
        int background = 0xff212121;
        int base = 0xff212121;
        DrawCallback(ClassLoader loader) {
            proxy = Proxy.newProxyInstance(loader, new Class<?>[]{function1}, (self, method, args) -> {
                if (method.getDeclaringClass() == Object.class) return objectMethod(self, method, args);
                try {
                    Object scope = args[0];
                    Object params = drawParams.get(scopeDraw.get(scope));
                    Object wrapper = paramsCanvas.get(params);
                    Field nativeField = canvasFields.get(wrapper.getClass());
                    if (nativeField == null) {
                        for (Field f : wrapper.getClass().getDeclaredFields()) {
                            if (Canvas.class.isAssignableFrom(f.getType())) { f.setAccessible(true); nativeField = f; break; }
                        }
                        if (nativeField == null) throw new IllegalStateException("Missing native canvas");
                        canvasFields.put(wrapper.getClass(), nativeField);
                    }
                    Canvas canvas = (Canvas)nativeField.get(wrapper);
                    long size = paramsSize.getLong(params);
                    float width = Float.intBitsToFloat((int)(size >>> 32));
                    float height = Float.intBitsToFloat((int)size);
                    long time=animationTime;
                    renderer.drawAt(canvas, width, height, base, background, enabled && versionOk, speed, strength,time);
                    latestChatBase = base;
                    trackNode(scopeNode.get(scope));
                    if(glass!=null)glass.backdrop(renderer,width,height,base,scopeNode.get(scope),time);
                    if (imeCompat && imeVisible && !imeWindow.isActive()) requestImeUpdate();
                } catch (Throwable error) { logOnce("Cannot draw conversation flow", error); }
                return unit;
            });
        }
    }
    private void trackNode(Object node) {
        if (node == null) return;
        long now = SystemClock.uptimeMillis();
        boolean found = false;
        for (Node n : nodes) if (n.ref.get() == node) { n.drawn = now; found = true; break; }
        if (!found) nodes.add(new Node(node, now));
        schedule();
    }
    private void schedule() {
        if (renderSession.isResumed() && enabled && strength > 0 && versionOk && !scheduled && (!nodes.isEmpty() || (glassFrameActive() && glass!=null && glass.hasActiveSurfaces()))) {
            scheduled = true; handler.postDelayed(tick, 33);
        }
    }
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            scheduled = false;
            if (!renderSession.isResumed() || !versionOk) return;
            animationTime=SystemClock.uptimeMillis();
            redraw();
            if(glass!=null && glassFrameActive())glass.frame(animationTime);
            if (imeWindow.isActive()) {
                if (!imeCompatActive()) requestImeUpdate();
                else imeWindow.redraw(latestChatBase, speed, strength);
            }
            schedule();
        }
    };
    private void redraw() {
        long now = SystemClock.uptimeMillis();
        Iterator<Node> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            Node n = iterator.next(); Object node = n.ref.get();
            if (node == null || !glassNodeAttached(node)) { iterator.remove(); continue; }
            try { invalidate.invoke(null, node); }
            catch (Throwable error) { iterator.remove(); logOnce("Cannot invalidate flow node", error); }
        }
    }
    private void onResume(Activity a) {
        activity = new WeakReference<>(a); renderSession.resume(a);
        animationTime=SystemClock.uptimeMillis();
        try {
            int actual = a.getPackageManager().getPackageInfo("com.openai.chatgpt", 0).versionCode;
            versionOk = actual == symbols.build;
            if (!versionOk) log(5, TAG, "ChatGPT build " + actual + " does not match Compose mapping " + symbols.build);
            Context context = a.getApplicationContext();
            readSettings(context);
            if (!observerRegistered) {
                context.getContentResolver().registerContentObserver(SettingsProvider.URI, false, new ContentObserver(handler) {
                    @Override public void onChange(boolean selfChange) {
                        readSettings(context);
                        if (renderSession.isResumed()) {
                            redraw(); schedule(); requestImeUpdate();
                            if(glass!=null)glass.changed();
                        }
                    }
                });
                observerRegistered = true;
            }
        } catch (Throwable error) { logOnce("Using default visual preferences", error); }
        // A paused Compose render node may keep its display list without drawing again.
        // Revive the retained nodes before expiry so returning to the app restarts the flow.
        long now = SystemClock.uptimeMillis();
        for (Node node : nodes) node.drawn = now;
        observeIme(a);
        recoverForeground(true);
        redraw(); schedule();
    }
    private void readSettings(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SettingsProvider.URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                enabled = cursor.getInt(0) != 0;
                speed = Palette.clamp(cursor.getInt(1), 1, 100);
                strength = Palette.clamp(cursor.getInt(2), 0, 100);
                int compat = cursor.getColumnIndex("ime_compat");
                imeCompat = compat >= 0 && cursor.getInt(compat) != 0;
                int g=cursor.getColumnIndex("glass_enabled");
                glassEnabled=g<0 || cursor.getInt(g)!=0;
                int blur=cursor.getColumnIndex("glass_blur");
                glassBlur=blur<0?22:Palette.clamp(cursor.getInt(blur),0,60);
                int tint=cursor.getColumnIndex("glass_tint");
                glassTint=tint<0?38:Palette.clamp(cursor.getInt(tint),0,100);
                int footer=cursor.getColumnIndex("footer_frosted");
                footerFrosted=footer<0 || cursor.getInt(footer)!=0;
            }
        } catch (Throwable error) { logOnce("Settings provider unavailable; using defaults", error); }
        finally { if (cursor != null) cursor.close(); }
    }
    private void logOnce(String message, Throwable error) {
        if (errorsLogged.add(message)) log(6, TAG, message, error);
    }

    void glassHook(Method method,io.github.libxposed.api.XposedInterface.Hooker callback) { hook(method).intercept(callback); }
    Object appendDrawBehind(Object original, Object callback) throws ReflectiveOperationException {
        return drawBehind.append(original, callback);
    }
    void glassDeopt(Method method) { deoptimize(method); }
    void glassError(String message,Throwable error) { logOnce(message,error); }
    boolean glassConfigured() { return renderSession.visualsAllowed(enabled,glassEnabled,versionOk); }
    boolean footerFrostedConfigured(){return renderSession.visualsAllowed(enabled,footerFrosted,versionOk);}
    boolean glassFrameActive(){return glassConfigured() || footerFrostedConfigured();}
    boolean glassCollecting() { return renderSession.visualsAllowed(enabled,true,versionOk); }
    boolean glassBottomScrimActive(){return glassCollecting() && chatAttached();}
    boolean glassSceneActive() {
        Activity a=activity.get();
        return glassConfigured() && a!=null && chatAttached();
    }
    boolean glassNodeAttached(Object node){
        try{return node!=null && nodeAttached.getBoolean(nodeOwner.get(node));}
        catch(Throwable ignored){return false;}
    }
    void glassWake(){schedule();}
    int glassSpeed() { return speed; }
    int glassStrength() { return strength; }
    int glassBlur() { return glassBlur; }
    int glassTint() { return glassTint; }
    int glassTheme(Object composer)throws Exception{
        Object theme=themeColors.invoke(null,composer);
        return (int)(themeBackground.getLong(themeGroup.get(theme))>>>32);
    }
    float glassDensity() { Activity a=activity.get();return a==null?1:a.getResources().getDisplayMetrics().density; }
}
