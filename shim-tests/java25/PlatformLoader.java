/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 9
 */

public class PlatformLoader {
  static void probe(String label, java.util.concurrent.Callable<Object> action) {
    try {
      System.out.println(label + ":" + action.call());
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  public static void main(String[] args) throws Exception {
    ClassLoader platform = ClassLoader.getPlatformClassLoader();
    ClassLoader app = ClassLoader.getSystemClassLoader();
    probe("platform-present", () -> platform != null);
    probe("platform-same", () -> platform == ClassLoader.getPlatformClassLoader());
    probe("platform-name", platform::getName);
    probe("platform-parent", () -> platform.getParent());
    probe("app-name", app::getName);
    probe("app-parent", () -> app.getParent() == platform);
    probe("platform-string", () -> platform.loadClass("java.lang.String") == String.class);
    probe("platform-list", () -> platform.loadClass("java.util.List") == java.util.List.class);
    probe("platform-missing", () -> platform.loadClass("does.not.Exist"));
    probe("platform-app-class", () -> platform.loadClass("PlatformLoader"));
    probe("forname-platform", () -> Class.forName("java.lang.Integer", false, platform) == Integer.class);
    probe("string-loader", () -> String.class.getClassLoader());
    probe("app-class-loader", () -> PlatformLoader.class.getClassLoader() == app);
    probe("context-loader", () -> Thread.currentThread().getContextClassLoader() == app);
    ClassLoader child = new ClassLoader(platform) { };
    probe("child-parent", () -> child.getParent() == platform);
    probe("child-jdk", () -> child.loadClass("java.util.Map") == java.util.Map.class);
    probe("child-app-class", () -> child.loadClass("PlatformLoader"));
    ClassLoader appChild = new ClassLoader(app) { };
    probe("app-child-app-class", () -> appChild.loadClass("PlatformLoader") == PlatformLoader.class);
  }
}
