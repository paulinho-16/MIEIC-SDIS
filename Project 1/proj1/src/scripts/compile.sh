#! /usr/bin/bash

# Basic compilation script
# To be executed in the root of the package (source code) hierarchy
# Assumes a package structure with only two directory levels
# Compiled code is placed under ./build/
# Modify it if needed to suite your purpose

# javac -d build ./*/*.java

mkdir -p build

javac -d build ./channels/*.java
javac -d build ./client/*.java
javac -d build ./messages/*.java
javac -d build ./peer/*.java
javac -d build ./storage/*.java
javac -d build ./threads/*.java

# (cd ./build; start rmiregistry)

# If you are using jar files, and these must be in some particular
#  place under the build tree, you should copy/move those jar files.
