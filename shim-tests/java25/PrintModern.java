import java.awt.*;import java.awt.geom.*;import java.awt.print.*;import javax.print.*;import javax.print.attribute.*;import javax.print.attribute.standard.*;import javax.print.event.*;import java.io.*;import java.util.*;
public class PrintModern{
 public static void main(String[] a)throws Exception{ PsA.run(); PsB.run(); }

static class PsA{
 static void run()throws Exception{
  StreamPrintServiceFactory[] f=StreamPrintServiceFactory.lookupStreamPrintServiceFactories(DocFlavor.SERVICE_FORMATTED.PRINTABLE,"application/postscript");
  System.out.println(f[0].getOutputFormat());
  for(DocFlavor d:f[0].getSupportedDocFlavors())System.out.println("F "+d);
  StreamPrintService s=f[0].getPrintService(new ByteArrayOutputStream());
  System.out.println(s.getName()+" | "+s.getName()+" | "+s.getOutputFormat()+" | "+s.isDisposed());
  for(Attribute at:s.getAttributes().toArray())System.out.println("A "+at.getClass().getName()+" "+at);
  for(Class<?> c:s.getSupportedAttributeCategories())System.out.println("C "+c.getName()+" def="+s.getDefaultAttributeValue((Class<? extends Attribute>)c)+" sup="+arr(s.getSupportedAttributeValues((Class<? extends Attribute>)c,null,null)));
  for(DocFlavor d:s.getSupportedDocFlavors())System.out.println("SF "+d);
  System.out.println(s.isAttributeCategorySupported(Copies.class)+" "+s.isAttributeCategorySupported(Chromaticity.class)+" "+s.getServiceUIFactory());
  System.out.println(s.isAttributeValueSupported(new Copies(3),DocFlavor.SERVICE_FORMATTED.PRINTABLE,null)+" "+s.isAttributeValueSupported(MediaSizeName.ISO_A4,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null)+" "+s.isAttributeValueSupported(Chromaticity.COLOR,null,null));
  System.out.println(s.getUnsupportedAttributes(DocFlavor.SERVICE_FORMATTED.PRINTABLE,new HashAttributeSet(new Copies(2))));
  System.out.println(s.getAttribute(PrinterName.class)+" "+s.getAttribute(ColorSupported.class));
  DocPrintJob j=s.createPrintJob();
  System.out.println(j.getPrintService().getName()+" "+j.getAttributes().size());
  s.dispose();System.out.println(s.isDisposed());
 }
 static String arr(Object o){ if(o==null)return "null"; if(o instanceof Object[]){return Arrays.toString((Object[])o).replace("\n"," ");} if(o.getClass().isArray()) return "arr"; return o.toString();}
}

static class PsB{
 static void t(String n,java.util.concurrent.Callable<Object> c){try{System.out.println(n+": "+c.call());}catch(Throwable e){System.out.println(n+": EX "+e.getClass().getName()+": "+e.getMessage()+(e.getCause()!=null?" cause="+e.getCause():""));}}
 static StreamPrintService svc(OutputStream o){return StreamPrintServiceFactory.lookupStreamPrintServiceFactories(DocFlavor.SERVICE_FORMATTED.PRINTABLE,"application/postscript")[0].getPrintService(o);}
 static void run()throws Exception{
  Printable one=(g,pf,i)->{ if(i>0)return Printable.NO_SUCH_PAGE; g.drawRect(1,1,10,10); return Printable.PAGE_EXISTS;};
  t("events",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();StringBuilder sb=new StringBuilder();
    j.addPrintJobListener(new PrintJobListener(){
      public void printDataTransferCompleted(PrintJobEvent e){sb.append("DTC ");}
      public void printJobCompleted(PrintJobEvent e){sb.append("COMPLETE ");}
      public void printJobFailed(PrintJobEvent e){sb.append("FAILED ");}
      public void printJobCanceled(PrintJobEvent e){sb.append("CANCELED ");}
      public void printJobNoMoreEvents(PrintJobEvent e){sb.append("NOMORE ");}
      public void printJobRequiresAttention(PrintJobEvent e){sb.append("ATTN ");}});
    j.print(new SimpleDoc(one,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);return sb;});
  
  t("badflavor",()->{svc(new ByteArrayOutputStream()).createPrintJob().print(new SimpleDoc("x",DocFlavor.STRING.TEXT_PLAIN,null),null);return "ok";});
  t("throwing",()->{svc(new ByteArrayOutputStream()).createPrintJob().print(new SimpleDoc((Printable)(g,pf,i)->{throw new RuntimeException("boom");},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);return "ok";});
  t("throwing2",()->{svc(new ByteArrayOutputStream()).createPrintJob().print(new SimpleDoc((Printable)(g,pf,i)->{throw new PrinterException("pe");},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);return "ok";});
  t("twice",()->{DocPrintJob j=svc(new ByteArrayOutputStream()).createPrintJob();Doc d=new SimpleDoc(one,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null);j.print(d,null);j.print(d,null);return "ok";});
  t("pf",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();final PageFormat[] seen=new PageFormat[1];
    j.print(new SimpleDoc((Printable)(g,pf,i)->{seen[0]=pf;if(i>0)return 1; return 0;},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);
    PageFormat pf=seen[0];return pf.getWidth()+" "+pf.getHeight()+" "+pf.getImageableX()+" "+pf.getImageableY()+" "+pf.getImageableWidth()+" "+pf.getImageableHeight()+" "+pf.getOrientation();});
  t("pfL",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();final PageFormat[] seen=new PageFormat[1];final AffineTransform[] tr=new AffineTransform[1];
    HashPrintRequestAttributeSet s=new HashPrintRequestAttributeSet();s.add(OrientationRequested.LANDSCAPE);s.add(MediaSizeName.NA_LETTER);
    j.print(new SimpleDoc((Printable)(g,pf,i)->{seen[0]=pf;tr[0]=((Graphics2D)g).getTransform();tr[0].setTransform(tr[0].getScaleX()+0.0,tr[0].getShearY()+0.0,tr[0].getShearX()+0.0,tr[0].getScaleY()+0.0,tr[0].getTranslateX()+0.0,tr[0].getTranslateY()+0.0);if(i>0)return 1; return 0;},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),s);
    PageFormat pf=seen[0];return pf.getWidth()+" "+pf.getHeight()+" "+pf.getImageableX()+" "+pf.getImageableY()+" "+pf.getImageableWidth()+" "+pf.getImageableHeight()+" "+pf.getOrientation()+" "+tr[0];});
  t("gfx",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();StringBuilder sb=new StringBuilder();
    j.print(new SimpleDoc((Printable)(g,pf,i)->{sb.append(g.getClip()+" "+((Graphics2D)g).getTransform().getScaleX()+" "+g.getFont()+" "+g.getColor()+" "+(g instanceof PrinterGraphics)+" "+((Graphics2D)g).getRenderingHint(RenderingHints.KEY_ANTIALIASING));if(i>0)return 1; return 0;},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);return sb;});
  t("pagesX",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();final int[] n={0};
    j.print(new SimpleDoc((Printable)(g,pf,i)->{n[0]++; return i>=3?1:0;},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),null);
    String ps=o.toString("ISO-8859-1");return n[0]+" pages in output: "+(ps.split("showpage",-1).length-1)+" first="+ps.substring(0,14)+" "+ps.contains("%%EOF")+" "+ps.contains("%%BoundingBox")+" "+ps.contains("%%Page:");});
  t("pagerange",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();final StringBuilder sb=new StringBuilder();
    HashPrintRequestAttributeSet s=new HashPrintRequestAttributeSet();s.add(new PageRanges(2,3));
    j.print(new SimpleDoc((Printable)(g,pf,i)->{sb.append(i+" "); return i>=4?1:0;},DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),s);return sb;});
  t("pageable",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();Book b=new Book();final StringBuilder sb=new StringBuilder();
    b.append((g,pf,i)->{sb.append("p"+i+" "+pf.getOrientation()+" ");return 0;},new PageFormat(),2);
    PageFormat l=new PageFormat();l.setOrientation(PageFormat.LANDSCAPE);b.append((g,pf,i)->{sb.append("q"+i+" "+pf.getOrientation()+" ");return 0;},l);
    j.print(new SimpleDoc(b,DocFlavor.SERVICE_FORMATTED.PAGEABLE,null),null);return sb;});
  t("jobPrinterJob",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();PrinterJob pj=PrinterJob.getPrinterJob();pj.setPrintService(svc(o));pj.setPrintable(one);pj.print();return pj.getPrintService().getName()+" "+(o.size()>0)+" "+"x";});
  t("jobSetNull",()->{PrinterJob pj=PrinterJob.getPrinterJob();pj.setPrintService(null);return "ok";});
  t("img",()->{ByteArrayOutputStream png=new ByteArrayOutputStream();java.awt.image.BufferedImage bi=new java.awt.image.BufferedImage(20,10,1);javax.imageio.ImageIO.write(bi,"png",png);
    ByteArrayOutputStream o=new ByteArrayOutputStream();svc(o).createPrintJob().print(new SimpleDoc(png.toByteArray(),DocFlavor.BYTE_ARRAY.PNG,null),null);return o.size()>0;});
  t("copies",()->{ByteArrayOutputStream o=new ByteArrayOutputStream();DocPrintJob j=svc(o).createPrintJob();HashPrintRequestAttributeSet s=new HashPrintRequestAttributeSet();s.add(new Copies(3));
    j.print(new SimpleDoc(one,DocFlavor.SERVICE_FORMATTED.PRINTABLE,null),s);String ps=o.toString("ISO-8859-1");return ps.split("showpage",-1).length-1;});
 }}
}
