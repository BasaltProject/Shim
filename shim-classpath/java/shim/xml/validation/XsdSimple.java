/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** XML Schema simple types: the built-in hierarchy, restriction facets, lists and unions. */
final class XsdSimple {
  static final String kXs = "http://www.w3.org/2001/XMLSchema";

  enum Lexical { STRING, BOOLEAN, DECIMAL, INTEGER, FLOAT, DOUBLE, DATE, DATETIME, TIME, DURATION, GYEAR, GYEARMONTH,
    GMONTH, GMONTHDAY, GDAY, ANYURI, QNAME, NCNAME, NAME, NMTOKEN, HEX, BASE64, NOTATION, ANYSIMPLE }

  static final int kPreserve = 0;
  static final int kReplace = 1;
  static final int kCollapse = 2;

  enum Variety { ATOMIC, LIST, UNION }

  final String name;
  final String namespace;
  XsdSimple base;
  Variety variety = Variety.ATOMIC;
  Lexical lexical = Lexical.STRING;
  XsdSimple itemType;
  final List<XsdSimple> members = new ArrayList<XsdSimple>();
  boolean anonymous;
  String anonymousOwner;
  int whiteSpace = -1;
  Integer length;
  Integer minLength;
  Integer maxLength;
  Integer totalDigits;
  Integer fractionDigits;
  String minInclusive;
  String maxInclusive;
  String minExclusive;
  String maxExclusive;
  List<String> enumeration;
  final List<List<Pattern>> patterns = new ArrayList<List<Pattern>>();
  final List<List<String>> patternSources = new ArrayList<List<String>>();
  boolean idKind;
  boolean idrefKind;

