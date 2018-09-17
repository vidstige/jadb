#!/bin/sh
set -eu

docker run -it --rm               \
    -v "$(pwd)":/opt/maven        \
    -w /opt/maven maven:3.5-jdk-7 \
     mvn clean install
