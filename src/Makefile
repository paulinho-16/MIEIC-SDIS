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

peer2:
	java ${PROPERTIES} g24.Peer peer2 0.0.0.0 9100 0.0.0.0 9050 &

peer3:
	java ${PROPERTIES} g24.Peer peer3 0.0.0.0 9150 0.0.0.0 9100 &

peer4:
	java ${PROPERTIES} g24.Peer peer4 0.0.0.0 9200 0.0.0.0 9150 &

peer5:
	java ${PROPERTIES} g24.Peer peer5 0.0.0.0 9250 0.0.0.0 9200 &

peers: peer1 peer2 peer3 peer4 peer5