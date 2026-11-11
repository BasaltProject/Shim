/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.util.*;
public class TiffExifJpegReadParity { public static void main(String[] a) throws Exception { BufferedImage im=new BufferedImage(40,24,BufferedImage.TYPE_INT_RGB); for(int y=0;y<24;y++)for(int x=0;x<40;x++) im.setRGB(x,y,(x*6)<<16|(y*9&255)<<8|90); ImageWriter w=ImageIO.getImageWritersByFormatName("tiff").next(); ImageWriteParam p=w.getDefaultWriteParam(); p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionType("Exif JPEG"); ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageOutputStream os=ImageIO.createImageOutputStream(bo); w.setOutput(os); w.write(null,new IIOImage(im,null,null),p); os.close(); ImageReader r=ImageIO.getImageReadersByFormatName("tiff").next(); r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bo.toByteArray()))); try{ BufferedImage rd=r.read(0); java.util.zip.CRC32 c=new java.util.zip.CRC32(); for(int y=0;y<rd.getHeight();y++)for(int x=0;x<rd.getWidth();x++) c.update(rd.getRGB(x,y)); System.out.println(rd.getType()+" "+rd.getWidth()+"x"+rd.getHeight()+" "+Long.toHexString(c.getValue())); }catch(Throwable t){ System.out.println("EXC "+t);} } }
