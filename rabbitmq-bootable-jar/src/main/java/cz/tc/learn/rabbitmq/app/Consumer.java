package cz.tc.learn.rabbitmq.app;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import static cz.tc.learn.rabbitmq.app.Producer.JAKARTA_QUEUE_NAME;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tomas.cejka
 */
@Singleton
@Startup
public class Consumer {
    
    private final AtomicLong counter = new AtomicLong(0);

    @PostConstruct
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();) {
                Channel channel = connection.createChannel();
                channel.queueDeclare(JAKARTA_QUEUE_NAME, true, false, false, null);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                channel.basicQos(1);
                DeliverCallback callback = (String consumerTag, Delivery delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    try {
                        //doWork(message);
                    } finally {
                        counter.getAndIncrement();
                        System.out.println(" [x] Done");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                };
                channel.basicConsume(JAKARTA_QUEUE_NAME, false, callback, consumerTag -> {
                });
            } catch (IOException | TimeoutException e) {
                throw new IllegalStateException("Connection to rabbitmq is not established", e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("ConnectionFactory to rabbitmq is not established", e);
        }
    }

    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
