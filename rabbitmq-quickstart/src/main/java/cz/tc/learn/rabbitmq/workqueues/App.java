package cz.tc.learn.rabbitmq.workqueues;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main( String[] args ) 
            throws InterruptedException, Exception {
        // 2 fronty ...
        Worker.main(null);
        Worker.main(null);
        
        List<String> messages = Arrays.asList("First message.","Second message..","Third message...","Fourth message....","Fifth message.....");
        while(true){
            for (int i=0; i<messages.size(); i++) {
                NewTask.main(new String[]{messages.get(i)});
                //java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000);
            }
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(5000);
        }
    }
}
