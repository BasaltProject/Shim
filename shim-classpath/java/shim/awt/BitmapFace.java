/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Shape;
import java.awt.geom.Path2D;

public final class BitmapFace extends FontFace {
  private static final String kData = "000000000000005F00000007000700147F147F14242A7F2A12231308646236495522500005030000001C2241000041221C0014083E081408083E080800503000000808080808006060000020100804023E5149453E00427F400042615149462141454B311814127F1027454545393C4A49493001710905033649494936064949291E003636000000563600000814224100141414141400412214080201510906324979413E7E1111117E7F494949363E414141227F4141221C7F494949417F090909013E4149497A7F0808087F00417F41002040413F017F081422417F404040407F020C027F7F0408107F3E4141413E7F090909063E4151215E7F09192946464949493101017F01013F4040403F1F2040201F3F4038403F631408146307087008076151494543007F41410002040810200041417F0004020102044040404040000102040020545454787F484444383844444420384444487F3854545418087E090102081454543C7F0804047800447D40002040443D00007F10284400417F40007C041804787C0804047838444444387C14141408081414187C7C080404084854545420043F4440203C4040207C1C2040201C3C4030403C44281028440C5050503C4464544C44000836410000007F000000413608001008081008";
  private static final float kAdvance = 0.6f;
  private static final float kCap = 0.72f;
  private final String family;

  public BitmapFace(String family) {
    this.family = family;
  }

  public String family() { return family; }

  public boolean canDisplay(int cp) { return cp >= 32 && cp < 127 || cp == 0xa0 || cp == '\t' && false; }

  public float advance(int cp) { return cp == '\t' ? kAdvance : kAdvance; }

  public float ascent() { return 0.93f; }

  public float descent() { return 0.22f; }

  public float leading() { return 0f; }

  public Shape outline(int cp, boolean bold, boolean italic) {
    Path2D.Float path = new Path2D.Float();
    int idx = cp - 32;
    if (cp == 0xa0) {
      return path;
    }
    float cw = kAdvance / 6f;
    float rh = kCap / 7f;
    if (idx < 0 || idx >= 95) {
      float x0 = cw * 0.5f;
      float x1 = kAdvance - cw * 1.5f;
      path.append(new java.awt.geom.Rectangle2D.Float(x0, -kCap, x1 - x0, kCap), false);
      path.append(new java.awt.geom.Rectangle2D.Float(x0 + cw * 0.6f, -kCap + rh * 0.6f, x1 - x0 - cw * 1.2f,
          kCap - rh * 1.2f), false);
      return path;
    }
    float colw = bold ? cw * 1.6f : cw;
    for (int c = 0; c < 5; c++) {
      int col = Integer.parseInt(kData.substring((idx * 5 + c) * 2, (idx * 5 + c) * 2 + 2), 16);
      int r = 0;
      while (r < 7) {
        if ((col & (1 << r)) == 0) {
          r++;
          continue;
        }
        int start = r;
        while (r < 7 && (col & (1 << r)) != 0) {
          r++;
        }
        float y = -kCap + start * rh;
        float x = cw * 0.5f + c * cw;
        path.append(new java.awt.geom.Rectangle2D.Float(x, y, colw, (r - start) * rh), false);
      }
    }
    if (italic) {
      path.transform(java.awt.geom.AffineTransform.getShearInstance(-0.2, 0));
    }
    return path;
  }
}
