package cz.tc.learn.rabbitmq.jakarta;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * @author airhacks.com
 */
@Path("ping")
public class PingResource { 

    @GET
    public String ping() {
        return " Jakarta EE with MicroProfile 2+!";
    }

}
