#!/bin/bash

# Setting current directory
cd ..

javac *.java
java PeerInitializer 1.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1

echo "Launched Peers"