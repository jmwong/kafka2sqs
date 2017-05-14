package com.jmwong;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by jamin on 5/13/17.
 */
public class Receive {
    public static void main( String[] args ) {
        Logger logger = Logger.getRootLogger();
        logger.setLevel(Level.WARN);

        AmazonSQSAsync client = new AmazonSQSAsyncClient(new BasicAWSCredentials("x", "x"));
        client.setEndpoint("http://localhost:9324");

        AmazonSQSBufferedAsyncClient asyncClient = new AmazonSQSBufferedAsyncClient(client);

        String queueURL = "http://localhost:9324/queue/test";

        int count = 0;

        while (true) {
            ReceiveMessageResult res = asyncClient.receiveMessage(queueURL);


            if (!res.getMessages().isEmpty()) {
                System.out.println("Length: " + res.getMessages().size());
                for (Message m: res.getMessages()) {
                    System.out.println(m.getBody());
                    asyncClient.deleteMessage(new DeleteMessageRequest(queueURL, m.getReceiptHandle()));
                    count += 1;
                    System.out.println("Received messages: " + count);
                }
            }
        }

//        CreateQueueResult result = client.createQueue("test");
//        System.out.println(result);


    }
}
