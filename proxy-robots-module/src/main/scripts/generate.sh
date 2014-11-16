#!/bin/zsh
# generate java code for the protobuff definition

protoc --java_out=proxy-robots-module/src/main/java/ messages/*.proto

