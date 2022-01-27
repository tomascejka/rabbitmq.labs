package cz.tc.learn.rabbitmq.app.producer;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Startup;

/**
 * @author tomas.cejka
 */
@Singleton
@Startup
public class ProducerBean {
    
    private final static Logger LOG = Logger.getLogger(ProducerBean.class.getSimpleName());

    public static final String QUEUE_NAME = "tc_queue";
    private ProducerClient client;
    private AtomicLong index = new AtomicLong(1);

    @PostConstruct
    public void init() {
        client = new ProducerClient();
        client.start(Long.toString(index.getAndIncrement()));
        LOG.log(Level.INFO, "Client (producer) initally created");
    }

    @Lock(LockType.READ)
    public void publish(String message) {
        if (client == null) {
            client = new ProducerClient();
            client.start(Long.toString(index.getAndIncrement()));
            LOG.log(Level.INFO, "Client has been (producer) created");
        } else if (!client.isUp()) {
            client.stop();
            client.start(Long.toString(index.getAndIncrement()));
            LOG.log(Level.INFO, "Client (producer) restarted");
        } else {
            client.publish(message+", i="+index.getAndIncrement()+", time="+LocalDateTime.now());
        }
    }

    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    @Lock(LockType.READ)
    public void publish() {
        if (client == null) {
            client = new ProducerClient();
            client.start(Long.toString(index.getAndIncrement()));
            LOG.log(Level.INFO, "Client has been (producer) created");
        } else if (client.isUp()) {
            for (int i = 0; i < 10; i++) {
                client.publish("Hello message, i="+index.getAndIncrement()+", time="+LocalDateTime.now());                
            }
        } else {
            client.stop();
            client.start(Long.toString(index.getAndIncrement()));
            LOG.log(Level.INFO, "Client (producer) restarted");
        }
    }
    
    @PreDestroy
    public void release() {
        if (client != null) {
            client.stop();
        }
    }
}
