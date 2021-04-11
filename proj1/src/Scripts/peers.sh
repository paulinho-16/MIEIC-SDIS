#! /usr/bin/bash
# Setting current directory
# Compile

javac *.java

#Version 1.0
#java PeerInitializer 1.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 1.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 1.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 1.0 4 Peer4 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

#Version 2.0
#java PeerInitializer 2.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 2.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 2.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
#java PeerInitializer 2.0 4 Peer4 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

..\scripts\peer.sh 1.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 1.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 1.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

..\scripts\peer.sh 2.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 4 Peer4 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

echo "Launched Peers"