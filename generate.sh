# generate java code for the protobuff definition
protoc -I=messages --java_out=./ messages/controller.proto
protoc -I=messages --java_out=./ messages/server-game.proto
protoc -I=messages --java_out=./ messages/server-web.proto
protoc -I=messages --java_out=./ messages/robot.proto
