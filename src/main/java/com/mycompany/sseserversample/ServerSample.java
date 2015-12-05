package com.mycompany.sseserversample;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Sever Sent Event Sample.
 * 
 * @author tomo
 */
@Path("event/{task-cnt}/{task-interval}")
public class ServerSample {
    public static void main(String... args) throws IOException, InterruptedException{
        ResourceConfig config = new ResourceConfig(ServerSample.class, SseFeature.class);
        URI uri = URI.create("http://localhost:8080/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
        }));
        server.start();
        System.out.println("Server started. Stop the Application using CTR-C");
        Thread.currentThread().join();
        System.out.println("Server end.");
    }
    
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getServerSentEvents(@PathParam("task-cnt") int taskCnt,
            @PathParam("task-interval") int taskInterval){
        System.out.println("*taskcnt=" + taskCnt +", " + taskInterval);
        final EventOutput eventOutput = new EventOutput();
        new Thread(() -> {
            try {
                for (int i = 0; i < taskCnt; i++) {
                    TimeUnit.SECONDS.sleep(taskInterval);
                    final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
                    eventBuilder.name("message-client");
                    eventBuilder.data(String.class, "Hello world " + i);
                    eventBuilder.comment("comment"+ i);
                    eventBuilder.id("id"+ i);
                    final OutboundEvent event = eventBuilder.build();
                    eventOutput.write(event);
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    eventOutput.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServerSample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        return eventOutput;
    }
}
