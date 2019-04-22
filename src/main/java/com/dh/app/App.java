package com.dh.app;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class App {
    public static void main(String[] args) {

        System.out.println("Start: kafka-streams-example");
        
        final Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-streams-example-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "STATE_DIR");
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final GlobalKTable<String, String> table = builder.globalTable("basic-java-producer-topic",
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("TABLE_STORE")
                        .withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
        
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.cleanUp();
        streams.start();

        ReadOnlyKeyValueStore<String, String> stateStore = streams.store("TABLE_STORE", QueryableStoreTypes.<String,String>keyValueStore());
        KeyValueIterator<String, String> stateData = stateStore.all();
        
        while (stateData.hasNext()) {
	        KeyValue<String, String> row = stateData.next();
	        System.out.println(row.key + row.value);
        }

        System.out.println("End: kafka-streams-example");
    }
}
