/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class LocaleDisplayData {
  private LocaleDisplayData() { }

  static final String[] kTags = {"en", "es", "fr", "de", "it", "pt", "ja", "zh", "zh-Hant", "ko", "ru", "nl", "pl", "tr", "sv", "ar", "hi", "th", "vi", "id", "cs", "da", "fi", "nb", "he", "el", "hu", "uk", "ro", "bg", "ca", "hr", "sk", "sl", };

  static String[] lines(String tag) {
    switch (tag) {
    case "en":
      return LocaleDisplay_en.kLines;
    case "es":
      return LocaleDisplay_es.kLines;
    case "fr":
      return LocaleDisplay_fr.kLines;
    case "de":
      return LocaleDisplay_de.kLines;
    case "it":
      return LocaleDisplay_it.kLines;
    case "pt":
      return LocaleDisplay_pt.kLines;
    case "ja":
      return LocaleDisplay_ja.kLines;
    case "zh":
      return LocaleDisplay_zh.kLines;
    case "zh-Hant":
      return LocaleDisplay_zh_Hant.kLines;
    case "ko":
      return LocaleDisplay_ko.kLines;
    case "ru":
      return LocaleDisplay_ru.kLines;
    case "nl":
      return LocaleDisplay_nl.kLines;
    case "pl":
      return LocaleDisplay_pl.kLines;
    case "tr":
      return LocaleDisplay_tr.kLines;
    case "sv":
      return LocaleDisplay_sv.kLines;
    case "ar":
      return LocaleDisplay_ar.kLines;
    case "hi":
      return LocaleDisplay_hi.kLines;
    case "th":
      return LocaleDisplay_th.kLines;
    case "vi":
      return LocaleDisplay_vi.kLines;
    case "id":
      return LocaleDisplay_id.kLines;
    case "cs":
      return LocaleDisplay_cs.kLines;
    case "da":
      return LocaleDisplay_da.kLines;
    case "fi":
      return LocaleDisplay_fi.kLines;
    case "nb":
      return LocaleDisplay_nb.kLines;
    case "he":
      return LocaleDisplay_he.kLines;
    case "el":
      return LocaleDisplay_el.kLines;
    case "hu":
      return LocaleDisplay_hu.kLines;
    case "uk":
      return LocaleDisplay_uk.kLines;
    case "ro":
      return LocaleDisplay_ro.kLines;
    case "bg":
      return LocaleDisplay_bg.kLines;
    case "ca":
      return LocaleDisplay_ca.kLines;
    case "hr":
      return LocaleDisplay_hr.kLines;
    case "sk":
      return LocaleDisplay_sk.kLines;
    case "sl":
      return LocaleDisplay_sl.kLines;
    default:
      return null;
    }
  }
}
