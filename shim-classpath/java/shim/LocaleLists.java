/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.util.Locale;

public final class LocaleLists {
  private LocaleLists() { }

  private static final String kCollation =
      " hi fr_CH sr_ME pt zh_TW hr es_PE hu ro_RO ar_SA th_TH_TH_#u-nu-thai es_PY ar_BH en_NZ fr_CA ar_JO " +
      "es_HN id es_PR sr_ME_#Latn zh_CN lt_LT en_US is zh_TW_#Hant it ar_QA zh es_PA ar ar_IQ es_GT ja " +
      "sr__#Latn ar_AE sr_CS zh_SG pt_PT sq_AL ro en_MT fr_BE be ru bg nb_NO ar_TN ga_IE id_ID ar_LB tr_TR " +
      "en_PH ar_LY es_BO sk sl de_AT sl_SI sq ca sr pt_BR sv ko bg_BG fr_LU en_GB ar_SD cs ar_KW es_AR " +
      "no_NO el_CY th ar_SY da tr de sk_SK lt zh_HK de_CH no_NO_NY lv ja_JP es_CU mk_MK en_SG uk_UA uk " +
      "es_DO vi_VN mk et_EE en_IN ca_ES hr_HR lv_LV ms el it_IT mt ar_MA fr_FR en zh_HK_#Hant ar_DZ en_IE " +
      "es et nn_NO en_AU es_CR nl_BE vi nb es_CO es_CL mt_MT ar_EG es_SV nl en_ZA no fi sv_SE es_NI cs_CZ " +
      "sr_RS_#Latn ja_JP_JP_#u-ca-japanese sr_BA_#Latn fr pl_PL zh_CN_#Hans nl_NL ar_YE he_IL hi_IN sr_RS " +
      "ga zh_SG_#Hans th_TH it_CH ms_MY ar_OM es_EC es_US de_LU is_IS en_CA fi_FI da_DK hu_HU sr_BA de_DE " +
      "es_VE es_ES ru_RU be_BY es_MX ko_KR pl he el_GR es_UY ";

  public static Locale[] collation() {
    String[] tags = kCollation.trim().split(" ", -1);
    java.util.ArrayList<Locale> out = new java.util.ArrayList<Locale>();
    out.add(Locale.ROOT);
    for (String tag : tags) {
      if (!tag.isEmpty()) {
        out.add(parse(tag));
      }
    }
    return out.toArray(new Locale[0]);
  }

  private static Locale parse(String tag) {
    String script = null;
    int hash = tag.indexOf("_#");
    if (hash >= 0) {
      script = tag.substring(hash + 2);
      tag = tag.substring(0, hash);
    }
    String[] parts = tag.split("_", -1);
    String language = parts[0];
    String country = parts.length > 1 ? parts[1] : "";
    String variant = parts.length > 2 ? parts[2] : "";
    if (script == null || script.startsWith("u-")) {
      return new Locale(language, country, variant);
    }
    return new Locale.Builder().setLanguage(language).setScript(script).setRegion(country).build();
  }
}
