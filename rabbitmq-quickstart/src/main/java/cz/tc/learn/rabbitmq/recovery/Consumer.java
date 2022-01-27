package cz.tc.learn.rabbitmq.recovery;

import cz.tc.learn.rabbitmq.quickstart.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author tomas.cejka
 * 
 * @see https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/Recv.java
 */
public class Consumer {

    private final static String QUEUE_NAME = "hello";
    private Connection connection;
    
    public void start() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //factory.setAutomaticRecoveryEnabled(true);
        connection = factory.newConnection("consumer");
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
    
    public void stop() throws IOException{
        if(connection != null) {
            connection.close();
        }
    }
}
