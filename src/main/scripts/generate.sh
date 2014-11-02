#!/bin/zsh
# generate java code for the protobuff definition

protoc --java_out=src/main/java/ messages/*.proto

