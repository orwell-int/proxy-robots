#!/bin/zsh
# Build and run program for the computer interface
# Bluetooth must be turned on beforehand and RemoteControl program must be running on the NXT

mkdir -p build/nxt

nxjc -d build/nxt proxy-robots-module/src/main/java/orwell/common/UnitMessage.java proxy-robots-module/src/main/java/orwell/common/MessageListenerInterface.java proxy-robots-module/src/main/java/orwell/robots/MessageFrameworkNXT.java proxy-robots-module/src/main/java/orwell/common/UnitMessageType.java proxy-robots-module/src/main/java/orwell/robots/BluetoothHandler.java

nxjpcc -d build/nxt proxy-robots-module/src/main/java/orwell/proxy/MessageFramework.java proxy-robots-module/src/main/java/orwell/proxy/MessageFrameworkTester.java 

nxjpc -cp .build/nxt MessageComponent.MessageFrameworkTester
