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

#include "java_props_macosx.h"

PreferredToolkit
getPreferredToolkit()
{
  return XToolkit;
}

void
setOSNameAndVersion(java_props_t* props)
{
  props->os_name = strdup("iOS");
  props->os_version = strdup("Unknown");
}

void
setProxyProperties(java_props_t* props)
{
  // ignore
}

void
setUserHome(java_props_t* props)
{
  // ignore
}

char*
setupMacOSXLocale(int cat)
{
  return 0;
}

char* environ[0];
