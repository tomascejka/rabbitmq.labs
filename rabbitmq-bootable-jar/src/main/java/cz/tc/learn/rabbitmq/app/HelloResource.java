package cz.tc.learn.rabbitmq.app;

import cz.tc.learn.rabbitmq.app.producer.ProducerBean;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.POST;

@Path("/hello")
public class HelloResource {
    
    @Inject
    private ProducerBean producer;
    
    @POST
    public Response doPost() {
        producer.publish();
        return Response.accepted().build();
    }
}
