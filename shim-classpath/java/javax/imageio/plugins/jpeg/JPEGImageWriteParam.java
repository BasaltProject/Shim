/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.jpeg;

import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class JPEGImageWriteParam extends ImageWriteParam {
  private JPEGQTable[] qTables = null;
  private JPEGHuffmanTable[] DCHuffmanTables = null;
  private JPEGHuffmanTable[] ACHuffmanTables = null;
  private boolean optimizeHuffman = false;
  private String[] compressionNames = {"JPEG"};
  private float[] qualityVals = {0.00F, 0.30F, 0.75F, 1.00F};
  private String[] qualityDescs = {"Low quality", "Medium quality", "Visually lossless"};

  public JPEGImageWriteParam(Locale locale) {
    super(locale);
    this.canWriteProgressive = true;
    this.progressiveMode = MODE_DISABLED;
    this.canWriteCompressed = true;
    this.compressionTypes = compressionNames;
    this.compressionType = compressionTypes[0];
    this.compressionQuality = 0.75F;
  }

  public void unsetCompression() {
    if (getCompressionMode() != MODE_EXPLICIT) {
      throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
    }
    this.compressionQuality = 0.75F;
  }

  public boolean isCompressionLossless() {
    if (getCompressionMode() != MODE_EXPLICIT) {
      throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
    }
    return false;
  }

  public String[] getCompressionQualityDescriptions() {
    if (getCompressionMode() != MODE_EXPLICIT) {
      throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
    }
    if ((getCompressionTypes() != null) && (getCompressionType() == null)) {
      throw new IllegalStateException("No compression type set!");
    }
    return qualityDescs.clone();
  }

  public float[] getCompressionQualityValues() {
    if (getCompressionMode() != MODE_EXPLICIT) {
      throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
    }
    if ((getCompressionTypes() != null) && (getCompressionType() == null)) {
      throw new IllegalStateException("No compression type set!");
    }
    return qualityVals.clone();
  }

  public boolean areTablesSet() { return (qTables != null); }

  public void setEncodeTables(JPEGQTable[] qTables, JPEGHuffmanTable[] DCHuffmanTables,
      JPEGHuffmanTable[] ACHuffmanTables) {
    if ((qTables == null) || (DCHuffmanTables == null) || (ACHuffmanTables == null) || (qTables.length > 4)
        || (DCHuffmanTables.length > 4) || (ACHuffmanTables.length > 4)
        || (DCHuffmanTables.length != ACHuffmanTables.length)) {
      throw new IllegalArgumentException("Invalid JPEG table arrays");
    }
    this.qTables = qTables.clone();
    this.DCHuffmanTables = DCHuffmanTables.clone();
    this.ACHuffmanTables = ACHuffmanTables.clone();
  }

  public void unsetEncodeTables() {
    this.qTables = null;
    this.DCHuffmanTables = null;
    this.ACHuffmanTables = null;
  }

  public JPEGQTable[] getQTables() { return (qTables != null) ? qTables.clone() : null; }

  public JPEGHuffmanTable[] getDCHuffmanTables() { return (DCHuffmanTables != null) ? DCHuffmanTables.clone() : null; }

  public JPEGHuffmanTable[] getACHuffmanTables() { return (ACHuffmanTables != null) ? ACHuffmanTables.clone() : null; }

  public void setOptimizeHuffmanTables(boolean optimize) { optimizeHuffman = optimize; }

  public boolean getOptimizeHuffmanTables() { return optimizeHuffman; }
}
