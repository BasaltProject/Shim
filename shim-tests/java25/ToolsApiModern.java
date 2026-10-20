/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.tools.*; import java.net.URI; import java.util.*;
public class ToolsApiModern { public static void main(String[] a) throws Exception {
 SimpleJavaFileObject f = new SimpleJavaFileObject(URI.create("string:///a/B.java"), JavaFileObject.Kind.SOURCE){ public CharSequence getCharContent(boolean b){return "class B{}";}};
 System.out.println(f.getName()+" "+f.getKind()+" "+f.isNameCompatible("B", JavaFileObject.Kind.SOURCE)+" "+f.isNameCompatible("C", JavaFileObject.Kind.SOURCE)+" "+f.getCharContent(true)+" "+f.getLastModified()+" "+f.delete());
 System.out.println(new Scanner(f.openReader(true)).nextLine());
 System.out.println(StandardLocation.CLASS_OUTPUT.isOutputLocation()+" "+StandardLocation.locationFor("CLASS_PATH")+" "+StandardLocation.locationFor("X_OUTPUT").isOutputLocation()+" "+StandardLocation.MODULE_PATH.isModuleOrientedLocation());
 System.out.println(javax.lang.model.SourceVersion.latest()+" "+javax.lang.model.element.Modifier.NON_SEALED+" "+javax.lang.model.element.Modifier.PUBLIC+" "+Arrays.toString(JavaFileObject.Kind.values())+" "+JavaFileObject.Kind.CLASS.extension);
 DiagnosticCollector<JavaFileObject> dc=new DiagnosticCollector<>(); System.out.println(dc.getDiagnostics());
 try{ new SimpleJavaFileObject(URI.create("mailto:x"), JavaFileObject.Kind.SOURCE){}; }catch(Exception e){System.out.println(e);}
 try{ f.openInputStream(); }catch(Exception e){System.out.println(e);}
 System.out.println(f.toString().replaceAll("ToolsApiModern\\$\\d+","ToolsApiModern\\$N"));
}}
