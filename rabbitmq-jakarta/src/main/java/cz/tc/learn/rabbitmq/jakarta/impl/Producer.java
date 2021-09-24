package cz.tc.learn.rabbitmq.jakarta.impl;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author tomas.cejka
 */
@Singleton
@Startup
public class Producer {
    
    public static final String JAKARTA_QUEUE_NAME = "jakarta_queue";
    Connection connection;
    
    @PostConstruct
    public void init() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }
    
    @PreDestroy
    public void release() throws Exception {
        if(connection != null) {
            connection.close();
        }
    }
    
    @Lock(LockType.READ)
    public void publish(String message) throws IOException, TimeoutException{
        try (Channel channel = connection.createChannel()) {
            channel.queueDeclare(JAKARTA_QUEUE_NAME, true, false, false, null);
            String msg = String.join(" ", message);
            channel.basicPublish("", JAKARTA_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + msg + "'");
        }
    }
    
}
