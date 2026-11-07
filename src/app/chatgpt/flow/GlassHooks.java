package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.view.View;
import android.view.ViewParent;
import android.widget.PopupWindow;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/** Exact adapter for the drawing bridge in ChatGPT 2630709. */
final class GlassHooks {
    private final FlowModule module;
    private final ClassLoader loader;
    private final ThreadLocal<Context> current=new ThreadLocal<>();
    private final ThreadLocal<Boolean> buildingModeRow=new ThreadLocal<>();
    private final ThreadLocal<Boolean> buildingConversationFooter=new ThreadLocal<>();
    private final ThreadLocal<Boolean> buildingTopBar=new ThreadLocal<>();
    private final ThreadLocal<Boolean> buildingSettingsHeader=new ThreadLocal<>();
    private final ThreadLocal<Boolean> buildingSettingsSearch=new ThreadLocal<>();
    private final FooterGradientRegistry footerGradients=new FooterGradientRegistry();
    private final FooterGradientRegistry topGradients=new FooterGradientRegistry();
    private final FooterGradientRegistry settingsGradients=new FooterGradientRegistry();
    private final Map<Object,FooterFrostedRenderer> footerMaterials=new WeakHashMap<>();
    private final Map<Object,Integer> kinds=new WeakHashMap<>();
    private final Map<Object,ArrayList<Surface>> surfaces=new WeakHashMap<>();
    private final Map<Object,Object> tagCallbacks=new WeakHashMap<>();
    private final ArrayList<WeakReference<Object>> live=new ArrayList<>();
    private final Field nativeCanvas,nativePaint,layout,layoutOwner,modifiers,scopeDraw,scopeNode,drawParams,paramsSize;
    private final Method parent,position,nodeCoordinator,fold,drawBehind,invalidate;
    private final Object inspectTags,inspectChatRoot,inspectBottomScrim,inspectSettingsScope,unit;
    private final Class<?> function1;
    private Field tagField;
    private FlowRenderer backdrop;
    private float viewportWidth,viewportHeight,originX,originY;
    private int base=0xff212121;
    private long frameTime;
    private final Map<PopupWindow,NativePopup> popups=new WeakHashMap<>();

    private static final class NativePopup {
        final WeakReference<PopupWindow> window;
        final WeakReference<View> content;
        final Drawable original;
        PopupGlassDrawable material;
        Drawable rootOriginal,clearMarker;
        int left,top,right,bottom;
        boolean rootCleared;
        boolean applied;
        NativePopup(PopupWindow w){window=new WeakReference<>(w);content=new WeakReference<>(w.getContentView());original=w.getBackground();}
    }

    private static final class Context {
        Object node,coordinator;
        float width,height,x,y;
        int kind=-1;
        Context previous;
        RectF injected;
    }
    private static final class Surface {
        GlassRenderer renderer;
        RectF bounds;
        long lastDraw;
    }

