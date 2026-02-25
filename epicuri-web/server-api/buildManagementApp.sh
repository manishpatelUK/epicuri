#! /bin/bash

mvn clean install -DskipTests
cd management
mvn jfx:native -DskipTests
