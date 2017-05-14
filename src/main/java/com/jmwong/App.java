package com.jmwong;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.buffered.QueueBufferConfig;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    private static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        AmazonSQSAsync client = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:9324", ""))
                .build();

        QueueBufferConfig conf = new QueueBufferConfig()
                .withFlushOnShutdown(true);
        final AmazonSQSBufferedAsyncClient asyncClient = new AmazonSQSBufferedAsyncClient(client, conf);


//      CreateQueueResult result = client.createQueue("test");
//      System.out.println(result);
//

        String QUEUE_NAME = "test";
        final String queueUrl = asyncClient.getQueueUrl(QUEUE_NAME).getQueueUrl();


        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:2181");
        props.put("group.id", "jmwong-test");
        ConsumerConfig consumerConf = new ConsumerConfig(props);

        final ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConf);


        TopicFilter filter = new Whitelist("magicbus");

        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valDecoder = new StringDecoder(new VerifiableProperties());
        List<KafkaStream<String, String>> streams = consumer.createMessageStreamsByFilter(filter, 1, keyDecoder, valDecoder);
        logger.info("Got streams. Length: " + streams.size());
        final ExecutorService executor = Executors.newFixedThreadPool(streams.size());

        // Make sure everything gets flushed
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Starting graceful shutdown");

                logger.info("Shutting down Kafka consumer");
                consumer.shutdown();

                logger.info("Shutting down Kafka stream executor");
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.info("Kafka sream executor shutdown timed out");
                    }
                } catch (InterruptedException e) {
                    logger.info("Kafka stream executor did not shutdown cleanly");
                }

                logger.info("Shutting down SQS client");
                asyncClient.shutdown();

                logger.info("Graceful shutdown completed");
            }
        });

        for (final KafkaStream<String, String> s: streams) {
            executor.submit(new Runnable() {
                public void run() {
                    for (MessageAndMetadata<String, String> msgAndMedata: s) {
                        String msg = msgAndMedata.message();
                        logger.info("Got Kafka message: " + msg);
                        SendMessageRequest req = new SendMessageRequest(queueUrl, msg);
                        asyncClient.sendMessageAsync(req);
                    }
                }
            });
        }


    }
}
