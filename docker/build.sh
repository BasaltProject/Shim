#!/bin/sh
# Based on Avian (https://github.com/ReadyTalk/avian).
# Original work Copyright (c) 2008-2015, Avian Contributors.
# Original work licensed under ISC; see LICENSE.txt:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
#
# Modifications Copyright (c) 2026 BasaltMC.
# Modifications licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
#
# SPDX-License-Identifier: ISC AND MIT


if test $# -eq 0; then
    echo "Usage: $0 [--container <container_name>] -- <command_to_run_in_docker>"
    echo "Ex: $0 make test"
    echo "Ex: $0 ./test/ci.sh"
    echo "Ex: $0 --container joshuawarner32/avian-build-windows -- make platform=windows"
    exit 1
fi

THE_USER="-u $(id -u "${USER}")"

while test $# -gt 1 ; do
    key="$1"
    case $key in
        -c|--container)
            shift
            CONTAINER="$1"
            shift
            ;;
        -r|--root)
            shift
            THE_USER=
            ;;
        --)
            shift
            break
            ;;
        *)
            break
            ;;
    esac
done

if test -z $CONTAINER; then
    CONTAINER=joshuawarner32/avian-build
fi

DIR=$(cd $(dirname "$0") && cd .. && pwd)

docker run --rm -i -t -w /var/src/shim -v "${DIR}":/var/src/shim ${THE_USER} "${CONTAINER}" "${@}"
