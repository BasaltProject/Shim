import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

public class PathMapperTest {
  private static void expect(boolean ok, String what) {
    if (!ok) {
      throw new AssertionError(what);
    }
    System.out.println("ok " + what);
  }

  public static void main(String[] args) throws Exception {
    Path dir = Files.createTempDirectory("shim-mapper");
    File real = new File(dir.toFile(), "real.txt");
    try (FileOutputStream out = new FileOutputStream(real)) {
      out.write("hi".getBytes());
    }
    String virtual = new File(dir.toFile(), "virtual.txt").getPath();
    int[] writes = new int[1];
    BiFunction<String, Integer, String> mapper = (path, op) -> {
      if (op == 1) {
        writes[0]++;
      }
      if (path.equals(virtual)) {
        return real.getPath();
      }
      if (path.endsWith("denied")) {
        return null;
      }
      return path;
    };
    java.lang.reflect.Method set = Class.forName("shim.FileNative").getMethod("setPathMapper", BiFunction.class);
    set.invoke(null, mapper);

    try (FileInputStream in = new FileInputStream(virtual)) {
      expect(new String(in.readAllBytes()).equals("hi"), "read through the mapper");
    }
    expect(new File(virtual).exists(), "exists through the mapper");
    expect(new File(virtual).length() == 2, "length through the mapper");
    expect(Files.exists(Path.of(virtual)), "nio exists through the mapper");
    expect(!new File(dir.toFile(), "denied").exists(), "denied path looks absent");
    boolean refused = false;
    try {
      new FileOutputStream(new File(dir.toFile(), "denied")).close();
    } catch (java.io.IOException expected) {
      refused = true;
    }
    expect(refused, "denied write throws");
    expect(!new File(dir.toFile(), "denied").mkdir(), "denied mkdir fails");
    int before = writes[0];
    try (FileOutputStream out = new FileOutputStream(virtual)) {
      out.write("yo".getBytes());
    }
    expect(writes[0] > before && Files.readString(real.toPath()).equals("yo"), "write redirected and flagged");
    set.invoke(null, new Object[] {null});
    expect(!new File(virtual).exists(), "mapper removed");
    System.out.println("PathMapperTest done");
  }
}
