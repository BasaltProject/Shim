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

public class Enums {
  private enum Suit { CLUBS, HEARTS, SPADES, DIAMONDS };
  private enum Rank { ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT,
                      NINE, TEN, JACK, QUEEN, KING };
  private enum Person { Joe(4), Mike(5) ;
                        private final int age;
                        private Person(int age) {
                          this.age = age;
                        }
                        public int getAge() {
                          return age;
                        }
  };
  
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static boolean checkFaceCard(Rank r) {
    switch (r) {
    case ACE:
    case JACK:
    case QUEEN:
    case KING:
      return true;
    }
    return false;
  }

  public static void main(String[] args) {
    expect(Suit.CLUBS.ordinal() == 0);
    expect(Suit.valueOf("DIAMONDS") == Suit.DIAMONDS);
    System.out.println(Suit.SPADES);
    expect(Suit.values()[1] == Suit.HEARTS);
    expect(!checkFaceCard(Rank.FIVE));
    expect(checkFaceCard(Rank.KING));
    expect(Person.Mike.getAge() == 5);
  }
}
