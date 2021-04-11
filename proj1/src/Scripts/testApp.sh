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
java TestApp Peer1 BACKUP TestApp/FichaPPIN.pdf 1
java TestApp Peer1 DELETE TestApp/hospital.jpg
java TestApp Peer1 DELETE TestApp/FichaPPIN.pdf
java TestApp Peer1 RESTORE TestApp/FichaPPIN.pdf
java TestApp Peer1 RECLAIM TestApp/FichaPPIN.pdf
java TestApp Peer1 STATE

..\scripts\test.sh Peer3 BACKUP Cartografia.pdf 1
..\scripts\test.sh Peer3 RESTORE Cartografia.pdf

..\scripts\test.sh Peer1 BACKUP FichaPPIN.pdf 1
..\scripts\test.sh Peer1 RESTORE FichaPPIN.pdf

..\scripts\test.sh Peer1 DELETE FichaPPIN.pdf
..\scripts\test.sh Peer1 RECLAIM FichaPPIN.pdf


echo "Launched TestApp"