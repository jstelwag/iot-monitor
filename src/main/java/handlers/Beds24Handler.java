package handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import retriever.Beds24BookingRetriever;
import util.HeatingProperties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 27-5-2016.
 */
public class Beds24Handler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("Beds24 request");
        HeatingProperties prop = new HeatingProperties();
        new Beds24BookingRetriever(prop.beds24ApiKey, prop.beds24PropKey).run();

        response.setContentType("application/json");
        response.getWriter().println("{\"status\"=\"OK\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
