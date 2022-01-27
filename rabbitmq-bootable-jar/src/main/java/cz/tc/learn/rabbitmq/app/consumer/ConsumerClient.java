package cz.tc.learn.rabbitmq.app.consumer;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tomas.cejka
 */
public class ConsumerClient {

    private final static Logger LOG = Logger.getLogger(ConsumerClient.class.getSimpleName());

    private final AtomicLong index = new AtomicLong(1);
    private Connection connection;

    private String queueName;
    private String connectionName;
    private String consumerTag;
    
    private boolean verbose;

    public boolean start(String host, String queueName, String nodeName, boolean verbose) {
        ConnectionFactory factory;
        try {
            factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);//5s
            factory.setRequestedHeartbeat(60);//60s
        } catch (Exception e) {
            throw new IllegalStateException("ConnectionFactory to rabbitmq is not established", e);
        }
        if (factory == null) {
            throw new IllegalStateException("ConnectionFactory is null");
        }
        this.queueName = queueName;
        this.connectionName = queueName + "_" + nodeName;
        this.consumerTag = connectionName + "_" + (index.getAndIncrement());
        try //(Connection connection = factory.newConnection("consumer_jakarta");) {
        {
            LOG.log(Level.INFO, "CN={0} Client try to start", connectionName);
            connection = factory.newConnection(connectionName);
            connection.addShutdownListener(new ClientShutdownListener());
            ((Recoverable)connection).addRecoveryListener(new ClientRecoveryListener("connection"));
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            //Logger.getLogger(ClientConsumer.class.getName()).log(Level.INFO," [*] Waiting for messages. To exit press CTRL+C");

            channel.basicQos(1);
            DeliverCallback callback = (String consumerTag1, Delivery delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                Long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                try {
                    LOG.log(Level.INFO, " [x] Received ' {0}'", message);
                    channel.basicAck(deliveryTag, false);
                    LOG.log(Level.INFO, " [x] Done");
                } catch (Exception e) {
                    if(verbose) {
                        LOG.log(Level.SEVERE, " [x] Message has been rejected reason={0}", e.getMessage());
                    } else {
                        LOG.log(Level.SEVERE, " [x] Message has been REJECTED");
                    }
                    channel.basicNack(deliveryTag, false, false);
                }
            };
            LOG.log(Level.INFO, "CN={0} Client started", connectionName);
            ((Recoverable)channel).addRecoveryListener(new ClientRecoveryListener("channel"));
            channel.basicConsume(queueName, false, consumerTag, callback, (String consumerTag1) -> {
                LOG.log(Level.WARNING, " [x] Consumer cancelled ' {}'", consumerTag1);
            });
            return true;
        } catch (IOException | TimeoutException e) {
            //throw new IllegalStateException("Connection to rabbitmq is not established", e);
            if(verbose) {
                LOG.log(Level.SEVERE, "Connection to rabbitmq is not established", e);
            } else {
                LOG.log(Level.SEVERE, "Connection to rabbitmq IS NOT ESTABLISHED");
            }
            return false;
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
                    if(verbose) {
                        LOG.log(Level.SEVERE, "Connection cannot be closed now", ex);
                    } else {
                        LOG.log(Level.SEVERE, "Connection CANNOT BE CLOSED now");
                    }
                }
            }
            return false;
        }
    }

    public void stop() {
        if (connection != null) {
            try {
                LOG.log(Level.INFO, "Client tries to stop");
                connection.close();
                LOG.log(Level.INFO, "Client is successlly stopped");
            } catch (AlreadyClosedException e) {
                if(verbose) {
                    LOG.log(Level.WARNING, "Connection is already closed"/*, e*/);//to neni chyba, o jiz zavrenem spojeni
                } else {
                    LOG.log(Level.WARNING, "Connection is ALREADY CLOSED");
                }
            } catch (IOException ex) {
                if(verbose) {
                    LOG.log(Level.SEVERE, "Connection cannot be closed", ex);
                } else {
                    LOG.log(Level.SEVERE, "Connection cannot be CLOSED");
                }
            }
        }
    }

    public boolean isUp() {
        if (connection == null) {
            return false;
        } else {
            try {
                return connection.isOpen();
            } catch (Exception e) {
                if(verbose) {
                    LOG.log(Level.SEVERE, "Connection is not open", e);
                } else {
                    LOG.log(Level.SEVERE, "Connection IS NOT OPEN");
                }
                return false;
            }
        }
    }

    /**
     * Listener, pro informovani o error stavu - shutdown protocol. V
     * prvni/druhe versi klienta jsem se mylne domnival, ze lze pouzit pro
     * obnovovani spojeni (nikoli - na server se mi pak vytvareli duplicitni
     * spojeni, viz. IT-22142).
     *
     * @see https://www.rabbitmq.com/api-guide.html#shutdown
     */
    private class ClientShutdownListener implements ShutdownListener {

        private final Logger LOG_CSL = Logger.getLogger(ClientShutdownListener.class.getSimpleName());

        @Override
        public void shutdownCompleted(ShutdownSignalException cause) {
            if (cause instanceof ShutdownSignalException) {
                ShutdownSignalException c = ((ShutdownSignalException) cause);
                if (c.isHardError()) {
                    Connection conn = (Connection) c.getReference();
                    if (!c.isInitiatedByApplication()) {
                        LOG_CSL.log(Level.SEVERE, "[QN={0}, CN={1}] Connection shutdown, reason={2}", new Object[]{queueName, connectionName, c.getReason()});
                    }
                } else {
                    Channel ch = (Channel) c.getReference();
                    LOG_CSL.log(Level.SEVERE, "[QN={0}, CN={1}] Channel shutdown, reason={2}", new Object[]{queueName, connectionName, c.getMessage()});
                }
            } else {
                LOG_CSL.log(Level.SEVERE, cause == null ? "n/a" : cause.getMessage(), cause);
            }
        }

        public void onConsumeError(String queueName, String consumerTag, String correlationId, String messageId, String applicationId, Throwable cause) {
            LOG_CSL.log(Level.INFO, "[QN={0}, CID={1}, MID={2}, AID={3}] | Queue (listener) consume error, cause:{4}", new Object[]{queueName, correlationId, messageId, applicationId, cause.getMessage()});
        }
    }

    /**
     * Listener, kterym lze sledovat, obnoveni spojeni na komponentach rabbitmq 
     * (konkretne connection a channel) client-a.
     *
     * @see https://www.rabbitmq.com/api-guide.html#recovery
     * @see https://rabbitmq.github.io/rabbitmq-java-client/api/current/com/rabbitmq/client/RecoveryListener.html
     * @see https://groups.google.com/g/ruby-amqp/c/04xxje4BDRM/m/SwJYFlzbAQAJ - diskuze o recovery
     */
    private class ClientRecoveryListener implements RecoveryListener {

        private final Logger LOG_CRL = Logger.getLogger(ClientRecoveryListener.class.getSimpleName());

        private final String source;

        public ClientRecoveryListener(String sourceName) {
            this.source = sourceName;
        }

        @Override
        public void handleRecovery(Recoverable recoverable) {
            LOG_CRL.log(Level.WARNING, "[QN={0}, CN={1}] Recovery of {2} is COMPLETED, item={3}", new Object[]{queueName, connectionName, source, recoverable == null ? "n/a" : recoverable.toString()});
        }

        @Override
        public void handleRecoveryStarted(Recoverable recoverable) {
            LOG_CRL.log(Level.WARNING, "[QN={0}, CN={1}] Recovery of {2} has been started, item={3}", new Object[]{queueName, connectionName, source, recoverable == null ? "n/a" : recoverable.toString()});
        }

    }
}
