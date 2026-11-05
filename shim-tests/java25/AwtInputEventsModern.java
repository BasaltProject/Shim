import java.awt.*;import java.awt.event.*;import javax.swing.*;
public class AwtInputEventsModern{public static void main(String[] a)throws Exception{
  java.util.Locale.setDefault(java.util.Locale.ENGLISH);System.setProperty("java.awt.headless","true");
  EvK.main(a);
 JPanel c=new JPanel();
 int[] ids={MouseEvent.MOUSE_PRESSED,MouseEvent.MOUSE_RELEASED,MouseEvent.MOUSE_CLICKED,MouseEvent.MOUSE_MOVED,MouseEvent.MOUSE_DRAGGED,MouseEvent.MOUSE_ENTERED};
 int[] mods={0,1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,1|16,64|1024,64|16,8|64,4|2048, 128|512|4096};
 for(int id:ids)for(int m:mods)for(int b=0;b<=3;b++){
   try{MouseEvent e=new MouseEvent(c,id,7L,m,1,2,1,false,b);
    System.out.println(id+" "+m+" "+b+": "+e.getModifiers()+" "+e.getModifiersEx()+" "+e.getButton()+" "+e.isShiftDown()+e.isControlDown()+e.isMetaDown()+e.isAltDown()+e.isAltGraphDown()+" "+e.paramString());
   }catch(Throwable t){System.out.println(id+" "+m+" "+b+" "+t);}
 }
 int[] kids={KeyEvent.KEY_PRESSED,KeyEvent.KEY_TYPED,KeyEvent.KEY_RELEASED};
 for(int id:kids)for(int m:mods){
   try{KeyEvent e=new KeyEvent(c,id,7L,m,id==KeyEvent.KEY_TYPED?0:65,id==KeyEvent.KEY_TYPED?'a':'A');
    System.out.println("K"+id+" "+m+": "+e.getModifiers()+" "+e.getModifiersEx()+" "+e.isShiftDown()+e.isControlDown()+e.isMetaDown()+e.isAltDown()+e.isAltGraphDown()+" "+e.paramString());
   }catch(Throwable t){System.out.println("K"+id+" "+m+" "+t);}
 }
 for(int m:mods){ try{ MouseWheelEvent w=new MouseWheelEvent(c,MouseEvent.MOUSE_WHEEL,7L,m,1,2,5,6,3,false,MouseWheelEvent.WHEEL_BLOCK_SCROLL,2,-3,0.5);
   System.out.println("W"+m+" "+w.getModifiers()+" "+w.getModifiersEx()+" "+w.paramString()+" "+w.getUnitsToScroll());}catch(Throwable t){System.out.println("W "+t);} }
}

static class EvK{
 static void t(java.util.concurrent.Callable<Object> c){try{System.out.println(c.call());}catch(Throwable e){System.out.println("EX "+e.getClass().getName()+": "+e.getMessage());}}
 public static void main(String[] a)throws Exception{
 JPanel c=new JPanel();
 t(()->new KeyEvent(c,KeyEvent.KEY_PRESSED,7L,0,65).paramString());
 t(()->new KeyEvent(c,KeyEvent.KEY_PRESSED,7L,0,KeyEvent.VK_ENTER).paramString());
 t(()->new KeyEvent(c,KeyEvent.KEY_TYPED,7L,0,65,'a').paramString());
 t(()->new KeyEvent(c,KeyEvent.KEY_TYPED,7L,0,0,KeyEvent.CHAR_UNDEFINED).paramString());
 t(()->new KeyEvent(c,KeyEvent.KEY_PRESSED,7L,0,0,KeyEvent.CHAR_UNDEFINED,5).paramString());
 t(()->new KeyEvent(c,KeyEvent.KEY_PRESSED,7L,0,10,'\n',KeyEvent.KEY_LOCATION_NUMPAD).paramString());
 for(char ch:new char[]{'\b','\t','\n',0x18,0x1b,0x7f,0xffff,'x'}) t(()->new KeyEvent(c,KeyEvent.KEY_PRESSED,7L,0,KeyEvent.VK_UNDEFINED,ch).paramString());
 t(()->KeyEvent.getKeyText(0x01000041)+"|"+KeyEvent.getKeyText(0x1000000+0x1f600)+"|"+KeyEvent.getKeyText(-1)+"|"+KeyEvent.getKeyText(0x7f));
 t(()->new MouseEvent(c,MouseEvent.MOUSE_PRESSED,7L,0,1,2,1,false,-1));
 t(()->new MouseEvent(c,MouseEvent.MOUSE_PRESSED,7L,0,1,2,1,false,4).paramString());
 t(()->new MouseEvent(c,MouseEvent.MOUSE_PRESSED,7L,0,1,2,1,true,4).isPopupTrigger());
 t(()->{MouseEvent m=new MouseEvent(c,MouseEvent.MOUSE_MOVED,7L,0,1,2,1,false);m.translatePoint(3,4);return m.getPoint()+" "+m.getLocationOnScreen()+" "+m.paramString();});
 t(()->MouseEvent.getMouseModifiersText(InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_MASK));
 t(()->InputEvent.getModifiersExText(0x3fff));
 t(()->{KeyEvent k=new KeyEvent(c,KeyEvent.KEY_PRESSED,9L,InputEvent.CTRL_MASK,KeyEvent.VK_A,'a');k.setModifiers(InputEvent.SHIFT_MASK);return k.getModifiers()+" "+k.getModifiersEx()+" "+k.isShiftDown()+k.isControlDown()+" "+k.getWhen()+" "+k.isActionKey()+" "+k.getKeyLocation();});
 t(()->{KeyEvent k=new KeyEvent(c,KeyEvent.KEY_PRESSED,9L,InputEvent.CTRL_DOWN_MASK,KeyEvent.VK_A,'a');k.setKeyChar('b');k.setKeyCode(KeyEvent.VK_B);return k.paramString()+" "+k.getExtendedKeyCode();});
}}

}
