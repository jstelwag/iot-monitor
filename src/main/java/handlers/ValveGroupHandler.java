package handlers;

import listener.IoTRequestDispatcher;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 23-9-2016.
 */
public class ValveGroupHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/plain");
        IoTRequestDispatcher dispatcher = new IoTRequestDispatcher(IOUtils.toString(request.getReader()));
        if(dispatcher.isGroup()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(dispatcher.actuatorsOut());
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        baseRequest.setHandled(true);
    }
}
