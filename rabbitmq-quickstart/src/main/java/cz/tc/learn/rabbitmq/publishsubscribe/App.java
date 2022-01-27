package cz.tc.learn.rabbitmq.publishsubscribe;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>Publish/Subscribe</p>
 * 
 * <p>In this part we'll do something completely different -- we'll deliver a message 
 * to multiple consumers. This pattern is known as "publish/subscribe".
 * 
 * <p>To illustrate the pattern, we're going to build a simple logging system. It will 
 * consist of two programs -- the first will emit log messages and the second will 
 * receive and print them.</p>
 * 
 * <p>In our logging system every running copy of the receiver program will get 
 * the messages. That way we'll be able to run one receiver and direct the logs to disk; 
 * and at the same time we'll be able to run another receiver and see the logs on the screen.</p>
 * 
 * <p>Essentially, published log messages are going to be broadcast to all the receivers.</p>
 * 
 * @author tomas.cejka
 * 
 * @see https://www.rabbitmq.com/tutorials/tutorial-three-java.html
 */
public class App {
    public static void main( String[] args ) 
            throws InterruptedException, Exception {
        ReceiveLogs.main(null);
        while(true) {
            EmitLog.main(new String[]{UUID.randomUUID().toString()});
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        
    }
}
