import javax.imageio.*; import javax.imageio.metadata.*; import java.awt.image.*; import java.io.*; import org.w3c.dom.*;
public class ImageMetadataTreesModern {
 static void dump(Node n, String ind, StringBuilder sb){ sb.append(ind).append(n.getNodeName()); NamedNodeMap m=n.getAttributes(); if(m!=null){ for(int i=0;i<m.getLength();i++) sb.append(' ').append(m.item(i).getNodeName()).append('=').append(m.item(i).getNodeValue()); } sb.append('\n'); for(Node c=n.getFirstChild();c!=null;c=c.getNextSibling()) dump(c,ind+" ",sb); }
 public static void main(String[] a) throws Exception { System.setProperty("java.awt.headless","true");
  for (String fmt : new String[]{"bmp","gif","jpeg","png"}) {
   BufferedImage im=new BufferedImage(5,4,fmt.equals("gif")?BufferedImage.TYPE_BYTE_INDEXED:BufferedImage.TYPE_INT_RGB);
   ByteArrayOutputStream o=new ByteArrayOutputStream(); ImageIO.write(im,fmt,o);
   ImageReader r=ImageIO.getImageReadersByFormatName(fmt).next(); r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(o.toByteArray())));
   IIOMetadata md=r.getImageMetadata(0); System.out.println("== "+fmt+" "+(md==null?null:md.getNativeMetadataFormatName())+" "+(md==null?null:java.util.Arrays.toString(md.getMetadataFormatNames())));
   if(md!=null){ StringBuilder sb=new StringBuilder(); try{ dump(md.getAsTree(md.getNativeMetadataFormatName()),"",sb);}catch(Throwable t){sb.append("ERR "+t);} System.out.print(sb);
     StringBuilder s2=new StringBuilder(); try{ dump(md.getAsTree("javax_imageio_1.0"),"",s2);}catch(Throwable t){s2.append("ERR "+t);} System.out.print(s2); }
   IIOMetadata sm=r.getStreamMetadata(); System.out.println("stream "+(sm==null?null:sm.getNativeMetadataFormatName()));
  }}}
