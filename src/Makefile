PROPERTIES = 	-Djavax.net.ssl.keyStore=server.keys \
        		-Djavax.net.ssl.keyStorePassword=sdisg24 \
        		-Djavax.net.ssl.trustStore=truststore \
        		-Djavax.net.ssl.trustStorePassword=sdisg24 \

all:
	javac g24/**/*.java g24/*.java
	
clean:
	rm g24/*.class && rm g24/*/*.class

rmi:
	rmiregistry &
	sleep 0.5

kill:
	# killall rmiregistry
	killall java

delout:
	rm -r g24/output/*

killall: kill clean delout

peer1: # 13
	java ${PROPERTIES} g24.Peer peer1 0.0.0.0 9050

peer2: # 5
	java ${PROPERTIES} g24.Peer peer2 0.0.0.0 9100 0.0.0.0 9050

peer3: # 9
	java ${PROPERTIES} g24.Peer peer3 0.0.0.0 9150 0.0.0.0 9050

peer4: # 14
	java ${PROPERTIES} g24.Peer peer4 0.0.0.0 9200 0.0.0.0 9100

peer5: # 8
	java ${PROPERTIES} g24.Peer peer5 0.0.0.0 9260 0.0.0.0 9200

peer6: # 12
	java ${PROPERTIES} g24.Peer peer6 0.0.0.0 9300 0.0.0.0 9050

backup: 
	java g24.TestApp peer1 BACKUP test/large_file.jpg 3

backup2: 
	java g24.TestApp peer4 BACKUP test/medium_file.pdf 7

backup3: 
	java g24.TestApp peer5 BACKUP test/small_file_3.html 4

restore:
	java g24.TestApp peer5 RESTORE large_file.jpg

restore2:
	java g24.TestApp peer3 RESTORE medium_file.pdf

restore3:
	java g24.TestApp peer2 RESTORE small_file_3.html

delete:
	java g24.TestApp peer4 DELETE large_file.jpg

delete2:
	java g24.TestApp peer1 DELETE medium_file.pdf

delete3:
	java g24.TestApp peer3 DELETE small_file_3.html

reclaim:
	java g24.TestApp peer2 RECLAIM 30

state1:
	java g24.TestApp peer1 STATE
state2:
	java g24.TestApp peer2 STATE
state3:
	java g24.TestApp peer3 STATE
state4:
	java g24.TestApp peer4 STATE
state5:
	java g24.TestApp peer5 STATE
state6:
	java g24.TestApp peer6 STATE