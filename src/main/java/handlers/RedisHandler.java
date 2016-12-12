package handlers;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Jaap on 12-12-2016.
 */
public class RedisHandler extends AbstractHandler {
    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Redis request");

        Jedis jedis = new Jedis("localhost");
        Set<String> all = jedis.keys("*");

        JSONObject redisResponse = new JSONObject();
        for (String key : all) {
            redisResponse.put(key, jedis.get(key));
        }

        IOUtils.closeQuietly(jedis);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("status", redisResponse).toString(2));
        request.setHandled(true);
    }
}
