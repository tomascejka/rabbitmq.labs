package cz.tc.learn.rabbitmq.quickstart;

/**
 * <p>Quickstart - hello world</p>
 * 
 * <p>In this part of the tutorial we'll write two programs in Java; a producer 
 * that sends a single message, and a consumer that receives messages and prints 
 * them out. We'll gloss over some of the detail in the Java API, concentrating 
 * on this very simple thing just to get started. It's a "Hello World" of messaging.</p>
 * 
 * @author tomas.cejka
 * 
 * @see https://www.rabbitmq.com/tutorials/tutorial-one-java.html
 */
public class App {
    public static void main( String[] args ) 
            throws InterruptedException, Exception {
        Recv.main(null);
        while(true){
            Send.main(null);
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(5000);
        }
    }
}
