import app.chatgpt.flow.Palette;
class PaletteTest {
 static double lum(int c) { double sum=0; double[] weights={.2126,.7152,.0722};for(int i=0;i<3;i++){double x=((c>>(16-i*8))&255)/255.;sum+=weights[i]*(x<=.04045?x/12.92:Math.pow((x+.055)/1.055,2.4));}return sum; }
 public static void main(String[] args){
  int tested=0;
  for(int base:new int[]{0xff000000,0xff171717,0xff212121,0xfff9f9f9,0xffffffff}){
   for(int intensity=0;intensity<=100;intensity++){
    int[] colors=Palette.colors(base,intensity);
    if(colors.length!=8||colors[0]!=colors[7])throw new AssertionError("Rainbow seam");
    for(int i=0;i<colors.length-1;i++)for(int step=0;step<=10;step++){
     int c=Palette.blend(colors[i],colors[i+1],step/10f);
     double l=lum(c),contrast=Palette.isDark(base)?1.05/(l+.05):(l+.05)/.05;
     if(contrast<4.5)throw new AssertionError("Contrast: "+contrast);
     if((c>>>24)!=255)throw new AssertionError("Opacity");tested++;
    }
   }
  }
  for(int speed=1;speed<=100;speed++){
   long duration=Palette.durationMs(speed);
   if(Palette.phase(1000,1000,speed)!=0||Palette.phase(1000+duration,1000,speed)!=0)throw new AssertionError("Loop");
   if(speed>1&&duration>=Palette.durationMs(speed-1))throw new AssertionError("Speed");
  }
  System.out.println("PASS: "+tested+" palette samples, dark/light text contrast, seamless loop and 100 speed settings");
 }
}
