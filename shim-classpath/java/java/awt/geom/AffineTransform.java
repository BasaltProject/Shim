/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public class AffineTransform implements Cloneable, java.io.Serializable {
  public static final int TYPE_IDENTITY = 0;
  public static final int TYPE_TRANSLATION = 1;
  public static final int TYPE_UNIFORM_SCALE = 2;
  public static final int TYPE_GENERAL_SCALE = 4;
  public static final int TYPE_MASK_SCALE = (TYPE_UNIFORM_SCALE | TYPE_GENERAL_SCALE);
  public static final int TYPE_FLIP = 64;
  public static final int TYPE_QUADRANT_ROTATION = 8;
  public static final int TYPE_GENERAL_ROTATION = 16;
  public static final int TYPE_MASK_ROTATION = (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_ROTATION);
  public static final int TYPE_GENERAL_TRANSFORM = 32;

  static final int APPLY_IDENTITY = 0;
  static final int APPLY_TRANSLATE = 1;
  static final int APPLY_SCALE = 2;
  static final int APPLY_SHEAR = 4;

  double m00;
  double m10;
  double m01;
  double m11;
  double m02;
  double m12;
  private static final long serialVersionUID = 1330973210523860834L;

  public AffineTransform() {
    m00 = m11 = 1.0;
  }

  public AffineTransform(AffineTransform Tx) {
    this.m00 = Tx.m00;
    this.m10 = Tx.m10;
    this.m01 = Tx.m01;
    this.m11 = Tx.m11;
    this.m02 = Tx.m02;
    this.m12 = Tx.m12;
  }

  public AffineTransform(float m00, float m10, float m01, float m11, float m02, float m12) {
    this.m00 = m00;
    this.m10 = m10;
    this.m01 = m01;
    this.m11 = m11;
    this.m02 = m02;
    this.m12 = m12;
  }

  public AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
    this.m00 = m00;
    this.m10 = m10;
    this.m01 = m01;
    this.m11 = m11;
    this.m02 = m02;
    this.m12 = m12;
  }

  public AffineTransform(float[] flatmatrix) {
    m00 = flatmatrix[0];
    m10 = flatmatrix[1];
    m01 = flatmatrix[2];
    m11 = flatmatrix[3];
    if (flatmatrix.length > 5) {
      m02 = flatmatrix[4];
      m12 = flatmatrix[5];
    }
  }

  public AffineTransform(double[] flatmatrix) {
    m00 = flatmatrix[0];
    m10 = flatmatrix[1];
    m01 = flatmatrix[2];
    m11 = flatmatrix[3];
    if (flatmatrix.length > 5) {
      m02 = flatmatrix[4];
      m12 = flatmatrix[5];
    }
  }

  private int state() {
    int s;
    if (m01 == 0.0 && m10 == 0.0) {
      if (m00 == 1.0 && m11 == 1.0) {
        s = APPLY_IDENTITY;
      } else {
        s = APPLY_SCALE;
      }
    } else if (m00 == 0.0 && m11 == 0.0) {
      s = APPLY_SHEAR;
    } else {
      s = APPLY_SHEAR | APPLY_SCALE;
    }
    if (m02 != 0.0 || m12 != 0.0) {
      s |= APPLY_TRANSLATE;
    }
    return s;
  }

  public static AffineTransform getTranslateInstance(double tx, double ty) {
    AffineTransform t = new AffineTransform();
    t.setToTranslation(tx, ty);
    return t;
  }

  public static AffineTransform getRotateInstance(double theta) {
    AffineTransform t = new AffineTransform();
    t.setToRotation(theta);
    return t;
  }

  public static AffineTransform getRotateInstance(double theta, double anchorx, double anchory) {
    AffineTransform t = new AffineTransform();
    t.setToRotation(theta, anchorx, anchory);
    return t;
  }

  public static AffineTransform getRotateInstance(double vecx, double vecy) {
    AffineTransform t = new AffineTransform();
    t.setToRotation(vecx, vecy);
    return t;
  }

  public static AffineTransform getRotateInstance(double vecx, double vecy, double anchorx, double anchory) {
    AffineTransform t = new AffineTransform();
    t.setToRotation(vecx, vecy, anchorx, anchory);
    return t;
  }

  public static AffineTransform getQuadrantRotateInstance(int numquadrants) {
    AffineTransform t = new AffineTransform();
    t.setToQuadrantRotation(numquadrants);
    return t;
  }

  public static AffineTransform getQuadrantRotateInstance(int numquadrants, double anchorx, double anchory) {
    AffineTransform t = new AffineTransform();
    t.setToQuadrantRotation(numquadrants, anchorx, anchory);
    return t;
  }

  public static AffineTransform getScaleInstance(double sx, double sy) {
    AffineTransform t = new AffineTransform();
    t.setToScale(sx, sy);
    return t;
  }

  public static AffineTransform getShearInstance(double shx, double shy) {
    AffineTransform t = new AffineTransform();
    t.setToShear(shx, shy);
    return t;
  }

  public int getType() {
    int ret = TYPE_IDENTITY;
    boolean sgn0;
    boolean sgn1;
    double m0;
    double m1;
    double m2;
    double m3;
    switch (state()) {
    case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
      ret = TYPE_TRANSLATION;
      // fall through
    case (APPLY_SHEAR | APPLY_SCALE):
      m0 = m00;
      m2 = m01;
      m3 = m10;
      m1 = m11;
      if (m0 * m2 + m3 * m1 != 0) {
        return TYPE_GENERAL_TRANSFORM;
      }
      sgn0 = (m0 >= 0.0);
      sgn1 = (m1 >= 0.0);
      if (sgn0 == sgn1) {
        if (m0 != m1 || m2 != -m3) {
          ret |= (TYPE_GENERAL_ROTATION | TYPE_GENERAL_SCALE);
        } else if (m0 * m1 - m2 * m3 != 1.0) {
          ret |= (TYPE_GENERAL_ROTATION | TYPE_UNIFORM_SCALE);
        } else {
          ret |= TYPE_GENERAL_ROTATION;
        }
      } else {
        if (m0 != -m1 || m2 != m3) {
          ret |= (TYPE_GENERAL_ROTATION | TYPE_FLIP | TYPE_GENERAL_SCALE);
        } else if (m0 * m1 - m2 * m3 != 1.0) {
          ret |= (TYPE_GENERAL_ROTATION | TYPE_FLIP | TYPE_UNIFORM_SCALE);
        } else {
          ret |= (TYPE_GENERAL_ROTATION | TYPE_FLIP);
        }
      }
      break;
    case (APPLY_SHEAR | APPLY_TRANSLATE):
      ret = TYPE_TRANSLATION;
      // fall through
    case (APPLY_SHEAR):
      m0 = m01;
      sgn0 = (m0 >= 0.0);
      m1 = m10;
      sgn1 = (m1 >= 0.0);
      if (sgn0 != sgn1) {
        if (m0 != -m1) {
          ret |= (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_SCALE);
        } else if (m0 != 1.0 && m0 != -1.0) {
          ret |= (TYPE_QUADRANT_ROTATION | TYPE_UNIFORM_SCALE);
        } else {
          ret |= TYPE_QUADRANT_ROTATION;
        }
      } else {
        if (m0 == m1) {
          ret |= (TYPE_QUADRANT_ROTATION | TYPE_FLIP | TYPE_UNIFORM_SCALE);
        } else {
          ret |= (TYPE_QUADRANT_ROTATION | TYPE_FLIP | TYPE_GENERAL_SCALE);
        }
      }
      break;
    case (APPLY_SCALE | APPLY_TRANSLATE):
      ret = TYPE_TRANSLATION;
      // fall through
    case (APPLY_SCALE):
      m0 = m00;
      sgn0 = (m0 >= 0.0);
      m1 = m11;
      sgn1 = (m1 >= 0.0);
      if (sgn0 == sgn1) {
        if (sgn0) {
          if (m0 == m1) {
            ret |= TYPE_UNIFORM_SCALE;
          } else {
            ret |= TYPE_GENERAL_SCALE;
          }
        } else {
          if (m0 != m1) {
            ret |= (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_SCALE);
          } else if (m0 != -1.0) {
            ret |= (TYPE_QUADRANT_ROTATION | TYPE_UNIFORM_SCALE);
          } else {
            ret |= TYPE_QUADRANT_ROTATION;
          }
        }
      } else {
        if (m0 == -m1) {
          if (m0 == 1.0 || m0 == -1.0) {
            ret |= TYPE_FLIP;
          } else {
            ret |= (TYPE_FLIP | TYPE_UNIFORM_SCALE);
          }
        } else {
          ret |= (TYPE_FLIP | TYPE_GENERAL_SCALE);
        }
      }
      break;
    case (APPLY_TRANSLATE):
      ret = TYPE_TRANSLATION;
      break;
    default:
      break;
    }
    return ret;
  }

  public double getDeterminant() {
    return m00 * m11 - m01 * m10;
  }

  public void getMatrix(double[] flatmatrix) {
    flatmatrix[0] = m00;
    flatmatrix[1] = m10;
    flatmatrix[2] = m01;
    flatmatrix[3] = m11;
    if (flatmatrix.length > 5) {
      flatmatrix[4] = m02;
      flatmatrix[5] = m12;
    }
  }

  public double getScaleX() {
    return m00;
  }

  public double getScaleY() {
    return m11;
  }

  public double getShearX() {
    return m01;
  }

  public double getShearY() {
    return m10;
  }

  public double getTranslateX() {
    return m02;
  }

  public double getTranslateY() {
    return m12;
  }

  public void translate(double tx, double ty) {
    m02 = tx * m00 + ty * m01 + m02;
    m12 = tx * m10 + ty * m11 + m12;
  }

  public void setToIdentity() {
    m00 = m11 = 1.0;
    m10 = m01 = m02 = m12 = 0.0;
  }

  public void setToTranslation(double tx, double ty) {
    m00 = 1.0;
    m10 = 0.0;
    m01 = 0.0;
    m11 = 1.0;
    m02 = tx;
    m12 = ty;
  }

  public void setToRotation(double theta) {
    double sin = Math.sin(theta);
    double cos;
    if (sin == 1.0 || sin == -1.0) {
      cos = 0.0;
    } else {
      cos = Math.cos(theta);
      if (cos == -1.0) {
        sin = 0.0;
      } else if (cos == 1.0) {
        sin = 0.0;
      }
    }
    m00 = cos;
    m10 = sin;
    m01 = -sin;
    m11 = cos;
    m02 = 0.0;
    m12 = 0.0;
  }

  public void setToRotation(double theta, double anchorx, double anchory) {
    setToRotation(theta);
    double sin = m10;
    double oneMinusCos = 1.0 - m00;
    m02 = anchorx * oneMinusCos + anchory * sin;
    m12 = anchory * oneMinusCos - anchorx * sin;
  }

  public void setToRotation(double vecx, double vecy) {
    double sin;
    double cos;
    if (vecy == 0) {
      sin = 0.0;
      if (vecx < 0.0) {
        cos = -1.0;
      } else {
        cos = 1.0;
      }
    } else if (vecx == 0) {
      cos = 0.0;
      sin = (vecy > 0.0) ? 1.0 : -1.0;
    } else {
      double len = Math.sqrt(vecx * vecx + vecy * vecy);
      cos = vecx / len;
      sin = vecy / len;
    }
    m00 = cos;
    m10 = sin;
    m01 = -sin;
    m11 = cos;
    m02 = 0.0;
    m12 = 0.0;
  }

  public void setToRotation(double vecx, double vecy, double anchorx, double anchory) {
    setToRotation(vecx, vecy);
    double sin = m10;
    double oneMinusCos = 1.0 - m00;
    m02 = anchorx * oneMinusCos + anchory * sin;
    m12 = anchory * oneMinusCos - anchorx * sin;
  }

  public void setToQuadrantRotation(int numquadrants) {
    switch (numquadrants & 3) {
    case 0:
      m00 = 1.0;
      m10 = 0.0;
      m01 = 0.0;
      m11 = 1.0;
      break;
    case 1:
      m00 = 0.0;
      m10 = 1.0;
      m01 = -1.0;
      m11 = 0.0;
      break;
    case 2:
      m00 = -1.0;
      m10 = 0.0;
      m01 = 0.0;
      m11 = -1.0;
      break;
    default:
      m00 = 0.0;
      m10 = -1.0;
      m01 = 1.0;
      m11 = 0.0;
      break;
    }
    m02 = 0.0;
    m12 = 0.0;
  }

  public void setToQuadrantRotation(int numquadrants, double anchorx, double anchory) {
    switch (numquadrants & 3) {
    case 0:
      m00 = 1.0;
      m10 = 0.0;
      m01 = 0.0;
      m11 = 1.0;
      m02 = 0.0;
      m12 = 0.0;
      break;
    case 1:
      m00 = 0.0;
      m10 = 1.0;
      m01 = -1.0;
      m11 = 0.0;
      m02 = anchorx + anchory;
      m12 = anchory - anchorx;
      break;
    case 2:
      m00 = -1.0;
      m10 = 0.0;
      m01 = 0.0;
      m11 = -1.0;
      m02 = anchorx + anchorx;
      m12 = anchory + anchory;
      break;
    default:
      m00 = 0.0;
      m10 = -1.0;
      m01 = 1.0;
      m11 = 0.0;
      m02 = anchorx - anchory;
      m12 = anchory + anchorx;
      break;
    }
  }

  public void setToScale(double sx, double sy) {
    m00 = sx;
    m10 = 0.0;
    m01 = 0.0;
    m11 = sy;
    m02 = 0.0;
    m12 = 0.0;
  }

  public void setToShear(double shx, double shy) {
    m00 = 1.0;
    m01 = shx;
    m10 = shy;
    m11 = 1.0;
    m02 = 0.0;
    m12 = 0.0;
  }

  public void setTransform(AffineTransform Tx) {
    this.m00 = Tx.m00;
    this.m10 = Tx.m10;
    this.m01 = Tx.m01;
    this.m11 = Tx.m11;
    this.m02 = Tx.m02;
    this.m12 = Tx.m12;
  }

  public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
    this.m00 = m00;
    this.m10 = m10;
    this.m01 = m01;
    this.m11 = m11;
    this.m02 = m02;
    this.m12 = m12;
  }

  public void rotate(double theta) {
    double sin = Math.sin(theta);
    if (sin == 1.0 || sin == -1.0) {
      rotate90(sin);
      return;
    }
    double cos = Math.cos(theta);
    if (cos == -1.0) {
      rotate180();
    } else if (cos != 1.0) {
      double m0 = m00;
      double m1 = m01;
      m00 = cos * m0 + sin * m1;
      m01 = -sin * m0 + cos * m1;
      m0 = m10;
      m1 = m11;
      m10 = cos * m0 + sin * m1;
      m11 = -sin * m0 + cos * m1;
    }
  }

  private void rotate90(double sin) {
    if (sin > 0) {
      rotate90();
    } else {
      rotate270();
    }
  }

  private void rotate90() {
    double m0 = m00;
    m00 = m01;
    m01 = -m0;
    m0 = m10;
    m10 = m11;
    m11 = -m0;
  }

  private void rotate180() {
    m00 = -m00;
    m11 = -m11;
    if (m01 != 0.0 || m10 != 0.0) {
      m01 = -m01;
      m10 = -m10;
    }
  }

  private void rotate270() {
    double m0 = m00;
    m00 = -m01;
    m01 = m0;
    m0 = m10;
    m10 = -m11;
    m11 = m0;
  }

  public void rotate(double theta, double anchorx, double anchory) {
    translate(anchorx, anchory);
    rotate(theta);
    translate(-anchorx, -anchory);
  }

  public void rotate(double vecx, double vecy) {
    if (vecy == 0.0) {
      if (vecx < 0.0) {
        rotate180();
      }
    } else if (vecx == 0.0) {
      if (vecy > 0.0) {
        rotate90();
      } else {
        rotate270();
      }
    } else {
      double len = Math.sqrt(vecx * vecx + vecy * vecy);
      double sin = vecy / len;
      double cos = vecx / len;
      double m0 = m00;
      double m1 = m01;
      m00 = cos * m0 + sin * m1;
      m01 = -sin * m0 + cos * m1;
      m0 = m10;
      m1 = m11;
      m10 = cos * m0 + sin * m1;
      m11 = -sin * m0 + cos * m1;
    }
  }

  public void rotate(double vecx, double vecy, double anchorx, double anchory) {
    translate(anchorx, anchory);
    rotate(vecx, vecy);
    translate(-anchorx, -anchory);
  }

  public void quadrantRotate(int numquadrants) {
    switch (numquadrants & 3) {
    case 0:
      break;
    case 1:
      rotate90();
      break;
    case 2:
      rotate180();
      break;
    default:
      rotate270();
      break;
    }
  }

  public void quadrantRotate(int numquadrants, double anchorx, double anchory) {
    switch (numquadrants & 3) {
    case 0:
      return;
    case 1:
      m02 += anchorx * (m00 - m01) + anchory * (m01 + m00);
      m12 += anchorx * (m10 - m11) + anchory * (m11 + m10);
      rotate90();
      break;
    case 2:
      m02 += anchorx * (m00 + m00) + anchory * (m01 + m01);
      m12 += anchorx * (m10 + m10) + anchory * (m11 + m11);
      rotate180();
      break;
    default:
      m02 += anchorx * (m00 + m01) + anchory * (m01 - m00);
      m12 += anchorx * (m10 + m11) + anchory * (m11 - m10);
      rotate270();
      break;
    }
  }

  public void scale(double sx, double sy) {
    m00 *= sx;
    m11 *= sy;
    m01 *= sy;
    m10 *= sx;
  }

  public void shear(double shx, double shy) {
    double m0 = m00;
    double m1 = m01;
    m00 = m0 + m1 * shy;
    m01 = m0 * shx + m1;
    m0 = m10;
    m1 = m11;
    m10 = m0 + m1 * shy;
    m11 = m0 * shx + m1;
  }

  public void concatenate(AffineTransform Tx) {
    double t00 = m00 * Tx.m00 + m01 * Tx.m10;
    double t01 = m00 * Tx.m01 + m01 * Tx.m11;
    double t02 = m00 * Tx.m02 + m01 * Tx.m12 + m02;
    double t10 = m10 * Tx.m00 + m11 * Tx.m10;
    double t11 = m10 * Tx.m01 + m11 * Tx.m11;
    double t12 = m10 * Tx.m02 + m11 * Tx.m12 + m12;
    m00 = t00;
    m01 = t01;
    m02 = t02;
    m10 = t10;
    m11 = t11;
    m12 = t12;
  }

  public void preConcatenate(AffineTransform Tx) {
    double t00 = Tx.m00 * m00 + Tx.m01 * m10;
    double t01 = Tx.m00 * m01 + Tx.m01 * m11;
    double t02 = Tx.m00 * m02 + Tx.m01 * m12 + Tx.m02;
    double t10 = Tx.m10 * m00 + Tx.m11 * m10;
    double t11 = Tx.m10 * m01 + Tx.m11 * m11;
    double t12 = Tx.m10 * m02 + Tx.m11 * m12 + Tx.m12;
    m00 = t00;
    m01 = t01;
    m02 = t02;
    m10 = t10;
    m11 = t11;
    m12 = t12;
  }

  public AffineTransform createInverse() throws NoninvertibleTransformException {
    AffineTransform t = new AffineTransform(this);
    t.invert();
    return t;
  }

  public void invert() throws NoninvertibleTransformException {
    double m0 = m00;
    double m1 = m01;
    double m2 = m02;
    double m3 = m10;
    double m4 = m11;
    double m5 = m12;
    double det;
    switch (state()) {
    case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
    case (APPLY_SHEAR | APPLY_SCALE):
      det = m0 * m4 - m1 * m3;
      if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
        throw new NoninvertibleTransformException("Determinant is " + det);
      }
      m00 = m4 / det;
      m10 = -m3 / det;
      m01 = -m1 / det;
      m11 = m0 / det;
      if (m2 != 0.0 || m5 != 0.0) {
        m02 = (m1 * m5 - m4 * m2) / det;
        m12 = (m3 * m2 - m0 * m5) / det;
      }
      break;
    case (APPLY_SHEAR | APPLY_TRANSLATE):
      if (m1 == 0.0 || m3 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      m00 = 0.0;
      m10 = 1.0 / m1;
      m01 = 1.0 / m3;
      m11 = 0.0;
      m02 = -m5 / m3;
      m12 = -m2 / m1;
      break;
    case (APPLY_SHEAR):
      if (m1 == 0.0 || m3 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      m10 = 1.0 / m1;
      m01 = 1.0 / m3;
      break;
    case (APPLY_SCALE | APPLY_TRANSLATE):
      if (m0 == 0.0 || m4 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      m00 = 1.0 / m0;
      m11 = 1.0 / m4;
      m02 = -m2 / m0;
      m12 = -m5 / m4;
      break;
    case (APPLY_SCALE):
      if (m0 == 0.0 || m4 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      m00 = 1.0 / m0;
      m11 = 1.0 / m4;
      break;
    case (APPLY_TRANSLATE):
      m02 = -m2;
      m12 = -m5;
      break;
    default:
      break;
    }
  }

  public Point2D transform(Point2D ptSrc, Point2D ptDst) {
    if (ptDst == null) {
      if (ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    double x = ptSrc.getX();
    double y = ptSrc.getY();
    switch (state()) {
    case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
      ptDst.setLocation(x * m00 + y * m01 + m02, x * m10 + y * m11 + m12);
      return ptDst;
    case (APPLY_SHEAR | APPLY_SCALE):
      ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11);
      return ptDst;
    case (APPLY_SHEAR | APPLY_TRANSLATE):
      ptDst.setLocation(y * m01 + m02, x * m10 + m12);
      return ptDst;
    case (APPLY_SHEAR):
      ptDst.setLocation(y * m01, x * m10);
      return ptDst;
    case (APPLY_SCALE | APPLY_TRANSLATE):
      ptDst.setLocation(x * m00 + m02, y * m11 + m12);
      return ptDst;
    case (APPLY_SCALE):
      ptDst.setLocation(x * m00, y * m11);
      return ptDst;
    case (APPLY_TRANSLATE):
      ptDst.setLocation(x + m02, y + m12);
      return ptDst;
    default:
      ptDst.setLocation(x, y);
      return ptDst;
    }
  }

  public void transform(Point2D[] ptSrc, int srcOff, Point2D[] ptDst, int dstOff, int numPts) {
    while (--numPts >= 0) {
      Point2D src = ptSrc[srcOff++];
      double x = src.getX();
      double y = src.getY();
      Point2D dst = ptDst[dstOff++];
      if (dst == null) {
        if (src instanceof Point2D.Double) {
          dst = new Point2D.Double();
        } else {
          dst = new Point2D.Float();
        }
        ptDst[dstOff - 1] = dst;
      }
      dst.setLocation(x * m00 + y * m01 + m02, x * m10 + y * m11 + m12);
    }
  }

  private void transformCoords(double[] src, int srcOff, double[] dst, int dstOff, int numPts) {
    int st = state();
    if (dst == src && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
      System.arraycopy(src, srcOff, dst, dstOff, numPts * 2);
      srcOff = dstOff;
    }
    while (--numPts >= 0) {
      double x = src[srcOff++];
      double y = src[srcOff++];
      switch (st) {
      case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        dst[dstOff++] = x * m00 + y * m01 + m02;
        dst[dstOff++] = x * m10 + y * m11 + m12;
        break;
      case (APPLY_SHEAR | APPLY_SCALE):
        dst[dstOff++] = x * m00 + y * m01;
        dst[dstOff++] = x * m10 + y * m11;
        break;
      case (APPLY_SHEAR | APPLY_TRANSLATE):
        dst[dstOff++] = y * m01 + m02;
        dst[dstOff++] = x * m10 + m12;
        break;
      case (APPLY_SHEAR):
        dst[dstOff++] = y * m01;
        dst[dstOff++] = x * m10;
        break;
      case (APPLY_SCALE | APPLY_TRANSLATE):
        dst[dstOff++] = x * m00 + m02;
        dst[dstOff++] = y * m11 + m12;
        break;
      case (APPLY_SCALE):
        dst[dstOff++] = x * m00;
        dst[dstOff++] = y * m11;
        break;
      case (APPLY_TRANSLATE):
        dst[dstOff++] = x + m02;
        dst[dstOff++] = y + m12;
        break;
      default:
        dst[dstOff++] = x;
        dst[dstOff++] = y;
        break;
      }
    }
  }

  public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
    int st = state();
    if (dstPts == srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
      System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
      srcOff = dstOff;
    }
    while (--numPts >= 0) {
      double x = srcPts[srcOff++];
      double y = srcPts[srcOff++];
      switch (st) {
      case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        dstPts[dstOff++] = (float) (x * m00 + y * m01 + m02);
        dstPts[dstOff++] = (float) (x * m10 + y * m11 + m12);
        break;
      case (APPLY_SHEAR | APPLY_SCALE):
        dstPts[dstOff++] = (float) (x * m00 + y * m01);
        dstPts[dstOff++] = (float) (x * m10 + y * m11);
        break;
      case (APPLY_SHEAR | APPLY_TRANSLATE):
        dstPts[dstOff++] = (float) (y * m01 + m02);
        dstPts[dstOff++] = (float) (x * m10 + m12);
        break;
      case (APPLY_SHEAR):
        dstPts[dstOff++] = (float) (y * m01);
        dstPts[dstOff++] = (float) (x * m10);
        break;
      case (APPLY_SCALE | APPLY_TRANSLATE):
        dstPts[dstOff++] = (float) (x * m00 + m02);
        dstPts[dstOff++] = (float) (y * m11 + m12);
        break;
      case (APPLY_SCALE):
        dstPts[dstOff++] = (float) (x * m00);
        dstPts[dstOff++] = (float) (y * m11);
        break;
      case (APPLY_TRANSLATE):
        dstPts[dstOff++] = (float) (x + m02);
        dstPts[dstOff++] = (float) (y + m12);
        break;
      default:
        dstPts[dstOff++] = (float) x;
        dstPts[dstOff++] = (float) y;
        break;
      }
    }
  }

  public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
    transformCoords(srcPts, srcOff, dstPts, dstOff, numPts);
  }

  public void transform(float[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
    double[] tmp = new double[numPts * 2];
    for (int i = 0; i < numPts * 2; i++) {
      tmp[i] = srcPts[srcOff + i];
    }
    transformCoords(tmp, 0, dstPts, dstOff, numPts);
  }

  public void transform(double[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
    double[] tmp = new double[numPts * 2];
    transformCoords(srcPts, srcOff, tmp, 0, numPts);
    for (int i = 0; i < numPts * 2; i++) {
      dstPts[dstOff + i] = (float) tmp[i];
    }
  }

  public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst) throws NoninvertibleTransformException {
    if (ptDst == null) {
      if (ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    double x = ptSrc.getX();
    double y = ptSrc.getY();
    switch (state()) {
    case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
      x -= m02;
      y -= m12;
      // fall through
    case (APPLY_SHEAR | APPLY_SCALE):
      double det = m00 * m11 - m01 * m10;
      if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
        throw new NoninvertibleTransformException("Determinant is " + det);
      }
      ptDst.setLocation((x * m11 - y * m01) / det, (y * m00 - x * m10) / det);
      return ptDst;
    case (APPLY_SHEAR | APPLY_TRANSLATE):
      x -= m02;
      y -= m12;
      // fall through
    case (APPLY_SHEAR):
      if (m01 == 0.0 || m10 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      ptDst.setLocation(y / m10, x / m01);
      return ptDst;
    case (APPLY_SCALE | APPLY_TRANSLATE):
      x -= m02;
      y -= m12;
      // fall through
    case (APPLY_SCALE):
      if (m00 == 0.0 || m11 == 0.0) {
        throw new NoninvertibleTransformException("Determinant is 0");
      }
      ptDst.setLocation(x / m00, y / m11);
      return ptDst;
    case (APPLY_TRANSLATE):
      ptDst.setLocation(x - m02, y - m12);
      return ptDst;
    default:
      ptDst.setLocation(x, y);
      return ptDst;
    }
  }

  public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst) {
    if (ptDst == null) {
      if (ptSrc instanceof Point2D.Double) {
        ptDst = new Point2D.Double();
      } else {
        ptDst = new Point2D.Float();
      }
    }
    double x = ptSrc.getX();
    double y = ptSrc.getY();
    switch (state()) {
    case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
    case (APPLY_SHEAR | APPLY_SCALE):
      ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11);
      return ptDst;
    case (APPLY_SHEAR | APPLY_TRANSLATE):
    case (APPLY_SHEAR):
      ptDst.setLocation(y * m01, x * m10);
      return ptDst;
    case (APPLY_SCALE | APPLY_TRANSLATE):
    case (APPLY_SCALE):
      ptDst.setLocation(x * m00, y * m11);
      return ptDst;
    default:
      ptDst.setLocation(x, y);
      return ptDst;
    }
  }

  public boolean isIdentity() {
    return state() == APPLY_IDENTITY;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(m00);
    bits = bits * 31 + java.lang.Double.doubleToLongBits(m01);
    bits = bits * 31 + java.lang.Double.doubleToLongBits(m02);
    bits = bits * 31 + java.lang.Double.doubleToLongBits(m10);
    bits = bits * 31 + java.lang.Double.doubleToLongBits(m11);
    bits = bits * 31 + java.lang.Double.doubleToLongBits(m12);
    return (((int) bits) ^ ((int) (bits >> 32)));
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AffineTransform)) {
      return false;
    }
    AffineTransform a = (AffineTransform) obj;
    return ((m00 == a.m00) && (m01 == a.m01) && (m02 == a.m02) && (m10 == a.m10) && (m11 == a.m11)
        && (m12 == a.m12));
  }

  private static double matround(double matval) {
    return Math.rint(matval * 1E15) / 1E15;
  }

  @Override
  public String toString() {
    return "AffineTransform[[" + matround(m00) + ", " + matround(m01) + ", " + matround(m02) + "], ["
        + matround(m10) + ", " + matround(m11) + ", " + matround(m12) + "]]";
  }
}
