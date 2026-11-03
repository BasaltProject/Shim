/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

final class HtmlDtdData {
  private HtmlDtdData() { }

  private static final String[] EMPTY_ELEMENTS = {"br", "hr", "img", "input", "meta", "link", "base", "basefont", "param",
      "area", "isindex", "frame", "wbr", "embed"};
  private static final String[] OMIT_END = {"p", "li", "dt", "dd", "tr", "td", "th", "option", "body", "html", "head",
      "thead", "tbody", "tfoot", "colgroup", "col"};
  private static final String[] OTHER = {"a", "address", "applet", "b", "big", "blockquote", "caption", "center", "cite",
      "code", "dfn", "dir", "div", "dl", "em", "font", "form", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "i", "kbd",
      "map", "menu", "nobr", "noframes", "object", "ol", "pre", "samp", "script", "select", "small", "span", "strike", "s",
      "strong", "style", "sub", "sup", "table", "textarea", "title", "tt", "u", "ul", "var"};
  private static final String LATIN1 = "nbsp iexcl cent pound curren yen brvbar sect uml copy ordf laquo not shy reg macr"
      + " deg plusmn sup2 sup3 acute micro para middot cedil sup1 ordm raquo frac14 frac12 frac34 iquest Agrave Aacute"
      + " Acirc Atilde Auml Aring AElig Ccedil Egrave Eacute Ecirc Euml Igrave Iacute Icirc Iuml ETH Ntilde Ograve Oacute"
      + " Ocirc Otilde Ouml times Oslash Ugrave Uacute Ucirc Uuml Yacute THORN szlig agrave aacute acirc atilde auml aring"
      + " aelig ccedil egrave eacute ecirc euml igrave iacute icirc iuml eth ntilde ograve oacute ocirc otilde ouml divide"
      + " oslash ugrave uacute ucirc uuml yacute thorn yuml";

  static void populate(DTD dtd) {
    for (String n : OTHER) {
      dtd.getElement(n).type = DTDConstants.ANY;
    }
    for (String n : EMPTY_ELEMENTS) {
      Element e = dtd.getElement(n);
      e.type = DTDConstants.EMPTY;
      e.oEnd = true;
    }
    for (String n : OMIT_END) {
      Element e = dtd.getElement(n);
      e.oEnd = true;
      if (n.equals("html") || n.equals("head") || n.equals("body")) {
        e.oStart = true;
      }
    }
    dtd.getElement("script").type = DTDConstants.CDATA;
    dtd.getElement("style").type = DTDConstants.CDATA;
    dtd.getElement("title").type = DTDConstants.RCDATA;
    dtd.getElement("textarea").type = DTDConstants.RCDATA;
    int g = DTDConstants.GENERAL | DTDConstants.CDATA;
    dtd.defEntity("quot", g, 34);
    dtd.defEntity("amp", g, 38);
    dtd.defEntity("lt", g, 60);
    dtd.defEntity("gt", g, 62);
    String[] names = LATIN1.split(" ");
    for (int i = 0; i < names.length; i++) {
      dtd.defEntity(names[i], g, 160 + i);
    }
  }
}
