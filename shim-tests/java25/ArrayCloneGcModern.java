/* Copyright (c) 2026 BasaltMC. Licensed under MIT. */
public class ArrayCloneGcModern {
  static final class Marker { final int value; Marker(int value) { this.value=value; } }
  public static void main(String[] args) {
    for (int round=0;round<24;++round) {
      Object[] source={new Marker(round), "retained"};
      int sourceHash=System.identityHashCode(source);
      int[] primitive={round,17,31};
      int primitiveHash=System.identityHashCode(primitive);
      System.gc();
      Object[][] copies=new Object[32][];
      int[][] values=new int[32][];
      int[] hashes=new int[32];
      for(int i=0;i<copies.length;++i) {
        copies[i]=source.clone(); values[i]=primitive.clone();
        hashes[i]=System.identityHashCode(copies[i]);
      }
      System.gc();
      if(sourceHash!=System.identityHashCode(source)||primitiveHash!=System.identityHashCode(primitive)) throw new AssertionError("Source identity changed");
      for(int i=0;i<copies.length;++i) {
        if(copies[i]==source || copies[i][0]!=source[0] || ((Marker)copies[i][0]).value!=round) throw new AssertionError("Reference clone corrupted");
        if(values[i]==primitive || values[i][0]!=round || values[i][2]!=31) throw new AssertionError("Primitive clone corrupted");
        if(hashes[i]!=System.identityHashCode(copies[i])) throw new AssertionError("Clone identity changed");
      }
    }
    System.out.println("hashed array clones survive collection");
  }
}
