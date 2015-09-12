#!/usr/bin/env bash

rm -rf java
mkdir java

echo Compiling for device...
protoc --java_out=java proto/device*

echo Copying to client project...
cp -R java/* ../client/src/main/java

echo Compiling for backend...
protoc --java_out=java proto/*

echo Copying to backend project...
cp -R java/* ../backend/shared/src/main/scala

echo Done!
