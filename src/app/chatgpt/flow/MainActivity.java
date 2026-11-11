package app.chatgpt.flow;

import android.app.Activity;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Module settings only. Preference names, defaults and the hook adapter stay unchanged. */
public final class MainActivity extends Activity {
    private SharedPreferences prefs;
    private Preview preview;
    private boolean dark;
    private int text,muted,background,surface,subtle,accent,accentSoft,line;
    private FrameLayout pages;
    private ScrollView scroll;
    private ScrollView outgoingPage;
    private ValueAnimator pageAnimation,navAnimation;
    private View navIndicator;
    private LinearLayout navRow;
    private static final long TAB_DURATION=300;
    private int page=-1;
    private final int[] offsets=new int[3];
    private final LinearLayout[] navItems=new LinearLayout[3];
    private final TextView[] navLabels=new TextView[3];
    private final Icon[] navIcons=new Icon[3];
    private TextView effectLabel;

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        int theme=getResources().getIdentifier(dark?"Theme.Material.NoActionBar":"Theme.Material.Light.NoActionBar","style","android");
        if(theme!=0)setTheme(theme);
        prefs=getSharedPreferences(SettingsProvider.PREFS,0);
        PreferenceMigration.importOnce(this,prefs);
        text=dark?0xfff2f3f7:0xff1b1d24;
        muted=dark?0xffa4a9b8:0xff707583;
        background=dark?0xff101114:0xfff4f5f8;
        surface=dark?0xff1d1f25:0xffffffff;
        subtle=dark?0xff282b34:0xfff1f3f8;
        accent=dark?0xff9aaeff:0xff4264ee;
        accentSoft=dark?0xff30394f:0xffedf0ff;
        line=dark?0xff2b2e37:0xfff0f1f5;
        systemBars();
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(background);
        pages=new FrameLayout(this);FrameLayout.LayoutParams pagesLp=new FrameLayout.LayoutParams(-1,-1);pagesLp.bottomMargin=dp(96);root.addView(pages,pagesLp);
        FrameLayout nav=new FrameLayout(this);nav.setBackground(round(surface,30));
        navIndicator=new View(this);navIndicator.setBackground(round(accentSoft,24));
        nav.addView(navIndicator,new FrameLayout.LayoutParams(0,dp(56)));
        navRow=new LinearLayout(this);navRow.setGravity(Gravity.CENTER);navRow.setPadding(dp(7),dp(6),dp(7),dp(6));
        nav.addView(navRow,new FrameLayout.LayoutParams(-1,-1));
        navRow.addOnLayoutChangeListener((view,l,t,r,b,ol,ot,or,ob)->{
            if(page>=0&&(r-l!=or-ol||b-t!=ob-ot||navIndicator.getWidth()==0))updateNavigation(false);
        });
        FrameLayout.LayoutParams navLp=new FrameLayout.LayoutParams(dp(300),dp(68),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        navLp.bottomMargin=dp(14);root.addView(nav,navLp);
        String[] titles={"概览","外观","关于"},icons={"home","sliders","info"};
        for(int i=0;i<3;i++){
            final int target=i;
            LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER);item.setOrientation(LinearLayout.VERTICAL);
            Icon icon=new Icon(icons[i],muted);item.addView(icon,new LinearLayout.LayoutParams(dp(22),dp(22)));
            TextView label=label(titles[i],11,muted,true);label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelLp=new LinearLayout.LayoutParams(-2,-2);labelLp.topMargin=dp(3);item.addView(label,labelLp);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.leftMargin=dp(2);lp.rightMargin=dp(2);navRow.addView(item,lp);
            item.setContentDescription(titles[i]);item.setOnClickListener(v->showPage(target));item.setFocusable(true);
            navItems[i]=item;navLabels[i]=label;navIcons[i]=icon;
        }
        setContentView(root);
        int initial=0;
        if(saved!=null){initial=Palette.clamp(saved.getInt("page",0),0,2);int[] old=saved.getIntArray("offsets");if(old!=null&&old.length==3)System.arraycopy(old,0,offsets,0,3);}
        showPage(initial);
    }

    private void systemBars(){
        // These APIs postdate the small compile stub, but exist on every supported Android version.
        try{getWindow().getClass().getMethod("setStatusBarColor",int.class).invoke(getWindow(),background);
            getWindow().getClass().getMethod("setNavigationBarColor",int.class).invoke(getWindow(),background);
            getWindow().getDecorView().setSystemUiVisibility(dark?0:0x2000|0x10);
            getWindow().getClass().getMethod("setNavigationBarContrastEnforced",boolean.class).invoke(getWindow(),false);
        }catch(ReflectiveOperationException ignored){}
    }

    private void showPage(int next){
        if(page==next)return;
        int previous=page;
        finishPageTransition();
        ScrollView old=scroll;
        if(old!=null&&previous>=0)offsets[previous]=old.getScrollY();
        boolean animate=previous>=0&&pages.getWidth()>0&&animationsAllowed();
        page=next;preview=null;effectLabel=null;
        if(!animate)pages.removeAllViews();
        else ((PageScrollView)old).interactive=false;
        scroll=new PageScrollView();scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setId(0x5100+next);
        FrameLayout shell=new FrameLayout(this);
        LinearLayout content=column();content.setPadding(dp(22),dp(20),dp(22),dp(20));
        int width=Math.min(getResources().getDisplayMetrics().widthPixels,dp(620));
        shell.addView(content,new FrameLayout.LayoutParams(width,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));
        scroll.addView(shell,new ScrollView.LayoutParams(-1,-2));pages.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        if(next==0)overview(content);else if(next==1)appearance(content);else about(content);
        updateNavigation(animate);
        final ScrollView current=scroll;final int position=offsets[next];current.post(()->current.scrollTo(0,position));
        if(animate)animatePage(old,current,next>previous?1:-1);
    }

    private boolean animationsAllowed(){
        try{return (Boolean)ValueAnimator.class.getMethod("areAnimatorsEnabled").invoke(null);}
        catch(ReflectiveOperationException ignored){return true;}
    }

    private void animatePage(ScrollView old,ScrollView incoming,int direction){
        outgoingPage=old;
        float distance=direction*pages.getWidth();
        incoming.setTranslationX(distance);incoming.setAlpha(0.75f);
        ValueAnimator animation=ValueAnimator.ofFloat(0,1);pageAnimation=animation;
        animation.setDuration(TAB_DURATION);animation.setInterpolator(new DecelerateInterpolator(1.5f));
        animation.addUpdateListener(value->{
            float fraction=(Float)value.getAnimatedValue();
            incoming.setTranslationX(distance*(1-fraction));incoming.setAlpha(0.75f+0.25f*fraction);
            old.setTranslationX(-distance*fraction);old.setAlpha(1-0.25f*fraction);
        });
        animation.addListener(new AnimatorListenerAdapter(){
            @Override public void onAnimationEnd(Animator finished){
                if(pageAnimation!=animation)return;
                incoming.setTranslationX(0);incoming.setAlpha(1);pages.removeView(old);
                outgoingPage=null;pageAnimation=null;
            }
        });animation.start();
    }

    private void finishPageTransition(){
        if(pageAnimation!=null){ValueAnimator animation=pageAnimation;pageAnimation=null;animation.removeAllListeners();animation.cancel();}
        if(outgoingPage!=null){pages.removeView(outgoingPage);outgoingPage=null;}
        if(scroll!=null){scroll.setTranslationX(0);scroll.setAlpha(1);((PageScrollView)scroll).interactive=true;}
    }

    private void updateNavigation(boolean animate){
        if(navAnimation!=null){navAnimation.cancel();navAnimation=null;}
        for(int i=0;i<3;i++){
            navItems[i].setSelected(i==page);
            navItems[i].setContentDescription(new String[]{"概览","外观","关于"}[i]+(i==page?"，当前页面":""));
        }
        View target=navItems[page];
        float fromX=navIndicator.getTranslationX(),toX=target.getLeft();
        if(target.getWidth()>0){
            ViewGroup.LayoutParams lp=navIndicator.getLayoutParams();
            if(lp.width!=target.getWidth()||lp.height!=target.getHeight()){
                lp.width=target.getWidth();lp.height=target.getHeight();navIndicator.setLayoutParams(lp);
            }
            navIndicator.setTranslationY(target.getTop());
        }
        if(!animate||target.getWidth()==0||!animationsAllowed()){
            navIndicator.setTranslationX(toX);
            for(int i=0;i<3;i++){int color=i==page?accent:muted;navLabels[i].setTextColor(color);navIcons[i].color(color);}
            return;
        }
        int[] from=new int[3],to=new int[3];
        for(int i=0;i<3;i++){from[i]=navLabels[i].getCurrentTextColor();to[i]=i==page?accent:muted;}
        ValueAnimator animation=ValueAnimator.ofFloat(0,1);navAnimation=animation;
        animation.setDuration(TAB_DURATION);animation.setInterpolator(new DecelerateInterpolator(1.5f));
        animation.addUpdateListener(value->{
            float fraction=(Float)value.getAnimatedValue();navIndicator.setTranslationX(fromX+(toX-fromX)*fraction);
            for(int i=0;i<3;i++){int color=blendColor(from[i],to[i],fraction);navLabels[i].setTextColor(color);navIcons[i].color(color);}
        });
        animation.addListener(new AnimatorListenerAdapter(){
            @Override public void onAnimationEnd(Animator finished){if(navAnimation==animation)navAnimation=null;}
        });animation.start();
    }

    private int blendColor(int from,int to,float fraction){
        int result=0xff000000;
        for(int shift=0;shift<=16;shift+=8){int a=(from>>>shift)&255,b=(to>>>shift)&255;result|=Math.round(a+(b-a)*fraction)<<shift;}
        return result;
    }

    @Override protected void onPause(){
        finishPageTransition();if(page>=0)updateNavigation(false);super.onPause();
    }

    private final class PageScrollView extends ScrollView {
        boolean interactive=true;
        PageScrollView(){super(MainActivity.this);}
        @Override public boolean dispatchTouchEvent(MotionEvent event){return !interactive||super.dispatchTouchEvent(event);}
    }

    private void heading(LinearLayout parent,String title,String subtitle){
        LinearLayout brand=row();brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView word=label("CHATGPT  /  FLOW",11,muted,true);brand.addView(word,new LinearLayout.LayoutParams(0,-2,1));
        TextView version=label("v1.0.20",11,accent,true);version.setPadding(dp(10),dp(5),dp(10),dp(5));version.setBackground(round(accentSoft,12));brand.addView(version);
        parent.addView(brand);space(parent,16);
        parent.addView(label(title,32,text,true));space(parent,6);parent.addView(label(subtitle,13,muted,false));space(parent,24);
    }

    private void overview(LinearLayout parent){
        heading(parent,"流光背景","让每一次对话，都有自己的色彩。");
        FrameLayout hero=new FrameLayout(this);
        preview=new Preview();preview.setContentDescription("流光背景实时预览");hero.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout overlay=column();overlay.setPadding(dp(23),dp(21),dp(23),dp(21));
        TextView tag=label("LIVE PREVIEW",10,dark?0xffb6c7ff:0xff5363a6,true);overlay.addView(tag);space(overlay,9);
        overlay.addView(label("流动的色彩",25,text,true));space(overlay,4);
        overlay.addView(label("此刻的聊天背景",12,dark?0xffc5c6d5:0xff686d85,false));space(overlay,24);
        LinearLayout composer=row();composer.setGravity(Gravity.CENTER_VERTICAL);composer.setPadding(dp(14),dp(12),dp(14),dp(12));
        composer.setBackground(round(dark?0x66434858:0xaaffffff,25));
        Icon plus=new Icon("plus",text);composer.addView(plus,new LinearLayout.LayoutParams(dp(19),dp(19)));
        TextView prompt=label("询问 ChatGPT",13,dark?0xffc1c4d0:0xff9397a7,false);
        LinearLayout.LayoutParams promptLp=new LinearLayout.LayoutParams(0,-2,1);promptLp.leftMargin=dp(12);composer.addView(prompt,promptLp);
        Icon wave=new Icon("wave",accent);composer.addView(wave,new LinearLayout.LayoutParams(dp(22),dp(22)));
        overlay.addView(composer,new LinearLayout.LayoutParams(-1,-2));
        hero.addView(overlay,new FrameLayout.LayoutParams(-1,-2));
        // wrap content lets the native labels grow with the system font size.
        overlay.measure(View.MeasureSpec.makeMeasureSpec(Math.min(getResources().getDisplayMetrics().widthPixels,dp(620))-dp(44),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        parent.addView(hero,new LinearLayout.LayoutParams(-1,Math.max(dp(200),overlay.getMeasuredHeight())));space(parent,18);
        LinearLayout master=card(parent);
        toggle(master,"flow","启用流光背景","循环渐变，后台自动暂停","enabled",true);
        space(parent,18);
        TextView launch=label("打开 ChatGPT  ↗",15,0xffffffff,true);launch.setGravity(Gravity.CENTER);launch.setPadding(dp(20),dp(17),dp(20),dp(17));
        launch.setBackground(buttonBackground(0xff4264ee,0xff3151d4,22));launch.setOnClickListener(v->launchChatGPT());launch.setFocusable(true);
        parent.addView(launch,new LinearLayout.LayoutParams(-1,-2));space(parent,11);
        TextView hint=label("首次使用，请先在 LSPosed 中启用模块",11,muted,false);hint.setGravity(Gravity.CENTER);parent.addView(hint);
        section(parent,"当前外观","小小调整，刚好喜欢");
        LinearLayout stats=row();
        stat(stats,"流动速度",Integer.toString(prefs.getInt("speed",55)),"speed");
        View gap=new View(this);stats.addView(gap,new LinearLayout.LayoutParams(dp(12),1));
        stat(stats,"色彩强度",prefs.getInt("strength",70)+"%","spark");parent.addView(stats);

    }

    private void appearance(LinearLayout parent){
        heading(parent,"外观","色彩、磨砂与细节，由你决定。");
        sectionLabel(parent,"流光","flow");
        LinearLayout flow=card(parent);
        slider(flow,"流动速度","speed",55,1,100,"");divider(flow);
        slider(flow,"色彩强度","strength",70,0,100,"%");
        note(parent,"拖动即生效，切回概览可查看实时预览。");
        sectionLabel(parent,"磨砂材质","glass");
        LinearLayout glass=card(parent);
        toggle(glass,"glass","控件磨砂材质","按钮、输入栏、侧栏与二级菜单","glass_enabled",true);divider(glass);
        slider(glass,"模糊半径","glass_blur",22,0,60," dp");divider(glass);
        slider(glass,"材质底色浓度","glass_tint",38,0,100,"%");
        note(parent,"文字和图标保持清晰，聊天／工作指示块沿用原生滑动动画。");
        sectionLabel(parent,"边缘与键盘","shade");
        LinearLayout edges=card(parent);
        toggle(edges,"shade","原阴影区域磨砂","聊天页、侧栏与设置页的滚动边缘","footer_frosted",true);
        description(edges,"共用上方模糊半径和底色浓度，随滚动渐入；关闭后保持去阴影效果。");divider(edges);
        toggle(edges,"keyboard","移除键盘黑色遮罩","实验功能","ime_compat",false);
        description(edges,"处理聊天页键盘遮罩，将流光延展到键盘下方。启用后收起并重新打开键盘。");
        note(parent,"所有参数与开关即时保存。");
    }

    private void about(LinearLayout parent){
        heading(parent,"关于","简单设置，让聊天焕然一新。");
        LinearLayout identity=card(parent);identity.setPadding(dp(23),dp(24),dp(23),dp(24));
        LinearLayout titleRow=row();titleRow.setGravity(Gravity.CENTER_VERTICAL);
        Icon mark=new Icon("flow",accent);mark.setPadding(dp(11),dp(11),dp(11),dp(11));mark.setBackground(round(accentSoft,17));titleRow.addView(mark,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout names=column();LinearLayout.LayoutParams namesLp=new LinearLayout.LayoutParams(0,-2,1);namesLp.leftMargin=dp(15);
        names.addView(label("ChatGPT 流光背景",18,text,true));space(names,5);names.addView(label("FLOW  ·  1.0.20",11,muted,true));titleRow.addView(names,namesLp);identity.addView(titleRow);
        space(identity,18);identity.addView(label("循环彩虹渐变与磨砂材质，\n为你的聊天增添一点流动感。",13,muted,false));
        section(parent,"开始使用","只需三步");
        LinearLayout guide=card(parent);
        step(guide,"01","启用模块","在 LSPosed 中启用「ChatGPT 流光背景」");divider(guide);
        step(guide,"02","选择作用域","勾选「ChatGPT」");divider(guide);
        step(guide,"03","重新打开应用","强行停止 ChatGPT，再重新打开");
        section(parent,"版本信息",null);
        LinearLayout versions=card(parent);
        info(versions,"模块版本","1.0.20");divider(versions);
        info(versions,"适配 ChatGPT","1.2026.307");divider(versions);
        info(versions,"构建版本","2630709 / 2630717 / 2630718");divider(versions);
        info(versions,"框架接口","LSPosed API 102");
        space(parent,22);TextView footer=label("FLOW WITH YOUR CONVERSATION",10,muted,true);footer.setGravity(Gravity.CENTER);parent.addView(footer);
    }

    private void stat(LinearLayout row,String title,String value,String icon){
        LinearLayout box=column();box.setPadding(dp(18),dp(17),dp(18),dp(16));box.setBackground(round(surface,24));
        Icon glyph=new Icon(icon,accent);box.addView(glyph,new LinearLayout.LayoutParams(dp(21),dp(21)));space(box,13);
        box.addView(label(value,28,text,true));space(box,3);box.addView(label(title,12,muted,false));
        row.addView(box,new LinearLayout.LayoutParams(0,-2,1));
    }

    private void toggle(LinearLayout parent,String icon,String title,String subtitle,String key,boolean fallback){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(18),dp(17),dp(14),dp(17));row.setMinimumHeight(dp(82));
        Icon glyph=new Icon(icon,accent);glyph.setPadding(dp(8),dp(8),dp(8),dp(8));glyph.setBackground(round(accentSoft,13));row.addView(glyph,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout copy=column();LinearLayout.LayoutParams copyLp=new LinearLayout.LayoutParams(0,-2,1);copyLp.leftMargin=dp(13);copyLp.rightMargin=dp(10);
        copy.addView(label(title,15,text,true));space(copy,5);
        TextView caption=label(subtitle,11,muted,false);copy.addView(caption);if(key.equals("enabled")){effectLabel=caption;caption.setText(prefs.getBoolean(key,fallback)?"效果已开启 · 后台自动暂停":"效果已暂停");}
        row.addView(copy,copyLp);
        Switch control=new Switch(this);control.setId(id(key));control.setTextOn("");control.setTextOff("");control.setContentDescription(title);
        control.setSwitchMinWidth(dp(54));control.setSwitchPadding(0);control.setPadding(0,0,0,0);
        GradientDrawable thumb=round(0xffffffff,20);thumb.setSize(dp(24),dp(24));control.setThumbDrawable(new InsetDrawable(thumb,dp(3)));
        StateListDrawable track=new StateListDrawable();GradientDrawable on=round(0xff4264ee,18),off=round(dark?0xff484c58:0xffdfe2ea,18);
        on.setSize(dp(54),dp(30));off.setSize(dp(54),dp(30));track.addState(new int[]{android.R.attr.state_checked},on);track.addState(new int[]{},off);control.setTrackDrawable(track);
        try{Switch.class.getMethod("setSplitTrack",boolean.class).invoke(control,false);}catch(ReflectiveOperationException ignored){}
        control.setChecked(prefs.getBoolean(key,fallback));
        control.setOnCheckedChangeListener((button,checked)->save(key,checked));
        row.addView(control,new LinearLayout.LayoutParams(dp(56),dp(48)));row.setOnClickListener(v->control.toggle());
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    private void slider(LinearLayout parent,String title,String key,int fallback,int minimum,int maximum,String unit){
        LinearLayout box=column();box.setPadding(dp(20),dp(18),dp(20),dp(10));
        LinearLayout labels=row();labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.addView(label(title,14,text,true),new LinearLayout.LayoutParams(0,-2,1));
        int value=Palette.clamp(prefs.getInt(key,fallback),minimum,maximum);
        TextView valueLabel=label(value+unit,12,accent,true);valueLabel.setGravity(Gravity.CENTER);valueLabel.setMinWidth(dp(48));valueLabel.setPadding(dp(10),dp(5),dp(10),dp(5));valueLabel.setBackground(round(accentSoft,10));labels.addView(valueLabel);box.addView(labels);
        SeekBar bar=new SeekBar(this);bar.setId(id(key));bar.setMax(maximum-minimum);bar.setPadding(dp(6),dp(8),dp(6),dp(8));
        android.graphics.drawable.Drawable rail=new SliderRail(dark?0xff393d48:0xffe9ecf4),fill=new SliderRail(accent);
        LayerDrawable progress=new LayerDrawable(new android.graphics.drawable.Drawable[]{rail,new ClipDrawable(fill,Gravity.LEFT,ClipDrawable.HORIZONTAL)});
        progress.setId(0,android.R.id.background);progress.setId(1,android.R.id.progress);bar.setProgressDrawable(progress);
        GradientDrawable thumb=round(accent,12);thumb.setSize(dp(20),dp(20));bar.setThumb(thumb);bar.setThumbOffset(dp(10));bar.setProgress(value-minimum);
        bar.setContentDescription(title+"，"+value+unit);
        box.addView(bar,new LinearLayout.LayoutParams(-1,dp(44)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar seek,int progress,boolean fromUser){int next=progress+minimum;valueLabel.setText(next+unit);seek.setContentDescription(title+"，"+next+unit);if(fromUser)save(key,next);}
            @Override public void onStartTrackingTouch(SeekBar seek){}
            @Override public void onStopTrackingTouch(SeekBar seek){}
        });parent.addView(box,new LinearLayout.LayoutParams(-1,-2));
    }

    private void save(String key,Object value){
        SharedPreferences.Editor edit=prefs.edit();if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);else edit.putInt(key,(Integer)value);edit.apply();
        getContentResolver().notifyChange(SettingsProvider.URI,null);
        if(preview!=null)preview.invalidate();
        if(effectLabel!=null&&key.equals("enabled"))effectLabel.setText((Boolean)value?"效果已开启 · 后台自动暂停":"效果已暂停");
    }
    private void launchChatGPT(){
        Intent intent=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
        if(intent!=null)startActivity(intent);else Toast.makeText(this,"未找到 ChatGPT",Toast.LENGTH_SHORT).show();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        if(scroll!=null&&page>=0)offsets[page]=scroll.getScrollY();out.putInt("page",page);out.putIntArray("offsets",offsets);super.onSaveInstanceState(out);
    }
    private void step(LinearLayout parent,String number,String title,String subtitle){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(20),dp(18),dp(20),dp(18));
        TextView n=label(number,13,accent,true);n.setGravity(Gravity.CENTER);n.setBackground(round(accentSoft,12));row.addView(n,new LinearLayout.LayoutParams(dp(35),dp(35)));
        LinearLayout copy=column();copy.addView(label(title,14,text,true));space(copy,5);copy.addView(label(subtitle,12,muted,false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(14);row.addView(copy,lp);parent.addView(row);
    }
    private void info(LinearLayout parent,String name,String value){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(20),dp(18),dp(20),dp(18));row.addView(label(name,13,text,false),new LinearLayout.LayoutParams(0,-2,1));row.addView(label(value,12,muted,false));parent.addView(row);
    }
    private void section(LinearLayout parent,String title,String subtitle){
        space(parent,26);LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.addView(label(title,18,text,true),new LinearLayout.LayoutParams(0,-2,1));if(subtitle!=null)row.addView(label(subtitle,11,muted,false));parent.addView(row);space(parent,13);
    }
    private void sectionLabel(LinearLayout parent,String title,String icon){
        LinearLayout row=row();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(3),0,0,0);row.addView(new Icon(icon,muted),new LinearLayout.LayoutParams(dp(16),dp(16)));
        TextView label=label(title,12,muted,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.leftMargin=dp(7);row.addView(label,lp);parent.addView(row);space(parent,12);
    }
    private void note(LinearLayout parent,String value){TextView note=label(value,11,muted,false);note.setPadding(dp(6),dp(11),dp(6),dp(24));parent.addView(note);}
    private void description(LinearLayout parent,String value){TextView note=label(value,12,muted,false);note.setPadding(dp(20),0,dp(20),dp(18));parent.addView(note);}
    private LinearLayout card(LinearLayout parent){LinearLayout card=column();card.setBackground(round(surface,24));parent.addView(card,new LinearLayout.LayoutParams(-1,-2));return card;}
    private void divider(LinearLayout parent){View v=new View(this);v.setBackgroundColor(line);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.leftMargin=dp(20);lp.rightMargin=dp(20);parent.addView(v,lp);}
    private TextView label(String value,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setIncludeFontPadding(false);v.setLineSpacing(dp(3),1);if(bold)v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));return v;}
    private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private StateListDrawable buttonBackground(int normal,int pressed,int radius){StateListDrawable d=new StateListDrawable();d.addState(new int[]{android.R.attr.state_pressed},round(pressed,radius));d.addState(new int[]{},round(normal,radius));return d;}
    private void space(LinearLayout parent,int height){parent.addView(new View(this),new LinearLayout.LayoutParams(1,dp(height)));}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private int id(String key){return 0x5200+java.util.Arrays.asList("enabled","speed","strength","glass_enabled","glass_blur","glass_tint","footer_frosted","ime_compat").indexOf(key);}

    private final class Preview extends View {
        private final FlowRenderer renderer=new FlowRenderer();
        private final Path clip=new Path();
        private final RectF bounds=new RectF();
        Preview(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas canvas){
            int save=canvas.save();bounds.set(0,0,getWidth(),getHeight());clip.reset();clip.addRoundRect(bounds,dp(28),dp(28),Path.Direction.CW);canvas.clipPath(clip);
            boolean on=prefs.getBoolean("enabled",true);int strength=prefs.getInt("strength",70);
            renderer.draw(canvas,getWidth(),getHeight(),dark?0xff212121:0xfff9f9f9,on,prefs.getInt("speed",55),strength);canvas.restoreToCount(save);
            if(on&&strength>0&&hasWindowFocus()&&getVisibility()==VISIBLE)postInvalidateDelayed(33);
        }
        @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)invalidate();}
    }

    private final class SliderRail extends android.graphics.drawable.Drawable {
        private final Paint paint=new Paint(3);
        SliderRail(int color){paint.setColor(color);}
        @Override public void draw(Canvas canvas){android.graphics.Rect b=getBounds();float y=b.exactCenterY();canvas.drawRoundRect(new RectF(b.left,y-dp(2),b.right,y+dp(2)),dp(2),dp(2),paint);}
        @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(android.graphics.ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
        @Override public int getIntrinsicHeight(){return dp(4);}
    }

    /** Small monochrome line icons, drawn at any density without a font dependency. */
    private final class Icon extends View {
        private final String kind;private final Paint paint=new Paint(3);private final Path path=new Path();
        Icon(String kind,int color){super(MainActivity.this);this.kind=kind;paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.8f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void color(int color){paint.setColor(color);invalidate();}
        private void segment(Canvas c,float x,float y,float a,float b){c.drawLine(x,y,a,b,paint);}
        @Override protected void onDraw(Canvas canvas){
            int saved=canvas.save();float size=Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight()-getPaddingTop()-getPaddingBottom());
            canvas.translate((getWidth()-size)/2,(getHeight()-size)/2);canvas.scale(size/24,size/24);path.reset();
            if(kind.equals("home")){path.moveTo(3,10);path.lineTo(12,3);path.lineTo(21,10);path.lineTo(21,20);path.lineTo(15,20);path.lineTo(15,14);path.lineTo(9,14);path.lineTo(9,20);path.lineTo(3,20);path.close();canvas.drawPath(path,paint);}
            else if(kind.equals("sliders")){segment(canvas,5,3,5,21);segment(canvas,12,3,12,21);segment(canvas,19,3,19,21);canvas.drawCircle(5,8,2.2f,paint);canvas.drawCircle(12,16,2.2f,paint);canvas.drawCircle(19,9,2.2f,paint);}
            else if(kind.equals("info")){canvas.drawCircle(12,12,9,paint);segment(canvas,12,11,12,17);canvas.drawCircle(12,7.5f,0.5f,paint);}
            else if(kind.equals("plus")){segment(canvas,5,12,19,12);segment(canvas,12,5,12,19);}
            else if(kind.equals("wave")){segment(canvas,4,10,4,14);segment(canvas,8,6,8,18);segment(canvas,12,3,12,21);segment(canvas,16,7,16,17);segment(canvas,20,10,20,14);}
            else if(kind.equals("flow")){path.moveTo(2,8);path.cubicTo(8,-1,16,17,22,8);canvas.drawPath(path,paint);path.reset();path.moveTo(2,16);path.cubicTo(8,7,16,25,22,16);canvas.drawPath(path,paint);}
            else if(kind.equals("glass")){canvas.drawRoundRect(new RectF(3,3,21,21),5,5,paint);segment(canvas,8,15,15,8);segment(canvas,12,17,17,12);}
            else if(kind.equals("keyboard")){canvas.drawRoundRect(new RectF(2,5,22,19),3,3,paint);for(int r=0;r<2;r++)for(int col=0;col<4;col++)segment(canvas,6+col*4,9+r*4,6.5f+col*4,9+r*4);segment(canvas,8,16,16,16);}
            else if(kind.equals("shade")){canvas.drawRoundRect(new RectF(4,3,20,21),4,4,paint);segment(canvas,7,8,17,8);segment(canvas,8,12,16,12);segment(canvas,9,16,15,16);}
            else if(kind.equals("speed")){canvas.drawArc(new RectF(3,4,21,22),180,180,false,paint);segment(canvas,12,13,17,8);segment(canvas,3,14,3,16);segment(canvas,21,14,21,16);}
            else if(kind.equals("spark")){path.moveTo(12,2);path.lineTo(15,9);path.lineTo(22,12);path.lineTo(15,15);path.lineTo(12,22);path.lineTo(9,15);path.lineTo(2,12);path.lineTo(9,9);path.close();canvas.drawPath(path,paint);}
            else {path.moveTo(9,6);path.lineTo(15,12);path.lineTo(9,18);canvas.drawPath(path,paint);}
            canvas.restoreToCount(saved);
        }
    }
}
