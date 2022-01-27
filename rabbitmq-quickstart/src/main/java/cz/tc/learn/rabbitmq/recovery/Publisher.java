package cz.tc.learn.rabbitmq.recovery;

import cz.tc.learn.rabbitmq.quickstart.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.time.LocalDateTime;

/**
 * @author tomas.cejka
 *
 * @see
 * https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/Send.java
 */
public class Publisher {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection("publisher");
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            int attempts = 0;
            int limit=1000;
            while (attempts < limit) {
                String message = "Hello World!, time="+LocalDateTime.now();
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "' + time="+System.currentTimeMillis()+"");
                attempts++;
                Thread.sleep(2000);
            }
        }
    }
}
