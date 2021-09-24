package cz.tc.learn.rabbitmq.publishsubscribe;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author tomas.cejka
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
