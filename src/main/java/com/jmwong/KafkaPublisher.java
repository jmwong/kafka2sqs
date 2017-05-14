package com.jmwong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by jamin on 5/14/17.
 */
public class KafkaPublisher {
    public static void main(String args[]) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
        for (int i = 0; i < 1000; i++) {
            String msg = "test " + i;
            ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("magicbus", msg, msg);
            System.out.println(producer.send(producerRecord).get());
        }
    }
}
