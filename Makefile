build: coletor java

coletor:
	erlc -o Collector/ Collector/tcpHandler.erl Collector/loginManager.erl

java:
	javac -cp .:dependencies/jar/jeromq-0.5.2.jar Aggregator/*.java
	javac -cp .:dependencies/jar/jeromq-0.5.2.jar Client/*.java
	javac Device/*.java
	
clean:
	-@rm Aggregator/*.class
	-@rm Collector/*.beam
	-@rm Client/*.class
	-@rm Device/*.class
