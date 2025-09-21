/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

final class BuiltInZoneRulesProvider extends ZoneRulesProvider {
  private static final String kVersion = "shim-rules";

  private static final String[][] kZones = {
    {"UTC", "0:00", "N"},
    {"GMT", "0:00", "N"},
    {"UCT", "0:00", "N"},
    {"Zulu", "0:00", "N"},
    {"Universal", "0:00", "N"},
    {"Greenwich", "0:00", "N"},
    {"GMT0", "0:00", "N"},
    {"Etc/UTC", "0:00", "N"},
    {"Etc/GMT", "0:00", "N"},
    {"Etc/UCT", "0:00", "N"},
    {"Etc/Zulu", "0:00", "N"},
    {"Etc/Universal", "0:00", "N"},
    {"Etc/Greenwich", "0:00", "N"},
    {"Etc/GMT0", "0:00", "N"},
    {"Etc/GMT+0", "0:00", "N"},
    {"Etc/GMT-0", "0:00", "N"},
    {"Atlantic/Reykjavik", "0:00", "N"},
    {"Africa/Accra", "0:00", "N"},
    {"Africa/Abidjan", "0:00", "N"},
    {"Africa/Dakar", "0:00", "N"},
    {"Iceland", "0:00", "N"},
    {"Etc/GMT+1", "-1:00", "N"},
    {"Etc/GMT+2", "-2:00", "N"},
    {"Etc/GMT+3", "-3:00", "N"},
    {"Etc/GMT+4", "-4:00", "N"},
    {"Etc/GMT+5", "-5:00", "N"},
    {"Etc/GMT+6", "-6:00", "N"},
    {"Etc/GMT+7", "-7:00", "N"},
    {"Etc/GMT+8", "-8:00", "N"},
    {"Etc/GMT+9", "-9:00", "N"},
    {"Etc/GMT+10", "-10:00", "N"},
    {"Etc/GMT+11", "-11:00", "N"},
    {"Etc/GMT+12", "-12:00", "N"},
    {"Etc/GMT-1", "+1:00", "N"},
    {"Etc/GMT-2", "+2:00", "N"},
    {"Etc/GMT-3", "+3:00", "N"},
    {"Etc/GMT-4", "+4:00", "N"},
    {"Etc/GMT-5", "+5:00", "N"},
    {"Etc/GMT-6", "+6:00", "N"},
    {"Etc/GMT-7", "+7:00", "N"},
    {"Etc/GMT-8", "+8:00", "N"},
    {"Etc/GMT-9", "+9:00", "N"},
    {"Etc/GMT-10", "+10:00", "N"},
    {"Etc/GMT-11", "+11:00", "N"},
    {"Etc/GMT-12", "+12:00", "N"},
    {"Etc/GMT-13", "+13:00", "N"},
    {"Etc/GMT-14", "+14:00", "N"},
    {"America/New_York", "-5:00", "US"},
    {"America/Detroit", "-5:00", "US"},
    {"America/Toronto", "-5:00", "US"},
    {"America/Indiana/Indianapolis", "-5:00", "US"},
    {"America/Kentucky/Louisville", "-5:00", "US"},
    {"US/Eastern", "-5:00", "US"},
    {"Canada/Eastern", "-5:00", "US"},
    {"America/Montreal", "-5:00", "US"},
    {"America/Chicago", "-6:00", "US"},
    {"America/Winnipeg", "-6:00", "US"},
    {"America/Menominee", "-6:00", "US"},
    {"US/Central", "-6:00", "US"},
    {"Canada/Central", "-6:00", "US"},
    {"America/Denver", "-7:00", "US"},
    {"America/Edmonton", "-7:00", "US"},
    {"America/Boise", "-7:00", "US"},
    {"US/Mountain", "-7:00", "US"},
    {"Canada/Mountain", "-7:00", "US"},
    {"America/Los_Angeles", "-8:00", "US"},
    {"America/Vancouver", "-8:00", "US"},
    {"US/Pacific", "-8:00", "US"},
    {"Canada/Pacific", "-8:00", "US"},
    {"America/Tijuana", "-8:00", "US"},
    {"America/Anchorage", "-9:00", "US"},
    {"America/Juneau", "-9:00", "US"},
    {"US/Alaska", "-9:00", "US"},
    {"America/Halifax", "-4:00", "US"},
    {"Atlantic/Bermuda", "-4:00", "US"},
    {"Canada/Atlantic", "-4:00", "US"},
    {"America/St_Johns", "-3:30", "US"},
    {"Canada/Newfoundland", "-3:30", "US"},
    {"America/Phoenix", "-7:00", "N"},
    {"US/Arizona", "-7:00", "N"},
    {"America/Hermosillo", "-7:00", "N"},
    {"Pacific/Honolulu", "-10:00", "N"},
    {"US/Hawaii", "-10:00", "N"},
    {"America/Mexico_City", "-6:00", "N"},
    {"America/Regina", "-6:00", "N"},
    {"America/Guatemala", "-6:00", "N"},
    {"America/Costa_Rica", "-6:00", "N"},
    {"America/El_Salvador", "-6:00", "N"},
    {"America/Tegucigalpa", "-6:00", "N"},
    {"America/Managua", "-6:00", "N"},
    {"Canada/Saskatchewan", "-6:00", "N"},
    {"America/Bogota", "-5:00", "N"},
    {"America/Lima", "-5:00", "N"},
    {"America/Panama", "-5:00", "N"},
    {"America/Jamaica", "-5:00", "N"},
    {"America/Cancun", "-5:00", "N"},
    {"America/Guayaquil", "-5:00", "N"},
    {"America/Caracas", "-4:00", "N"},
    {"America/Puerto_Rico", "-4:00", "N"},
    {"America/La_Paz", "-4:00", "N"},
    {"America/Santo_Domingo", "-4:00", "N"},
    {"America/Manaus", "-4:00", "N"},
    {"America/Sao_Paulo", "-3:00", "N"},
    {"America/Argentina/Buenos_Aires", "-3:00", "N"},
    {"America/Buenos_Aires", "-3:00", "N"},
    {"America/Montevideo", "-3:00", "N"},
    {"America/Bahia", "-3:00", "N"},
    {"America/Fortaleza", "-3:00", "N"},
    {"Europe/London", "0:00", "EU"},
    {"Europe/Dublin", "0:00", "EU"},
    {"Europe/Lisbon", "0:00", "EU"},
    {"Atlantic/Canary", "0:00", "EU"},
    {"Atlantic/Faroe", "0:00", "EU"},
    {"GB", "0:00", "EU"},
    {"Eire", "0:00", "EU"},
    {"Portugal", "0:00", "EU"},
    {"Atlantic/Azores", "-1:00", "EU"},
    {"Europe/Paris", "1:00", "EU"},
    {"Europe/Berlin", "1:00", "EU"},
    {"Europe/Madrid", "1:00", "EU"},
    {"Europe/Rome", "1:00", "EU"},
    {"Europe/Amsterdam", "1:00", "EU"},
    {"Europe/Brussels", "1:00", "EU"},
    {"Europe/Vienna", "1:00", "EU"},
    {"Europe/Zurich", "1:00", "EU"},
    {"Europe/Stockholm", "1:00", "EU"},
    {"Europe/Oslo", "1:00", "EU"},
    {"Europe/Copenhagen", "1:00", "EU"},
    {"Europe/Warsaw", "1:00", "EU"},
    {"Europe/Prague", "1:00", "EU"},
    {"Europe/Budapest", "1:00", "EU"},
    {"Europe/Belgrade", "1:00", "EU"},
    {"Europe/Zagreb", "1:00", "EU"},
    {"Europe/Luxembourg", "1:00", "EU"},
    {"Europe/Monaco", "1:00", "EU"},
    {"Europe/Malta", "1:00", "EU"},
    {"Europe/Bratislava", "1:00", "EU"},
    {"Europe/Ljubljana", "1:00", "EU"},
    {"Europe/Sarajevo", "1:00", "EU"},
    {"Europe/Skopje", "1:00", "EU"},
    {"Europe/Tirane", "1:00", "EU"},
    {"Europe/Andorra", "1:00", "EU"},
    {"Europe/Vaduz", "1:00", "EU"},
    {"Europe/Vatican", "1:00", "EU"},
    {"Europe/San_Marino", "1:00", "EU"},
    {"Europe/Gibraltar", "1:00", "EU"},
    {"CET", "1:00", "EU"},
    {"Poland", "1:00", "EU"},
    {"Africa/Ceuta", "1:00", "EU"},
    {"Arctic/Longyearbyen", "1:00", "EU"},
    {"Europe/Athens", "2:00", "EU"},
    {"Europe/Helsinki", "2:00", "EU"},
    {"Europe/Kiev", "2:00", "EU"},
    {"Europe/Kyiv", "2:00", "EU"},
    {"Europe/Bucharest", "2:00", "EU"},
    {"Europe/Sofia", "2:00", "EU"},
    {"Europe/Riga", "2:00", "EU"},
    {"Europe/Tallinn", "2:00", "EU"},
    {"Europe/Vilnius", "2:00", "EU"},
    {"Europe/Mariehamn", "2:00", "EU"},
    {"EET", "2:00", "EU"},
    {"Asia/Nicosia", "2:00", "EU"},
    {"Europe/Nicosia", "2:00", "EU"},
    {"Asia/Beirut", "2:00", "EU"},
    {"Europe/Kaliningrad", "2:00", "N"},
    {"Africa/Johannesburg", "2:00", "N"},
    {"Africa/Harare", "2:00", "N"},
    {"Africa/Maputo", "2:00", "N"},
    {"Africa/Khartoum", "2:00", "N"},
    {"Africa/Tripoli", "2:00", "N"},
    {"Africa/Lusaka", "2:00", "N"},
    {"Africa/Kigali", "2:00", "N"},
    {"Africa/Gaborone", "2:00", "N"},
    {"Asia/Amman", "2:00", "N"},
    {"Asia/Damascus", "2:00", "N"},
    {"Europe/Istanbul", "3:00", "N"},
    {"Europe/Moscow", "3:00", "N"},
    {"Europe/Minsk", "3:00", "N"},
    {"Europe/Simferopol", "3:00", "N"},
    {"Asia/Istanbul", "3:00", "N"},
    {"Asia/Baghdad", "3:00", "N"},
    {"Asia/Riyadh", "3:00", "N"},
    {"Asia/Kuwait", "3:00", "N"},
    {"Asia/Qatar", "3:00", "N"},
    {"Asia/Bahrain", "3:00", "N"},
    {"Africa/Nairobi", "3:00", "N"},
    {"Africa/Addis_Ababa", "3:00", "N"},
    {"Africa/Dar_es_Salaam", "3:00", "N"},
    {"Africa/Kampala", "3:00", "N"},
    {"Africa/Mogadishu", "3:00", "N"},
    {"Turkey", "3:00", "N"},
    {"W-SU", "3:00", "N"},
    {"Africa/Lagos", "1:00", "N"},
    {"Africa/Algiers", "1:00", "N"},
    {"Africa/Tunis", "1:00", "N"},
    {"Africa/Kinshasa", "1:00", "N"},
    {"Africa/Casablanca", "1:00", "N"},
    {"Africa/Luanda", "1:00", "N"},
    {"Africa/Douala", "1:00", "N"},
    {"Europe/Samara", "4:00", "N"},
    {"Asia/Dubai", "4:00", "N"},
    {"Asia/Muscat", "4:00", "N"},
    {"Asia/Baku", "4:00", "N"},
    {"Asia/Yerevan", "4:00", "N"},
    {"Asia/Tbilisi", "4:00", "N"},
    {"Indian/Mauritius", "4:00", "N"},
    {"Asia/Tehran", "3:30", "N"},
    {"Iran", "3:30", "N"},
    {"Asia/Kabul", "4:30", "N"},
    {"Asia/Karachi", "5:00", "N"},
    {"Asia/Tashkent", "5:00", "N"},
    {"Asia/Yekaterinburg", "5:00", "N"},
    {"Asia/Almaty", "5:00", "N"},
    {"Indian/Maldives", "5:00", "N"},
    {"Asia/Kolkata", "5:30", "N"},
    {"Asia/Calcutta", "5:30", "N"},
    {"Asia/Colombo", "5:30", "N"},
    {"Asia/Kathmandu", "5:45", "N"},
    {"Asia/Katmandu", "5:45", "N"},
    {"Asia/Dhaka", "6:00", "N"},
    {"Asia/Omsk", "6:00", "N"},
    {"Asia/Thimphu", "6:00", "N"},
    {"Asia/Yangon", "6:30", "N"},
    {"Asia/Rangoon", "6:30", "N"},
    {"Asia/Bangkok", "7:00", "N"},
    {"Asia/Jakarta", "7:00", "N"},
    {"Asia/Ho_Chi_Minh", "7:00", "N"},
    {"Asia/Saigon", "7:00", "N"},
    {"Asia/Novosibirsk", "7:00", "N"},
    {"Asia/Krasnoyarsk", "7:00", "N"},
    {"Asia/Phnom_Penh", "7:00", "N"},
    {"Asia/Vientiane", "7:00", "N"},
    {"Asia/Shanghai", "8:00", "N"},
    {"Asia/Hong_Kong", "8:00", "N"},
    {"Asia/Taipei", "8:00", "N"},
    {"Asia/Singapore", "8:00", "N"},
    {"Asia/Kuala_Lumpur", "8:00", "N"},
    {"Asia/Manila", "8:00", "N"},
    {"Asia/Macau", "8:00", "N"},
    {"Asia/Ulaanbaatar", "8:00", "N"},
    {"Asia/Irkutsk", "8:00", "N"},
    {"Australia/Perth", "8:00", "N"},
    {"Singapore", "8:00", "N"},
    {"Hongkong", "8:00", "N"},
    {"PRC", "8:00", "N"},
    {"Asia/Tokyo", "9:00", "N"},
    {"Asia/Seoul", "9:00", "N"},
    {"Asia/Pyongyang", "9:00", "N"},
    {"Asia/Yakutsk", "9:00", "N"},
    {"Asia/Jayapura", "9:00", "N"},
    {"Japan", "9:00", "N"},
    {"ROK", "9:00", "N"},
    {"Australia/Darwin", "9:30", "N"},
    {"Australia/Adelaide", "9:30", "AU"},
    {"Australia/Broken_Hill", "9:30", "AU"},
    {"Australia/Brisbane", "10:00", "N"},
    {"Asia/Vladivostok", "10:00", "N"},
    {"Pacific/Guam", "10:00", "N"},
    {"Pacific/Port_Moresby", "10:00", "N"},
    {"Pacific/Guadalcanal", "10:00", "N"},
    {"Australia/Sydney", "10:00", "AU"},
    {"Australia/Melbourne", "10:00", "AU"},
    {"Australia/Canberra", "10:00", "AU"},
    {"Australia/Hobart", "10:00", "AU"},
    {"Australia/ACT", "10:00", "AU"},
    {"Australia/NSW", "10:00", "AU"},
    {"Australia/Victoria", "10:00", "AU"},
    {"Australia/Tasmania", "10:00", "AU"},
    {"Pacific/Fiji", "12:00", "N"},
    {"Pacific/Majuro", "12:00", "N"},
    {"Pacific/Tarawa", "12:00", "N"},
    {"Asia/Kamchatka", "12:00", "N"},
    {"Pacific/Auckland", "12:00", "NZ"},
    {"NZ", "12:00", "NZ"},
    {"Antarctica/McMurdo", "12:00", "NZ"},
    {"Pacific/Apia", "13:00", "N"},
    {"Pacific/Tongatapu", "13:00", "N"},
    {"Pacific/Fakaofo", "13:00", "N"},
    {"Africa/Cairo", "2:00", "EG"},
    {"Egypt", "2:00", "EG"},
    {"Asia/Jerusalem", "2:00", "IL"},
    {"Asia/Tel_Aviv", "2:00", "IL"},
    {"Israel", "2:00", "IL"},
  };

