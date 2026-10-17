/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.Comment;
import org.w3c.dom.Node;

public class CommentImpl extends CharacterDataImpl implements Comment {
  private static final long serialVersionUID = -2685736833408134044L;

  CommentImpl(DocumentImpl ownerDocument, String data) {
    super(ownerDocument, data);
  }

  public short getNodeType() {
    return Node.COMMENT_NODE;
  }

  public String getNodeName() {
    return "#comment";
  }
}
