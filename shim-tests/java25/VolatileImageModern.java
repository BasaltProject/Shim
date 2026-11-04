import java.awt.*;
import java.awt.image.*;

public class VolatileImageModern{public static void main(String[] a)throws Exception{
 GraphicsConfiguration gc=new BufferedImage(4,4,1).createGraphics().getDeviceConfiguration();
 VolatileImage v=gc.createCompatibleVolatileImage(8,6,Transparency.TRANSLUCENT);
 System.out.println(v.getWidth()+" "+v.getHeight()+" "+v.getTransparency()+" "+v.validate(gc)+" "+v.contentsLost()+" "+v.getCapabilities().isAccelerated()+" "+v.getCapabilities().isTrueVolatile());
 Graphics2D g=v.createGraphics();g.setColor(Color.RED);g.fillRect(0,0,4,3);g.dispose();
 BufferedImage s=v.getSnapshot();System.out.println(Integer.toHexString(s.getRGB(1,1))+" "+Integer.toHexString(s.getRGB(6,5))+" "+s.getType()+" "+v.getWidth(null)+" "+v.getProperty("x",null).getClass().getSimpleName());
 System.out.println(v.getGraphics().getClass().getSimpleName().length()>0);
 System.out.println(v.getSource()!=null);
 VolatileImage o=gc.createCompatibleVolatileImage(3,3);System.out.println(o.getTransparency()+" "+o.getSnapshot().getType());
 VolatileImage b=gc.createCompatibleVolatileImage(3,3,Transparency.BITMASK);System.out.println(b.getTransparency()+" "+b.getSnapshot().getType()+" "+gc.createCompatibleImage(5,5,Transparency.BITMASK).getType());
}}
