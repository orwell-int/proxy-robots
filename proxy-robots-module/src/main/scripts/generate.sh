#!/bin/sh
# generate java code for the protobuff definition

DIR=$(cd "$(dirname "$0")" ; pwd)
cd "$DIR/../../../.."
MSG_PATH=$DIR/../../../../messages
echo "Generating .java classes in" $MSG_PATH
protoc --java_out=proxy-robots-module/src/main/java/ --proto_path=$MSG_PATH $MSG_PATH/common.proto $MSG_PATH/controller.proto $MSG_PATH/robot.proto $MSG_PATH/server-game.proto $MSG_PATH/server-web.proto

