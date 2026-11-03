/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

import java.util.Hashtable;
import javax.naming.CannotProceedException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

public class DirectoryManager extends NamingManager {
  DirectoryManager() {
  }

  public static DirContext getContinuationDirContext(CannotProceedException cpe) throws NamingException {
    Context c = NamingManager.getContinuationContext(cpe);
    if (c instanceof DirContext) {
      return (DirContext) c;
    }
    throw cpe;
  }

  public static Object getObjectInstance(Object refInfo, Name name, Context nameCtx, Hashtable<?, ?> environment,
      Attributes attrs) throws Exception {
    ObjectFactory factory;
    ObjectFactoryBuilder builder = getObjectFactoryBuilder();
    if (builder != null) {
      factory = builder.createObjectFactory(refInfo, environment);
      if (factory instanceof DirObjectFactory) {
        return ((DirObjectFactory) factory).getObjectInstance(refInfo, name, nameCtx, environment, attrs);
      }
      return factory.getObjectInstance(refInfo, name, nameCtx, environment);
    }
    Reference ref = null;
    if (refInfo instanceof Reference) {
      ref = (Reference) refInfo;
    } else if (refInfo instanceof Referenceable) {
      ref = ((Referenceable) (refInfo)).getReference();
    }
    if (ref != null && ref.getFactoryClassName() != null) {
      factory = getObjectFactoryFromReference(ref);
      if (factory instanceof DirObjectFactory) {
        return ((DirObjectFactory) factory).getObjectInstance(ref, name, nameCtx, environment, attrs);
      } else if (factory != null) {
        return factory.getObjectInstance(ref, name, nameCtx, environment);
      }
      return refInfo;
    }
    String list = environment == null ? null : (String) environment.get(Context.OBJECT_FACTORIES);
    if (list != null) {
      java.util.StringTokenizer parser = new java.util.StringTokenizer(list, ":");
      while (parser.hasMoreTokens()) {
        try {
          factory = (ObjectFactory) loadClass(parser.nextToken()).getDeclaredConstructor().newInstance();
          Object answer = factory instanceof DirObjectFactory
              ? ((DirObjectFactory) factory).getObjectInstance(refInfo, name, nameCtx, environment, attrs)
              : factory.getObjectInstance(refInfo, name, nameCtx, environment);
          if (answer != null) {
            return answer;
          }
        } catch (ReflectiveOperationException e) {
        }
      }
    }
    return NamingManager.getObjectInstance(refInfo, name, nameCtx, environment);
  }

  public static DirStateFactory.Result getStateToBind(Object obj, Name name, Context nameCtx,
      Hashtable<?, ?> environment, Attributes attrs) throws NamingException {
    String list = environment == null ? null : (String) environment.get(Context.STATE_FACTORIES);
    if (list != null) {
      java.util.StringTokenizer parser = new java.util.StringTokenizer(list, ":");
      while (parser.hasMoreTokens()) {
        try {
          StateFactory factory = (StateFactory) loadClass(parser.nextToken()).getDeclaredConstructor().newInstance();
          if (factory instanceof DirStateFactory) {
            DirStateFactory.Result res = ((DirStateFactory) factory).getStateToBind(obj, name, nameCtx, environment,
                attrs);
            if (res != null) {
              return res;
            }
          } else {
            Object answer = factory.getStateToBind(obj, name, nameCtx, environment);
            if (answer != null) {
              return new DirStateFactory.Result(answer, attrs);
            }
          }
        } catch (NamingException e) {
          throw e;
        } catch (ReflectiveOperationException e) {
        }
      }
    }
    return new DirStateFactory.Result(obj, attrs);
  }
}
