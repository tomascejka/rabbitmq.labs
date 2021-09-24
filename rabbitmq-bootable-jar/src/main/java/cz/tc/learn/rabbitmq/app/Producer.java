package cz.tc.learn.rabbitmq.app;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tomas.cejka
 */
@Singleton
@Startup
public class Producer {

    public static final String JAKARTA_QUEUE_NAME = "jakarta_v2_queue";
    Connection connection;

    @PostConstruct
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException("Connection to rabbitmq is not established", e);
        }
    }

    @PreDestroy
    public void release() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Lock(LockType.READ)
    public void publish(String message) {
        try (Channel channel = connection.createChannel()) {
            channel.queueDeclare(JAKARTA_QUEUE_NAME, true, false, false, null);
            String msg = String.join(" ", message);
            channel.basicPublish("", JAKARTA_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + msg + "'");
        } catch (IOException | TimeoutException e) {
            Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
