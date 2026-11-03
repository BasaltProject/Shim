/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import javax.imageio.metadata.IIOMetadataFormat;

public abstract class ImageReaderWriterSpi extends IIOServiceProvider {
  protected String[] names = null;
  protected String[] suffixes = null;
  protected String[] MIMETypes = null;
  protected String pluginClassName = null;
  protected boolean supportsStandardStreamMetadataFormat = false;
  protected String nativeStreamMetadataFormatName = null;
  protected String nativeStreamMetadataFormatClassName = null;
  protected String[] extraStreamMetadataFormatNames = null;
  protected String[] extraStreamMetadataFormatClassNames = null;
  protected boolean supportsStandardImageMetadataFormat = false;
  protected String nativeImageMetadataFormatName = null;
  protected String nativeImageMetadataFormatClassName = null;
  protected String[] extraImageMetadataFormatNames = null;
  protected String[] extraImageMetadataFormatClassNames = null;

  public ImageReaderWriterSpi(String vendorName, String version, String[] names, String[] suffixes,
      String[] MIMETypes, String pluginClassName, boolean supportsStandardStreamMetadataFormat,
      String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName,
      String[] extraStreamMetadataFormatNames, String[] extraStreamMetadataFormatClassNames,
      boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName,
      String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames,
      String[] extraImageMetadataFormatClassNames) {
    super(vendorName, version);
    if (names == null) {
      throw new IllegalArgumentException("names == null!");
    }
    if (names.length == 0) {
      throw new IllegalArgumentException("names.length == 0!");
    }
    if (pluginClassName == null) {
      throw new IllegalArgumentException("pluginClassName == null!");
    }
    this.names = names.clone();
    if (suffixes != null && suffixes.length > 0) {
      this.suffixes = suffixes.clone();
    }
    if (MIMETypes != null && MIMETypes.length > 0) {
      this.MIMETypes = MIMETypes.clone();
    }
    this.pluginClassName = pluginClassName;
    this.supportsStandardStreamMetadataFormat = supportsStandardStreamMetadataFormat;
    this.nativeStreamMetadataFormatName = nativeStreamMetadataFormatName;
    this.nativeStreamMetadataFormatClassName = nativeStreamMetadataFormatClassName;
    this.extraStreamMetadataFormatNames = extraStreamMetadataFormatNames == null ? null : extraStreamMetadataFormatNames.clone();
    this.extraStreamMetadataFormatClassNames = extraStreamMetadataFormatClassNames == null ? null : extraStreamMetadataFormatClassNames.clone();
    this.supportsStandardImageMetadataFormat = supportsStandardImageMetadataFormat;
    this.nativeImageMetadataFormatName = nativeImageMetadataFormatName;
    this.nativeImageMetadataFormatClassName = nativeImageMetadataFormatClassName;
    this.extraImageMetadataFormatNames = extraImageMetadataFormatNames == null ? null : extraImageMetadataFormatNames.clone();
    this.extraImageMetadataFormatClassNames = extraImageMetadataFormatClassNames == null ? null : extraImageMetadataFormatClassNames.clone();
  }

  public ImageReaderWriterSpi() { }

  public String[] getFormatNames() { return names.clone(); }

  public String[] getFileSuffixes() { return suffixes == null ? null : suffixes.clone(); }

  public String[] getMIMETypes() { return MIMETypes == null ? null : MIMETypes.clone(); }

  public String getPluginClassName() { return pluginClassName; }

  public boolean isStandardStreamMetadataFormatSupported() { return supportsStandardStreamMetadataFormat; }

  public String getNativeStreamMetadataFormatName() { return nativeStreamMetadataFormatName; }

  public String[] getExtraStreamMetadataFormatNames() {
    return extraStreamMetadataFormatNames == null ? null : extraStreamMetadataFormatNames.clone();
  }

  public boolean isStandardImageMetadataFormatSupported() { return supportsStandardImageMetadataFormat; }

  public String getNativeImageMetadataFormatName() { return nativeImageMetadataFormatName; }

  public String[] getExtraImageMetadataFormatNames() {
    return extraImageMetadataFormatNames == null ? null : extraImageMetadataFormatNames.clone();
  }

  public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
    throw new IllegalArgumentException("Unsupported format name");
  }

  public IIOMetadataFormat getImageMetadataFormat(String formatName) {
    throw new IllegalArgumentException("Unsupported format name");
  }
}
