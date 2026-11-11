/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.metadata.*; import javax.imageio.plugins.jpeg.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.util.*; import org.w3c.dom.*;
public class JpegTablesParity { static void walk(Node n){ if(n instanceof IIOMetadataNode in){ Object u=in.getUserObject(); if(u instanceof JPEGQTable q) System.out.println(n.getParentNode().getNodeName()+" Q "+Arrays.toString(q.getTable())); if(u instanceof JPEGHuffmanTable h) System.out.println("H "+Arrays.toString(h.getLengths())+" "+Arrays.toString(h.getValues())); } for(Node c=n.getFirstChild();c!=null;c=c.getNextSibling()) walk(c); }
 public static void main(String[] a) throws Exception { ImageWriter w=ImageIO.getImageWritersByFormatName("jpeg").next(); ImageWriteParam p=w.getDefaultWriteParam(); IIOMetadata md=w.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),p); System.out.println("DEFAULT"); walk(md.getAsTree(md.getNativeMetadataFormatName())); p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionQuality(0.3f); md=w.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),p); System.out.println("Q0.3"); walk(md.getAsTree(md.getNativeMetadataFormatName())); BufferedImage im=new BufferedImage(16,16,1); ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageIO.write(im,"jpeg",bo); ImageReader r=ImageIO.getImageReadersByFormatName("jpeg").next(); r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bo.toByteArray()))); System.out.println("READ"); walk(r.getImageMetadata(0).getAsTree("javax_imageio_jpeg_image_1.0")); } }
