/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class XsltModern {
  static final String NS = "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'";

  static final String BOOKS = "<lib><book id='b1' year='2001'><title>Zeta</title><price>12.50</price><tag>x</tag><tag>y</tag></book>"
      + "<book id='b2' year='1999'><title>Alpha</title><price>7</price></book>"
      + "<book id='b3' year='2010'><title>Mid</title><price>30.125</price><tag>z</tag></book></lib>";

  static String run(String name, String xsl, String xml, String... params) {
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xsl)));
      for (int i = 0; i + 1 < params.length; i += 2) {
        t.setParameter(params[i], params[i + 1]);
      }
      StringWriter w = new StringWriter();
      t.transform(new StreamSource(new StringReader(xml)), new StreamResult(w));
      return w.toString().replace("\r", "");
    } catch (Throwable e) {
      return "ERROR " + e.getClass().getSimpleName();
    }
  }

  static void show(String name, String result) {
    System.out.println(name + " => " + result.replace("\n", "\\n"));
  }

  public static void main(String[] args) {
    show("text-sort", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>"
        + "<xsl:for-each select='lib/book'><xsl:sort select='title'/><xsl:value-of select='position()'/>/<xsl:value-of select='last()'/>:<xsl:value-of select='title'/>;</xsl:for-each>"
        + "</xsl:template></xsl:stylesheet>", BOOKS));
    show("sort-number-desc", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>"
        + "<xsl:for-each select='lib/book'><xsl:sort select='price' data-type='number' order='descending'/><xsl:value-of select='@id'/> </xsl:for-each>"
        + "</xsl:template></xsl:stylesheet>", BOOKS));
    show("xml-literal", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='xml' omit-xml-declaration='yes'/><xsl:template match='/'>"
        + "<out count='{count(lib/book)}'><xsl:for-each select='lib/book'><item id='{@id}' y='{@year + 1}'><xsl:value-of select='title'/></item></xsl:for-each></out>"
        + "</xsl:template></xsl:stylesheet>", BOOKS));
    show("identity-copy", run("t", "<xsl:stylesheet " + NS + "><xsl:output omit-xml-declaration='yes'/><xsl:template match='@*|node()'><xsl:copy><xsl:apply-templates select='@*|node()'/></xsl:copy></xsl:template></xsl:stylesheet>", BOOKS));
    show("builtin-rules", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='price'>[<xsl:value-of select='.'/>]</xsl:template></xsl:stylesheet>", BOOKS));
    show("modes-priority", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/>"
        + "<xsl:template match='/'><xsl:apply-templates select='lib/book'/>|<xsl:apply-templates select='lib/book' mode='short'/></xsl:template>"
        + "<xsl:template match='book'>B(<xsl:value-of select='@id'/>)</xsl:template>"
        + "<xsl:template match='book[@year &gt; 2000]' priority='2'>N(<xsl:value-of select='@id'/>)</xsl:template>"
        + "<xsl:template match='book' mode='short'><xsl:value-of select='substring(title,1,2)'/>.</xsl:template>"
        + "</xsl:stylesheet>", BOOKS));
    show("call-template-recursion", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/>"
        + "<xsl:template match='/'><xsl:call-template name='fact'><xsl:with-param name='n' select='6'/></xsl:call-template></xsl:template>"
        + "<xsl:template name='fact'><xsl:param name='n'/><xsl:choose><xsl:when test='$n &lt;= 1'>1</xsl:when><xsl:otherwise><xsl:variable name='r'><xsl:call-template name='fact'><xsl:with-param name='n' select='$n - 1'/></xsl:call-template></xsl:variable><xsl:value-of select='$n * $r'/></xsl:otherwise></xsl:choose></xsl:template>"
        + "</xsl:stylesheet>", "<x/>"));
    show("global-param", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:param name='who' select=\"'nobody'\"/><xsl:variable name='greet' select=\"concat('hi ', $who)\"/>"
        + "<xsl:template match='/'><xsl:value-of select='$greet'/></xsl:template></xsl:stylesheet>", "<x/>", "who", "bob"));
    show("global-param-default", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:param name='who' select=\"'nobody'\"/>"
        + "<xsl:template match='/'><xsl:value-of select='$who'/></xsl:template></xsl:stylesheet>", "<x/>"));
    show("key", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:key name='byid' match='book' use='@id'/>"
        + "<xsl:template match='/'><xsl:value-of select=\"key('byid','b2')/title\"/></xsl:template></xsl:stylesheet>", BOOKS));
    show("if-choose-position", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>"
        + "<xsl:for-each select='lib/book'><xsl:if test='position() != 1'>,</xsl:if><xsl:choose><xsl:when test='price &gt; 20'>H</xsl:when><xsl:when test='price &gt; 10'>M</xsl:when><xsl:otherwise>L</xsl:otherwise></xsl:choose></xsl:for-each>"
        + "</xsl:template></xsl:stylesheet>", BOOKS));
    show("element-attribute", run("t", "<xsl:stylesheet " + NS + "><xsl:output omit-xml-declaration='yes'/><xsl:template match='/'>"
        + "<xsl:element name='r{count(lib/book)}'><xsl:attribute name='a'>v<xsl:value-of select='1+1'/></xsl:attribute><xsl:text>&lt;t&gt;</xsl:text></xsl:element>"
        + "</xsl:template></xsl:stylesheet>", BOOKS));
    show("copy-of", run("t", "<xsl:stylesheet " + NS + "><xsl:output omit-xml-declaration='yes'/><xsl:template match='/'><w><xsl:copy-of select='lib/book[2]'/><xsl:copy-of select='count(lib/book)'/></w></xsl:template></xsl:stylesheet>", BOOKS));
    show("number", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'><xsl:for-each select='lib/book'><xsl:number format='1.'/>,</xsl:for-each></xsl:template></xsl:stylesheet>", BOOKS));
    show("number-mixed-names", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'><xsl:for-each select='l/*'><xsl:number/>-</xsl:for-each></xsl:template></xsl:stylesheet>", "<l><b/><b/><c/><b/></l>"));
    show("number-any", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'><xsl:for-each select='//b'><xsl:number level='any'/>;</xsl:for-each></xsl:template></xsl:stylesheet>", "<l><b/><b/><c/><b><d/></b></l>"));
    show("format-number", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>"
        + "<xsl:value-of select=\"format-number(1234.5, '#,##0.00')\"/>|<xsl:value-of select=\"format-number(0.256, '0.0%')\"/>|<xsl:value-of select=\"format-number(-7, '000')\"/></xsl:template></xsl:stylesheet>", "<x/>"));
    show("strip-space", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:strip-space elements='*'/><xsl:template match='/'><xsl:value-of select='count(//text())'/></xsl:template></xsl:stylesheet>", "<a> <b>t</b> <c/> </a>"));
    show("generate-id", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'><xsl:value-of select='generate-id(lib/book[1]) = generate-id(lib/book[1])'/><xsl:value-of select='generate-id(lib/book[1]) = generate-id(lib/book[2])'/></xsl:template></xsl:stylesheet>", BOOKS));
    show("attribute-set", run("t", "<xsl:stylesheet " + NS + "><xsl:output omit-xml-declaration='yes'/><xsl:attribute-set name='s'><xsl:attribute name='k'>v</xsl:attribute></xsl:attribute-set><xsl:template match='/'><e xsl:use-attribute-sets='s'/></xsl:template></xsl:stylesheet>", "<x/>"));
    show("html-output", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='html'/><xsl:template match='/'><html><body><p>a<br/>b</p></body></html></xsl:template></xsl:stylesheet>", "<x/>"));
    show("xml-declaration", run("t", "<xsl:stylesheet " + NS + "><xsl:template match='/'><r>é</r></xsl:template></xsl:stylesheet>", "<x/>"));
    show("indent", run("t", "<xsl:stylesheet " + NS + "><xsl:output indent='yes' omit-xml-declaration='yes'/><xsl:template match='/'><r><a>1</a><b><c/></b></r></xsl:template></xsl:stylesheet>", "<x/>"));
    show("string-functions", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>"
        + "<xsl:value-of select=\"concat(translate('abc','ab','XY'), '|', normalize-space('  a   b '), '|', string-length('hello'), '|', substring-before('a-b','-'), '|', contains('abc','bc'), '|', sum(lib/book/price))\"/></xsl:template></xsl:stylesheet>", BOOKS));
    show("variable-rtf", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:variable name='v'><a>1</a><a>2</a></xsl:variable><xsl:template match='/'><xsl:value-of select='$v'/>|<xsl:copy-of select='$v'/></xsl:template></xsl:stylesheet>", "<x/>"));
    show("import-include-missing", run("t", "<xsl:stylesheet " + NS + "><xsl:include href='nonexistent.xsl'/></xsl:stylesheet>", "<x/>"));
    show("bad-stylesheet", run("t", "<notxsl/>", "<x/>"));
    show("bad-expression", run("t", "<xsl:stylesheet " + NS + "><xsl:template match='/'><xsl:value-of select='((('/></xsl:template></xsl:stylesheet>", "<x/>"));
    show("whitespace-text", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'>  <xsl:text>  a  </xsl:text>  <xsl:value-of select=\"'b'\"/>  </xsl:template></xsl:stylesheet>", "<x/>"));
    show("namespaces", run("t", "<xsl:stylesheet " + NS + " xmlns:p='urn:p'><xsl:output omit-xml-declaration='yes'/><xsl:template match='/'><p:r xmlns:q='urn:q'><p:c/></p:r></xsl:template></xsl:stylesheet>", "<x/>"));
    show("apply-templates-with-param", run("t", "<xsl:stylesheet " + NS + "><xsl:output method='text'/><xsl:template match='/'><xsl:apply-templates select='lib/book[1]'><xsl:with-param name='p' select=\"'P'\"/></xsl:apply-templates></xsl:template><xsl:template match='book'><xsl:param name='p'>default</xsl:param><xsl:value-of select='$p'/>:<xsl:value-of select='@id'/></xsl:template></xsl:stylesheet>", BOOKS));
    show("comment-pi", run("t", "<xsl:stylesheet " + NS + "><xsl:output omit-xml-declaration='yes'/><xsl:template match='/'><r><xsl:comment>note</xsl:comment><xsl:processing-instruction name='go'>now</xsl:processing-instruction></r></xsl:template></xsl:stylesheet>", "<x/>"));
  }
}
