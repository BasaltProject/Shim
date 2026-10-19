/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarCacheLifecycleModern {
  public static final class Provider implements Runnable {
    public Provider() { }
    public void run() { }
  }
  static int checks;
  static void expect(boolean value) {if(!value)throw new AssertionError("JAR lifecycle check "+checks);++checks;}
  static JarURLConnection connection(URL url,boolean cache) throws Exception {
    JarURLConnection c=(JarURLConnection)url.openConnection();c.setUseCaches(cache);return c;
  }
  static void closed(JarFile file) {
    try {file.getJarEntry("payload.txt");throw new AssertionError("Closed JAR still usable");}
    catch(IllegalStateException expected){++checks;}
  }
  static void write(JarOutputStream out,String name,byte[] bytes) throws Exception {
    out.putNextEntry(new JarEntry(name));out.write(bytes);out.closeEntry();
  }
  static byte[] classBytes(String name) throws Exception {
    try(InputStream in=JarCacheLifecycleModern.class.getResourceAsStream(name+".class")){return in.readAllBytes();}
  }
  public static void main(String[] args) throws Exception {
    Path dir=Files.createTempDirectory("jar lifecycle + ");Path jar=dir.resolve("services + test.jar");
    try {
      try(JarOutputStream out=new JarOutputStream(Files.newOutputStream(jar))) {
        write(out,"payload.txt","payload".getBytes("UTF-8"));
        write(out,"META-INF/services/java.lang.Runnable","JarCacheLifecycleModern$Provider\n".getBytes("UTF-8"));
        write(out,"JarCacheLifecycleModern$Provider.class",classBytes("JarCacheLifecycleModern$Provider"));
        write(out,"JarCacheLifecycleModern.class",classBytes("JarCacheLifecycleModern"));
      }
      URL url=new URL("jar:"+jar.toUri().toURL()+"!/payload.txt");
      JarFile first=connection(url,true).getJarFile();
      expect(first==connection(url,true).getJarFile());
      first.close();closed(first);
      // A new connection must not inherit a closed shared file.
      JarFile second=connection(url,true).getJarFile();expect(second!=first);
      try(InputStream in=url.openStream()){expect(new String(in.readAllBytes(),"UTF-8").equals("payload"));}
      expect(second==connection(url,true).getJarFile());
      first.close();expect(second==connection(url,true).getJarFile());
      // Uncached streams own their JAR, cached streams do not.
      JarURLConnection uncached=connection(url,false);JarFile own=uncached.getJarFile();expect(own!=second);
      InputStream stream=uncached.getInputStream();expect(stream.read()=='p');stream.close();stream.close();closed(own);
      expect(second.getJarEntry("payload.txt")!=null);
      try(InputStream in=connection(url,true).getInputStream()){expect(in.read()=='p');}
      expect(second.getJarEntry("payload.txt")!=null);
      // Competing cache misses must yield live files and leave a usable cache.
      // The JDK may return distinct live objects to simultaneous first opens.
      second.close();
      java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
      java.util.concurrent.atomic.AtomicReference<Throwable> failed=new java.util.concurrent.atomic.AtomicReference<>();
      JarFile[] files=new JarFile[8];Thread[] threads=new Thread[files.length];
      for(int i=0;i<threads.length;++i) {
        final int index=i;
        threads[i]=new Thread(() -> {
          try {start.await();files[index]=connection(url,true).getJarFile();if(files[index].getJarEntry("payload.txt")==null)throw new AssertionError();}
          catch(Throwable e){failed.compareAndSet(null,e);}
        });threads[i].start();
      }
      start.countDown();for(Thread thread:threads)thread.join();
      if(failed.get()!=null)throw new AssertionError("Concurrent cache open",failed.get());
      for(JarFile file:files)expect(file.getJarEntry("payload.txt")!=null);
      expect(connection(url,true).getJarFile().getJarEntry("payload.txt")!=null);
      for(JarFile file:files)file.close();
      // Reproduce late service discovery after another component closes the
      // JarFile exposed by a resource connection; providers stay isolated.
      try(URLClassLoader loader=new URLClassLoader(new URL[]{jar.toUri().toURL()},null)) {
        URL service=loader.getResource("META-INF/services/java.lang.Runnable");expect(service!=null);
        for(int i=0;i<12;++i) {
          JarFile exposed=((JarURLConnection)service.openConnection()).getJarFile();exposed.close();
          int providers=0;
          for(Runnable provider:ServiceLoader.load(Runnable.class,loader)) {
            expect(provider.getClass().getClassLoader()==loader);provider.run();++providers;
          }
          expect(providers==1);
          try(InputStream in=loader.getResourceAsStream("payload.txt")){expect(in!=null && in.read()=='p');}
        }
      }
      JarFile current=connection(url,true).getJarFile();current.close();closed(current);
      System.out.println("JAR lifecycle checks="+checks);
    } finally {Files.deleteIfExists(jar);Files.deleteIfExists(dir);}
  }
}
