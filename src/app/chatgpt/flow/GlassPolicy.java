package app.chatgpt.flow;

/** Targets the chat chrome, leaving message bodies and tiny icon primitives alone. */
public final class GlassPolicy {
    public static final int NONE=0, BUTTON=1, SWITCH=2, INPUT=3, VOICE=4, MENU=5, EXCLUDE=6, SELECTED_TAB=7, PLAIN_BUTTON=8;
    private GlassPolicy() {}
    public static int kind(String tag) {
        if (tag == null) return NONE;
        if(tag.equals("FlowModeSelectionIndicator"))return SELECTED_TAB;
        if(composerButton(tag))return PLAIN_BUTTON;
        if(tag.contains("Scrim") || tag.contains("CursorHandle") || tag.contains("SelectionHandle"))return EXCLUDE;
        if (menuTag(tag)) return MENU;
        if (tag.equals("ComposerVoiceButton") || tag.equals("ConversationTopBarVoiceButton")) return VOICE;
        if (tag.equals("WorkModeToggle") || tag.equals("SegmentedPillTabRow") || tag.equals("TopBarPill")) return SWITCH;
        if (tag.equals("MultilineComposerLayout") || tag.equals("MultilineComposerLayoutContent")
                || tag.equals("MultilineComposerLayoutContentImpl") || tag.equals("ComposerInputHost")
                || tag.equals("ComposerInputHostContent") || tag.equals("ComposerLayout")
                || tag.equals("ConversationComposer") || tag.equals("ChatComposer")
                || tag.equals("MultilineComposerTextFieldContent") || tag.equals("MultilineComposerTextFieldContentImpl")) return INPUT;
        if (tag.equals("ConversationTopBar") || tag.equals("ConversationTopBarIconButton")
                || tag.equals("ConversationTopBarNewChatButton") || tag.equals("MainScreenTopBar")
                || tag.equals("TabTopBar") || tag.equals("TopBarNavButton") || tag.equals("TopBarButton")
                || tag.equals("NewChatButton") || tag.equals("SidebarMenuIcon") || tag.equals("SidebarLogoButton")
                || tag.equals("ComposerIconButton") || tag.equals("ComposerAttachButton")
                || tag.equals("ComposerAttachmentsButton") || tag.equals("ComposerAttachmentsButtonImpl")
                || tag.equals("ComposerDictationButton") || tag.equals("ComposerActionButton")) return BUTTON;
        return NONE;
    }
    public static int combine(int current, int parent) {
        if(current==EXCLUDE || parent==EXCLUDE)return EXCLUDE;
        if(current==PLAIN_BUTTON || parent==PLAIN_BUTTON)return PLAIN_BUTTON;
        if(current==SELECTED_TAB || parent==SELECTED_TAB)return SELECTED_TAB;
        if(current==MENU || parent==MENU)return MENU;
        if (current==VOICE || parent==VOICE) return VOICE;
        if (current==SWITCH || parent==SWITCH) return SWITCH;
        if (current==BUTTON || parent==BUTTON) return BUTTON;
        return current!=NONE ? current : parent;
    }
    public static boolean menuTag(String tag){
        return tag.startsWith("Sidebar") || tag.equals("ConversationSidebar") || tag.equals("ConversationSidebarContent")
                || tag.equals("FlowSidebarMaterialSurface")
                || tag.equals("SidebarContentLayer") || tag.equals("SidebarContentWithEffects")
                || tag.contains("PopupMenu") || tag.contains("ContextMenu")
                || tag.contains("Dropdown") || tag.contains("Popover")
                || tag.contains("BottomSheet") || tag.endsWith("Menu")
                || tag.endsWith("MenuContent") || tag.endsWith("MenuContainer")
                || tag.endsWith("SettingsScreen") || tag.endsWith("SettingsContent");
    }
    public static boolean settingsTag(String tag){return tag!=null && (tag.equals("YouScreen") || tag.contains("Settings"));}
    public static boolean selected(int kind,float width,float height,float density,int color,boolean dark){
        return kind==SELECTED_TAB && width>=48*density && width<=220*density && height>=28*density && height<=64*density;
    }
    public static boolean inject(String tag) {
        return "FlowSidebarMaterialSurface".equals(tag);
    }
    public static boolean suppressBoundaryFade(int kind,boolean insideChatRoot){
        if(kind==MENU || kind==EXCLUDE || kind==PLAIN_BUTTON)return false;
        return kind==INPUT || insideChatRoot;
    }
    public static int inherit(int nearest,int ancestor){
        if(nearest==EXCLUDE || ancestor==EXCLUDE)return EXCLUDE;
        return nearest==NONE?ancestor:nearest;
    }
    public static boolean sidebarSurface(float width,float height,float density){
        return density>0 && width>=180*density && height>=240*density && width<=680*density && height<=1600*density;
    }
    public static boolean composerButton(String tag){
        return "ComposerAttachButton".equals(tag) || "ComposerAttachmentsButton".equals(tag)
                || "ComposerAttachmentsButtonImpl".equals(tag) || "ComposerIconButton".equals(tag)
                || "ComposerDictationButton".equals(tag) || "ComposerActionButton".equals(tag)
                || "ComposerVoiceButton".equals(tag);
    }
    public static boolean panelPathEligible(int kind,float width,float height,float scopeWidth,float scopeHeight,
                                            float density,int color,boolean dark){
        boolean container=kind==INPUT && width>=140*density && height>=44*density
                || kind==MENU && (width>=140*density && height>=36*density || width>=120*density && height>=72*density);
        return container
                && eligible(kind,width,height,scopeWidth,scopeHeight,density,color,dark);
    }
    public static boolean eligible(int kind, float width, float height, float scopeWidth,
                                   float scopeHeight, float density, int color, boolean dark) {
        if (kind==NONE || kind==EXCLUDE || kind==PLAIN_BUTTON || density<=0 || !Float.isFinite(width) || !Float.isFinite(height)) return false;
        if (width<28*density || height<28*density || height>(kind==MENU?1500:kind==INPUT?1200:240)*density) return false;
        float coverage=kind==SWITCH?.25f:.68f;
        if (width<scopeWidth*coverage || height<scopeHeight*.6f) return false;
        if (kind==MENU && width>680*density)return false;
        if (kind!=INPUT && kind!=MENU && (width>280*density || height>88*density)) return false;
        if ((color>>>24)<20) return false;
        if(kind==MENU && (color>>>24)<160)return false;
        int r=(color>>>16)&255,g=(color>>>8)&255,b=color&255;
        if(kind==SELECTED_TAB && (color>>>24)>=160)return true;
        // A tagged, full-size panel is a backdrop even when it is near-black or near-white.
        // This also handles a theme transition before the conversation redraws its palette.
        if((kind==INPUT && width>=140*density && height>=44*density
                || kind==MENU && (width>=140*density && height>=36*density || width>=120*density && height>=72*density)) && (color>>>24)>=160)return true;
        // Black vectors in light mode, and white vectors in dark mode, retain their paints.
        if (!dark && r<65 && g<65 && b<65) return false;
        if (dark && r>220 && g>220 && b>220) return false;
        return true;
    }
}
