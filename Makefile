build: coletor java

coletor:
	erlc -I dependencies/erlzmq2/include -o Colector/ Colector/erlzmq.erl
	erlc -I dependencies/erlzmq2/include -o Colector/ Colector/erlzmq_nif.erl
	erlc -o Colector/ Colector/tcpHandler.erl Colector/loginManager.erl

java:
	javac -cp .:dependencies/jar/jeromq-0.5.2.jar Aggregator/*.java
	javac -cp .:dependencies/jar/jeromq-0.5.2.jar Client/*.java
	
clean:
	-@rm Aggregator/*.class
	-@rm Colector/*.beam
	-@rm Client/*.class