    GlassHooks(FlowModule module,ClassLoader loader) throws Exception {
        this.module=module;this.loader=loader;
        Class<?> modifier=Class.forName("g5i0",false,loader);
        Class<?> bridge=Class.forName("pib0",false,loader);
        Class<?> wrapper=Class.forName("ol2",false,loader);
        Class<?> paint=Class.forName("ew2",false,loader);
        Class<?> coordinator=Class.forName("qwj0",false,loader);
        Class<?> layoutNode=Class.forName("androidx.compose.ui.node.LayoutNode",false,loader);
        function1=Class.forName("kotlin.jvm.functions.Function1",false,loader);
        Class<?> function2=Class.forName("kotlin.jvm.functions.Function2",false,loader);
        unit=Class.forName("k771",false,loader).getField("a").get(null);
        nativeCanvas=field(wrapper,"a"); nativePaint=field(paint,"a");
        layout=field(coordinator,"u"); modifiers=field(layoutNode,"N1");
        layoutOwner=field(layoutNode,"o");
        parent=layoutNode.getDeclaredMethod("F"); parent.setAccessible(true);
        position=coordinator.getDeclaredMethod("R",long.class);position.setAccessible(true);
        nodeCoordinator=Class.forName("kha0",false,loader).getDeclaredMethod("a0",Class.forName("y6x",false,loader),int.class);
        nodeCoordinator.setAccessible(true);
        fold=modifier.getMethod("a",Object.class,function2);
        drawBehind=Class.forName("cja0",false,loader).getDeclaredMethod("x",modifier,function1);
        drawBehind.setAccessible(true);
        invalidate=Class.forName("bl7",false,loader).getDeclaredMethod("F",Class.forName("b600",false,loader));invalidate.setAccessible(true);
        scopeDraw=field(bridge,"a");scopeNode=field(bridge,"b");
        drawParams=field(Class.forName("akf",false,loader),"a");paramsSize=field(Class.forName("yjf",false,loader),"d");
        inspectTags=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            int result=(Integer)args[0];Object element=args[1];
            if(element.getClass().getName().equals("io.sentry.compose.SentryModifier$SentryTagModifierNodeElement")){
                if(tagField==null)tagField=field(element.getClass(),"tag");
                result=GlassPolicy.combine(result,GlassPolicy.kind((String)tagField.get(element)));
            }
            return result;
        });
        inspectChatRoot=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            if(Boolean.TRUE.equals(args[0]))return true;
            Object element=args[1];
            if(!element.getClass().getName().equals("io.sentry.compose.SentryModifier$SentryTagModifierNodeElement"))return false;
            if(tagField==null)tagField=field(element.getClass(),"tag");
            return "ConversationScreenContentScaffold".equals(tagField.get(element));
        });
        inspectBottomScrim=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            if(Boolean.TRUE.equals(args[0]))return true;
            Object element=args[1];
            if(!element.getClass().getName().equals("io.sentry.compose.SentryModifier$SentryTagModifierNodeElement"))return false;
            if(tagField==null)tagField=field(element.getClass(),"tag");
            return "OrbitNavigationBarScrim".equals(tagField.get(element));
        });
        inspectSettingsScope=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            if(Boolean.TRUE.equals(args[0]))return true;
            Object element=args[1];
            if(!element.getClass().getName().equals("io.sentry.compose.SentryModifier$SentryTagModifierNodeElement"))return false;
            if(tagField==null)tagField=field(element.getClass(),"tag");
            return GlassPolicy.settingsTag((String)tagField.get(element));
        });

        Method draw=bridge.getDeclaredMethod("b",Class.forName("wjf",false,loader),long.class,coordinator,
                Class.forName("b600",false,loader),Class.forName("ec60",false,loader));draw.setAccessible(true);
        module.glassHook(draw,chain->{
            if(!module.glassCollecting())return chain.proceed();
            Context c=new Context();c.previous=current.get();c.node=chain.getArg(3);c.coordinator=chain.getArg(2);
            long size=(Long)chain.getArg(1);c.width=hi(size);c.height=lo(size);
            current.set(c);
            try{return chain.proceed();}finally{if(c.previous==null)current.remove();else current.set(c.previous);}
        });
        Method round=wrapper.getDeclaredMethod("e",float.class,float.class,float.class,float.class,float.class,float.class,paint);
        Method circle=wrapper.getDeclaredMethod("b",float.class,long.class,paint);
        // Rectangle, circle and round-rectangle fills; vector paths are intentionally retained.
        Method rectangle=wrapper.getDeclaredMethod("s",float.class,float.class,float.class,float.class,paint);
        for(Method m:new Method[]{round,circle,rectangle}){
            m.setAccessible(true);
            module.glassHook(m,chain->{
                boolean handled=false;
                try{
                    Context c=current.get();
                    if(c!=null && module.glassCollecting()){
                        RectF rect;float rx,ry;Object paintObject;
                        if(m.equals(circle)){
                            float radius=(Float)chain.getArg(0);long center=(Long)chain.getArg(1);
                            rect=new RectF(hi(center)-radius,lo(center)-radius,hi(center)+radius,lo(center)+radius);
                            rx=ry=radius;paintObject=chain.getArg(2);
                        }else{
                            rect=new RectF((Float)chain.getArg(0),(Float)chain.getArg(1),(Float)chain.getArg(2),(Float)chain.getArg(3));
                            rx=m.equals(round)?(Float)chain.getArg(4):0;
                            ry=m.equals(round)?(Float)chain.getArg(5):0;
                            paintObject=chain.getArg(m.equals(round)?6:4);
                        }
                        Paint p=(Paint)nativePaint.get(paintObject);
                        if(p.getStyle()==Paint.Style.FILL){
                            int kind=kind(c);
                            if(GlassPolicy.eligible(kind,rect.width(),rect.height(),c.width,c.height,module.glassDensity(),p.getColor(),Palette.isDark(base))){
                                track(c.node);
                                ensureBackdrop(c);
                                if(ready(kind)){
                                    locate(c);
                                    int color=p.getColor();
                                    int r=(color>>>16)&255,g=(color>>>8)&255,b=color&255;
                                    boolean colored=Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))>65;
                                    if(!colored && alreadyInjected(c,rect))handled=true;
                                    else handled=drawSurface(c,(Canvas)nativeCanvas.get(chain.getThisObject()),rect,rx,ry,p.getColor(),false);
                                }
                            }
                        }
                    }
                }catch(Throwable error){module.glassError("Cannot draw glass control fill",error);}
                return handled?null:chain.proceed();
            });
        }
        Method sentry=Class.forName("io.sentry.compose.e",false,loader).getDeclaredMethod("a",modifier,String.class);
        sentry.setAccessible(true);
        module.glassHook(sentry,chain->{
            Object original=chain.proceed();String tag=(String)chain.getArg(1);
            if(GlassPolicy.kind(tag)==GlassPolicy.NONE || GlassPolicy.kind(tag)==GlassPolicy.EXCLUDE)return original;
            Object callback=tagCallbacks.get(original);
            if(callback==null){
            callback=Proxy.newProxyInstance(loader,new Class<?>[]{function1},(proxy,method,args)->{
                if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
                try{
                    Object scope=args[0],node=scopeNode.get(scope);
                    track(node);
                    if(module.glassConfigured() && GlassPolicy.inject(tag)){
                        Context c=current.get();
                        if(c!=null)ensureBackdrop(c);
                        if(c!=null && ready(kind(c)) && kind(c)!=GlassPolicy.VOICE){
                            Object params=drawParams.get(scopeDraw.get(scope));long size=paramsSize.getLong(params);
                            float w=hi(size),h=lo(size),d=module.glassDensity();
                            if(GlassPolicy.sidebarSurface(w,h,d)){
                                locate(c);RectF rect=new RectF(0,0,w,h);
                                Canvas canvas=canvas(scope);
                                if(!alreadyInjected(c,rect) && drawSurface(c,canvas,rect,0,0,Palette.isDark(base)?0xff212121:0xffffffff,false))
                                    c.injected=new RectF(c.x+rect.left,c.y+rect.top,c.x+rect.right,c.y+rect.bottom);
                            }
                        }
                    }
                }catch(Throwable error){module.glassError("Cannot draw glass composer button",error);}
                return unit;
            });
            tagCallbacks.put(original,callback);
            }
            try{return drawBehind.invoke(null,original,callback);}
            catch(Throwable error){module.glassError("Cannot attach glass composer button",error);return original;}
        });
        for(Method m:Class.forName("akf",false,loader).getDeclaredMethods())module.glassDeopt(m);
        for(Method m:bridge.getDeclaredMethods())if(m.getName().equals("a"))module.glassDeopt(m);
        for(Method m:coordinator.getDeclaredMethods())if(m.getName().equals("e1"))module.glassDeopt(m);
        String[][] tagCallers={
                {"nje","d"},{"z9q","invoke"},{"a181","H"},{"sz2","l"},
                {"kff0","c"},{"yau","d"},{"aq00","h"},{"fy91","f"},
                {"nt51","invoke"},{"ot51","invoke"},{"pt51","invoke"},{"h3w0","a"},
                {"xa01","e"},{"xa01","f"},{"tzt","invoke"},
                {"t0u","a"},{"t0u","b"},{"t0u","c"},{"t0u","h"},
                {"au00","W0"},{"nbf1","c"},{"kfi0","a"},{"nei0","a"},{"umt0","m"},{"umt0","n"},
                {"nst","invoke"},{"bl7","q"},{"bu00","create"},{"bu00","update"},
                {"dt51","invoke"},{"lqf0","J"},{"ppe","invoke"},{"grf1","e"},{"nqe1","a"},
                {"vof1","b"},{"p5y0","invoke"},{"r5y0","invoke"}};
        for(String[] caller:tagCallers)
            for(Method m:Class.forName(caller[0],false,loader).getDeclaredMethods())
                if(m.getName().equals(caller[1]))module.glassDeopt(m);
        installExactSurfaceHooks(modifier,sentry);
        installPanelPathHook(wrapper,paint);
        installFooterGradientOwnership(modifier,function2);
        installComposerFadeHook(bridge);
        installPopupHooks();
    }

    private void installFooterGradientOwnership(Class<?> modifier,Class<?> function2)throws Exception{
        Object collect=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            Object element=args[1];
            if(element.getClass().getName().equals("bu00"))footerGradients.rememberElement(element);
            return args[0];
        });
        Object collectSettings=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            Object element=args[1];
            if(element.getClass().getName().equals("bu00"))settingsGradients.rememberElement(element);
            return args[0];
        });
        // nst creates the wrapper around mut.a (the composer). Its scroll gradient is a
        // sibling of the input layout, so input tags and IME visibility cannot identify it.
        Method footer=Class.forName("nst",false,loader).getDeclaredMethod("invoke",Object.class,Object.class);
        footer.setAccessible(true);
        module.glassHook(footer,chain->{
            Boolean previous=buildingConversationFooter.get();buildingConversationFooter.set(true);
            try{return chain.proceed();}
            finally{if(previous==null)buildingConversationFooter.remove();else buildingConversationFooter.set(previous);}
        });
        Method gradient=Class.forName("bl7",false,loader).getDeclaredMethod("q",int.class,int.class,long.class,
                Class.forName("a1q",false,loader),modifier,Class.forName("vox0",false,loader),boolean.class);
        gradient.setAccessible(true);
        module.glassHook(gradient,chain->{
            Object result=chain.proceed();
            if(Boolean.TRUE.equals(buildingConversationFooter.get())
                    && Integer.valueOf(6).equals(chain.getArg(0)) && Integer.valueOf(0).equals(chain.getArg(1)))
                fold.invoke(result,0,collect);
            if(Boolean.TRUE.equals(buildingSettingsSearch.get()) && Integer.valueOf(4).equals(chain.getArg(1))){
                fold.invoke(result,0,collectSettings);
                try{base=module.glassTheme(chain.getArg(3));}catch(Throwable error){module.glassError("Cannot read settings search theme",error);}
            }
            return result;
        });
        Object collectTop=Proxy.newProxyInstance(loader,new Class<?>[]{function2},(proxy,method,args)->{
            if(method.getDeclaringClass()==Object.class)return objectMethod(proxy,method,args);
            Object element=args[1];
            if(element.getClass().getName().equals("bu00"))topGradients.rememberElement(element);
            return args[0];
        });
        Method settingsSearch=Class.forName("grf1",false,loader).getDeclaredMethod("e",Class.forName("x6z0",false,loader),
                Class.forName("vox0",false,loader),function1,Class.forName("kotlin.jvm.functions.Function0",false,loader),
                modifier,Class.forName("a1q",false,loader),int.class);
        settingsSearch.setAccessible(true);
        module.glassHook(settingsSearch,chain->{
            Boolean previous=buildingSettingsSearch.get();buildingSettingsSearch.set(true);
            try{return chain.proceed();}
            finally{if(previous==null)buildingSettingsSearch.remove();else buildingSettingsSearch.set(previous);}
        });
        Method settingsHeader=Class.forName("ppe",false,loader).getDeclaredMethod("invoke",Object.class,Object.class,Object.class,Object.class);
        settingsHeader.setAccessible(true);
        module.glassHook(settingsHeader,chain->{
            Boolean previous=buildingSettingsHeader.get();buildingSettingsHeader.set(true);
            try{return chain.proceed();}
            finally{if(previous==null)buildingSettingsHeader.remove();else buildingSettingsHeader.set(previous);}
        });
        Method topBar=Class.forName("dt51",false,loader).getDeclaredMethod("invoke",Object.class,Object.class);
        topBar.setAccessible(true);
        module.glassHook(topBar,chain->{
            Boolean previous=buildingTopBar.get();buildingTopBar.set(true);
            try{return chain.proceed();}
            finally{if(previous==null)buildingTopBar.remove();else buildingTopBar.set(previous);}
        });
        Method topGradient=Class.forName("lqf0",false,loader).getDeclaredMethod("J",int.class,int.class,long.class,
                Class.forName("a1q",false,loader),modifier,Class.forName("vox0",false,loader),boolean.class);
        topGradient.setAccessible(true);
        module.glassHook(topGradient,chain->{
            Object result=chain.proceed();
            if(Boolean.TRUE.equals(buildingTopBar.get())
                    && Integer.valueOf(1).equals(chain.getArg(0)) && Integer.valueOf(0).equals(chain.getArg(1)))
                fold.invoke(result,0,collectTop);
            if(Boolean.TRUE.equals(buildingSettingsHeader.get()) && Integer.valueOf(1).equals(chain.getArg(0))){
                fold.invoke(result,0,collectSettings);
                try{base=module.glassTheme(chain.getArg(3));}catch(Throwable error){module.glassError("Cannot read settings header theme",error);}
            }
            return result;
        });
        Class<?> element=Class.forName("bu00",false,loader);
        Method create=element.getDeclaredMethod("create");create.setAccessible(true);
        module.glassHook(create,chain->{
            Object node=chain.proceed();footerGradients.bind(chain.getThisObject(),node);
            topGradients.bind(chain.getThisObject(),node);settingsGradients.bind(chain.getThisObject(),node);return node;
        });
        Method update=element.getDeclaredMethod("update",Class.forName("f5i0",false,loader));update.setAccessible(true);
        module.glassHook(update,chain->{
            Object result=chain.proceed();footerGradients.bind(chain.getThisObject(),chain.getArg(0));
            topGradients.bind(chain.getThisObject(),chain.getArg(0));settingsGradients.bind(chain.getThisObject(),chain.getArg(0));return result;
        });
    }

    /** Target the sidebar's own content modifier, including its independent shaped background. */
    private void installExactSurfaceHooks(Class<?> modifier,Method sentry)throws Exception{
        Method sidebar=null;
        for(Method m:Class.forName("t0u",false,loader).getDeclaredMethods()){
            Class<?>[] p=m.getParameterTypes();
            if(m.getName().equals("c") && p.length==11 && p[8]==modifier)sidebar=m;
        }
        if(sidebar==null)throw new NoSuchMethodException("Sidebar content background");
        sidebar.setAccessible(true);
        module.glassHook(sidebar,chain->{
            try{base=module.glassTheme(chain.getArg(9));}catch(Throwable error){module.glassError("Cannot read sidebar theme",error);}
            Object[] args=chain.getArgs().toArray();
            args[8]=sentry.invoke(null,args[8],"FlowSidebarMaterialSurface");
            args[10]=((Integer)args[10]&~0x07000000)|0x04000000;
            return chain.proceed(args);
        });
        // Style the actual animated indicator created inside p5y0, preserving its Animatable
        // offset and animated width. A static background on the selected tab causes a jump.
        Method row=Class.forName("p5y0",false,loader).getDeclaredMethod("invoke",Object.class,Object.class,Object.class);
        row.setAccessible(true);
        module.glassHook(row,chain->{
            Boolean previous=buildingModeRow.get();buildingModeRow.set(true);
            try{return chain.proceed();}
            finally{if(previous==null)buildingModeRow.remove();else buildingModeRow.set(previous);}
        });
        Method indicator=Class.forName("vb6",false,loader).getDeclaredMethod("z",int.class,
                Class.forName("p550",false,loader),modifier,modifier,boolean.class);
        indicator.setAccessible(true);
        module.glassHook(indicator,chain->{
            if(!Boolean.TRUE.equals(buildingModeRow.get())
                    || ((Integer)fold.invoke(chain.getArg(2),0,inspectTags))!=GlassPolicy.SWITCH)return chain.proceed();
            try{base=module.glassTheme(chain.getArg(1));}catch(Throwable error){module.glassError("Cannot read mode indicator theme",error);}
            Object[] args=chain.getArgs().toArray();
            args[3]=sentry.invoke(null,args[3],"FlowModeSelectionIndicator");
            kinds.clear();
            return chain.proceed(args);
        });
    }

    private void installComposerFadeHook(Class<?> bridge)throws Exception{
        Method content=bridge.getDeclaredMethod("a");content.setAccessible(true);
        Class<?> fadeType=Class.forName("au00",false,loader);
        Method fade=fadeType.getDeclaredMethod("W0",bridge);fade.setAccessible(true);
        Field direction=field(fadeType,"o"),extended=field(fadeType,"q");
        Method metrics=fadeType.getDeclaredMethod("w1");metrics.setAccessible(true);
        Class<?> metricType=Class.forName("vox0",false,loader);
        Method reverse=metricType.getMethod("a"),startDistance=metricType.getMethod("c"),endDistance=metricType.getMethod("d");
        reverse.setAccessible(true);startDistance.setAccessible(true);endDistance.setAccessible(true);
        module.glassHook(fade,chain->{
            Context c=current.get();
            Object node=chain.getThisObject();boolean footer=footerGradients.owns(node),topOwner=topGradients.owns(node);
            boolean settings=settingsGradients.owns(node) || c!=null && inSettingsScope(c);
            boolean top=topOwner && (settings || module.glassBottomScrimActive());
            if(footer || topOwner || settings)track(node);
            if(module.glassCollecting() && (footer || top || settings || c!=null && isChatBoundaryFade(c))){
                if(c!=null)track(c.node);
                if((footer || top || settings) && module.footerFrostedConfigured()){
                    try{
                        Object scope=chain.getArg(0);
                        if(c==null){
                            c=new Context();c.node=node;c.coordinator=nodeCoordinator.invoke(null,node,4);
                            Object params=drawParams.get(scopeDraw.get(scope));long size=paramsSize.getLong(params);
                            c.width=hi(size);c.height=lo(size);
                        }
                        int edge=direction.getInt(node);Object scroll=metrics.invoke(node);
                        boolean reversed=(Boolean)reverse.invoke(scroll);
                        float distance=((Number)((edge==1)!=reversed?startDistance:endDistance).invoke(scroll)).floatValue();
                        FooterFrostedGeometry geometry=FooterFrostedGeometry.create(c.height,module.glassDensity(),edge,extended.getBoolean(node),distance);
                        if(geometry!=null && geometry.alpha>0){
                            ensureBackdrop(c);locate(c);
                            FooterFrostedRenderer material=footerMaterials.get(node);
                            if(material==null){material=new FooterFrostedRenderer();footerMaterials.put(node,material);}
                            material.draw(canvas(scope),geometry,c.width,c.x,c.y,backdrop,viewportWidth,viewportHeight,base,
                                    module.glassSpeed(),module.glassStrength(),SystemClock.uptimeMillis(),module.glassDensity(),module.glassBlur(),module.glassTint());
                        }
                    }catch(Throwable error){module.glassError("Cannot draw frosted scroll edge region",error);}
                }
                // This node adds an elevatedContentGradient outside its content bounds.
                // Scrolling changes the gradient's alpha even while the IME is hidden.
                // Keep foreground content above the optional glass; never restore the black gradient.
                content.invoke(chain.getArg(0));return null;
            }
            return chain.proceed();
        });
    }

    private boolean inSettingsScope(Context c)throws Exception{
        Object cursor=layout.get(c.coordinator);
        for(int i=0;i<32 && cursor!=null;i++){
            if(Boolean.TRUE.equals(fold.invoke(modifiers.get(cursor),false,inspectSettingsScope)))return true;
            cursor=parent.invoke(cursor);
        }
        return false;
    }

    private boolean isChatBoundaryFade(Context c)throws Exception{
        int role=kind(c);
        // OrbitNavigationBarScrim lives outside the conversation scaffold and deliberately
        // has EXCLUDE role. Match only this bottom gradient, not arbitrary Scrim-tagged UI.
        if(role==GlassPolicy.EXCLUDE && module.glassBottomScrimActive()){
            Object cursor=layout.get(c.coordinator);
            for(int i=0;i<32 && cursor!=null;i++){
                if(Boolean.TRUE.equals(fold.invoke(modifiers.get(cursor),false,inspectBottomScrim)))return true;
                cursor=parent.invoke(cursor);
            }
        }
        boolean chatRoot=false;
        if(role!=GlassPolicy.INPUT && role!=GlassPolicy.MENU && role!=GlassPolicy.EXCLUDE && role!=GlassPolicy.PLAIN_BUTTON){
            Object cursor=layout.get(c.coordinator);
            for(int i=0;i<32 && cursor!=null;i++){
                if(Boolean.TRUE.equals(fold.invoke(modifiers.get(cursor),false,inspectChatRoot))){chatRoot=true;break;}
                cursor=parent.invoke(cursor);
            }
        }
        return GlassPolicy.suppressBoundaryFade(role,chatRoot);
    }

    private void installPanelPathHook(Class<?> wrapper,Class<?> paint)throws Exception{
        Class<?> pathType=Class.forName("sw2",false,loader);
        Field nativePath=field(pathType,"a");
        Method convex=Path.class.getMethod("isConvex");
        Method draw=wrapper.getDeclaredMethod("o",pathType,paint);
        draw.setAccessible(true);
        module.glassHook(draw,chain->{
            boolean handled=false;
            try{
                Context c=current.get();
                if(c!=null && module.glassConfigured() && (kind(c)==GlassPolicy.MENU || kind(c)==GlassPolicy.INPUT)){
                    Path path=(Path)nativePath.get(chain.getArg(0));
                    Paint p=(Paint)nativePaint.get(chain.getArg(1));
                    RectF rect=new RectF();path.computeBounds(rect,true);
                    float density=module.glassDensity();
                    // Handle resized composer outlines as well as drawer panels; keep icon and text paths intact.
                    if(p.getStyle()==Paint.Style.FILL && !path.isInverseFillType()
                            && Boolean.TRUE.equals(convex.invoke(path))
                            && GlassPolicy.panelPathEligible(c.kind,rect.width(),rect.height(),c.width,c.height,density,p.getColor(),Palette.isDark(base))){
                        track(c.node);ensureBackdrop(c);
                        if(ready(c.kind)){
                            locate(c);
                            handled=drawSurface(c,(Canvas)nativeCanvas.get(chain.getThisObject()),rect,0,0,p.getColor(),false,path);
                        }
                    }
                }
            }catch(Throwable error){module.glassError("Cannot draw shaped sidebar material",error);}
            return handled?null:chain.proceed();
        });
    }

    private static Field field(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
    private static Object objectMethod(Object proxy,Method method,Object[] args){
        if(method.getName().equals("equals"))return proxy==args[0];
        if(method.getName().equals("hashCode"))return System.identityHashCode(proxy);
        return "ChatGPTFlowGlass";
    }
    private static float hi(long packed){return Float.intBitsToFloat((int)(packed>>>32));}
    private static float lo(long packed){return Float.intBitsToFloat((int)packed);}
    private Canvas canvas(Object scope)throws Exception{
        Object params=drawParams.get(scopeDraw.get(scope));
        Field f=field(params.getClass(),"c");return (Canvas)nativeCanvas.get(f.get(params));
    }
    private int kind(Context c)throws Exception{
        if(c.kind>=0)return c.kind;
        Object node=layout.get(c.coordinator);Integer cached=kinds.get(node);
        if(cached!=null){c.kind=cached;return cached;}
        Object cursor=node;int kind=GlassPolicy.NONE;
        for(int i=0;i<14 && cursor!=null;i++){
            int local=(Integer)fold.invoke(modifiers.get(cursor),0,inspectTags);
            if(local==GlassPolicy.EXCLUDE){kind=local;break;}
            // Child controls and a resized composer keep their own role instead of inheriting a broad ancestor role.
            kind=GlassPolicy.inherit(kind,local);
            cursor=parent.invoke(cursor);
        }
        if(kind==GlassPolicy.NONE && popupOwner(node))kind=GlassPolicy.MENU;
        kinds.put(node,kind);c.kind=kind;return kind;
    }
    private float[] screenPosition(Object coordinator)throws Exception{
        long p=(Long)position.invoke(coordinator,0L);
        Object owner=layoutOwner.get(layout.get(coordinator));int[] screen={0,0};
        if(owner instanceof View)((View)owner).getLocationOnScreen(screen);
        return new float[]{hi(p)+screen[0],lo(p)+screen[1]};
    }
    private void locate(Context c)throws Exception{float[] p=screenPosition(c.coordinator);c.x=p[0]-originX;c.y=p[1]-originY;}
    private boolean popupOwner(Object node)throws Exception{
        Object owner=layoutOwner.get(node);
        if(!(owner instanceof View))return false;
        View cursor=(View)owner;
        for(int i=0;i<14 && cursor!=null;i++){
            String name=cursor.getClass().getName().toLowerCase(java.util.Locale.ROOT);
            if(name.contains("popuplayout") || name.contains("popupdecorview") || name.contains("dialoglayout"))return true;
            for(NativePopup popup:popups.values())if(popup.content.get()==cursor)return true;
            ViewParent parent=cursor.getParent();cursor=parent instanceof View?(View)parent:null;
        }
        return false;
    }
    private boolean alreadyInjected(Context c,RectF r){
        for(Context p=c.previous;p!=null;p=p.previous){
            RectF a=p.injected;
            if(a!=null && Math.abs(a.left-c.x-r.left)<2 && Math.abs(a.top-c.y-r.top)<2
                    && Math.abs(a.width()-r.width())<2 && Math.abs(a.height()-r.height())<2)return true;
        }
        return false;
    }
    private boolean drawSurface(Context c,Canvas canvas,RectF rect,float rx,float ry,int color,boolean selected)throws Exception{
        return drawSurface(c,canvas,rect,rx,ry,color,selected,null);
    }
    private boolean drawSurface(Context c,Canvas canvas,RectF rect,float rx,float ry,int color,boolean selected,Path outline)throws Exception{
        ArrayList<Surface> pool=surfaces.get(c.node);
        if(pool==null){pool=new ArrayList<>();surfaces.put(c.node,pool);}
        Surface s=null;
        for(Surface candidate:pool){
            RectF b=candidate.bounds;
            if(b.left==rect.left && b.top==rect.top && b.right==rect.right && b.bottom==rect.bottom){s=candidate;break;}
        }
        // A draw node can emit both the tab track and its moving indicator. They must
        // not share a RenderNode whose later recording would replace an earlier command.
        if(s==null){
            s=new Surface();s.bounds=new RectF(rect);
            if(pool.size()>=12)pool.remove(0);
            pool.add(s);
        }
        if(s.renderer==null)s.renderer=new GlassRenderer();
        s.lastDraw=SystemClock.uptimeMillis();
        return s.renderer.draw(canvas,rect,rx,ry,c.x,c.y,backdrop,viewportWidth,viewportHeight,base,color,
                module.glassSpeed(),module.glassStrength(),frameTime,module.glassDensity(),module.glassBlur(),module.glassTint(),
                selected || GlassPolicy.selected(c.kind,rect.width(),rect.height(),module.glassDensity(),color,Palette.isDark(base)),outline);
    }
    private boolean ready(int kind){return backdrop!=null && viewportWidth>0 && viewportHeight>0 && module.glassConfigured();}
    // Work mode and a cold-start drawer may be drawn before a conversation scaffold exists.
    // Use the actual Compose owner and the composer's theme until the chat supplies its backdrop.
    private void ensureBackdrop(Context c)throws Exception{
        if(backdrop!=null && viewportWidth>0 && viewportHeight>0)return;
        Object owner=layoutOwner.get(layout.get(c.coordinator));
        if(!(owner instanceof View))return;
        View view=(View)owner;
        if(view.getWidth()<=0 || view.getHeight()<=0)return;
        int[] screen={0,0};view.getLocationOnScreen(screen);
        backdrop=new FlowRenderer();viewportWidth=view.getWidth();viewportHeight=view.getHeight();
        originX=screen[0];originY=screen[1];frameTime=SystemClock.uptimeMillis();
        module.glassWake();
    }
    private void track(Object node){
        if(node==null)return;
        for(WeakReference<Object> ref:live)if(ref.get()==node)return;
        live.add(new WeakReference<>(node));
        module.glassWake();
    }
    void backdrop(FlowRenderer renderer,float w,float h,int base,Object node,long time){
        this.backdrop=renderer;viewportWidth=w;viewportHeight=h;this.base=base;frameTime=time;
        try{Object coordinator=nodeCoordinator.invoke(null,node,4);float[] p=screenPosition(coordinator);originX=p[0];originY=p[1];}
        catch(Throwable error){module.glassError("Cannot locate glass backdrop",error);}
    }
    void invalidateAll(){
        Iterator<WeakReference<Object>> it=live.iterator();
        while(it.hasNext()){
            Object node=it.next().get();
            if(node==null || !module.glassNodeAttached(node)){it.remove();continue;}
            try{invalidate.invoke(null,node);}catch(Throwable error){it.remove();}
        }
    }
    boolean hasActiveSurfaces(){
        if(backdrop==null)return false;
        for(NativePopup popup:popups.values()){PopupWindow w=popup.window.get();if(w!=null && w.isShowing())return true;}
        for(WeakReference<Object> ref:live)if(module.glassNodeAttached(ref.get()))return true;
        return false;
    }
    void frame(long time){
        frameTime=time;kinds.clear();invalidateAll();
        for(NativePopup popup:popups.values())if(popup.material!=null)popup.material.invalidateSelf();
    }
    void changed(){
        kinds.clear();invalidateAll();
        for(NativePopup popup:popups.values())applyPopup(popup);
    }
    void foreground(){
        kinds.clear();surfaces.clear();footerMaterials.clear();
        for(NativePopup popup:popups.values())if(popup.material!=null)popup.material.reset();
        changed();
    }
    void destroyed(){
        footerGradients.clear();topGradients.clear();settingsGradients.clear();footerMaterials.clear();
        for(NativePopup popup:popups.values())restorePopup(popup);
        popups.clear();surfaces.clear();kinds.clear();live.clear();backdrop=null;viewportWidth=viewportHeight=0;
    }
    float density(){return module.glassDensity();}
    boolean menuReady(){return ready(GlassPolicy.MENU);}
    void popupError(Throwable error){module.glassError("Cannot draw native popup material",error);}
    boolean drawPopup(GlassRenderer renderer,View content,Canvas canvas,RectF rect)throws Exception{
        int[] screen={0,0};content.getLocationOnScreen(screen);
        float radius=16*module.glassDensity();
        return renderer.draw(canvas,rect,radius,radius,screen[0]-originX,screen[1]-originY,
                backdrop,viewportWidth,viewportHeight,base,Palette.isDark(base)?0xff252530:0xffffffff,
                module.glassSpeed(),module.glassStrength(),frameTime,module.glassDensity(),module.glassBlur(),module.glassTint(),false);
    }
    private void installPopupHooks(){
        for(Method m:PopupWindow.class.getDeclaredMethods()){
            if(!m.getName().equals("showAtLocation") && !m.getName().equals("showAsDropDown"))continue;
            m.setAccessible(true);
            module.glassHook(m,chain->{
                NativePopup state=null;
                try{
                    PopupWindow window=(PopupWindow)chain.getThisObject();
                    if(window.getContentView()!=null){
                        state=popups.get(window);
                        if(state==null || state.content.get()!=window.getContentView()){
                            state=new NativePopup(window);popups.put(window,state);
                        }
                        applyPopup(state);
                    }
                }catch(Throwable error){popupError(error);}
                Object result=chain.proceed();
                module.glassWake();return result;
            });
        }
        try{
            Method dismiss=PopupWindow.class.getDeclaredMethod("dismiss");
            module.glassHook(dismiss,chain->{
                Object result=chain.proceed();
                NativePopup popup=popups.get(chain.getThisObject());
                if(popup!=null)restorePopup(popup);
                return result;
            });
        }catch(NoSuchMethodException missing){module.glassError("Cannot observe popup dismissal",missing);}
    }
    private void applyPopup(NativePopup state){
        PopupWindow w=state.window.get();View content=state.content.get();
        if(w==null || content==null)return;
        if(!module.glassConfigured()){restorePopup(state);return;}
        Drawable root=content.getBackground();
        if(!state.rootCleared && root instanceof ColorDrawable && (((ColorDrawable)root).getColor()>>>24)>32){
            state.rootOriginal=root;
            state.left=content.getPaddingLeft();state.top=content.getPaddingTop();
            state.right=content.getPaddingRight();state.bottom=content.getPaddingBottom();
            state.clearMarker=new ColorDrawable(0);
            content.setBackground(state.clearMarker);
            content.setPadding(state.left,state.top,state.right,state.bottom);
            state.rootCleared=true;
        }
        if(state.material==null)state.material=new PopupGlassDrawable(this,content,state.original);
        state.material.rootFallback(state.rootOriginal);
        if(!state.applied){w.setBackgroundDrawable(state.material);state.applied=true;}
        state.material.invalidateSelf();
    }
    private void restorePopup(NativePopup state){
        PopupWindow w=state.window.get();
        if(w!=null && state.applied){w.setBackgroundDrawable(state.original);state.applied=false;}
        View content=state.content.get();
        if(state.rootCleared && content!=null && content.getBackground()==state.clearMarker){
            content.setBackground(state.rootOriginal);
            content.setPadding(state.left,state.top,state.right,state.bottom);
        }
        state.rootCleared=false;
        if(state.material!=null)state.material.invalidateSelf();
    }
}
