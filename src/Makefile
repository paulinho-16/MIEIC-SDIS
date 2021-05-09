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
	java ${PROPERTIES} g24.Peer peer1 0.0.0.0 9050 &

peer3:
	java ${PROPERTIES} g24.Peer peer2 0.0.0.0 9100 0.0.0.0 9150 &

peer2:
	java ${PROPERTIES} g24.Peer peer3 0.0.0.0 9150 0.0.0.0 9050 &

peer4:
	java ${PROPERTIES} g24.Peer peer4 0.0.0.0 9200 0.0.0.0 9100 &

peer5:
	java ${PROPERTIES} g24.Peer peer5 0.0.0.0 9260 0.0.0.0 9200 &

peer6:
	java ${PROPERTIES} g24.Peer peer6 0.0.0.0 9300 0.0.0.0 9050 &

peers: peer1 peer2 peer3 peer4 peer5


backup: 
	java g24.TestApp peer1 BACKUP Makefile 2