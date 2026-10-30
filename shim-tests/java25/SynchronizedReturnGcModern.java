/* Copyright (c) 2026 BasaltMC. Licensed under MIT. */
public class SynchronizedReturnGcModern {
  static final class Marker { final int value; Marker(int value) { this.value=value; } }
  static final class Source {
    synchronized Object[] value(int n) { return new Object[]{new Marker(n), "live"}; }
  }
  public static void main(String[] args) throws Exception {
    Source source=new Source();
    java.util.concurrent.atomic.AtomicReference<Throwable> failure=new java.util.concurrent.atomic.AtomicReference<>();
    Thread[] workers=new Thread[4];
    for(int t=0;t<workers.length;++t) {
      workers[t]=new Thread(()->{
        try {
          for(int i=0;i<20000;++i) {
            Object[] value=source.value(i);
            if(value.length!=2 || !(value[0] instanceof Marker) || ((Marker)value[0]).value!=i || !"live".equals(value[1])) throw new AssertionError("Corrupt synchronized return");
          }
        } catch(Throwable error) { failure.set(error); }
      });
      workers[t].start();
    }
    for(int i=0;i<100;++i) { System.gc();Thread.sleep(1); }
    for(Thread worker:workers)worker.join();
    if(failure.get()!=null)throw new AssertionError(failure.get());
    System.out.println("synchronized reference returns survive collection");
  }
}
