/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ScriptEngineManager {
  private final HashSet<ScriptEngineFactory> engineSpis = new HashSet<ScriptEngineFactory>();
  private final HashMap<String, ScriptEngineFactory> nameAssociations = new HashMap<String, ScriptEngineFactory>();
  private final HashMap<String, ScriptEngineFactory> extensionAssociations =
      new HashMap<String, ScriptEngineFactory>();
  private final HashMap<String, ScriptEngineFactory> mimeTypeAssociations =
      new HashMap<String, ScriptEngineFactory>();
  private Bindings globalScope;

  public ScriptEngineManager() {
    init(Thread.currentThread().getContextClassLoader());
  }

  public ScriptEngineManager(ClassLoader loader) {
    init(loader);
  }

  private void init(final ClassLoader loader) {
    globalScope = new SimpleBindings();
    initEngines(loader);
  }

  private void initEngines(final ClassLoader loader) {
    ServiceLoader<ScriptEngineFactory> loaded = loader != null ? ServiceLoader.load(ScriptEngineFactory.class, loader)
        : ServiceLoader.loadInstalled(ScriptEngineFactory.class);
    Iterator<ScriptEngineFactory> itr = loaded.iterator();
    while (true) {
      try {
        if (!itr.hasNext()) {
          break;
        }
        engineSpis.add(itr.next());
      } catch (ServiceConfigurationError err) {
        continue;
      }
    }
  }

  public void setBindings(Bindings bindings) {
    if (bindings == null) {
      throw new IllegalArgumentException("Global scope cannot be null.");
    }
    globalScope = bindings;
  }

  public Bindings getBindings() {
    return globalScope;
  }

  public void put(String key, Object value) {
    globalScope.put(key, value);
  }

  public Object get(String key) {
    return globalScope.get(key);
  }

  public ScriptEngine getEngineByName(String shortName) {
    java.util.Objects.requireNonNull(shortName);
    Object obj;
    if (null != (obj = nameAssociations.get(shortName))) {
      ScriptEngineFactory spi = (ScriptEngineFactory) obj;
      try {
        ScriptEngine engine = spi.getScriptEngine();
        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
        return engine;
      } catch (Exception exp) {
      }
    }

    for (ScriptEngineFactory spi : engineSpis) {
      List<String> names = null;
      try {
        names = spi.getNames();
      } catch (Exception exp) {
      }
      if (names != null) {
        for (String name : names) {
          if (shortName.equals(name)) {
            try {
              ScriptEngine engine = spi.getScriptEngine();
              engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
              return engine;
            } catch (Exception exp) {
            }
          }
        }
      }
    }

    return null;
  }

  public ScriptEngine getEngineByExtension(String extension) {
    java.util.Objects.requireNonNull(extension);
    Object obj;
    if (null != (obj = extensionAssociations.get(extension))) {
      ScriptEngineFactory spi = (ScriptEngineFactory) obj;
      try {
        ScriptEngine engine = spi.getScriptEngine();
        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
        return engine;
      } catch (Exception e) {
      }
    }

    for (ScriptEngineFactory spi : engineSpis) {
      List<String> exts = null;
      try {
        exts = spi.getExtensions();
      } catch (Exception exp) {
      }
      if (exts == null) {
        continue;
      }
      for (String ext : exts) {
        if (extension.equals(ext)) {
          try {
            ScriptEngine engine = spi.getScriptEngine();
            engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
            return engine;
          } catch (Exception exp) {
          }
        }
      }
    }
    return null;
  }

  public ScriptEngine getEngineByMimeType(String mimeType) {
    java.util.Objects.requireNonNull(mimeType);
    Object obj;
    if (null != (obj = mimeTypeAssociations.get(mimeType))) {
      ScriptEngineFactory spi = (ScriptEngineFactory) obj;
      try {
        ScriptEngine engine = spi.getScriptEngine();
        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
        return engine;
      } catch (Exception e) {
      }
    }

    for (ScriptEngineFactory spi : engineSpis) {
      List<String> types = null;
      try {
        types = spi.getMimeTypes();
      } catch (Exception exp) {
      }
      if (types == null) {
        continue;
      }
      for (String type : types) {
        if (mimeType.equals(type)) {
          try {
            ScriptEngine engine = spi.getScriptEngine();
            engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
            return engine;
          } catch (Exception exp) {
          }
        }
      }
    }
    return null;
  }

  public List<ScriptEngineFactory> getEngineFactories() {
    List<ScriptEngineFactory> res = new ArrayList<ScriptEngineFactory>(engineSpis.size());
    for (ScriptEngineFactory spi : engineSpis) {
      res.add(spi);
    }
    return java.util.Collections.unmodifiableList(res);
  }

  public void registerEngineName(String name, ScriptEngineFactory factory) {
    if (name == null || factory == null) {
      throw new NullPointerException();
    }
    nameAssociations.put(name, factory);
  }

  public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
    if (type == null || factory == null) {
      throw new NullPointerException();
    }
    mimeTypeAssociations.put(type, factory);
  }

  public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
    if (extension == null || factory == null) {
      throw new NullPointerException();
    }
    extensionAssociations.put(extension, factory);
  }
}
