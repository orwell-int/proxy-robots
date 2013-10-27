# generate java code for the protobuff definition
protoc -I=messages --java_out=GeneratedMessages/ messages/version1.proto
protoc -I=messages --java_out=GeneratedMessages/ messages/controller.proto
protoc -I=messages --java_out=GeneratedMessages/ messages/server-game.proto
protoc -I=messages --java_out=GeneratedMessages/ messages/server-web.proto
protoc -I=messages --java_out=GeneratedMessages/ messages/robot.proto
