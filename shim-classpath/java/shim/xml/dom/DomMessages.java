/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMException;

final class DomMessages {
  private DomMessages() {
  }

  static DOMException error(short code) {
    return new DOMException(code, name(code) + ": " + message(code));
  }

  static String name(short code) {
    switch (code) {
    case DOMException.INDEX_SIZE_ERR: return "INDEX_SIZE_ERR";
    case DOMException.DOMSTRING_SIZE_ERR: return "DOMSTRING_SIZE_ERR";
    case DOMException.HIERARCHY_REQUEST_ERR: return "HIERARCHY_REQUEST_ERR";
    case DOMException.WRONG_DOCUMENT_ERR: return "WRONG_DOCUMENT_ERR";
    case DOMException.INVALID_CHARACTER_ERR: return "INVALID_CHARACTER_ERR";
    case DOMException.NO_DATA_ALLOWED_ERR: return "NO_DATA_ALLOWED_ERR";
    case DOMException.NO_MODIFICATION_ALLOWED_ERR: return "NO_MODIFICATION_ALLOWED_ERR";
    case DOMException.NOT_FOUND_ERR: return "NOT_FOUND_ERR";
    case DOMException.NOT_SUPPORTED_ERR: return "NOT_SUPPORTED_ERR";
    case DOMException.INUSE_ATTRIBUTE_ERR: return "INUSE_ATTRIBUTE_ERR";
    case DOMException.INVALID_STATE_ERR: return "INVALID_STATE_ERR";
    case DOMException.SYNTAX_ERR: return "SYNTAX_ERR";
    case DOMException.INVALID_MODIFICATION_ERR: return "INVALID_MODIFICATION_ERR";
    case DOMException.NAMESPACE_ERR: return "NAMESPACE_ERR";
    case DOMException.INVALID_ACCESS_ERR: return "INVALID_ACCESS_ERR";
    case DOMException.VALIDATION_ERR: return "VALIDATION_ERR";
    case DOMException.TYPE_MISMATCH_ERR: return "TYPE_MISMATCH_ERR";
    default: return "UNKNOWN_ERR";
    }
  }

  static String message(short code) {
    switch (code) {
    case DOMException.INDEX_SIZE_ERR:
      return "The index or size is negative, or greater than the allowed value.";
    case DOMException.DOMSTRING_SIZE_ERR:
      return "The specified range of text does not fit into a DOMString.";
    case DOMException.HIERARCHY_REQUEST_ERR:
      return "An attempt was made to insert a node where it is not permitted.";
    case DOMException.WRONG_DOCUMENT_ERR:
      return "A node is used in a different document than the one that created it.";
    case DOMException.INVALID_CHARACTER_ERR:
      return "An invalid or illegal XML character is specified.";
    case DOMException.NO_DATA_ALLOWED_ERR:
      return "Data is specified for a node which does not support data.";
    case DOMException.NO_MODIFICATION_ALLOWED_ERR:
      return "An attempt is made to modify an object where modifications are not allowed.";
    case DOMException.NOT_FOUND_ERR:
      return "An attempt is made to reference a node in a context where it does not exist.";
    case DOMException.NOT_SUPPORTED_ERR:
      return "The implementation does not support the requested type of object or operation.";
    case DOMException.INUSE_ATTRIBUTE_ERR:
      return "An attempt is made to add an attribute that is already in use elsewhere.";
    case DOMException.INVALID_STATE_ERR:
      return "An attempt is made to use an object that is not, or is no longer, usable.";
    case DOMException.SYNTAX_ERR:
      return "An invalid or illegal string is specified.";
    case DOMException.INVALID_MODIFICATION_ERR:
      return "An attempt is made to modify the type of the underlying object.";
    case DOMException.NAMESPACE_ERR:
      return "An attempt is made to create or change an object in a way which is incorrect with regard to namespaces.";
    case DOMException.INVALID_ACCESS_ERR:
      return "A parameter or an operation is not supported by the underlying object.";
    case DOMException.VALIDATION_ERR:
      return "A call to a method such as insertBefore or removeChild would make the Node invalid with respect to "
          + "document grammar.";
    case DOMException.TYPE_MISMATCH_ERR:
      return "The value type for this parameter name is incompatible with the expected value type.";
    default:
      return "Unknown DOM error";
    }
  }
}
