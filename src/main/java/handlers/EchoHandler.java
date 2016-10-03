package handlers;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 28-5-2016.
 */
public class EchoHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("Echo request");
        response.getWriter().println("{" + s + ":" + IOUtils.toString(request.getReader()).trim() + "}");
        LogstashLogger.INSTANCE.message("echo: " + s);
        baseRequest.setHandled(true);
    }
}
