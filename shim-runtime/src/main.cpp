/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include <locale.h>
#include "stdlib.h"
#include "stdio.h"
#include "string.h"
#include "jni.h"

#include <shim/system/system.h>
#include "shim/finder.h"

#include <shim/util/runtime-array.h>

#if (defined __MINGW32__) || (defined _MSC_VER)
#define PATH_SEPARATOR ';'
#else
#define PATH_SEPARATOR ':'
#endif

#ifdef _MSC_VER

#define not!
#define or ||
#define and &&
#define xor ^

#endif  // not _MSC_VER

#ifdef BOOT_LIBRARY

// since we aren't linking against libstdc++, we must implement this
// ourselves:
extern "C" void __cxa_pure_virtual(void)
{
  abort();
}

// we link against a System implmentation, which requires this at link
// time, but it should not be used at runtime:
extern "C" uint64_t vmNativeCall(void*, void*, unsigned, unsigned)
{
  abort();
  // abort is not declared __declspec(noreturn) on MSVC, so we have to
  // pretend it might return to make the compiler happy:
  return 0;
}

#endif  // BOOT_LIBRARY

namespace {

const char* mainClass(const char* jar)
{
  using namespace vm;

  System* system = makeSystem();

  class MyAllocator : public shim::util::Alloc {
   public:
    MyAllocator(System* s) : s(s)
    {
    }

    virtual void* allocate(size_t size)
    {
      void* p = s->tryAllocate(size);
      if (p == 0) {
        abort(s);
      }
      return p;
    }

    virtual void free(const void* p, size_t)
    {
      s->free(p);
    }

    System* s;
  } allocator(system);

  Finder* finder = makeFinder(system, &allocator, jar, 0);

  char* result = 0;

  System::Region* region = finder->find("META-INF/MANIFEST.MF");
  if (region) {
    size_t start = 0;
    size_t length;
    while (readLine(region->start(), region->length(), &start, &length)) {
      const unsigned PrefixLength = 12;
      if (strncasecmp("Main-Class: ",
                      reinterpret_cast<const char*>(region->start() + start),
                      PrefixLength) == 0) {
        result = static_cast<char*>(malloc(length + 1 - PrefixLength));
        memcpy(result,
               region->start() + start + PrefixLength,
               length - PrefixLength);
        result[length - PrefixLength] = 0;
        break;
      }
      start += length;
    }

    region->dispose();
  }

  finder->dispose();

  system->dispose();

  return result;
}

void usageAndExit(const char* name)
{
  fprintf(
      stderr,
      "usage: %s\n"
      "\t[{-cp|-classpath} <classpath>]\n"
      "\t[-Xmx<maximum heap size>]\n"
      "\t[-Xss<maximum stack size>]\n"
      "\t[-Xbootclasspath/p:<classpath to prepend to bootstrap classpath>]\n"
      "\t[-Xbootclasspath:<bootstrap classpath>]\n"
      "\t[-Xbootclasspath/a:<classpath to append to bootstrap classpath>]\n"
      "\t[--enable-preview]\n"
      "\t[-D<property name>=<property value> ...]\n"
      "\t{<class name>|-jar <app jar>} [<argument> ...]\n",
      name);
  exit(-1);
}

}  // namespace

