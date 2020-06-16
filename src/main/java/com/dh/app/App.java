package com.dh.app;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
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
        
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-streams-example-client2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.STATE_DIR_CONFIG, "STATE_DIR");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        // *GlobalKTable Example*
        final GlobalKTable<String, String> table = builder.globalTable("basic-java-producer-topic1234",
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("TABLE_STORE")
                        .withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
        
        // *Stream Join Example*
        /*
         * NOTE: messages must be produced with the same KEY
         * Example output of filter/mapValue/groupByKey/aggregate
         * positive
		 * positive1-MAPPED
		 * /negative1-MAPPED/positive0-MAPPED/positive1-MAPPED
		 * positive
		 * positive2-MAPPED
		 * /negative1-MAPPED/positive0-MAPPED/positive1-MAPPED/positive2-MAPPED
		 * positive
		 * positive3-MAPPED
		 * /negative1-MAPPED/positive0-MAPPED/positive1-MAPPED/positive2-MAPPED/positive3-MAPPED
		 * positive
		 * positive4-MAPPED
		 * /negative1-MAPPED/positive0-MAPPED/positive1-MAPPED/positive2-MAPPED/positive3-MAPPED/positive4-MAPPED
         */
        final KStream<String, String> stream = builder.stream("basic-python-producer-topic", Consumed.with(Serdes.String(), Serdes.String()));
        stream.filter(
        	new Predicate<String, String>() {
            public boolean test(String key, String value) {
            	if (value.contains("positive")) {
            		System.out.println("positive");
            		return true;
            	}
            	if (value.contains("negative")) {
            		System.out.println("negative");
            		return true;
            	}
              return false;
            }
          })
        .mapValues(
        		new ValueMapper<String,String>() {
        		      public String apply(String s) {
        		    	  System.out.println(s+"-MAPPED");
        		        return s+"-MAPPED";
        		      }
        		    })
        .groupByKey()
        .aggregate(
        		new Initializer<String>() { 
        			  /* initializer */
        		      public String apply() {
        		        return "";
        		      }
        		    },
        		    new Aggregator<String, String, String>() { /* adder */
        		      public String apply(String aggKey, String newValue, String aggValue) {
        		    	  System.out.println(aggValue +"/"+ newValue);
        		        return aggValue +"/"+ newValue;
        		      }
        		    },
        		    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("TABLE_STORE_AGG")
                    .withKeySerde(Serdes.String()).withValueSerde(Serdes.String()));
        
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081");
        final Serde<GenericRecord> valueGenericAvroSerde = new GenericAvroSerde();
        valueGenericAvroSerde.configure(serdeConfig, false); // `false` for record values
        final KStream<String, GenericRecord> avro_java_producer_stream = builder.stream("avro-java-producer-topic", Consumed.with(Serdes.String(), valueGenericAvroSerde));
//        

//        final KStream<String, GenericRecord> sjoin = basic_java_producer_stream.join(otherStream, joiner, windows, joined)
        
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
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
