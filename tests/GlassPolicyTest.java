import app.chatgpt.flow.GlassPolicy;
class GlassPolicyTest {
 static void ok(boolean b,String label){if(!b)throw new AssertionError(label);}
 static boolean fill(int k,float w,float h,float sw,float sh,int color){return GlassPolicy.eligible(k,w,h,sw,sh,1,color,false);}
 public static void main(String[] args){
  ok(fill(GlassPolicy.BUTTON,48,48,48,48,0xffffffff),"Top circular buttons");
  ok(fill(GlassPolicy.SWITCH,180,48,180,48,0xffe6e6e6),"Chat/work track");
  ok(fill(GlassPolicy.SWITCH,86,40,180,48,0xffffffff),"Selected tab indicator");
  ok(fill(GlassPolicy.INPUT,370,54,370,54,0xffffffff),"Input bar");
  ok(fill(GlassPolicy.INPUT,340,360,340,360,0xff303030),"Input with image and multiple lines exceeds old 240dp limit");
  ok(GlassPolicy.panelPathEligible(GlassPolicy.INPUT,340,220,340,220,1,0xff303030,true),"Multiline composer custom path");
  ok(GlassPolicy.eligible(GlassPolicy.MENU,288,804,288,804,1,0xff212121,true),"Dark drawer");
  ok(GlassPolicy.eligible(GlassPolicy.MENU,288,804,288,804,1,0xffffffff,true),"Dark drawer shader uses white paint mask");
  ok(fill(GlassPolicy.MENU,288,804,288,804,0xff212121),"Tagged drawer still works before palette theme updates");
  ok(!GlassPolicy.eligible(GlassPolicy.PLAIN_BUTTON,48,48,48,48,1,0xff303030,true),"Plain composer button keeps original paint");
  ok(GlassPolicy.kind("ComposerVoiceButton")==GlassPolicy.PLAIN_BUTTON,"Keep native blue send and voice action");
  ok(GlassPolicy.kind("ComposerDictationButton")==GlassPolicy.PLAIN_BUTTON,"Keep native microphone");
  ok(GlassPolicy.inherit(GlassPolicy.INPUT,GlassPolicy.BUTTON)==GlassPolicy.INPUT,"A broad parent cannot cap resized composer at button height");
  ok(GlassPolicy.inherit(GlassPolicy.PLAIN_BUTTON,GlassPolicy.INPUT)==GlassPolicy.PLAIN_BUTTON,"Nested native action never inherits composer glass");
  ok(GlassPolicy.inherit(GlassPolicy.INPUT,GlassPolicy.EXCLUDE)==GlassPolicy.EXCLUDE,"Preserve scrims and selection handles");
  ok(GlassPolicy.sidebarSurface(288,804,1),"Paint full dark sidebar content");
  ok(!GlassPolicy.sidebarSurface(288,76,1),"Do not mistake SidebarFooter for the full panel");
  ok(fill(GlassPolicy.VOICE,36,36,40,40,0xff4168e9),"Top voice action");
  ok(!fill(GlassPolicy.BUTTON,4,20,40,40,0xffffffff),"Keep voice waveform");
  ok(!fill(GlassPolicy.BUTTON,24,24,24,24,0xff000000),"Keep menu glyph");
  ok(!fill(GlassPolicy.BUTTON,48,48,48,48,0xff000000),"Keep dark icon fill");
  ok(!fill(GlassPolicy.NONE,370,54,370,54,0xffffffff),"Do not restyle messages");
  ok(!fill(GlassPolicy.BUTTON,390,700,390,700,0xffffffff),"Do not restyle whole page");
  ok(!fill(GlassPolicy.INPUT,2,30,300,48,0xff000000),"Keep text cursor");
  ok(GlassPolicy.kind("ConversationTopBarIconButton")==GlassPolicy.BUTTON,"Top tag");
  ok(GlassPolicy.kind("WorkModeToggle")==GlassPolicy.SWITCH,"Mode switch tag");
  ok(GlassPolicy.kind("MultilineComposerLayout")==GlassPolicy.INPUT,"Input tag");
  ok(!GlassPolicy.inject("ComposerAttachButton")&&!GlassPolicy.inject("ComposerDictationButton"),"No extra circle backgrounds on composer buttons");
  ok(!GlassPolicy.inject("ComposerVoiceButton"),"Preserve voice-specific color");
  ok(GlassPolicy.combine(GlassPolicy.SWITCH,GlassPolicy.BUTTON)==GlassPolicy.SWITCH,"Preserve tab selection context");
  ok(GlassPolicy.kind("PopupMenuCard")==GlassPolicy.MENU,"Popup card tag");
  ok(GlassPolicy.kind("ComposerToolsMenuContainer")==GlassPolicy.MENU,"Secondary tools menu");
  ok(GlassPolicy.kind("GeneralSettingsScreen")==GlassPolicy.MENU,"Settings page tag");
  ok(GlassPolicy.kind("ConversationSidebar")==GlassPolicy.MENU,"Sidebar panel");
  ok(GlassPolicy.kind("FlowSidebarMaterialSurface")==GlassPolicy.MENU,"Sidebar shaped background modifier");
  ok(GlassPolicy.kind("ConversationSidebarContent")==GlassPolicy.MENU,"Sidebar content root");
  ok(GlassPolicy.kind("SidebarFloatingNewChatButton")==GlassPolicy.MENU,"Floating new chat action");
  ok(GlassPolicy.kind("SidebarAccountButton")==GlassPolicy.MENU,"Sidebar account action");
  ok(GlassPolicy.kind("SidebarFooter")==GlassPolicy.MENU,"Sidebar footer");
  ok(fill(GlassPolicy.MENU,288,804,288,804,0xffffffff),"Screenshot full height drawer surface");
  ok(GlassPolicy.panelPathEligible(GlassPolicy.MENU,328,48,328,48,1,0xff303030,true),"Settings first and last rounded rows");
  ok(GlassPolicy.panelPathEligible(GlassPolicy.MENU,656,96,656,96,2,0xffffffff,true),"Settings shader mask at scaled density");
  ok(GlassPolicy.panelPathEligible(GlassPolicy.MENU,328,48,328,48,1,0xffffffff,false),"Light settings rounded row");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.MENU,328,1,328,48,1,0xffffffff,true),"Preserve settings separators");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.MENU,24,24,24,24,1,0xffffffff,true),"Preserve settings icon paths");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.MENU,328,48,328,48,1,0x22000000,true),"Preserve translucent row press feedback");
  ok(GlassPolicy.panelPathEligible(GlassPolicy.MENU,288,804,288,804,1,0xffffffff,false),"Shaped full height drawer");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.MENU,32,32,32,32,1,0xffffffff,false),"Preserve white vector icon paths");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.MENU,288,804,288,804,1,0x66000000,false),"Preserve shaped dimming scrim");
  ok(!GlassPolicy.panelPathEligible(GlassPolicy.NONE,288,804,288,804,1,0xffffffff,false),"Never restyle untagged content paths");
  ok(GlassPolicy.kind("FlowModeSelectionIndicator")==GlassPolicy.SELECTED_TAB,"Actual moving selection indicator");
  ok(!GlassPolicy.inject("FlowModeSelectionIndicator")&&!GlassPolicy.inject("FlowModeSelectedTab"),"Never inject a static background on the active tab");
  ok(GlassPolicy.selected(GlassPolicy.SELECTED_TAB,84,40,1,0xffe8e8e8,false),"Selected tab independent of near-white theme color");
  ok(GlassPolicy.combine(GlassPolicy.SELECTED_TAB,GlassPolicy.SWITCH)==GlassPolicy.SELECTED_TAB,"Track cannot erase explicit selected marker");
  ok(GlassPolicy.combine(GlassPolicy.SELECTED_TAB,GlassPolicy.EXCLUDE)==GlassPolicy.EXCLUDE,"Selection marker still preserves scrims and handles");
  ok(fill(GlassPolicy.MENU,300,450,300,450,0xffffffff),"Tall secondary menu");
  ok(fill(GlassPolicy.MENU,380,560,380,560,0xffffffff),"Bottom sheet panel");
  ok(!fill(GlassPolicy.EXCLUDE,380,560,380,560,0xff333333),"Preserve dimming scrim");
  ok(!fill(GlassPolicy.MENU,380,560,380,560,0x66000000),"Preserve unlabelled dimming scrim");
  ok(!fill(GlassPolicy.MENU,280,48,280,48,0x22000000),"Preserve menu hover and press feedback");
  ok(GlassPolicy.kind("BottomSheetScaffoldScrimAlpha")==GlassPolicy.EXCLUDE,"Do not classify scrim as menu");
  ok(!GlassPolicy.selected(GlassPolicy.SWITCH,84,40,1,0xffffffff,false),"White hover fill is not the moving indicator");
  ok(!GlassPolicy.selected(GlassPolicy.BUTTON,84,40,1,0xffffffff,false),"Generic buttons do not acquire a static selected fill");
  ok(!GlassPolicy.selected(GlassPolicy.SWITCH,180,44,1,0xffffffff,false),"Do not highlight entire mode track");
  ok(!GlassPolicy.selected(GlassPolicy.BUTTON,48,48,1,0xffffffff,false),"Do not mark circular button as selected");
  ok(GlassPolicy.selected(GlassPolicy.SELECTED_TAB,84,40,1,0xff555560,true),"Dark mode native animated indicator");
  ok(GlassPolicy.suppressBoundaryFade(GlassPolicy.INPUT,false),"Composer fading shadow removed with keyboard hidden as well as visible");
  ok(GlassPolicy.suppressBoundaryFade(GlassPolicy.NONE,true),"Chat-root boundary shadow remains suppressed while scrolling");
  ok(!GlassPolicy.suppressBoundaryFade(GlassPolicy.NONE,false),"Do not affect unrelated pages");
  ok(!GlassPolicy.suppressBoundaryFade(GlassPolicy.MENU,true),"Keep menu fades and scrims");
  ok(!GlassPolicy.suppressBoundaryFade(GlassPolicy.EXCLUDE,true),"Keep excluded scrims");
  ok(GlassPolicy.settingsTag("YouScreen"),"Settings profile header scope");
  ok(GlassPolicy.settingsTag("SettingsSearchBar"),"Settings bottom search scope");
  ok(GlassPolicy.settingsTag("GeneralSettingsScreen"),"Settings child page scope");
  ok(!GlassPolicy.settingsTag("TopAppBar")&&!GlassPolicy.settingsTag("SidebarContent"),"Generic header and sidebar are not settings");
  ok(!GlassPolicy.settingsTag(null),"Missing scope remains unrelated");
  for(int w=64;w<=140;w++){
   ok(fill(GlassPolicy.SELECTED_TAB,w,36,w,36,0xffffffff),"Animation intermediate width "+w);
   ok(GlassPolicy.selected(GlassPolicy.SELECTED_TAB,w,36,1,0xffffffff,false),"Keep selected material throughout interpolation "+w);
  }
  System.out.println("PASS: screenshot control geometry, tab selection, glyph/cursor exclusion and tag targeting");
 }
}
