# SDIS Project

SDIS Project for group T7 G24.

Group members:

1. Hugo Guimarães up201806490@up.pt
2. Paulo Ribeiro up201806505@up.pt
3. Eduardo Ribeiro up201806271@up.pt
4. Diana Freitas up201806230@up.pt

## Instructions 

> See the Makefile inside the src folder for more information

1. Clone this repository:
   `git clone https://git.fe.up.pt/sdis1/2021/t7/g24/proj2.git`

2. Go to the `src` directory:
   `cd src`

3. Compile all the java classes:
   `make all`

4. Initialize the rmi registry:
   `make rmi`

5. Start creating the peers:
   There are default commands for creating up to 6 peers. You can create them like this, in order:
   `make peer1`, `make peer2`, ...

   If you don't want to run the make instructions, run the direct commands like this:
   ```
   java -Djavax.net.ssl.keyStore=server.keys \
        -Djavax.net.ssl.keyStorePassword=sdisg24 \
        -Djavax.net.ssl.trustStore=truststore \
        -Djavax.net.ssl.trustStorePassword=sdisg24 \

        g24.Peer peer2 0.0.0.0 9050 0.0.0.0 9050
   ```

   This command has two versions:
   1. `g24.Peer <peerAp> <ip> <port>` for creating a new ring
   2. `g24.Peer <peerAp> <ip> <port> <ip_successor> <port_successor>` for joining an existing ring

6. Execute every protocol you want:
   There are default commands for the files inside the `test` folder, for example:
   `make backup`, `make restore3`, `make delete2`, `make reclaim5`, `make state1`, ...

    These protocols commands are the same as those in the first project.
    
    > IMP: 
    > The BACKUP protocol can receive any path from the filesystem, being it relative or absolute.
    > The RESTORE and DELETE protocols should only receive the file name.

7. Clear all the output files:
   `make clean`

8. Clean the peers storage:
   `make delout`
