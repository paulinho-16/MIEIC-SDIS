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

peer1:
	java ${PROPERTIES} g24.Peer peer1 0.0.0.0 9050

peer3:
	java ${PROPERTIES} g24.Peer peer2 0.0.0.0 9100 0.0.0.0 9150

peer2:
	java ${PROPERTIES} g24.Peer peer3 0.0.0.0 9150 0.0.0.0 9050

peer4:
	java ${PROPERTIES} g24.Peer peer4 0.0.0.0 9200 0.0.0.0 9100

peer5:
	java ${PROPERTIES} g24.Peer peer5 0.0.0.0 9260 0.0.0.0 9200

peer6:
	java ${PROPERTIES} g24.Peer peer6 0.0.0.0 9300 0.0.0.0 9050

backup: 
	java g24.TestApp peer1 BACKUP test/large_file.jpg 4

backup2: 
	java g24.TestApp peer4 BACKUP test/medium_file.pdf 7

backup3: 
	java g24.TestApp peer6 BACKUP test/small_file_3.html 2

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
	java g24.TestApp peer6 DELETE small_file_3.html