#!/bin/bash

# Setting current directory
mydir=$(pwd)

# Launching rmi registry
echo "Starting RMI Registry..."
start rmiregistry

#Peers

javac

echo "Launched Peers"