  XsdSimple(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  String displayName() {
    if (anonymous) {
      return "#AnonType_" + (anonymousOwner == null ? "" : anonymousOwner);
    }
    return name;
  }

  int effectiveWhiteSpace() {
    for (XsdSimple t = this; t != null; t = t.base) {
      if (t.whiteSpace >= 0) {
        return t.whiteSpace;
      }
      if (t.variety == Variety.LIST) {
        return kCollapse;
      }
    }
    return lexical == Lexical.STRING ? kPreserve : kCollapse;
  }

  static String normalize(String s, int ws) {
    if (ws == kPreserve) {
      return s;
    }
    StringBuilder sb = new StringBuilder(s.length());
    boolean pendingSpace = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      boolean space = c == ' ' || c == '\t' || c == '\n' || c == '\r';
      if (ws == kReplace) {
        sb.append(space ? ' ' : c);
      } else if (space) {
        pendingSpace = sb.length() > 0;
      } else {
        if (pendingSpace) {
          sb.append(' ');
          pendingSpace = false;
        }
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static final Map<String, XsdSimple> builtins = new HashMap<String, XsdSimple>();

  static synchronized XsdSimple builtin(String local) {
    if (builtins.isEmpty()) {
      init();
    }
    return builtins.get(local);
  }

  private static XsdSimple def(String name, String base, Lexical lexical) {
    XsdSimple t = new XsdSimple(kXs, name);
    t.base = base == null ? null : builtins.get(base);
    t.lexical = lexical;
    builtins.put(name, t);
    return t;
  }

  private static void init() {
    XsdSimple any = def("anySimpleType", null, Lexical.ANYSIMPLE);
    XsdSimple string = def("string", "anySimpleType", Lexical.STRING);
    string.whiteSpace = kPreserve;
    def("normalizedString", "string", Lexical.STRING).whiteSpace = kReplace;
    def("token", "normalizedString", Lexical.STRING).whiteSpace = kCollapse;
    XsdSimple language = def("language", "token", Lexical.STRING);
    language.addPattern("([a-zA-Z]{1,8})(-[a-zA-Z0-9]{1,8})*");
    def("NMTOKEN", "token", Lexical.NMTOKEN);
    def("Name", "token", Lexical.NAME);
    def("NCName", "Name", Lexical.NCNAME);
    def("ID", "NCName", Lexical.NCNAME).idKind = true;
    def("IDREF", "NCName", Lexical.NCNAME).idrefKind = true;
    def("ENTITY", "NCName", Lexical.NCNAME);
    for (String listName : new String[] {"NMTOKENS", "IDREFS", "ENTITIES"}) {
      XsdSimple l = def(listName, "anySimpleType", Lexical.STRING);
      l.variety = Variety.LIST;
      l.itemType = builtins.get(listName.equals("NMTOKENS") ? "NMTOKEN" : listName.equals("IDREFS") ? "IDREF" : "ENTITY");
      l.minLength = Integer.valueOf(1);
    }
    def("boolean", "anySimpleType", Lexical.BOOLEAN);
    def("decimal", "anySimpleType", Lexical.DECIMAL);
    XsdSimple integer = def("integer", "decimal", Lexical.INTEGER);
    integer.fractionDigits = Integer.valueOf(0);
    def("nonPositiveInteger", "integer", Lexical.INTEGER).maxInclusive = "0";
    def("negativeInteger", "nonPositiveInteger", Lexical.INTEGER).maxInclusive = "-1";
    XsdSimple lng = def("long", "integer", Lexical.INTEGER);
    lng.minInclusive = "-9223372036854775808";
    lng.maxInclusive = "9223372036854775807";
    XsdSimple in = def("int", "long", Lexical.INTEGER);
    in.minInclusive = "-2147483648";
    in.maxInclusive = "2147483647";
    XsdSimple sh = def("short", "int", Lexical.INTEGER);
    sh.minInclusive = "-32768";
    sh.maxInclusive = "32767";
    XsdSimple by = def("byte", "short", Lexical.INTEGER);
    by.minInclusive = "-128";
    by.maxInclusive = "127";
    def("nonNegativeInteger", "integer", Lexical.INTEGER).minInclusive = "0";
    def("unsignedLong", "nonNegativeInteger", Lexical.INTEGER).maxInclusive = "18446744073709551615";
    def("unsignedInt", "unsignedLong", Lexical.INTEGER).maxInclusive = "4294967295";
    def("unsignedShort", "unsignedInt", Lexical.INTEGER).maxInclusive = "65535";
    def("unsignedByte", "unsignedShort", Lexical.INTEGER).maxInclusive = "255";
    def("positiveInteger", "nonNegativeInteger", Lexical.INTEGER).minInclusive = "1";
    def("float", "anySimpleType", Lexical.FLOAT);
    def("double", "anySimpleType", Lexical.DOUBLE);
    def("date", "anySimpleType", Lexical.DATE);
    def("dateTime", "anySimpleType", Lexical.DATETIME);
    def("time", "anySimpleType", Lexical.TIME);
    def("duration", "anySimpleType", Lexical.DURATION);
    def("gYear", "anySimpleType", Lexical.GYEAR);
    def("gYearMonth", "anySimpleType", Lexical.GYEARMONTH);
    def("gMonth", "anySimpleType", Lexical.GMONTH);
    def("gMonthDay", "anySimpleType", Lexical.GMONTHDAY);
    def("gDay", "anySimpleType", Lexical.GDAY);
    def("anyURI", "anySimpleType", Lexical.ANYURI);
    def("QName", "anySimpleType", Lexical.QNAME);
    def("NOTATION", "anySimpleType", Lexical.NOTATION);
    def("hexBinary", "anySimpleType", Lexical.HEX);
    def("base64Binary", "anySimpleType", Lexical.BASE64);
    any.whiteSpace = kPreserve;
    for (XsdSimple t : builtins.values()) {
      if (t.lexical != Lexical.STRING && t != any && t.whiteSpace < 0 && t.variety == Variety.ATOMIC) {
        t.whiteSpace = kCollapse;
      }
    }
  }

  void addPattern(String regex) {
    List<Pattern> list = new ArrayList<Pattern>();
    list.add(Pattern.compile(translate(regex)));
    patterns.add(list);
    List<String> sources = new ArrayList<String>();
    sources.add(regex);
    patternSources.add(sources);
  }

  void addPatternGroup(List<String> regexes) {
    List<Pattern> list = new ArrayList<Pattern>();
    for (String r : regexes) {
      list.add(Pattern.compile(translate(r)));
    }
    patterns.add(list);
    patternSources.add(new ArrayList<String>(regexes));
  }

  /** Converts the XML Schema regular expression dialect to java.util.regex. */
  static String translate(String r) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < r.length(); i++) {
      char c = r.charAt(i);
      if (c == '\\' && i + 1 < r.length()) {
        char n = r.charAt(i + 1);
        if (n == 'i') {
          sb.append("[_:A-Za-z]");
          i++;
        } else if (n == 'c') {
          sb.append("[-._:A-Za-z0-9]");
          i++;
        } else if (n == 'I') {
          sb.append("[^_:A-Za-z]");
          i++;
        } else if (n == 'C') {
          sb.append("[^-._:A-Za-z0-9]");
          i++;
        } else {
          sb.append(c).append(n);
          i++;
        }
      } else if (c == '^' && i > 0 && r.charAt(i - 1) != '[' && r.charAt(i - 1) != '\\') {
        sb.append("\\^");
      } else if (c == '$') {
        sb.append("\\$");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /** Result of validating a literal: null when valid, otherwise the first error message. */
  static final class Outcome {
    String error;
    String canonical;
  }

  String validate(String raw, Ctx ctx) {
    Outcome o = check(raw, ctx);
    return o.error;
  }

  /** Context used by QName, ID and IDREF checks. */
  interface Ctx {
    String resolvePrefix(String prefix);

    boolean declareId(String id);

    void referenceId(String id);

    String owner();
  }

  Outcome check(String raw, Ctx ctx) {
    Outcome out = new Outcome();
    XsdSimple type = this;
    if (variety == Variety.UNION) {
      String text = normalize(raw, kCollapse);
      for (XsdSimple m : members) {
        if (m.check(raw, ctx).error == null) {
          return applyFacets(out, raw, text);
        }
      }
      out.error = "cvc-datatype-valid.1.2.3: '" + raw + "' is not a valid value of union type '" + displayName() + "'.";
      return out;
    }
    if (variety == Variety.LIST) {
      String text = normalize(raw, kCollapse);
      String[] items = text.isEmpty() ? new String[0] : text.split(" ");
      XsdSimple it = effectiveItem();
      for (String item : items) {
        Outcome o = it.check(item, ctx);
        if (o.error != null) {
          return o;
        }
      }
      return applyListFacets(out, raw, items);
    }
    String text = normalize(raw, effectiveWhiteSpace());
    out.canonical = text;
    String err = patternError(text);
    if (err != null) {
      out.error = err;
      return out;
    }
    String lexicalError = lexicalError(text, ctx);
    if (lexicalError != null) {
      out.error = lexicalError;
      return out;
    }
    return applyFacets(out, raw, text);
  }

  private XsdSimple effectiveItem() {
    for (XsdSimple t = this; t != null; t = t.base) {
      if (t.itemType != null) {
        return t.itemType;
      }
    }
    return builtin("string");
  }

  private Outcome applyListFacets(Outcome out, String raw, String[] items) {
    List<XsdSimple> chain = chain();
    Integer len = null;
    Integer min = null;
    Integer max = null;
    List<String> enumer = null;
    for (XsdSimple t : chain) {
      if (t.length != null) {
        len = t.length;
      }
      if (t.minLength != null) {
        min = t.minLength;
      }
      if (t.maxLength != null) {
        max = t.maxLength;
      }
      if (t.enumeration != null) {
        enumer = t.enumeration;
      }
    }
    String type = displayName();
    if (len != null && items.length != len.intValue()) {
      out.error = "cvc-length-valid: Value '" + raw + "' with length = '" + items.length
          + "' is not facet-valid with respect to length '" + len + "' for type '" + type + "'.";
    } else if (min != null && items.length < min.intValue()) {
      out.error = "cvc-minLength-valid: Value '" + raw + "' with length = '" + items.length
          + "' is not facet-valid with respect to minLength '" + min + "' for type '" + type + "'.";
    } else if (max != null && items.length > max.intValue()) {
      out.error = "cvc-maxLength-valid: Value '" + raw + "' with length = '" + items.length
          + "' is not facet-valid with respect to maxLength '" + max + "' for type '" + type + "'.";
    } else if (enumer != null) {
      String joined = normalize(raw, kCollapse);
      boolean found = false;
      for (String e : enumer) {
        if (normalize(e, kCollapse).equals(joined)) {
          found = true;
        }
      }
      if (!found) {
        out.error = "cvc-enumeration-valid: Value '" + raw + "' is not facet-valid with respect to enumeration '"
            + enumer + "'. It must be a value from the enumeration.";
      }
    }
    return out;
  }

  List<XsdSimple> chain() {
    List<XsdSimple> list = new ArrayList<XsdSimple>();
    for (XsdSimple t = this; t != null; t = t.base) {
      list.add(0, t);
    }
    return list;
  }

  private String patternError(String text) {
    for (XsdSimple t : chain()) {
      for (int g = 0; g < t.patterns.size(); g++) {
        boolean ok = false;
        for (Pattern p : t.patterns.get(g)) {
          if (p.matcher(text).matches()) {
            ok = true;
            break;
          }
        }
        if (!ok) {
          List<String> sources = t.patternSources.get(g);
          String shown = sources.size() == 1 ? sources.get(0) : String.join("|", sources);
          return "cvc-pattern-valid: Value '" + text + "' is not facet-valid with respect to pattern '" + shown
              + "' for type '" + displayName() + "'.";
        }
      }
    }
    return null;
  }

  private Lexical effectiveLexical() {
    return lexical;
  }

  /** Name of the built-in type whose lexical rule rejects the literal (what the reference implementation reports). */
  private String lexicalTypeName() {
    switch (effectiveLexical()) {
    case INTEGER:
      return "integer";
    case NCNAME:
      return "NCName";
    case NAME:
      return "Name";
    case NMTOKEN:
      return "NMTOKEN";
    case STRING:
      return "string";
    case BOOLEAN:
      return "boolean";
    case DECIMAL:
      return "decimal";
    case FLOAT:
      return "float";
    case DOUBLE:
      return "double";
    case DATE:
      return "date";
    case DATETIME:
      return "dateTime";
    case TIME:
      return "time";
    case DURATION:
      return "duration";
    case GYEAR:
      return "gYear";
    case GYEARMONTH:
      return "gYearMonth";
    case GMONTH:
      return "gMonth";
    case GMONTHDAY:
      return "gMonthDay";
    case GDAY:
      return "gDay";
    case QNAME:
      return "QName";
    case HEX:
      return "hexBinary";
    case BASE64:
      return "base64Binary";
    case ANYURI:
      return "anyURI";
    default:
      return name;
    }
  }

  private String lexicalError(String text, Ctx ctx) {
    boolean ok = XsdLexical.valid(effectiveLexical(), text, ctx);
    if (ok) {
      if (idKind && ctx != null && !ctx.declareId(text)) {
        return "cvc-id.2: There are multiple occurrences of ID value '" + text + "'.";
      }
      if (idrefKind && ctx != null) {
        ctx.referenceId(text);
      }
      return null;
    }
    if (effectiveLexical() == Lexical.NOTATION) {
      return "enumeration-required-notation: The NOTATION type, '" + displayName() + "' used by " + ctx.owner()
          + ", must have an enumeration facet value which specifies the notation elements used by this type.";
    }
    return "cvc-datatype-valid.1.2.1: '" + text + "' is not a valid value for '" + lexicalTypeName() + "'.";
  }

  private Outcome applyFacets(Outcome out, String raw, String text) {
    Integer len = null;
    Integer min = null;
    Integer max = null;
    Integer total = null;
    Integer fraction = null;
    String minI = null;
    String maxI = null;
    String minE = null;
    String maxE = null;
    List<String> enumer = null;
    for (XsdSimple t : chain()) {
      if (t.length != null) {
        len = t.length;
      }
      if (t.minLength != null) {
        min = t.minLength;
      }
      if (t.maxLength != null) {
        max = t.maxLength;
      }
      if (t.totalDigits != null) {
        total = t.totalDigits;
      }
      if (t.fractionDigits != null) {
        fraction = t.fractionDigits;
      }
      if (t.minInclusive != null) {
        minI = t.minInclusive;
        minE = null;
      }
      if (t.maxInclusive != null) {
        maxI = t.maxInclusive;
        maxE = null;
      }
      if (t.minExclusive != null) {
        minE = t.minExclusive;
      }
      if (t.maxExclusive != null) {
        maxE = t.maxExclusive;
      }
      if (t.enumeration != null) {
        enumer = t.enumeration;
      }
    }
    String type = displayName();
    int measure = text.length();
    if (len != null && measure != len.intValue()) {
      out.error = "cvc-length-valid: Value '" + text + "' with length = '" + measure
          + "' is not facet-valid with respect to length '" + len + "' for type '" + type + "'.";
      return out;
    }
    if (min != null && measure < min.intValue()) {
      out.error = "cvc-minLength-valid: Value '" + text + "' with length = '" + measure
          + "' is not facet-valid with respect to minLength '" + min + "' for type '" + type + "'.";
      return out;
    }
    if (max != null && measure > max.intValue()) {
      out.error = "cvc-maxLength-valid: Value '" + text + "' with length = '" + measure
          + "' is not facet-valid with respect to maxLength '" + max + "' for type '" + type + "'.";
      return out;
    }
    if (enumer != null) {
      boolean found = false;
      for (String e : enumer) {
        if (XsdLexical.equalValues(effectiveLexical(), normalize(e, effectiveWhiteSpace()), text)) {
          found = true;
          break;
        }
      }
      if (!found) {
        out.error = "cvc-enumeration-valid: Value '" + text + "' is not facet-valid with respect to enumeration '"
            + enumer + "'. It must be a value from the enumeration.";
        return out;
      }
    }
    if (maxI != null && XsdLexical.compare(effectiveLexical(), text, maxI) > 0) {
      out.error = rangeError("maxInclusive", text, maxI, type);
      return out;
    }
    if (maxE != null && XsdLexical.compare(effectiveLexical(), text, maxE) >= 0) {
      out.error = rangeError("maxExclusive", text, maxE, type);
      return out;
    }
    if (minI != null && XsdLexical.compare(effectiveLexical(), text, minI) < 0) {
      out.error = rangeError("minInclusive", text, minI, type);
      return out;
    }
    if (minE != null && XsdLexical.compare(effectiveLexical(), text, minE) <= 0) {
      out.error = rangeError("minExclusive", text, minE, type);
      return out;
    }
    if ((total != null || fraction != null) && isDecimalFamily()) {
      BigDecimal d = new BigDecimal(text.startsWith("+") ? text.substring(1) : text).stripTrailingZeros();
      int fractionDigitsCount = Math.max(0, d.scale());
      int totalDigitsCount = Math.max(d.precision(), d.scale());
      if (d.signum() == 0) {
        totalDigitsCount = 1;
        fractionDigitsCount = 0;
      }
      if (total != null && totalDigitsCount > total.intValue()) {
        out.error = "cvc-totalDigits-valid: Value '" + text + "' has " + totalDigitsCount
            + " total digits, but the number of total digits has been limited to " + total + ".";
        return out;
      }
      if (fraction != null && fractionDigitsCount > fraction.intValue()) {
        out.error = "cvc-fractionDigits-valid: Value '" + text + "' has " + fractionDigitsCount
            + " fraction digits, but the number of fraction digits has been limited to " + fraction + ".";
        return out;
      }
    }
    return out;
  }

  private boolean isDecimalFamily() {
    return effectiveLexical() == Lexical.DECIMAL || effectiveLexical() == Lexical.INTEGER;
  }

  private static String rangeError(String facet, String value, String limit, String type) {
    return "cvc-" + facet + "-valid: Value '" + value + "' is not facet-valid with respect to " + facet + " '" + limit
        + "' for type '" + type + "'.";
  }

  static BigInteger bigInt(String s) {
    return new BigInteger(s.startsWith("+") ? s.substring(1) : s);
  }
}
