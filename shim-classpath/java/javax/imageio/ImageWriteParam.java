/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

import java.awt.Dimension;
import java.util.Locale;

public class ImageWriteParam extends IIOParam {
  public static final int MODE_DISABLED = 0;
  public static final int MODE_DEFAULT = 1;
  public static final int MODE_EXPLICIT = 2;
  public static final int MODE_COPY_FROM_METADATA = 3;

  protected boolean canWriteTiles = false;
  protected int tilingMode = MODE_COPY_FROM_METADATA;
  protected Dimension[] preferredTileSizes = null;
  protected boolean tilingSet = false;
  protected int tileWidth = 0;
  protected int tileHeight = 0;
  protected boolean canOffsetTiles = false;
  protected int tileGridXOffset = 0;
  protected int tileGridYOffset = 0;
  protected boolean canWriteProgressive = false;
  protected int progressiveMode = MODE_COPY_FROM_METADATA;
  protected boolean canWriteCompressed = false;
  protected int compressionMode = MODE_COPY_FROM_METADATA;
  protected String[] compressionTypes = null;
  protected String compressionType = null;
  protected float compressionQuality = 1.0F;
  protected Locale locale = null;

  protected ImageWriteParam() { }

  public ImageWriteParam(Locale locale) { this.locale = locale; }

  public Locale getLocale() { return locale; }

  private static boolean validMode(int mode) { return mode >= MODE_DISABLED && mode <= MODE_COPY_FROM_METADATA; }

  private void needTiles(String suffix) {
    if (!canWriteTiles) {
      throw new UnsupportedOperationException("Tiling not supported" + suffix);
    }
  }

  private void needExplicitTiling() {
    needTiles("!");
    if (tilingMode != MODE_EXPLICIT) {
      throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
    }
  }

  private void needTileGrid() {
    needExplicitTiling();
    if (!tilingSet) {
      throw new IllegalStateException("Tiling parameters not set!");
    }
  }

  private void needCompression(String suffix) {
    if (!canWriteCompressed) {
      throw new UnsupportedOperationException("Compression not supported" + suffix);
    }
  }

  private void needExplicitCompression() {
    needCompression(".");
    if (compressionMode != MODE_EXPLICIT) {
      throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
    }
  }

  private void needCompressionType() {
    needExplicitCompression();
    if (compressionTypes != null && compressionType == null) {
      throw new IllegalStateException("No compression type set!");
    }
  }

  public boolean canWriteTiles() { return canWriteTiles; }

  public boolean canOffsetTiles() { return canOffsetTiles; }

  public void setTilingMode(int mode) {
    needTiles("!");
    if (!validMode(mode)) {
      throw new IllegalArgumentException("Illegal value for mode!");
    }
    tilingMode = mode;
    if (mode == MODE_EXPLICIT) {
      unsetTiling();
    }
  }

  public int getTilingMode() {
    needTiles("");
    return tilingMode;
  }

  public Dimension[] getPreferredTileSizes() {
    needTiles("");
    if (preferredTileSizes == null) {
      return null;
    }
    Dimension[] copy = new Dimension[preferredTileSizes.length];
    for (int i = 0; i < copy.length; i++) {
      copy[i] = new Dimension(preferredTileSizes[i]);
    }
    return copy;
  }

  public void setTiling(int tileWidth, int tileHeight, int tileGridXOffset, int tileGridYOffset) {
    needExplicitTiling();
    if (tileWidth <= 0 || tileHeight <= 0) {
      throw new IllegalArgumentException("tile dimensions are non-positive!");
    }
    if (!canOffsetTiles && (tileGridXOffset != 0 || tileGridYOffset != 0)) {
      throw new UnsupportedOperationException("Can't offset tiles!");
    }
    this.tilingSet = true;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.tileGridXOffset = tileGridXOffset;
    this.tileGridYOffset = tileGridYOffset;
  }

  public void unsetTiling() {
    needExplicitTiling();
    tilingSet = false;
    tileWidth = tileHeight = tileGridXOffset = tileGridYOffset = 0;
  }

  public int getTileWidth() {
    needTileGrid();
    return tileWidth;
  }

  public int getTileHeight() {
    needTileGrid();
    return tileHeight;
  }

  public int getTileGridXOffset() {
    needTileGrid();
    return tileGridXOffset;
  }

  public int getTileGridYOffset() {
    needTileGrid();
    return tileGridYOffset;
  }

  public boolean canWriteProgressive() { return canWriteProgressive; }

  public void setProgressiveMode(int mode) {
    if (!canWriteProgressive) {
      throw new UnsupportedOperationException("Progressive output not supported");
    }
    if (!validMode(mode)) {
      throw new IllegalArgumentException("Illegal value for mode!");
    }
    if (mode == MODE_EXPLICIT) {
      throw new IllegalArgumentException("MODE_EXPLICIT is not supported for progressive output");
    }
    progressiveMode = mode;
  }

  public int getProgressiveMode() {
    if (!canWriteProgressive) {
      throw new UnsupportedOperationException("Progressive output not supported");
    }
    return progressiveMode;
  }

  public boolean canWriteCompressed() { return canWriteCompressed; }

  public void setCompressionMode(int mode) {
    needCompression(".");
    if (!validMode(mode)) {
      throw new IllegalArgumentException("Illegal value for mode!");
    }
    compressionMode = mode;
    if (mode == MODE_EXPLICIT) {
      unsetCompression();
    }
  }

  public int getCompressionMode() {
    needCompression(".");
    return compressionMode;
  }

  public String[] getCompressionTypes() {
    needCompression("");
    return compressionTypes == null ? null : compressionTypes.clone();
  }

  public void setCompressionType(String compressionType) {
    needExplicitCompression();
    if (compressionType == null) {
      this.compressionType = null;
      return;
    }
    String[] legal = getCompressionTypes();
    if (legal == null) {
      throw new UnsupportedOperationException("No settable compression types");
    }
    for (String t : legal) {
      if (compressionType.equals(t)) {
        this.compressionType = compressionType;
        return;
      }
    }
    throw new IllegalArgumentException("Unknown compression type!");
  }

  public String getCompressionType() {
    needExplicitCompression();
    return compressionType;
  }

  public void unsetCompression() {
    needExplicitCompression();
    compressionType = null;
    compressionQuality = 1.0F;
  }

  public String getLocalizedCompressionTypeName() {
    needExplicitCompression();
    if (compressionType == null) {
      throw new IllegalStateException("No compression type set!");
    }
    return compressionType;
  }

  public boolean isCompressionLossless() {
    needCompressionType();
    return true;
  }

  public void setCompressionQuality(float quality) {
    needCompressionType();
    if (quality < 0.0F || quality > 1.0F) {
      throw new IllegalArgumentException("Quality out-of-bounds!");
    }
    compressionQuality = quality;
  }

  public float getCompressionQuality() {
    needCompressionType();
    return compressionQuality;
  }

  public float getBitRate(float quality) {
    needCompressionType();
    if (quality < 0.0F || quality > 1.0F) {
      throw new IllegalArgumentException("Quality out-of-bounds!");
    }
    return -1.0F;
  }

  public String[] getCompressionQualityDescriptions() {
    needCompressionType();
    return null;
  }

  public float[] getCompressionQualityValues() {
    needCompressionType();
    return null;
  }
}
