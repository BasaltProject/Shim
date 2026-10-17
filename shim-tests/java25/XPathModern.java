/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathNodes;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XPathModern {
  static void p(String l, Object o) {
    System.out.println(l + "=" + o);
  }

  static String show(Object r) {
    if (r instanceof NodeList) {
      NodeList nl = (NodeList) r;
      StringBuilder sb = new StringBuilder("[" + nl.getLength() + ":");
      for (int i = 0; i < nl.getLength(); i++) {
        sb.append(nl.item(i).getNodeName()).append(',');
      }
      return sb.append(']').toString();
    }
    if (r instanceof Node) {
      return "node:" + ((Node) r).getNodeName();
    }
    return String.valueOf(r);
  }

  static String fail(Throwable x) {
    if (x instanceof XPathExpressionException) {
      return "!X:" + String.valueOf(x.getMessage()).replace("\n", " ");
    }
    return "!" + x.getClass().getName() + ":" + x.getMessage();
  }

  static void run(XPath xp, Node ctx, String[] exprs, QName[] types) {
    for (String e : exprs) {
      StringBuilder sb = new StringBuilder(e + " => ");
      for (QName t : types) {
        try {
          sb.append(show(xp.evaluate(e, ctx, t)));
        } catch (Throwable x) {
          sb.append(fail(x));
          break;
        }
        sb.append(" | ");
      }
      System.out.println(sb);
    }
  }

  static final String[] kSimple = {
    "count()",
    "count(1)",
    "count(1,2)",
    "string(1,2)",
    "concat('a')",
    "substring('a')",
    "substring('a',1,2,3)",
    "starts-with('a')",
    "not()",
    "true(1)",
    "position(1)",
    "last(1)",
    "sum('a')",
    "translate('a','b')",
    "id()",
    "lang()",
    "local-name(1)",
    "name(1,2)",
    "number(1,2)",
    "floor()",
    "1 +",
    "+ 1",
    "(1",
    "1)",
    "a[1",
    "a]",
    "a/",
    "/ /",
    "a b",
    "@",
    "child::",
    "foo::a",
    "a::b",
    "comment(1)",
    "text('a')",
    "processing-instruction(1)",
    "processing-instruction('a','b')",
    "@@a",
    "1 = ",
    "= 1",
    "a | ",
    "| a",
    "$a b",
    "1 2 3",
    "'a' 'b'",
    "a !",
    "!a",
    "#",
    "1.2.3",
    ".5",
    "5.",
    "1..",
    "p:",
    "p:*",
    "*",
    "* *",
    "* * *",
    "a/*",
    "div",
    "div div div",
    "and",
    "a and",
    "mod mod mod",
    "- - 1",
    "--1",
    "- -1",
    "1 - - 1",
    "a[]",
    "a[1][2]",
    "(a)(b)",
    "(//a)[1]",
    "$x/a",
    "a/(b)",
    "a/1",
    "ancestor::*",
    "p:foo()",
    "p:foo(1)",
    "f:foo()",
    "string(.)",
    "string(..)",
    ".",
    "..",
    "./a",
    "../a",
    "self::node()",
    "node()/text()",
    "//",
    "a//",
    "//a//",
    "/..",
    "/.",
    "/*",
    "/p:*",
    "p:r",
    "/r/p:*",
    "id('x')",
    "1 and 2",
    "'' or 0",
    "a or b",
    "(1 = 2) = false()",
    "'1' = 1",
    "'a' < 'b'",
    "'a' = 'a' = true()",
    "-'3'",
    "- 'a'",
    "2 - -2",
    "string(0 div 0)",
    "string(-1 div 0)",
    "number(-0)",
    "string(number('-0'))",
    "number('+1')",
    "number('1e1')",
    "number('.5')",
    "number('5.')",
    "number('- 1')",
    "number('1 2')",
    "number(true())",
    "string(1.0)",
    "string(-1.50)",
    "string(100000000000000000000)",
    "string(0.1 + 0.7)",
    "string(1 div 3 * 3)",
    "string(12345678901234567890)",
    "string(0.00000001)",
    "string(1.5e0)",
    "round(2.5)",
    "round(-2.5)",
    "round(1 div 0)",
    "round(0 div 0)",
    "floor(-0)",
    "ceiling(0.2)",
    "substring('12345', 0)",
    "substring('12345', -1, 3)",
    "substring('12345', 2)",
    "substring('12345', 1 div 0)",
    "substring('12345', 0 div 0)",
    "substring('12345', 2, 1 div 0)",
    "substring('12345', -1 div 0, 1 div 0)",
    "string-length()",
    "string-length(/r)",
    "normalize-space()",
    "normalize-space(//a)",
    "translate('abcabc','abc','AB')",
    "translate('abc','aab','xyz')",
    "substring-before('abc','')",
    "substring-after('abc','')",
    "substring-after('abc','z')",
    "contains('abc','')",
    "starts-with('abc','')",
    "concat('a',1,true())",
    "boolean('false')",
    "boolean(0 div 0)",
    "boolean(-0)",
    "boolean(//zz)",
    "boolean(/)",
    "not('')",
    "lang('')",
    "sum(//zz)",
    "sum(//@n) + sum(//a)",
    "count(/)",
    "count(//node())",
    "count(//*|//@*)",
    "count(//a | //a)",
    "name(//@n)",
    "name(/)",
    "name(//text())",
    "name(//namespace::*[1])",
    "local-name(//@n)",
    "namespace-uri(/*)",
    "namespace-uri(//@n)",
    "name(//a/..)",
    "name(/..)",
    "name(.)",
    "name()",
    "local-name()",
    "namespace-uri()",
    "string-length(name(//comment()))",
    "//a/text()",
    "//a/node()",
    "//a/@n",
    "//@n/..",
    "//@n/parent::*",
    "//a/attribute::*",
    "//a/attribute::node()",
    "//a/self::a",
    "//a/self::b",
    "//a/descendant-or-self::node()",
    "//text()/ancestor::*",
    "//text()/ancestor-or-self::*",
    "(//a)[position()=1]",
    "(//a | //text())[last()]",
    "//a[1][1]",
    "//a[@n][1]",
    "//a[2]",
    "//a[0]",
    "//a[1.5]",
    "//a['x']",
    "//a[true()]",
    "//a[last()]",
    "//a[position()=last()]",
    "//a[./@n=1]",
    "//a[@n=1 or @n=2]",
    "//*[name()='a']",
    "//*[local-name()='a']",
    "//*[namespace-uri()='u']",
    "//p:*",
    "/r/p:a",
    "//*[self::a]",
    "//a[text()='t']",
    "//a[.='t']",
    "//a[string(.)='t']",
    "//@*[.=1]",
    "//@*[name()='n']",
    "count(//@*) = 1",
    "count(//@*) = '1'",
    "//a/@n = //a",
    "//a/@n = 1",
    "//a/@n != 2",
    "//a/@n != 1",
    "//zz = //zz",
    "//zz != //zz",
    "//zz = 1",
    "//zz = ''",
    "//zz = true()",
    "//zz = false()",
    "/ = /",
    "/ = 't'",
    "/r = 't'",
    "//a < 2",
    "//a > 0",
    "//a <= 'x'",
    "1 < //a",
    "//@n >= 1",
    "//@n > //@n",
    "//@n >= //@n"
  };

  static final String[] kRich = {
    "/r/a",
    "count(//a)",
    "string(/r/a[2])",
    "sum(//@n)",
    "//p:c + 1",
    "/r/a[last()]/@n",
    "//a[1] | //p:c",
    "name(/*)",
    "1 div 0",
    "-1 div 0",
    "0 div 0",
    "0.1 + 0.2",
    "string(1 div 3)",
    "round(-0.5)",
    "floor(2.5)",
    "substring('12345', 1.5, 2.6)",
    "translate('abc','ab','X')",
    "normalize-space('  a   b ')",
    "concat('a','b','c')",
    "boolean(//zz)",
    "not(1)",
    "'a' = 'a'",
    "//a = 't1'",
    "//a/@n > 1",
    "string-length('héllo')",
    "lang('en')",
    "id('x')",
    "local-name(//p:c)",
    "namespace-uri(//p:c)",
    "//comment()",
    "//processing-instruction()",
    "//processing-instruction('pi')",
    "//text()",
    "/r/node()",
    "count(/r/*)",
    "/r/a[position() = 2]/preceding-sibling::*",
    "//b/ancestor::*",
    "//b/ancestor-or-self::node()",
    "/r/a[1]/following::*",
    "//b/preceding::*",
    "//@*",
    "count(//namespace::*)",
    "foo()",
    "1 +",
    "//",
    "a[",
    "/r/a[",
    "1 2",
    "'unterminated",
    "p:c",
    "q:c",
    "/r/@id",
    "string(/r/@id)",
    "number('abc')",
    "number(' 12 ')",
    "true() and false()",
    "1 = 1 and 2 = 2",
    "(1 + 2) * 3 mod 4",
    "5 mod 2",
    "-5 mod 2",
    "5 div 2",
    "string(true())",
    "string(-0)",
    "ceiling(-0.5)",
    "count(//*[@n])",
    "//a[@n=2]/b",
    "/descendant::a[1]",
    "/r/child::a",
    "/r/self::r",
    "//a[not(@n=1)]",
    "string(//a)",
    "starts-with('abc','ab')",
    "contains('abc','b')",
    "substring-before('a-b','-')",
    "substring-after('a-b','-')",
    "string(1e20)",
    "string(0.000001)",
    "string(123456789.123)",
    "2 * 3 * 4",
    "10 - 2 - 3",
    "8 div 2 div 2",
    "1 < 2 < 3",
    "3 > 2 > 1"
  };

  static final String[] kVars = {
    "$one + 1",
    "$s",
    "concat($s,'-',$b)",
    "$n",
    "name($n)",
    "count($l)",
    "$d",
    "$undefinedvar",
    "$x:v",
    "count(//x:item)",
    "//x:item[@id='i2']/@v",
    "sum(//x:item/@v)",
    "string(//x:item[2])",
    "string-length(//x:item[2])",
    "//x:item[2]/text()",
    "count(//x:item[2]/text())",
    "count(//text())",
    "count(//comment())",
    "count(/comment())",
    "string(/comment())",
    "name(//processing-instruction())",
    "string(//processing-instruction('proc'))",
    "count(//processing-instruction('none'))",
    "normalize-space(//plain)",
    "string-length(//plain)",
    "lang('en')",
    "lang('EN')",
    "lang('en-us')",
    "lang('fr')",
    "count(//*[lang('en')])",
    "name(//x:item[1]/@id)",
    "name(//@xml:lang)",
    "string(//@xml:lang)",
    "count(//@*)",
    "count(//namespace::*)",
    "count(/root/namespace::*)",
    "count(//x:item[1]/namespace::*)",
    "name(/root/namespace::*[1])",
    "name(//empty/following-sibling::*)",
    "count(//x:item[1]/following::*)",
    "count(//plain/preceding::*)",
    "count(//plain/preceding-sibling::*)",
    "count(//plain/following-sibling::node())",
    "name(//plain/preceding-sibling::*[1])",
    "name(//plain/preceding-sibling::*[2])",
    "name((//plain/preceding-sibling::*)[1])",
    "count(//x:item[position() > 1])",
    "x:up(1,'a',true(),//x:item)",
    "q:up()",
    "q:nope(1)",
    "up(1)",
    "count(id('i1'))",
    "string(id('i1 i2')[2]/@v)",
    "round(2.4) + round(2.6) + round(-2.6)",
    "floor(-1.5) + ceiling(-1.5)",
    "10 mod 3 + 10 mod -3 + -10 mod 3",
    "7 div 2",
    "string(7 div 2)",
    "string(1 div 7)",
    "string(100 div 3)",
    "string(1234567890123456789012)",
    "string(0.5 - 0.25)",
    "string(-0.0001)",
    "string(3.0E0)",
    "number('')",
    "number('  7.5  ')",
    "string(number('x'))",
    "boolean('0')",
    "boolean(0.0)",
    "boolean(1 div 0)",
    "true() = 'x'",
    "0 = false()",
    "'abc' = 'abc'",
    "1 = '1.0'",
    "'1' = '1.0'",
    "//x:item/@v > 15",
    "//x:item/@v < 15",
    "//x:item/@v = 20",
    "//x:item/@v != 10",
    "(//x:item)[1]/@v + (//x:item)[2]/@v",
    "string(/root/@x + 1)",
    "string(/root/@nonexistent + 1)",
    "count(/root/*[self::plain or self::empty])",
    "count(/root/*[not(self::plain)])",
    "name(/root/*[last()])",
    "name(/root/*[last()-1])",
    "name(/root/node()[3])",
    "count(/root/node())",
    "count(/root//node())",
    "count(//node())",
    "count(/descendant::*)",
    "count(/descendant-or-self::*)",
    "count(//empty/ancestor::*)",
    "count(//empty/ancestor-or-self::node())",
    "name(//empty/..)",
    "name(//empty/parent::node())",
    "count(//empty/parent::x:*)",
    "count(//*[starts-with(name(), 'x:')])",
    "count(//*[contains(local-name(), 'e')])",
    "substring-before(name(//x:item[1]), ':')",
    "substring-after(name(//x:item[1]), ':')",
    "translate(string(//plain), ' ', '_')",
    "string(/)",
    "string(/*)",
    "string(/root/x:item[2])",
    "string(/root/x:item[2]/text()[2])",
    "string(//empty)",
    "string-length(string(//empty))",
    "string(/root/@*)",
    "string(//x:item/@*)",
    "count(//x:item/@*[. > 5])",
    "count(//x:item[@v > 15 and @id = 'i2'])",
    "count(//x:item[@v > 15 or @id = 'i1'])",
    "count(//x:item[not(@v > 15)])",
    "//x:item[@v > 15]/@id",
    "string(//x:item[@v > 15]/@id)",
    "count(//x:item/..)",
    "count((//x:item | //plain | //x:item)[position() < 3])",
    "name((//x:item | //plain)[last()])",
    "count(//x:item/self::x:item)",
    "count(//x:item/self::*)",
    "count(//x:item/self::node())",
    "count(//x:item/self::text())",
    "count(//x:item/text() | //x:item/node())",
    "count(/root/x:*)",
    "count(/root/*)",
    "count(/root/x:item/following-sibling::x:item)",
    "count(/root/x:item/preceding-sibling::x:item)",
    "name(/root/x:item[2]/preceding-sibling::*)",
    "count(//x:item[1]/following-sibling::*[1])",
    "name(//x:item[1]/following-sibling::*[1])",
    "name(//x:item[1]/following-sibling::*[2])"
  };

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    QName[] all = {XPathConstants.STRING, XPathConstants.NUMBER, XPathConstants.BOOLEAN, XPathConstants.NODESET};
    QName[] three = {XPathConstants.STRING, XPathConstants.NUMBER, XPathConstants.BOOLEAN};

    Document d1 = f.newDocumentBuilder().parse(new InputSource(new StringReader(
        "<r xmlns:p='u' id='x'><a n='1'>t1</a><a n='2'><b/>t2</a><p:c>5</p:c><!--cm--><?pi data?></r>")));
    XPath xp1 = XPathFactory.newInstance().newXPath();
    xp1.setNamespaceContext(new NamespaceContext() {
      public String getNamespaceURI(String p) {
        return p.equals("p") ? "u" : p.equals("xml") ? XMLConstants.XML_NS_URI : XMLConstants.NULL_NS_URI;
      }

      public String getPrefix(String u) {
        return null;
      }

      public Iterator<String> getPrefixes(String u) {
        return null;
      }
    });
    run(xp1, d1, kSimple, all);

    Document d2 = f.newDocumentBuilder().parse(new InputSource(new StringReader("<r xmlns:p='u'><a n='1'>t</a></r>")));
    run(XPathFactory.newInstance().newXPath(), d2, kRich, all);

    Document d = f.newDocumentBuilder().parse(new InputSource(new StringReader(
        "<?xml version='1.0'?><!-- top --><root xmlns:n='urn:n' xml:lang='en-US' x='1'><n:item id='i1' v='10'>A</n:item>"
        + "<n:item id='i2' v='20'>B<![CDATA[C]]></n:item><plain>  sp  ace  </plain><?proc ess?><empty/></root>")));
    XPath xp = XPathFactory.newInstance().newXPath();
    final Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("one", 1);
    vars.put("s", "A");
    vars.put("b", Boolean.TRUE);
    vars.put("n", d.getDocumentElement());
    vars.put("l", d.getElementsByTagName("*"));
    vars.put("d", 2.5);
    xp.setXPathVariableResolver(new XPathVariableResolver() {
      public Object resolveVariable(QName q) {
        System.out.println("resolve " + q);
        return vars.get(q.getLocalPart());
      }
    });
    xp.setNamespaceContext(new NamespaceContext() {
      public String getNamespaceURI(String p) {
        return p.equals("x") ? "urn:n" : p.equals("xml") ? XMLConstants.XML_NS_URI : p.equals("q") ? "urn:q"
            : XMLConstants.NULL_NS_URI;
      }

      public String getPrefix(String u) {
        return null;
      }

      public Iterator<String> getPrefixes(String u) {
        return null;
      }
    });
    xp.setXPathFunctionResolver(new XPathFunctionResolver() {
      public XPathFunction resolveFunction(QName n, int arity) {
        System.out.println("fn " + n + "/" + arity);
        if (!n.getLocalPart().equals("up")) {
          return null;
        }
        return new XPathFunction() {
          @SuppressWarnings("rawtypes")
          public Object evaluate(List args) throws XPathFunctionException {
            StringBuilder sb = new StringBuilder();
            for (Object o : args) {
              sb.append(o instanceof NodeList ? "NL" + ((NodeList) o).getLength() : o.getClass().getSimpleName() + ":" + o)
                  .append(';');
            }
            return sb.toString();
          }
        };
      }
    });
    run(xp, d, kVars, three);
    XPathExpression ce = xp.compile("count(//x:item) + $one");
    p("compiled", ce.evaluate(d) + " " + ce.evaluate(d, XPathConstants.NUMBER) + " " + ce.evaluate(d.getDocumentElement())
        + " " + ce.evaluate(new InputSource(new StringReader("<a/>"))));
    xp.reset();
    p("reset ns", xp.getNamespaceContext() + " " + xp.getXPathVariableResolver() + " " + xp.getXPathFunctionResolver());
    try {
      p("no ns ctx", xp.evaluate("count(//x:item)", d));
    } catch (XPathExpressionException e) {
      p("nsx", e.getMessage());
    }
    try {
      p("dom ns", xp.evaluate("count(//n:item)", d));
    } catch (XPathExpressionException e) {
      p("nsx", e.getMessage());
    }
    try {
      xp.compile("count(//x:item)");
      System.out.println("compile ok");
    } catch (XPathExpressionException e) {
      p("compile ns", e.getMessage());
    }
    try {
      xp.setNamespaceContext(null);
    } catch (NullPointerException e) {
      p("npe ns", e.getMessage());
    }
    try {
      xp.setXPathVariableResolver(null);
    } catch (NullPointerException e) {
      p("npe var", e.getMessage());
    }
    try {
      xp.setXPathFunctionResolver(null);
    } catch (NullPointerException e) {
      p("npe fn", e.getMessage());
    }
    Document nd = f.newDocumentBuilder().newDocument();
    Element r = nd.createElement("r");
    nd.appendChild(r);
    r.appendChild(nd.createElement("c"));
    r.appendChild(nd.createTextNode("tx"));
    p("built", xp.evaluate("count(/r/*)", nd) + " " + xp.evaluate("/r", nd) + " " + xp.evaluate("count(c)", r) + " "
        + xp.evaluate("name(..)", r.getFirstChild()) + " " + xp.evaluate("count(/*)", r.getFirstChild()));
    Node nn = (Node) xp.evaluate("/r/c", nd, XPathConstants.NODE);
    p("node", nn.getNodeName() + " " + (nn == r.getFirstChild()));
    p("node none", xp.evaluate("/r/zz", nd, XPathConstants.NODE));
    NodeList nl = (NodeList) xp.evaluate("//*", nd, XPathConstants.NODESET);
    p("nodeset", nl.getLength() + " " + nl.item(0).getNodeName() + " " + nl.item(1).getNodeName() + " " + nl.item(2));
    XPathNodes xn = xp.evaluateExpression("//*", nd, XPathNodes.class);
    p("xnodes", xn.size() + " " + xn.get(0).getNodeName());
    int cnt = 0;
    for (Node n2 : xn) {
      cnt++;
    }
    p("iter", cnt);
    try {
      xn.get(5);
    } catch (Exception e) {
      p("xnodes oob", e.getClass().getName() + ":" + e.getMessage());
    }
    p("exprs", xp.evaluateExpression("count(//*)", nd, Integer.class) + " " + xp.evaluateExpression("count(//*)", nd, Long.class)
        + " " + xp.evaluateExpression("count(//*)", nd, Double.class) + " " + xp.evaluateExpression("name(/*)", nd, String.class)
        + " " + xp.evaluateExpression("/r", nd, Node.class).getNodeName() + " " + xp.evaluateExpression("/zz", nd, Node.class)
        + " " + xp.evaluateExpression("1 > 0", nd, Boolean.class));
    XPathEvaluationResult<?> er = xp.evaluateExpression("'s'", nd);
    p("er", er.type() + " " + er.value());
    er = xp.evaluateExpression("true()", nd);
    p("er", er.type() + " " + er.value());
    er = xp.evaluateExpression("/r", nd);
    p("er", er.type());
    p("qnames", XPathEvaluationResult.XPathResultType.getQNameType(String.class) + " "
        + XPathEvaluationResult.XPathResultType.getQNameType(Node.class) + " "
        + XPathEvaluationResult.XPathResultType.getQNameType(Integer.class));
    XPathFactory xf = XPathFactory.newInstance();
    xf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    p("secure", xf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    try {
      xf.getFeature("nope");
    } catch (Exception e) {
      p("getFeature", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.setFeature("nope", true);
    } catch (Exception e) {
      p("setFeature", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.setFeature(null, true);
    } catch (Exception e) {
      p("setFeature null", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.getFeature(null);
    } catch (Exception e) {
      p("getFeature null", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.isObjectModelSupported(null);
    } catch (Exception e) {
      p("model null", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.isObjectModelSupported("");
    } catch (Exception e) {
      p("model empty", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.setXPathVariableResolver(null);
    } catch (Exception e) {
      p("fvar null", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xf.setXPathFunctionResolver(null);
    } catch (Exception e) {
      p("ffn null", e.getClass().getName() + ":" + e.getMessage());
    }
    xf.setXPathVariableResolver(new XPathVariableResolver() {
      public Object resolveVariable(QName q) {
        return "fv";
      }
    });
    p("factory var", xf.newXPath().evaluate("$z", nd));
    XPath x2 = xf.newXPath();
    x2.setXPathVariableResolver(new XPathVariableResolver() {
      public Object resolveVariable(QName q) {
        return "own";
      }
    });
    p("own var", x2.evaluate("$z", nd));
    x2.reset();
    p("reset var", x2.evaluate("$z", nd));
    p("newDefault", XPathFactory.newDefaultInstance().newXPath().evaluate("1+1", nd));
    p("source", xp.evaluate("/r/a", new InputSource(new StringReader("<r><a>from source</a></r>"))));
    try {
      xp.evaluate("/r", new InputSource(new StringReader("<r")));
    } catch (XPathExpressionException e) {
      p("bad source", e.getMessage());
    }
    try {
      xp.evaluate("/r", d, new QName("x"));
    } catch (Exception e) {
      p("bad type", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xp.evaluate("/r", d, (QName) null);
    } catch (Exception e) {
      p("null type", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xp.evaluate(null, d);
    } catch (Exception e) {
      p("null expr", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      xp.evaluate("/r", (InputSource) null);
    } catch (Exception e) {
      p("null source", e.getClass().getName() + ":" + e.getMessage());
    }
    try {
      p("null item", xp.evaluate("/r/a", (Object) null));
    } catch (Exception e) {
      p("null item", fail(e));
    }
    try {
      p("null item const", xp.evaluate("1+1", (Object) null));
    } catch (Exception e) {
      p("null item const", fail(e));
    }
    try {
      p("null item count", xp.evaluate("count(/*)", (Object) null));
    } catch (Exception e) {
      p("null item count", fail(e));
    }
  }
}
