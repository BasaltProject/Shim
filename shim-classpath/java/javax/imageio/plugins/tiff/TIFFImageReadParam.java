/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

public final class TIFFImageReadParam extends javax.imageio.ImageReadParam {
  private java.util.List<javax.imageio.plugins.tiff.TIFFTagSet> allowedTagSets = new java.util.ArrayList<>(4);
  private boolean readUnknownTags = false;

  public TIFFImageReadParam() {
    addAllowedTagSet(javax.imageio.plugins.tiff.BaselineTIFFTagSet.getInstance());
    addAllowedTagSet(javax.imageio.plugins.tiff.FaxTIFFTagSet.getInstance());
    addAllowedTagSet(javax.imageio.plugins.tiff.ExifParentTIFFTagSet.getInstance());
    addAllowedTagSet(javax.imageio.plugins.tiff.GeoTIFFTagSet.getInstance());
  }

  public void addAllowedTagSet(javax.imageio.plugins.tiff.TIFFTagSet tagSet) {
    if (tagSet == null) {
      throw new IllegalArgumentException("tagSet == null!");
    }
    if (!allowedTagSets.contains(tagSet)) {
      allowedTagSets.add(tagSet);
    }
  }

  public void removeAllowedTagSet(javax.imageio.plugins.tiff.TIFFTagSet tagSet) {
    if (tagSet == null) {
      throw new IllegalArgumentException("tagSet == null!");
    }
    allowedTagSets.remove(tagSet);
  }

  public java.util.List<javax.imageio.plugins.tiff.TIFFTagSet> getAllowedTagSets() { return allowedTagSets; }

  public void setReadUnknownTags(boolean readUnknownTags) { this.readUnknownTags = readUnknownTags; }

  public boolean getReadUnknownTags() { return readUnknownTags; }
}
