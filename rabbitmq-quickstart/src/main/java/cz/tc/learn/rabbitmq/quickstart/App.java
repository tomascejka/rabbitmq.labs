package cz.tc.learn.rabbitmq.quickstart;

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
