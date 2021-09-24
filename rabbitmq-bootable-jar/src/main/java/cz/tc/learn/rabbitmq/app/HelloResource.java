package cz.tc.learn.rabbitmq.app;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.POST;

@Path("/hello")
public class HelloResource {
    
    @Inject
    private Producer producer;
    
    @POST
    public Response doPost() {
        producer.publish("Hello from WildFly bootable jar!");
        return Response.accepted().build();
    }
}
