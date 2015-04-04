#!/bin/sh
# generate java code for the protobuff definition

DIR=$(cd "$(dirname "$0")" ; pwd)
cd "$DIR/../../../.."
MSG_PATH=$DIR/../../../../messages
echo "Generating .java classes in" $MSG_PATH
protoc --java_out=proxy-robots-module/src/main/java/ --proto_path=$MSG_PATH $MSG_PATH/*.proto

