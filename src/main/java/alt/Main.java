package alt;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import alt.backend.TodosOsClientes;
import alt.backend.TodosOsRecursos;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 20/11/2019
 * @description Main em alt
 */
public final class Main {

    private static final int PORT = Integer.parseInt(System.getenv()
                                                           .getOrDefault("PORT", "8080"));
    private static final String MOUNT_POINT = "/api/v1";
    private static final Logger LOGGER = Logger.getLogger("Main");

    private static final Vertx VERTX = Vertx.vertx();
    private static final HttpServer HTTP_SERVER = VERTX.createHttpServer();

    private static final Router ROOT = Router.router(VERTX);
    private static final Router API = Router.router(VERTX);

    private static void mount() throws ClassNotFoundException, SQLException {
        // XXX Endpoints
        new TodosOsRecursos(API);
        new TodosOsClientes(API);
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        LOGGER.log(Level.INFO, "Starting microservice above Vert.x");

        LOGGER.log(Level.INFO, "Listening on {0}", PORT);
        HTTP_SERVER.requestHandler(ROOT)
                   .listen(PORT);

        LOGGER.log(Level.INFO, "Configuring JSON serializer/deserializer");
        DatabindCodec.mapper()
                     .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        LOGGER.log(Level.INFO, "Mouting on {0}", MOUNT_POINT);
        mount();
        ROOT.mountSubRouter(MOUNT_POINT, API);
    }

}
