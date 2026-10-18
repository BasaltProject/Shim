/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
public class CurrencyTableModern { public static void main(String[] a) {
  Locale.setDefault(Locale.US);
  Set<Currency> s = Currency.getAvailableCurrencies(); List<String> l = new ArrayList<>(); for (Currency c : s) l.add(c.getCurrencyCode() + ":" + c.getDefaultFractionDigits() + ":" + c.getNumericCode() + ":" + c.getNumericCodeAsString() + ":" + c.getSymbol(Locale.US) + ":" + c.getDisplayName(Locale.ENGLISH)); Collections.sort(l); System.out.println(s.size()); for (String x : l) System.out.println(x);
  StringBuilder sb = new StringBuilder();
  for (String cc : Locale.getISOCountries()) { Currency c = Currency.getInstance(new Locale("", cc)); sb.append(cc).append('=').append(c).append(' '); }
  System.out.println(sb);
  for (String x : new String[] {"", "xx", "ZZ", "us", "USA", "1"}) { try { System.out.println(x + " -> " + Currency.getInstance(new Locale("", x))); } catch (Exception e) { System.out.println(x + " !" + e.getClass().getName() + ":" + e.getMessage()); } }
  try { Currency.getInstance("ZZZ"); } catch (Exception e) { System.out.println(e.getClass().getName() + ":" + e.getMessage()); }
  try { Currency.getInstance((String) null); } catch (Exception e) { System.out.println(e.getClass().getName()); }
  try { Currency.getInstance((Locale) null); } catch (Exception e) { System.out.println(e.getClass().getName()); }
  System.out.println(Currency.getInstance(Locale.US) + " " + Currency.getInstance(Locale.FRANCE) + " " + Currency.getInstance("EUR").getSymbol(Locale.FRANCE) + " " + Currency.getInstance("EUR").getDisplayName(Locale.FRANCE) + " " + Currency.getInstance("CAD").getSymbol(Locale.US));
  System.out.println(Currency.getInstance("XXX").getDefaultFractionDigits() + " " + Currency.getInstance("XAU").getDefaultFractionDigits() + " " + Currency.getInstance("XXX").getNumericCode() + " " + Currency.getInstance("USD").equals(Currency.getInstance("USD")));
}}
