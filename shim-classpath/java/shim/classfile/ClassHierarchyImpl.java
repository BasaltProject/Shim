/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** The class hierarchy resolvers behind the static factories of {@link ClassHierarchyResolver}. */
public final class ClassHierarchyImpl {
    private static final ClassHierarchyResolver DEFAULT =
        new ClassLoadingResolver(ClassLoader.getSystemClassLoader());

    private ClassHierarchyImpl() { }

    public static ClassHierarchyResolver defaultResolver() {
        return DEFAULT;
    }

    public static ClassHierarchyResolver of(Collection<ClassDesc> interfaceNames,
                                            Map<ClassDesc, ClassDesc> classToSuperClass) {
        final Map<ClassDesc, ClassHierarchyInfo> map =
            new HashMap<>(interfaceNames.size() + classToSuperClass.size() + 1);
        map.put(ConstantDescs.CD_Object, new ClassHierarchyInfoImpl(null, false));
        for (Map.Entry<ClassDesc, ClassDesc> e : classToSuperClass.entrySet()) {
            map.put(e.getKey(), new ClassHierarchyInfoImpl(e.getValue(), false));
        }
        for (ClassDesc d : interfaceNames) {
            map.put(d, new ClassHierarchyInfoImpl(ConstantDescs.CD_Object, true));
        }
        return new ClassHierarchyResolver() {
            @Override
            public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
                return map.get(cd);
            }
        };
    }

    public static ClassHierarchyResolver orElse(final ClassHierarchyResolver first,
                                                final ClassHierarchyResolver other) {
        Objects.requireNonNull(other);
        return new ClassHierarchyResolver() {
            @Override
            public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
                ClassHierarchyInfo info = first.getClassInfo(cd);
                return info != null ? info : other.getClassInfo(cd);
            }
        };
    }

    public static ClassHierarchyResolver cached(ClassHierarchyResolver delegate,
                                                Supplier<Map<ClassDesc, ClassHierarchyInfo>> cacheFactory) {
        return new CachedResolver(delegate, cacheFactory.get());
    }

    public static ClassHierarchyResolver cached(ClassHierarchyResolver delegate) {
        return new CachedResolver(delegate, new HashMap<ClassDesc, ClassHierarchyInfo>());
    }

    public static ClassHierarchyResolver ofResourceParsing(final ClassLoader loader) {
        Objects.requireNonNull(loader);
        return ofResourceParsing(new Function<ClassDesc, InputStream>() {
            @Override
            public InputStream apply(ClassDesc cd) {
                if (!cd.isClassOrInterface()) {
                    throw new IllegalArgumentException(cd.descriptorString());
                }
                String d = cd.descriptorString();
                return loader.getResourceAsStream(d.substring(1, d.length() - 1) + ".class");
            }
        });
    }

    public static ClassHierarchyResolver ofResourceParsing(Function<ClassDesc, InputStream> classStreamResolver) {
        return new ResourceParsingResolver(Objects.requireNonNull(classStreamResolver));
    }

    public static ClassHierarchyResolver ofClassLoading(ClassLoader loader) {
        return new ClassLoadingResolver(Objects.requireNonNull(loader));
    }

    public static ClassHierarchyResolver ofClassLoading(MethodHandles.Lookup lookup) {
        return new LookupResolver(Objects.requireNonNull(lookup));
    }

    static ClassHierarchyInfo infoOf(Class<?> c) {
        if (c.isInterface()) {
            return new ClassHierarchyInfoImpl(ConstantDescs.CD_Object, true);
        }
        Class<?> sup = c.getSuperclass();
        return new ClassHierarchyInfoImpl(sup == null ? null : ClassDesc.of(sup.getName()), false);
    }

    static String binaryName(ClassDesc cd) {
        String d = cd.descriptorString();
        return d.substring(1, d.length() - 1).replace('/', '.');
    }

    private static final class CachedResolver implements ClassHierarchyResolver {
        private static final ClassHierarchyInfo NOPE = new ClassHierarchyInfoImpl(null, true);

        private final ClassHierarchyResolver delegate;
        private final Map<ClassDesc, ClassHierarchyInfo> resolvedCache;

        CachedResolver(ClassHierarchyResolver delegate, Map<ClassDesc, ClassHierarchyInfo> resolvedCache) {
            this.delegate = delegate;
            this.resolvedCache = resolvedCache;
        }

        @Override
        public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
            ClassHierarchyInfo info = resolvedCache.computeIfAbsent(cd, new Function<ClassDesc, ClassHierarchyInfo>() {
                @Override
                public ClassHierarchyInfo apply(ClassDesc k) {
                    ClassHierarchyInfo r = delegate.getClassInfo(k);
                    return r == null ? NOPE : r;
                }
            });
            return info == NOPE ? null : info;
        }
    }

    private static final class ClassLoadingResolver implements ClassHierarchyResolver {
        private final ClassLoader loader;

        ClassLoadingResolver(ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
            if (!cd.isClassOrInterface()) {
                return null;
            }
            try {
                return infoOf(Class.forName(binaryName(cd), false, loader));
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private static final class LookupResolver implements ClassHierarchyResolver {
        private final MethodHandles.Lookup lookup;

        LookupResolver(MethodHandles.Lookup lookup) {
            this.lookup = lookup;
        }

        @Override
        public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
            if (!cd.isClassOrInterface()) {
                return null;
            }
            try {
                return infoOf(lookup.findClass(binaryName(cd)));
            } catch (ClassNotFoundException e) {
                return null;
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static final class ResourceParsingResolver implements ClassHierarchyResolver {
        private final Function<ClassDesc, InputStream> streams;

        ResourceParsingResolver(Function<ClassDesc, InputStream> streams) {
            this.streams = streams;
        }

        @Override
        public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
            try (InputStream in = streams.apply(cd)) {
                return in == null ? null : parse(new DataInputStream(in));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static ClassHierarchyInfo parse(DataInputStream in) throws IOException {
            in.readInt();
            in.readUnsignedShort();
            in.readUnsignedShort();
            int count = in.readUnsignedShort();
            String[] utf8 = new String[count];
            int[] classNames = new int[count];
            for (int i = 1; i < count; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1:
                        utf8[i] = in.readUTF();
                        break;
                    case 7:
                        classNames[i] = in.readUnsignedShort();
                        break;
                    case 8: case 16: case 19: case 20:
                        in.readUnsignedShort();
                        break;
                    case 3: case 4: case 9: case 10: case 11: case 12: case 17: case 18:
                        in.readInt();
                        break;
                    case 5: case 6:
                        in.readLong();
                        i++;
                        break;
                    case 15:
                        in.readUnsignedByte();
                        in.readUnsignedShort();
                        break;
                    default:
                        throw new IllegalStateException("Bad tag (" + tag + ") at index (" + i + ")");
                }
            }
            int flags = in.readUnsignedShort();
            in.readUnsignedShort();
            int superIndex = in.readUnsignedShort();
            ClassDesc sup = superIndex == 0 ? null : ClassDesc.ofInternalName(utf8[classNames[superIndex]]);
            return new ClassHierarchyInfoImpl(sup, (flags & ClassFile.ACC_INTERFACE) != 0);
        }
    }
}
