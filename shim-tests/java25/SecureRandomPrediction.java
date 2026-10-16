/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class SecureRandomPrediction {
  public static void main(String[] args) throws Exception {
    SecureRandom random = new SecureRandom();
    byte[] first = new byte[32], second = new byte[32];
    random.nextBytes(first);
    random.nextBytes(second);
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(first);
    for (int i = 0; i < 8; ++i) digest.update((byte) (2L >>> (i * 8)));
    if (Arrays.equals(second, digest.digest())) throw new AssertionError("Disclosed output predicts next block");
    try { random.nextBytes(null); throw new AssertionError(); }
    catch (NullPointerException expected) { System.out.println("null rejected"); }
    random.nextBytes(new byte[0]);
    if (random.generateSeed(32).length != 32) throw new AssertionError();
    System.out.println("prediction rejected");
  }
}
