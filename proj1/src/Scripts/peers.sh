#!/bin/bash

# Setting current directory
cd ..

# Launching rmi registry
echo "Starting RMI Registry..."
start rmiregistry

#Peers

javac *.java
java PeerInitializer 1.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

echo "Launched Peers"