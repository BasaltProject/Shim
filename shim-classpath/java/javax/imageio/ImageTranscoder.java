/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

public interface ImageTranscoder {
  javax.imageio.metadata.IIOMetadata convertStreamMetadata(javax.imageio.metadata.IIOMetadata p0, javax.imageio.ImageWriteParam p1);
  javax.imageio.metadata.IIOMetadata convertImageMetadata(javax.imageio.metadata.IIOMetadata p0, javax.imageio.ImageTypeSpecifier p1, javax.imageio.ImageWriteParam p2);
}
