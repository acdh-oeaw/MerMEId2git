#!/bin/bash

mvn clean compile assembly:single

printf "\nCopying settings.xml\n"
cp settings.xml target/

exit 0
