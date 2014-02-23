#!/bin/zsh
# Build and run program for the computer interface
# Bluetooth must be turned on beforehand and RemoteControl program must be running on the NXT

mkdir -p bin

nxjc -d bin MessageComponent/UnitMessage.java MessageComponent/MessageListenerInterface.java MessageComponent/MessageFrameworkNXT.java MessageComponent/UnitMessageType.java MessageComponent/BluetoothHandler.java

nxjpcc -d bin MessageComponent/MessageFramework.java MessageComponent/MessageFrameworkTester.java 

nxjpc -cp ./bin MessageComponent.MessageFrameworkTester
