#!/bin/bash
# Based on Avian (https://github.com/ReadyTalk/avian).
# Original work Copyright (c) 2008-2015, Avian Contributors.
# Original work licensed under ISC; see LICENSE.txt:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
# Includes work Copyright (c) 2014 Christopher M Balz (aws-mock), under MIT:
# https://github.com/BasaltProject/Shim/blob/main/docs/licenses/aws-mock-MIT.txt
#
# Modifications Copyright (c) 2026 BasaltMC.
# Modifications licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
#
# SPDX-License-Identifier: ISC AND MIT

# This script was originally written by maxiaohao in the aws-mock GitHub project.
# https://github.com/treelogic-swe/aws-mock/

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then

  echo "Start to publish lastest Javadoc to gh-pages..."

  cd build/

  if test -d gh-pages/avian-web
  then
    cd gh-pages/avian-web
    git pull
  else
    git clone --quiet https://${GH_TOKEN}@github.com/ReadyTalk/readytalk.github.io gh-pages > /dev/null
    cd gh-pages/avian-web
    git config user.email "travis@travis-ci.org"
    git config user.name "travis-ci"
  fi

  git rm -rf ./javadoc
  cp -Rf ../javadoc ./javadoc
  git add -f .
  git commit -m "Latest javadoc on successful Travis build $TRAVIS_BUILD_NUMBER auto-pushed to readytalk.github.io"
  if ! git push -fq origin master &> /dev/null; then
    echo "Error pushing gh-pages to origin. Bad GH_TOKEN? GitHub down?"
  else
    echo "Done magic with auto publishment to readytalk.github.io."
  fi
fi
