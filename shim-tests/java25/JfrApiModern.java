/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import jdk.jfr.*;

public class JfrApiModern {
  @Name("test.Sample")
  @Label("Sample Event")
  @Description("An event for tests")
  @Category({"Test", "Sample"})
  @StackTrace(false)
  @Threshold("10 ms")
  static class Sample extends Event {
    @Label("Message")
    String message;
    @Timespan
    long span;
    @DataAmount
    long bytes;
    @Percentage
    float ratio;
  }

  @Enabled(false)
  static class Off extends Event { }

  public static void main(String[] args) throws Exception {
    Sample s = new Sample();
    s.begin();
    s.message = "m";
    s.end();
    s.commit();
    System.out.println("commit ok " + s.shouldCommit() + " " + s.isEnabled());
    Off off = new Off();
    off.commit();
    System.out.println("off " + off.isEnabled());
    Name n = Sample.class.getAnnotation(Name.class);
    Label l = Sample.class.getAnnotation(Label.class);
    Description d = Sample.class.getAnnotation(Description.class);
    Category c = Sample.class.getAnnotation(Category.class);
    StackTrace st = Sample.class.getAnnotation(StackTrace.class);
    Threshold th = Sample.class.getAnnotation(Threshold.class);
    System.out.println(n.value() + "|" + l.value() + "|" + d.value() + "|" + String.join("/", c.value()) + "|" + st.value() + "|" + th.value());
    System.out.println(Sample.class.getDeclaredField("span").getAnnotation(Timespan.class).value());
    System.out.println(Sample.class.getDeclaredField("bytes").getAnnotation(DataAmount.class).value());
    System.out.println(Sample.class.getDeclaredField("ratio").isAnnotationPresent(Percentage.class));
    System.out.println(Off.class.getAnnotation(Enabled.class).value());
    System.out.println(Event.class.getSuperclass().getName().isEmpty());
    System.out.println(java.util.Arrays.toString(RecordingState.values()));
    System.out.println(Timespan.NANOSECONDS + " " + Timespan.MILLISECONDS + " " + Timespan.SECONDS);
    System.out.println(Timestamp.MILLISECONDS_SINCE_EPOCH + " " + DataAmount.BYTES + " " + DataAmount.BITS);
    System.out.println(Threshold.class.isAnnotationPresent(MetadataDefinition.class) + " " + Name.class.isAnnotation());
    System.out.println(java.lang.annotation.Retention.class.cast(Name.class.getAnnotation(java.lang.annotation.Retention.class)).value());
    System.out.println(java.util.Arrays.toString(Name.class.getAnnotation(java.lang.annotation.Target.class).value()));
    System.out.println(Enabled.NAME + " " + Threshold.NAME + " " + Period.NAME + " " + StackTrace.NAME);
    System.out.println(Period.class.getDeclaredMethod("value").getDefaultValue());
    System.out.println(Enabled.class.getDeclaredMethod("value").getDefaultValue());
  }
}
