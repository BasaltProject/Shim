/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import com.sun.imageio.plugins.tiff.TIFFImageMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Node;

public class TIFFDirectory implements Cloneable {
  private final List<TIFFTagSet> tagSets;
  private final TIFFTag parentTag;
  private final Map<Integer, TIFFField> fields = new TreeMap<Integer, TIFFField>();

  public static TIFFDirectory createFromMetadata(IIOMetadata tiffImageMetadata) throws IIOInvalidTreeException {
    if (tiffImageMetadata == null) {
      throw new NullPointerException("tiffImageMetadata == null");
    }
    if (tiffImageMetadata instanceof TIFFImageMetadata) {
      try {
        return ((TIFFImageMetadata) tiffImageMetadata).getRootIFD().clone();
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException(e);
      }
    }
    throw new IllegalArgumentException("tiffImageMetadata is not an instance of TIFFImageMetadata");
  }

  public TIFFDirectory(TIFFTagSet[] tagSets, TIFFTag parentTag) {
    if (tagSets == null) {
      throw new NullPointerException("tagSets == null!");
    }
    this.tagSets = new ArrayList<TIFFTagSet>(tagSets.length);
    for (TIFFTagSet ts : tagSets) {
      this.tagSets.add(ts);
    }
    this.parentTag = parentTag;
  }

  public TIFFTagSet[] getTagSets() { return tagSets.toArray(new TIFFTagSet[tagSets.size()]); }

  public void addTagSet(TIFFTagSet tagSet) {
    if (tagSet == null) {
      throw new NullPointerException("tagSet == null");
    }
    if (!tagSets.contains(tagSet)) {
      tagSets.add(tagSet);
    }
  }

  public void removeTagSet(TIFFTagSet tagSet) {
    if (tagSet == null) {
      throw new NullPointerException("tagSet == null");
    }
    if (tagSets.contains(tagSet)) {
      tagSets.remove(tagSet);
    }
  }

  public TIFFTag getParentTag() { return parentTag; }

  public TIFFTag getTag(int tagNumber) {
    for (TIFFTagSet tagSet : tagSets) {
      TIFFTag tag = tagSet.getTag(tagNumber);
      if (tag != null) {
        return tag;
      }
    }
    return null;
  }

  public int getNumTIFFFields() { return fields.size(); }

  public boolean containsTIFFField(int tagNumber) { return fields.containsKey(Integer.valueOf(tagNumber)); }

  public void addTIFFField(TIFFField f) {
    if (f == null) {
      throw new NullPointerException("f == null");
    }
    fields.put(Integer.valueOf(f.getTagNumber()), f);
  }

  public TIFFField getTIFFField(int tagNumber) { return fields.get(Integer.valueOf(tagNumber)); }

  public void removeTIFFField(int tagNumber) { fields.remove(Integer.valueOf(tagNumber)); }

  public TIFFField[] getTIFFFields() { return fields.values().toArray(new TIFFField[fields.size()]); }

  public void removeTIFFFields() { fields.clear(); }

  public IIOMetadata getAsMetadata() { return new TIFFImageMetadata(this); }

  public TIFFDirectory clone() throws CloneNotSupportedException {
    TIFFDirectory dir = (TIFFDirectory) super.clone();
    return copy();
  }

  private TIFFDirectory copy() throws CloneNotSupportedException {
    TIFFDirectory dir = new TIFFDirectory(getTagSets(), parentTag);
    for (TIFFField f : fields.values()) {
      dir.addTIFFField(f.clone());
    }
    return dir;
  }
}
