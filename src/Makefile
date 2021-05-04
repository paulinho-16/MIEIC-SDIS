all:
	javac g24/*.java
	
clean:
	rm g24/*.class && rm g24/*/*.class

rmi:
	rmiregistry &
	sleep 0.5

kill:
	killall rmiregistry
	killall java

delout:
	rm -r g24/output/*

killall: kill clean delout
