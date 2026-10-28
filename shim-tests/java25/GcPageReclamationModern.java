/* Copyright (c) 2026 BasaltMC. Licensed under MIT. */
public class GcPageReclamationModern {
  public static void main(String[] args) {
    byte[][][] groups = new byte[48][700][];
    for (int g = 0; g < groups.length; ++g) {
      for (int i = 0; i < groups[g].length; ++i) {
        byte[] value = new byte[256];
        for (int j = 0; j < value.length; ++j) value[j] = (byte) (g + j);
        groups[g][i] = value;
      }
    }
    // Promote the arrays before making large holes ending beside live objects.
    for (int i = 0; i < 4; ++i) System.gc();
    byte[][] retained = new byte[48][];
    for (int g = 0; g < retained.length; ++g) retained[g] = groups[g][699];
    groups = null;
    for (int round = 0; round < 12; ++round) {
      System.gc();
      byte[][] pressure = new byte[2000][];
      for (int i = 0; i < pressure.length; ++i) pressure[i] = new byte[1024];
      for (int g = 0; g < retained.length; ++g) {
        if (retained[g].length != 256) throw new AssertionError("Reclaimed live header");
        for (int j = 0; j < retained[g].length; ++j)
          if (retained[g][j] != (byte) (g + j)) throw new AssertionError("Reclaimed live data");
      }
    }
    System.out.println("page reclamation preserves live neighbours");
  }
}
