#!/bin/bash

# Setting current directory
cd ..

# Launching rmi registry
echo "Starting Test Application..."

#TestApp
java TestApp Peer1 BACKUP TestApp/Ficha_de_TrabalhoPPI_-_Estudantes.pdf

echo "Launched TestApp"