int main(int ac, const char** av)
{
#ifdef LC_MESSAGES
  setlocale(LC_MESSAGES, "");
#endif
  JavaVMInitArgs vmArgs;
  vmArgs.version = JNI_VERSION_1_2;
  vmArgs.nOptions = 1;
  vmArgs.ignoreUnrecognized = JNI_TRUE;

  const char* class_ = 0;
  const char* jar = 0;
  int argc = 0;
  const char** argv = 0;
  const char* classpath = ".";
  const char* jdwpOptions = 0;
  const char* agents[16];
  int agentCount = 0;
  int addOpensCount = 0;
  int addExportsCount = 0;

  for (int i = 1; i < ac; ++i) {
    if (strncmp(av[i], "-agentlib:jdwp", 14) == 0 or strncmp(av[i], "-Xrunjdwp:", 10) == 0) {
      jdwpOptions = strchr(av[i], '=');
      if (jdwpOptions == 0) {
        jdwpOptions = strchr(av[i], ':');
      }
      jdwpOptions = jdwpOptions ? jdwpOptions + 1 : "";
    } else if (strcmp(av[i], "-cp") == 0 or strcmp(av[i], "-classpath") == 0) {
      if (i + 1 == ac)
        usageAndExit(av[0]);
      classpath = av[++i];
    } else if (strcmp(av[i], "-jar") == 0) {
      if (i + 1 == ac)
        usageAndExit(av[0]);
      jar = av[++i];
    } else if (strncmp(av[i], "-X", 2) == 0 or strncmp(av[i], "-D", 2) == 0) {
      ++vmArgs.nOptions;
    } else if (strcmp(av[i], "--enable-preview") == 0) {
      ++vmArgs.nOptions;
    } else if (strcmp(av[i], "--add-opens") == 0) {
      if (i + 1 == ac)
        usageAndExit(av[0]);
      ++i;
      if (addOpensCount == 0) {
        ++vmArgs.nOptions;
      }
      ++addOpensCount;
    } else if (strcmp(av[i], "--add-exports") == 0) {
      if (i + 1 == ac)
        usageAndExit(av[0]);
      ++i;
      if (addExportsCount == 0) {
        ++vmArgs.nOptions;
      }
      ++addExportsCount;
    } else if (strcmp(av[i], "--add-reads") == 0 or strcmp(av[i], "--add-modules") == 0
               or strcmp(av[i], "--limit-modules") == 0 or strcmp(av[i], "--patch-module") == 0
               or strcmp(av[i], "--module-path") == 0 or strcmp(av[i], "-p") == 0 or strcmp(av[i], "--upgrade-module-path") == 0) {
      if (i + 1 == ac)
        usageAndExit(av[0]);
      ++i;
    } else if (strncmp(av[i], "--", 2) == 0 and strchr(av[i], '=') != 0) {
      // ignore --option=value launcher flags
    } else if (strcmp(av[i], "-ea") == 0 or strcmp(av[i], "-da") == 0 or strcmp(av[i], "-esa") == 0
               or strcmp(av[i], "-dsa") == 0 or strncmp(av[i], "-enableassertions", 17) == 0
               or strncmp(av[i], "-disableassertions", 18) == 0 or strncmp(av[i], "-ea:", 4) == 0
               or strncmp(av[i], "-da:", 4) == 0 or strcmp(av[i], "-Xshare:auto") == 0) {
      // ignore
    } else if (strncmp(av[i], "-javaagent:", 11) == 0) {
      if (agentCount < 16) {
        agents[agentCount++] = av[i] + 11;
      }
    } else if (strncmp(av[i], "-agentpath:", 11) == 0) {
      fprintf(stderr, "Shim: %s is not supported and is ignored\n", av[i]);
    } else if (strcmp(av[i], "-client") == 0 or strcmp(av[i], "-server") == 0) {
      // ignore
    } else if (strcmp(av[i], "-version") == 0) {
      fprintf(stderr, "Basalt Shim " SHIM_VERSION "\n");
      exit(0);
    } else {
      if (jar == 0) {
        class_ = av[i++];
      }
      if (i < ac) {
        argc = ac - i;
        argv = av + i;
        i = ac;
      }
    }
  }

  if (jar) {
    classpath = jar;

    class_ = mainClass(jar);

    if (class_ == 0) {
      fprintf(stderr, "Main-Class manifest header not found in %s\n", jar);
      exit(-1);
    }
  }

#ifdef BOOT_LIBRARY
  ++vmArgs.nOptions;
#endif

#ifdef BOOT_IMAGE
  vmArgs.nOptions += 2;
#endif

#ifdef BOOT_BUILTINS
  ++vmArgs.nOptions;
#endif

  RUNTIME_ARRAY(JavaVMOption, options, vmArgs.nOptions);
  vmArgs.options = RUNTIME_ARRAY_BODY(options);

  unsigned optionIndex = 0;

#ifdef BOOT_IMAGE
  vmArgs.options[optionIndex++].optionString
      = const_cast<char*>("-Dshim.bootimage=bootimageBin");

  vmArgs.options[optionIndex++].optionString
      = const_cast<char*>("-Dshim.codeimage=codeimageBin");
#endif

#ifdef BOOT_LIBRARY
  vmArgs.options[optionIndex++].optionString
      = const_cast<char*>("-Dshim.bootstrap=" BOOT_LIBRARY);
#endif

#ifdef BOOT_BUILTINS
  vmArgs.options[optionIndex++].optionString
      = const_cast<char*>("-Dshim.builtins=" BOOT_BUILTINS);
#endif

#define CLASSPATH_PROPERTY "-Djava.class.path="

  if (agentCount > 0) {
    size_t total = strlen(classpath) + 1;
    for (int k = 0; k < agentCount; ++k) {
      total += strlen(agents[k]) + 1;
    }
    char* extended = static_cast<char*>(malloc(total + 1));
    strcpy(extended, classpath);
    for (int k = 0; k < agentCount; ++k) {
      const char* eq = strchr(agents[k], '=');
      size_t n = strlen(extended);
      extended[n] = ':';
      size_t len = eq ? static_cast<size_t>(eq - agents[k]) : strlen(agents[k]);
      memcpy(extended + n + 1, agents[k], len);
      extended[n + 1 + len] = 0;
    }
    classpath = extended;
  }
  size_t classpathSize = strlen(classpath);
  size_t classpathPropertyBufferSize = sizeof(CLASSPATH_PROPERTY)
                                         + classpathSize;

  RUNTIME_ARRAY(char, classpathPropertyBuffer, classpathPropertyBufferSize);
  memcpy(RUNTIME_ARRAY_BODY(classpathPropertyBuffer),
         CLASSPATH_PROPERTY,
         sizeof(CLASSPATH_PROPERTY) - 1);
  memcpy(RUNTIME_ARRAY_BODY(classpathPropertyBuffer)
         + sizeof(CLASSPATH_PROPERTY) - 1,
         classpath,
         classpathSize + 1);

  vmArgs.options[optionIndex++].optionString
      = RUNTIME_ARRAY_BODY(classpathPropertyBuffer);

  if (addOpensCount > 0) {
    size_t total = sizeof("-Dshim.add-opens=");
    for (int i = 1; i + 1 < ac; ++i) {
      if (strcmp(av[i], "--add-opens") == 0) {
        total += strlen(av[i + 1]) + 1;
      }
    }
    char* prop = static_cast<char*>(malloc(total + 1));
    strcpy(prop, "-Dshim.add-opens=");
    bool firstOpen = true;
    for (int i = 1; i + 1 < ac; ++i) {
      if (strcmp(av[i], "--add-opens") == 0) {
        if (not firstOpen) {
          strcat(prop, ",");
        }
        firstOpen = false;
        strcat(prop, av[i + 1]);
      }
    }
    vmArgs.options[optionIndex++].optionString = prop;
  }

  if (addExportsCount > 0) {
    size_t total = sizeof("-Dshim.add-exports=");
    for (int i = 1; i + 1 < ac; ++i) {
      if (strcmp(av[i], "--add-exports") == 0) {
        total += strlen(av[i + 1]) + 1;
      }
    }
    char* prop = static_cast<char*>(malloc(total + 1));
    strcpy(prop, "-Dshim.add-exports=");
    bool firstExport = true;
    for (int i = 1; i + 1 < ac; ++i) {
      if (strcmp(av[i], "--add-exports") == 0) {
        if (not firstExport) {
          strcat(prop, ",");
        }
        firstExport = false;
        strcat(prop, av[i + 1]);
      }
    }
    vmArgs.options[optionIndex++].optionString = prop;
  }

  for (int i = 1; i < ac; ++i) {
    if (strncmp(av[i], "-Xrunjdwp:", 10) == 0) {
      continue;
    }
    if (strncmp(av[i], "-X", 2) == 0 or strncmp(av[i], "-D", 2) == 0) {
      vmArgs.options[optionIndex++].optionString = const_cast<char*>(av[i]);
    } else if (strcmp(av[i], "--enable-preview") == 0) {
      vmArgs.options[optionIndex++].optionString = const_cast<char*>("-Xenable-preview");
    }
  }

  if (class_ == 0) {
    usageAndExit(av[0]);
  }

  JavaVM* vm;
  void* env;
  JNI_CreateJavaVM(&vm, &env, &vmArgs);
  JNIEnv* e = static_cast<JNIEnv*>(env);

  if (jdwpOptions and not e->ExceptionCheck()) {
    jclass agent = e->FindClass("shim/jdwp/Agent");
    if (not e->ExceptionCheck()) {
      jmethodID start = e->GetStaticMethodID(agent, "start", "(Ljava/lang/String;)V");
      if (not e->ExceptionCheck()) {
        e->CallStaticVoidMethod(agent, start, e->NewStringUTF(jdwpOptions));
      }
    }
    if (e->ExceptionCheck()) {
      e->ExceptionDescribe();
      e->ExceptionClear();
    }
  }

  for (int k = 0; k < agentCount and not e->ExceptionCheck(); ++k) {
    jclass cls = e->FindClass("shim/Agents");
    if (not e->ExceptionCheck()) {
      jmethodID start = e->GetStaticMethodID(cls, "start", "(Ljava/lang/String;)V");
      if (not e->ExceptionCheck()) {
        e->CallStaticVoidMethod(cls, start, e->NewStringUTF(agents[k]));
      }
    }
  }

  if (jar and not e->ExceptionCheck()) {
    jclass cls = e->FindClass("shim/Agents");
    if (not e->ExceptionCheck()) {
      jmethodID start = e->GetStaticMethodID(cls, "launcher", "(Ljava/lang/String;)V");
      if (not e->ExceptionCheck()) {
        e->CallStaticVoidMethod(cls, start, e->NewStringUTF(jar));
      }
    }
  }

  jclass c = 0;
  if (not e->ExceptionCheck()) {
    c = e->FindClass(class_);
  }

  if (jar) {
    free(const_cast<char*>(class_));
  }

  if (not e->ExceptionCheck()) {
    jmethodID m = e->GetStaticMethodID(c, "main", "([Ljava/lang/String;)V");
    if (not e->ExceptionCheck()) {
      jclass stringClass = e->FindClass("java/lang/String");
      if (not e->ExceptionCheck()) {
        jobjectArray a = e->NewObjectArray(argc, stringClass, 0);
        if (not e->ExceptionCheck()) {
          for (int i = 0; i < argc; ++i) {
            e->SetObjectArrayElement(a, i, e->NewStringUTF(argv[i]));
          }

          e->CallStaticVoidMethod(c, m, a);
        }
      }
    }
  }

  int exitCode = 0;
  if (e->ExceptionCheck()) {
    exitCode = -1;
    e->ExceptionDescribe();
  }

  vm->DestroyJavaVM();

  return exitCode;
}
