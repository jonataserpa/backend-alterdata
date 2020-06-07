package alt.vertx.ddd;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import alt.vertx.rest.HttpStatusExceptionFactory;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.MultiTenantHandler;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 09/10/2019
 * @description ApiRepository em alt.vertx.ddd
 */
public abstract class ApiRecursoRepository<A> {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private static final Logger LOGGER = Logger.getLogger(ApiRecursoRepository.class.getName());

    private static final String NOT_IMPLEMENTED = "not implemented";

    public ApiRecursoRepository(String path, Router router) {

        BodyHandler bodyHandler = BodyHandler.create();

        CorsHandler corsHandler = CorsHandler.create("*")
                                             .allowedHeader("X-Requested-With")
                                             .allowedHeader(CONTENT_TYPE)
                                             .allowedHeader("Accept")
                                             .allowedHeader("Origin")
                                             .allowedHeader("Authorization")
                                             .allowedHeader("Tenant")
                                             .allowedMethod(HttpMethod.OPTIONS)
                                             .allowedMethod(HttpMethod.GET)
                                             .allowedMethod(HttpMethod.PATCH)
                                             .allowedMethod(HttpMethod.POST)
                                             .allowedMethod(HttpMethod.DELETE)
                                             .allowedMethod(HttpMethod.HEAD)
                                             .maxAgeSeconds(86400);
        router.route()
              .handler(corsHandler);

        MultiTenantHandler multiTenantHandler = MultiTenantHandler.create("Tenant");

        router.route(path + "/*")
              .failureHandler(this::handleFailure);

        router.route(HttpMethod.GET, path + "/:id")
              .handler(multiTenantHandler)
              .handler(this::handleGetById);

        router.route(HttpMethod.GET, path + "/:ids")
              .handler(multiTenantHandler)
              .handler(this::handleGetByIds);

        router.route(HttpMethod.GET, path)
              .handler(multiTenantHandler)
              .handler(this::handleGet);

        router.route(HttpMethod.POST, path)
              .consumes(APPLICATION_JSON)
              .handler(bodyHandler)
              .handler(multiTenantHandler)
              .handler(this::handlePost);

        router.route(HttpMethod.PATCH, path + "/:id")
              .consumes(APPLICATION_JSON)
              .handler(bodyHandler)
              .handler(multiTenantHandler)
              .handler(this::handlePatch);

        router.route(HttpMethod.DELETE, path + "/:id")
              .handler(multiTenantHandler)
              .handler(this::handleDelete);

    }

    private void handleFailure(RoutingContext ctx) {
        ctx.response()
           .setStatusCode(HttpStatusExceptionFactory.create(ctx))
           .end("{}");
    }

    private void handleGetById(RoutingContext ctx) {
        String id = ctx.request()
                       .getParam("id");
        if (id.contains(",")) {
            ctx.next();
            return;
        }
        A one = findOne(id);
        if (one == null) {
            ctx.response()
               .setStatusCode(404)
               .end();
            return;
        }
        ctx.response()
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end(JsonObject.mapFrom(one)
                          .encode());

    }

    private void handleGetByIds(RoutingContext ctx) {
        String ids = ctx.request()
                        .getParam("ids");
        List<String> list = Arrays.asList(ids.split("\\,"));
        ctx.response()
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end(new JsonArray(findMany(list)).encode());
    }

    private void handleGet(RoutingContext ctx) {
        ctx.response()
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end(JsonObject.mapFrom(findMany(new QuerySpec(ctx.request()
                                                             .query())))
                          .encode());
    }

    private void handlePost(RoutingContext ctx) {
        JsonObject json = ctx.getBodyAsJson();
        Preconditions.checkArgument(json != null);

        A object = json.mapTo(getResourceClass());

        object = create(object);

        ctx.response()
           .setStatusCode(201)
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end(JsonObject.mapFrom(object)
                          .encode());
    }

    void handlePostArray(RoutingContext ctx) {
        LOGGER.log(Level.INFO, "TODO: Post de vários objetos em uma única transação {0}", ctx);
    }

    private void handlePatch(RoutingContext ctx) {
        JsonObject partialJson = ctx.getBodyAsJson();
        Preconditions.checkArgument(partialJson != null);

        String id = ctx.request()
                       .getParam("id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));

        A one = findOne(id);
        Preconditions.checkArgument(one != null);
        JsonObject json = JsonObject.mapFrom(one);

        JsonObject mergedJson = json.mergeIn(partialJson);
        A merged = mergedJson.mapTo(getResourceClass());

        ctx.response()
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end(JsonObject.mapFrom(save(merged))
                          .encode());
    }

    void handlePatchArray(RoutingContext ctx) {
        LOGGER.log(Level.INFO, "TODO: Patch de vários objetos em uma única transação {0}", ctx);
    }

    private void handleDelete(RoutingContext ctx) {
        String id = ctx.request()
                       .getParam("id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));

        this.delete(id);

        ctx.response()
           .setStatusCode(204)
           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
           .end();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Class<A> getResourceClass() {
        ParameterizedType param = (ParameterizedType) this.getClass()
                                                          .getGenericSuperclass();
        return (Class) param.getActualTypeArguments()[0];
    }

    public A findOne(String id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public List<A> findMany(List<String> ids) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public Many<A> findMany(QuerySpec spec) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public A create(A obj) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public A save(A obj) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    public void delete(String id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

}
