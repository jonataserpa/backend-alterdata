package alt.vertx.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;

/**
 * @copyright Alterdata Software
 * @author boletta
 * @since 19/11/2019
 * @description HttpStatusExceptionFactory em br.com.alterdata.vertx.ddd
 */
public class HttpStatusExceptionFactory {

    static final Logger LOGGER = Logger.getLogger(HttpStatusExceptionFactory.class.getName());

    static final Map<Integer, Class<? extends HttpException>> statusCodes = new LinkedHashMap<>();
    static final Map<Class<?>, Integer> exceptions = new LinkedHashMap<>();

    static {
        exceptions.put(UnsupportedOperationException.class, 501);
        exceptions.put(DecodeException.class, 400);
        exceptions.put(IllegalArgumentException.class, 400);
    }

    private HttpStatusExceptionFactory() {
        super();
    }

    public static void create(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        if (statusCode == 400) {
            throw new BadRequestException();
        }

        if (statusCode == 401) {
            throw new UnauthorizedException();
        }

        if (statusCode == 404) {
            throw new NotFoundException();
        }

        if (statusCode == 501) {
            throw new NotImplementedException();
        }

        throw new HttpException(String.valueOf(statusCode));

    }

    public static Integer create(RoutingContext routingContext) {
        Integer status = exceptions.getOrDefault(routingContext.failure()
                                                               .getClass(),
                500);

        if (routingContext.failure() instanceof HttpStatusException) {
            status = ((HttpStatusException) routingContext.failure()).getStatusCode();
        }

        LOGGER.log(Level.WARNING, "{0} on {1} by {2}", new Object[] { routingContext.failure()
                                                                                    .getClass()
                                                                                    .getSimpleName(),
                routingContext.normalisedPath(), routingContext.request()
                                                               .remoteAddress() });

        LOGGER.log(Level.FINEST, routingContext.failure()
                                               .getMessage(),
                routingContext.failure());
        
        if (status == 500) {
            LOGGER.log(Level.SEVERE, "On " + routingContext.normalisedPath() + ":", routingContext.failure());
        }

        return status;
    }

}
