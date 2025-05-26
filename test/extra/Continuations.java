/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

package extra;

import static shim.Continuations.callWithCurrentContinuation;

import shim.Function;
import shim.Callback;

public class Continuations {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) throws Exception {
    expect
      (callWithCurrentContinuation
       (new Function<Callback<Integer>,Integer>() {
         public Integer call(Callback<Integer> continuation) {
           continuation.handleResult(42);
           throw new AssertionError();
         }
       }) == 42);

    expect
      (callWithCurrentContinuation
       (new Function<Callback<Integer>,Integer>() {
         public Integer call(Callback<Integer> continuation) {
           return 43;
         }
       }) == 43);

    try {
      callWithCurrentContinuation(new Function<Callback<Integer>,Integer>() {
          public Integer call(Callback<Integer> continuation) {
            continuation.handleException(new MyException());
            throw new AssertionError();
          }
        });
      throw new AssertionError();      
    } catch (MyException e) {
      e.printStackTrace();
    }

    try {
      callWithCurrentContinuation(new Function<Callback<Integer>,Integer>() {
          public Integer call(Callback<Integer> continuation)
            throws MyException
          {
            throw new MyException();
          }
        });
      throw new AssertionError();
    } catch (MyException e) {
      e.printStackTrace();
    }
  }

  private static class MyException extends Exception { }
}
