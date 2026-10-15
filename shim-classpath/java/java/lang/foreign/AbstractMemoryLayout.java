/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract class AbstractMemoryLayout implements MemoryLayout {
  final long byteSize;
  final long byteAlignment;
  final String name;

  AbstractMemoryLayout(long byteSize, long byteAlignment, String name) {
    this.byteSize = byteSize;
    this.byteAlignment = byteAlignment;
    this.name = name;
  }

  @Override
  public long byteSize() {
    return byteSize;
  }

  @Override
  public long byteAlignment() {
    return byteAlignment;
  }

  @Override
  public Optional<String> name() {
    return Optional.ofNullable(name);
  }

  @Override
  public MethodHandle scaleHandle() {
    try {
      return MethodHandles.lookup().findVirtual(
          MemoryLayout.class, "scale", MethodType.methodType(long.class, long.class, long.class)
      ).bindTo(this);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  static class ResolvedPath {
    final MemoryLayout layout;
    final long fixedOffset;
    final boolean hasOpenSequence;
    final long elementSize;

    ResolvedPath(MemoryLayout layout, long fixedOffset, boolean hasOpenSequence, long elementSize) {
      this.layout = layout;
      this.fixedOffset = fixedOffset;
      this.hasOpenSequence = hasOpenSequence;
      this.elementSize = elementSize;
    }
  }

  static ResolvedPath resolvePath(MemoryLayout root, MemoryLayout.PathElement... elements) {
    MemoryLayout cur = root;
    long offset = 0L;
    boolean hasOpen = false;
    long elemSize = 0L;

    if (elements != null) {
      for (MemoryLayout.PathElement pe : elements) {
        if (pe instanceof PathElements.GroupByName) {
          String name = ((PathElements.GroupByName) pe).name;
          if (!(cur instanceof GroupLayout)) {
            throw new IllegalArgumentException("Cannot select group element on non-group layout: " + cur);
          }
          GroupLayout gl = (GroupLayout) cur;
          boolean found = false;
          long memberOffset = 0L;
          for (MemoryLayout m : gl.memberLayouts()) {
            if (m.name().isPresent() && m.name().get().equals(name)) {
              found = true;
              cur = m;
              break;
            }
            if (gl instanceof StructLayout) {
              memberOffset += m.byteSize();
            }
          }
          if (!found) {
            throw new IllegalArgumentException("Bad layout path: cannot resolve '" + name + "' in layout " + gl);
          }
          offset += memberOffset;
        } else if (pe instanceof PathElements.GroupByIndex) {
          long idx = ((PathElements.GroupByIndex) pe).index;
          if (!(cur instanceof GroupLayout)) {
            throw new IllegalArgumentException("Cannot select group element on non-group layout: " + cur);
          }
          GroupLayout gl = (GroupLayout) cur;
          List<MemoryLayout> members = gl.memberLayouts();
          if (idx >= members.size()) {
            throw new IllegalArgumentException("Group index out of bounds: " + idx + " >= " + members.size());
          }
          long memberOffset = 0L;
          for (int i = 0; i < idx; i++) {
            if (gl instanceof StructLayout) {
              memberOffset += members.get(i).byteSize();
            }
          }
          offset += memberOffset;
          cur = members.get((int) idx);
        } else if (pe instanceof PathElements.SequenceByIndex) {
          long idx = ((PathElements.SequenceByIndex) pe).index;
          if (!(cur instanceof SequenceLayout)) {
            throw new IllegalArgumentException("Cannot select sequence element on non-sequence layout: " + cur);
          }
          SequenceLayout sl = (SequenceLayout) cur;
          if (sl.elementCount() >= 0 && idx >= sl.elementCount()) {
            throw new IllegalArgumentException("Sequence index out of bounds: " + idx);
          }
          offset += idx * sl.elementLayout().byteSize();
          cur = sl.elementLayout();
        } else if (pe instanceof PathElements.OpenSequence) {
          if (!(cur instanceof SequenceLayout)) {
            throw new IllegalArgumentException("Cannot select open sequence on non-sequence layout: " + cur);
          }
          SequenceLayout sl = (SequenceLayout) cur;
          hasOpen = true;
          elemSize = sl.elementLayout().byteSize();
          cur = sl.elementLayout();
        } else if (pe instanceof PathElements.Dereference) {
          if (!(cur instanceof AddressLayout)) {
            throw new IllegalArgumentException("Cannot dereference non-address layout: " + cur);
          }
          AddressLayout al = (AddressLayout) cur;
          if (al.targetLayout().isEmpty()) {
            throw new IllegalArgumentException("Address layout has no target layout: " + al);
          }
          cur = al.targetLayout().get();
        } else if (pe instanceof PathElements.SequenceByRange) {
          PathElements.SequenceByRange sbr = (PathElements.SequenceByRange) pe;
          if (!(cur instanceof SequenceLayout)) {
            throw new IllegalArgumentException("Cannot select sequence range on non-sequence layout: " + cur);
          }
          SequenceLayout sl = (SequenceLayout) cur;
          offset += sbr.start * sl.elementLayout().byteSize();
          hasOpen = true;
          elemSize = sbr.step * sl.elementLayout().byteSize();
          cur = sl.elementLayout();
        }
      }
    }
    return new ResolvedPath(cur, offset, hasOpen, elemSize);
  }

  @Override
  public MemoryLayout select(PathElement... elements) {
    return resolvePath(this, elements).layout;
  }

  @Override
  public long byteOffset(PathElement... elements) {
    ResolvedPath rp = resolvePath(this, elements);
    if (rp.hasOpenSequence) {
      throw new IllegalArgumentException("Layout path contains open sequence elements");
    }
    return rp.fixedOffset;
  }

  @Override
  public MethodHandle byteOffsetHandle(PathElement... elements) {
    ResolvedPath rp = resolvePath(this, elements);
    try {
      if (!rp.hasOpenSequence) {
        return MethodHandles.lookup().findVirtual(
            PathHandleHelper.Fixed.class, "computeOffset", MethodType.methodType(long.class, long.class)
        ).bindTo(new PathHandleHelper.Fixed(rp.fixedOffset, rp.layout.byteSize()));
      } else {
        return MethodHandles.lookup().findVirtual(
            PathHandleHelper.Indexed.class, "computeOffset", MethodType.methodType(long.class, long.class, long.class)
        ).bindTo(new PathHandleHelper.Indexed(rp.fixedOffset, rp.elementSize));
      }
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public MethodHandle sliceHandle(PathElement... elements) {
    ResolvedPath rp = resolvePath(this, elements);
    try {
      if (!rp.hasOpenSequence) {
        return MethodHandles.lookup().findVirtual(
            PathHandleHelper.Fixed.class, "slice",
            MethodType.methodType(MemorySegment.class, MemorySegment.class, long.class)
        ).bindTo(new PathHandleHelper.Fixed(rp.fixedOffset, rp.layout.byteSize()));
      } else {
        return MethodHandles.lookup().findVirtual(
            PathHandleHelper.Indexed.class, "slice",
            MethodType.methodType(MemorySegment.class, MemorySegment.class, long.class, long.class)
        ).bindTo(new PathHandleHelper.Indexed(rp.fixedOffset, rp.elementSize));
      }
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public VarHandle varHandle(PathElement... elements) {
    ResolvedPath rp = resolvePath(this, elements);
    if (!(rp.layout instanceof ValueLayout)) {
      throw new IllegalArgumentException("Layout path does not resolve to a ValueLayout: " + rp.layout);
    }
    if (rp.hasOpenSequence) {
      return new SegmentVarHandle((ValueLayout) rp.layout, rp.fixedOffset, rp.elementSize);
    } else {
      return new SegmentVarHandle((ValueLayout) rp.layout, rp.fixedOffset);
    }
  }

  @Override
  public VarHandle arrayElementVarHandle(PathElement... elements) {
    if (elements == null || elements.length == 0) {
      if (this instanceof ValueLayout) {
        return new SegmentVarHandle((ValueLayout) this, 0L, byteSize());
      }
      return varHandle(PathElement.sequenceElement());
    }
    PathElement[] extended = new PathElement[elements.length + 1];
    System.arraycopy(elements, 0, extended, 0, elements.length);
    extended[elements.length] = PathElement.sequenceElement();
    return varHandle(extended);
  }

  static String formatName(String str, String name) {
    return name != null ? str + "(" + name + ")" : str;
  }
}
