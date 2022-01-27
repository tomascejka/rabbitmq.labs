package cz.tc.learn.rabbitmq.app.consumer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import static cz.tc.learn.rabbitmq.app.producer.ProducerBean.QUEUE_NAME;

/**
 *
 * @author tomas.cejka
 */
@Singleton
@Startup
public class ConsumerBean {

    private final static Logger LOG = Logger.getLogger(ConsumerBean.class.getSimpleName());

    private ConsumerClient client;
    private AtomicInteger panicLimit = new AtomicInteger(1);
    
    @Schedule(minute = "*/2", hour = "*", persistent = false)
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @Lock(LockType.WRITE)
    public void check() {
        String host = "localhost";
        String queueName = QUEUE_NAME;
        String nodeName = "consumer_tc";
        boolean verbose = false;

        if (client == null) {
            client = new ConsumerClient();
            boolean state = client.start(host, queueName, nodeName, verbose);
            LOG.log(state ? Level.INFO : Level.WARNING, "Client (consumer) {0} STARTED", state ? "has been succesfully" : "is not");
            if(!state)panicLimit.getAndIncrement();
        } else if (client.isUp()) {
            LOG.log(Level.INFO, "Client (consumer) is already RUNNING ...");
            panicLimit.set(0);
        } else {
            client.stop();
            boolean state = client.start(host, queueName, nodeName, verbose);
            LOG.log(state ? Level.INFO : Level.WARNING, "Client (consumer) {0} RESTARTED", state ? "has been succesfully" : "is not");
            if(!state)panicLimit.getAndIncrement();
        }
        
        if(panicLimit.get()>3) {
            LOG.log(Level.SEVERE, "Client (consumer) has not been recovered for 3 times. Panic mail has been sent!");
            panicLimit.set(0);
        }

    }

    @PreDestroy
    public void release() {
        if (client != null) {
            client.stop();
        }
    }

}
