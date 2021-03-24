#!/bin/bash

# Setting current directory
cd ..

# Launching rmi registry
echo "Starting RMI Registry..."
start rmiregistry

# Launching rmi registry
echo "Starting Test Application..."

#TestApp
java TestApp Peer1 BACKUP TestApp/hospital.jpg 1
# java TestApp Peer1 BACKUP TestApp/FichaPPIN.pdf 1
# java TestApp Peer1 DELETE TestApp/hospital.jpg
# java TestApp Peer1 DELETE TestApp/FichaPPIN.pdf
# java TestApp Peer1 RESTORE TestApp/FichaPPIN.pdf
# java TestApp Peer1 RECLAIM TestApp/FichaPPIN.pdf

echo "Launched TestApp"