  private final Map<String, String[]> zones = new HashMap<String, String[]>();
  private final Map<String, ZoneRules> cache = new ConcurrentHashMap<String, ZoneRules>();

  BuiltInZoneRulesProvider() {
    for (String[] row : kZones) {
      zones.put(row[0], row);
    }
  }

  @Override
  protected Set<String> provideZoneIds() {
    return new HashSet<String>(zones.keySet());
  }

  @Override
  protected ZoneRules provideRules(String zoneId, boolean forCaching) {
    ZoneRules rules = cache.get(zoneId);
    if (rules == null) {
      String[] row = zones.get(zoneId);
      if (row == null) {
        throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
      }
      rules = build(parseOffset(row[1]), row[2]);
      cache.put(zoneId, rules);
    }
    return rules;
  }

  @Override
  protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
    TreeMap<String, ZoneRules> map = new TreeMap<String, ZoneRules>();
    map.put(kVersion, provideRules(zoneId, false));
    return map;
  }

  private static ZoneOffset parseOffset(String text) {
    boolean negative = text.startsWith("-");
    String body = text.startsWith("+") || negative ? text.substring(1) : text;
    int colon = body.indexOf(':');
    int hours = Integer.parseInt(body.substring(0, colon));
    int minutes = Integer.parseInt(body.substring(colon + 1));
    int total = hours * 3600 + minutes * 60;
    return ZoneOffset.ofTotalSeconds(negative ? -total : total);
  }

  private static ZoneRules build(ZoneOffset standard, String rule) {
    if (rule.equals("N")) {
      return ZoneRules.of(standard);
    }
    ZoneOffset summer = ZoneOffset.ofTotalSeconds(standard.getTotalSeconds() + 3600);
    List<ZoneOffsetTransitionRule> rules = new ArrayList<ZoneOffsetTransitionRule>();
    switch (rule) {
      case "US":
        rules.add(ZoneOffsetTransitionRule.of(Month.MARCH, 8, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.WALL, standard, standard, summer));
        rules.add(ZoneOffsetTransitionRule.of(Month.NOVEMBER, 1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.WALL, standard, summer, standard));
        break;
      case "EU":
        rules.add(ZoneOffsetTransitionRule.of(Month.MARCH, -1, DayOfWeek.SUNDAY, LocalTime.of(1, 0), false,
            TimeDefinition.UTC, standard, standard, summer));
        rules.add(ZoneOffsetTransitionRule.of(Month.OCTOBER, -1, DayOfWeek.SUNDAY, LocalTime.of(1, 0), false,
            TimeDefinition.UTC, standard, summer, standard));
        break;
      case "AU":
        rules.add(ZoneOffsetTransitionRule.of(Month.APRIL, 1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.STANDARD, standard, summer, standard));
        rules.add(ZoneOffsetTransitionRule.of(Month.OCTOBER, 1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.STANDARD, standard, standard, summer));
        break;
      case "NZ":
        rules.add(ZoneOffsetTransitionRule.of(Month.APRIL, 1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.STANDARD, standard, summer, standard));
        rules.add(ZoneOffsetTransitionRule.of(Month.SEPTEMBER, -1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.STANDARD, standard, standard, summer));
        break;
      case "EG":
        rules.add(ZoneOffsetTransitionRule.of(Month.APRIL, -1, DayOfWeek.FRIDAY, LocalTime.MIDNIGHT, false,
            TimeDefinition.WALL, standard, standard, summer));
        rules.add(ZoneOffsetTransitionRule.of(Month.OCTOBER, -1, DayOfWeek.THURSDAY, LocalTime.MIDNIGHT, true,
            TimeDefinition.WALL, standard, summer, standard));
        break;
      case "IL":
        rules.add(ZoneOffsetTransitionRule.of(Month.MARCH, 23, DayOfWeek.FRIDAY, LocalTime.of(2, 0), false,
            TimeDefinition.WALL, standard, standard, summer));
        rules.add(ZoneOffsetTransitionRule.of(Month.OCTOBER, -1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
            TimeDefinition.WALL, standard, summer, standard));
        break;
      default:
        throw new ZoneRulesException("Unknown rule set: " + rule);
    }
    List<ZoneOffsetTransition> none = Collections.emptyList();
    return ZoneRules.of(standard, standard, none, none, rules);
  }
}
