// An int constant or call result must keep its sign when widened to a long.
public class Widening {
  static int f;
  static int neg() { return -1; }
  static int big() { return 0x80000000; }
  static long id(long v) { return v; }
  static void check(String name, boolean ok) {
    if (!ok) {
      throw new RuntimeException(name);
    }
  }

  public static void main(String[] args) {
    check("i2l call", ((long) neg()) == -1);
    check("i2l call big", ((long) big()) == -2147483648L);
    int x = neg();
    check("i2l local", ((long) x) == -1L);
    f = -5;
    check("i2l field", ((long) f) == -5L);
    check("i2l arg", id((long) neg()) == -1L);
    long l = neg();
    check("assign", l == -1L);
    check("cmp neg", ((long) neg()) < 0L);
    check("cmp eq const", ((long) neg()) == -1L);
    check("add", ((long) neg()) + 1L == 0L);
    long m = 25214903884L;
    check("mul", m * 3 == 75644711652L);
    check("shift", (1L << 40) == 1099511627776L);
    check("ushr", (-1L >>> 32) == 4294967295L);
    check("l2i", (int) 0x1FFFFFFFFL == -1);
    int b = 200;
    check("i2l shift", ((long) b << 56) == 0xC800000000000000L);
    check("or", ((long) 1 << 56 | (long) 2 << 48) == 0x0102000000000000L);
    check("byte", ((long) (byte) -3) == -3L);
    check("neg", -((long) 5) == -5L);
  }
}
