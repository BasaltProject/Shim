/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.*; import java.awt.font.*; import java.util.*;
public class LigatureAdvanceParity { public static void main(String[] a){ String[] txt={"f","ff","fi","fl","ffi","ffl","ffif","of","fo","if","office","f f","AV","To","fa","fb","fj","f.","f,"}; for(String fam: new String[]{"Dialog","Serif","Monospaced"}) for(String t: txt){ StringBuilder sb=new StringBuilder(fam+" '"+t+"'"); for(int lig=0; lig<2; lig++){ Map<TextAttribute,Object> m=new HashMap<>(); m.put(TextAttribute.FAMILY,fam); m.put(TextAttribute.SIZE,24f); if(lig==1) m.put(TextAttribute.LIGATURES,TextAttribute.LIGATURES_ON); Font f=new Font(m); TextLayout tl=new TextLayout(t,f,new FontRenderContext(null,true,true)); sb.append(" "+tl.getAdvance()); } System.out.println(sb);} } }
