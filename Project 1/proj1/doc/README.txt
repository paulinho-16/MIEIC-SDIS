SDIS 2020/2021: Project 1 - Distributed Backup Service

In order to compile and run our application you will need to run some scripts.

-- Compilation -- 

Directory: .\proj1\src
Command: .\scripts\compile.sh
Description: Compiles the code inside the .\proj1\src\build directory

-- Peer Execution --

Directory: .\proj1\src\build
Command: ..\scripts\peer.sh <protocolVersion> <peerId> <accessPoint> <mcAddr> <mcPort> <mdbAddr> <mdbPort> <mdrAddr> <mdrPort>
Description: Examples of initialization of 4 peers in both versions:

..\scripts\peer.sh 1.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 1.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 1.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 1.0 4 Peer4 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

..\scripts\peer.sh 2.0 1 Peer1 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 2 Peer2 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 3 Peer3 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002
..\scripts\peer.sh 2.0 4 Peer4 225.0.0.1 8000 225.0.0.1 8001 225.0.0.1 8002

-- Test Application -- 

Directory: .\proj1\src\build
Command: ..\scripts\test.sh <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
Description: Examples of running each of the protocols:

..\scripts\test.sh Peer1 BACKUP FichaPPIN.pdf 1
..\scripts\test.sh Peer1 RESTORE FichaPPIN.pdf
..\scripts\test.sh Peer1 DELETE FichaPPIN.pdf
..\scripts\test.sh Peer1 RECLAIM 0
..\scripts\test.sh Peer1 STATE

NOTE: We advise to execute the STATE protocol using "java client.TestApp Peer1 STATE" instead of the script if you are using Windows,
so that the console doesn't close after terminating the operation and you can have enough time to analyze the state output.

-- Cleanup --

Directory: .\proj1\src\build
Command: ..\scripts\cleanup.sh <peerId>
Description: If 1 argument is passed, the peer with the given peerId will be cleaned up: chunks and restored_files folders become empty.
             If 0 arguments are passed, cleans up all peers by deleting the peers folder, inside the build folder.
             Examples of running this script with and without arguments:

..\scripts\cleanup.sh 1
..\scripts\cleanup.sh

-- Other Notes \ File Setup --

To run our application properly, there must be files in the directory "personal_files" of any peer.

For example, the Peer 1 must place his personal files in the following directory:

.\proj1\src\build\peers\1\personal_files

Each peer can only backup, delete and restore files that are in his "personal_files" folder.
Therefore, you must first setup the project by adding some files to the desired peer directory.
We already have some files distributed between the personal files of some peers, and every example file used can be found under the .\proj1\src\files directory.
Be free to try out all the files you desire, just by dragging them to the personal_files of the peer you want!

The backed up chunks are placed under the directory:

.\proj1\src\build\peers\<peerId>\chunks

The restored files are placed under the directory:

.\proj1\src\build\peers\<peerId>\restored_files

with the name of the original file concatenated with the string "_copy".
For example, a restore of the file Cartografia.pdf would result in a restored file called Cartografia_copy.pdf