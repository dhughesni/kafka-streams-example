# kafka-streams-example
Basic Apache Kafka Streams Examples

## References
- https://docs.confluent.io/current/streams/developer-guide/index.html#

# *Start Confluent Stack*
```
$ curl -O http://packages.confluent.io/archive/5.2/confluent-5.2.1-2.12.zip
$ unzip confluent-5.2.1-2.12.zip
$ ./confluent-5.2.1/bin/confluent destroy
$ ./confluent-5.2.1/bin/confluent start
```

# BASIC-JAVA-STREAMS
## Project Setup
```
$ mvn -B archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.dh.app -DartifactId=kafka-streams-example
```
- https://docs.confluent.io/current/streams/developer-guide/write-streams.html
## To Run
```
kafka-consumer-example/basic-java-consumer $ mvn clean compile exec:java -Dexec.mainClass="com.dh.app.App"
```