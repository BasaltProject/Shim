/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

final class XmlMessages {
  private XmlMessages() {
  }

  static final String kPrematureEof = "Premature end of file.";
  static final String kContentIllegalInProlog = "Content is not allowed in prolog.";
  static final String kContentIllegalInTrailingMisc = "Content is not allowed in trailing section.";
  static final String kMarkupNotRecognizedInProlog = "The markup in the document preceding the root element must be well-formed.";
  static final String kMarkupNotRecognizedInMisc = "The markup in the document following the root element must be well-formed.";
  static final String kMarkupNotRecognizedInContent = "The content of elements must consist of well-formed character data or markup.";
  static final String kMarkupEntityMismatch = "XML document structures must start and end within the same entity.";
  static final String kReferenceIllegalInProlog = "Reference is not allowed in prolog.";
  static final String kReferenceIllegalInTrailingMisc = "Reference is not allowed in trailing section.";
  static final String kDashDashInComment = "The string \"--\" is not permitted within comments.";
  static final String kReservedPiTarget = "The processing instruction target matching \"[xX][mM][lL]\" is not allowed.";
  static final String kPiTargetRequired = "The processing instruction must begin with the name of the target.";
  static final String kSpaceRequiredInPi = "White space is required between the processing instruction target and data.";
  static final String kCdEndInContent = "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.";
  static final String kNameRequiredInReference = "The entity name must immediately follow the '&' in the entity reference.";
  static final String kNameRequiredInPeReference = "The entity name must immediately follow the '%' in the parameter entity reference.";
  static final String kAlreadySeenDoctype = "Already seen doctype.";
  static final String kVersionInfoRequired = "The version is required in the XML declaration.";
  static final String kXmlDeclUnterminated = "The XML declaration must end with \"?>\".";
  static final String kDigitRequiredInCharRef = "A decimal representation must immediately follow the \"&#\" in a character reference.";
  static final String kHexdigitRequiredInCharRef = "A hexadecimal representation must immediately follow the \"&#x\" in a character reference.";
  static final String kSemicolonRequiredInCharRef = "The character reference must end with the ';' delimiter.";
  static final String kMarkupNotRecognizedInDtd = "The markup declarations contained or pointed to by the document type declaration must be well-formed.";
  static final String kRootElementRequired = "The root element is required in a well-formed document.";
  static final String kDoctypeIllegalInContent = "A DOCTYPE is not allowed in content.";

  static String eTagRequired(String name) {
    return "The element type \"" + name + "\" must be terminated by the matching end-tag \"</" + name + ">\".";
  }

  static String eTagUnterminated(String name) {
    return "The end-tag for element type \"" + name + "\" must end with a '>' delimiter.";
  }

  static String elementUnterminated(String name) {
    return "Element type \"" + name + "\" must be followed by either attribute specifications, \">\" or \"/>\".";
  }

  static String eqRequiredInAttribute(String element, String attr) {
    return "Attribute name \"" + attr + "\" associated with an element type \"" + element
        + "\" must be followed by the ' = ' character.";
  }

  static String openQuoteExpected(String element, String attr) {
    return "Open quote is expected for attribute \"" + attr + "\" associated with an  element type  \"" + element + "\".";
  }

  static String lessThanInAttValue(String element, String attr) {
    return "The value of attribute \"" + attr + "\" associated with an element type \"" + element
        + "\" must not contain the '<' character.";
  }

  static String attributeNotUnique(String element, String attr) {
    return "Attribute \"" + attr + "\" was already specified for element \"" + element + "\".";
  }

  static String entityNotDeclared(String name) {
    return "The entity \"" + name + "\" was referenced, but not declared.";
  }

  static String semicolonRequiredInReference(String name) {
    return "The reference to entity \"" + name + "\" must end with the ';' delimiter.";
  }

  static String semicolonRequiredInPeReference(String name) {
    return "The parameter entity reference \"%" + name + ";\" must end with the ';' delimiter.";
  }

  static String invalidCharRef(String digits) {
    return "Character reference \"&#" + digits + "\" is an invalid XML character.";
  }

  static String invalidChar(String where, int c) {
    return "An invalid XML character (Unicode: 0x" + Integer.toHexString(c) + ") was found in " + where + ".";
  }

  static String versionNotSupported(String v) {
    return "XML version \"" + v + "\" is not supported, only XML 1.0 is supported.";
  }

  static String sdDeclInvalid(String v) {
    return "The standalone document declaration value must be \"yes\" or \"no\", not \"" + v + "\".";
  }

  static String recursiveReference(String name, String path) {
    return "Recursive entity reference \"" + name + "\". (Reference path: " + path + "),";
  }

  static String referenceToExternalEntity(String name) {
    return "The external entity reference \"&" + name + ";\" is not permitted in an attribute value.";
  }

  static String referenceToUnparsedEntity(String name) {
    return "The unparsed entity reference \"&" + name + ";\" is not permitted.";
  }

  static String eqRequiredInXmlDecl(String name) {
    return "The ' = ' character must follow \"" + name + "\" in the XML declaration.";
  }

  static String quoteRequiredInXmlDecl(String name) {
    return "The value following \"" + name + "\" in the XML declaration must be a quoted string.";
  }

  static String encodingDeclInvalid(String name) {
    return "Invalid encoding name \"" + name + "\".";
  }

  static String doctypeUnterminated(String root) {
    return "The document type declaration for root element type \"" + root + "\" must end with '>'.";
  }

  static String doctypeNotClosed(String root) {
    return "The document type declaration for root element type \"" + root + "\" must be closed with ']'.";
  }
}
