package cz.tc.learn.rabbitmq.workqueues;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Work Queues</p>
 * 
 * <p>The main idea behind Work Queues (aka: Task Queues) is to avoid 
 * doing a resource-intensive task immediately and having to wait for it to complete. 
 * Instead we schedule the task to be done later. We encapsulate a task as a message 
 * and send it to a queue. A worker process running in the background will pop the tasks 
 * and eventually execute the job. When you run many workers the tasks will be shared between them.</p>
 * 
 * @author tomas.cejka
 * 
 * @see https://www.rabbitmq.com/tutorials/tutorial-two-java.html
 */
public class App {
    public static void main( String[] args ) 
            throws InterruptedException, Exception {
        // 2 consumers ...
        Worker.main(null);
        Worker.main(null);
        
        List<String> messages = Arrays.asList("First message.","Second message..","Third message...","Fourth message....","Fifth message.....");
        while(true){
            for (int i=0; i<messages.size(); i++) {
                NewTask.main(new String[]{messages.get(i)});
            }
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(5000);
        }
    }
}
