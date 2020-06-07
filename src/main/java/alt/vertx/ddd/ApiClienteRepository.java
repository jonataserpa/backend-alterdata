package alt.vertx.ddd;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import alt.backend.Cliente;
import alt.dao.ClienteDAO;
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
public abstract class ApiClienteRepository<A> {

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";

	private static final Logger LOGGER = Logger.getLogger(ApiClienteRepository.class.getName());

	private static final String NOT_IMPLEMENTED = "not implemented";

	public ApiClienteRepository(String path, Router router) {

		BodyHandler bodyHandler = BodyHandler.create();

		CorsHandler corsHandler = CorsHandler.create("*").allowedHeader("X-Requested-With").allowedHeader(CONTENT_TYPE)
				.allowedHeader("Accept").allowedHeader("Origin").allowedHeader("Authorization").allowedHeader("Tenant")
				.allowedMethod(HttpMethod.OPTIONS).allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.PATCH)
				.allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.HEAD)
				.maxAgeSeconds(86400);
		router.route().handler(corsHandler);

		MultiTenantHandler multiTenantHandler = MultiTenantHandler.create("Tenant");

		router.route(path + "/*").failureHandler(this::handleFailure);

		router.route(HttpMethod.GET, path + "/:id").handler(multiTenantHandler).handler(this::handleGetById);

		router.route(HttpMethod.GET, path + "/:ids").handler(multiTenantHandler).handler(this::handleGetByIds);

		router.route(HttpMethod.GET, path).handler(multiTenantHandler).handler(event -> {
			try {
				handleGet(event);
			} catch (ClassNotFoundException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		router.route(HttpMethod.POST, path).consumes(APPLICATION_JSON).handler(bodyHandler).handler(multiTenantHandler)
				.handler(event -> {
					try {
						handlePost(event);
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

		router.route(HttpMethod.PATCH, path + "/:id").consumes(APPLICATION_JSON).handler(bodyHandler)
				.handler(multiTenantHandler).handler(event -> {
					try {
						handlePatch(event);
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

		router.route(HttpMethod.DELETE, path + "/:id").handler(multiTenantHandler).handler(event -> {
			try {
				handleDelete(event);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	private void handleFailure(RoutingContext ctx) {
		ctx.response().setStatusCode(HttpStatusExceptionFactory.create(ctx)).end("{}");
	}

	private void handleGetById(RoutingContext ctx) {
		String id = ctx.request().getParam("id");
		if (id.contains(",")) {
			ctx.next();
			return;
		}
		A one = findOne(id);
		if (one == null) {
			ctx.response().setStatusCode(404).end();
			return;
		}
		ctx.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(JsonObject.mapFrom(one).encode());

	}

	private void handleGetByIds(RoutingContext ctx) {
		String ids = ctx.request().getParam("ids");
		List<String> list = Arrays.asList(ids.split("\\,"));
		ctx.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(new JsonArray(findMany(list)).encode());
	}

	private void handleGet(RoutingContext ctx)
			throws ClassNotFoundException, SQLException, JsonGenerationException, JsonMappingException, IOException {

		List<Cliente> listClient = new ArrayList<Cliente>();
		ClienteDAO clienteDAO = new ClienteDAO();
		listClient = clienteDAO.findAll();

		Gson gson = new Gson();
		String json = gson.toJson(listClient);

		System.out.println(json);

		ctx.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(json);
	}

	private void handlePost(RoutingContext ctx) throws ClassNotFoundException, SQLException {
		JsonObject json = ctx.getBodyAsJson();
		Preconditions.checkArgument(json != null);

		A object = json.mapTo(getResourceClass());
		System.out.println(JsonObject.mapFrom(object).encode());

		Gson gson = new Gson();
		Cliente jsonToObj = gson.fromJson(JsonObject.mapFrom(object).encode(), Cliente.class);

		Cliente cliente = new Cliente();
		cliente.setId(jsonToObj.id);
		cliente.setNome(jsonToObj.nome);
		cliente.setEndereco(jsonToObj.endereco);
		cliente.setTelefone(jsonToObj.telefone);
		cliente.setDataNascimento(jsonToObj.dataNascimento);

		System.out.println(jsonToObj.id);
		System.out.println(jsonToObj.nome);
		System.out.println(jsonToObj.dataNascimento.toString());

		ClienteDAO clienteDAO = new ClienteDAO();
		if (jsonToObj.id == null) {
			var id = UUID.randomUUID().toString();
			cliente.setId(id);
			System.out.println(id);
			clienteDAO.save(cliente);
		} else {
			clienteDAO.update(cliente);
		}

		object = create(object);

		ctx.response().setStatusCode(201).putHeader(CONTENT_TYPE, APPLICATION_JSON)
				.end(JsonObject.mapFrom(object).encode());

	}

	void handlePostArray(RoutingContext ctx) {
		LOGGER.log(Level.INFO, "TODO: Post de vários objetos em uma única transação {0}", ctx);
	}

	private void handlePatch(RoutingContext ctx) throws ClassNotFoundException, SQLException {
		JsonObject partialJson = ctx.getBodyAsJson();
		Preconditions.checkArgument(partialJson != null);

		String id = ctx.request().getParam("id");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(id));

		A one = findOne(id);
		Preconditions.checkArgument(one != null);
		JsonObject json = JsonObject.mapFrom(one);

		JsonObject mergedJson = json.mergeIn(partialJson);
		A merged = mergedJson.mapTo(getResourceClass());

		A object = partialJson.mapTo(getResourceClass());

		Gson gson = new Gson();
		Cliente jsonToObj = gson.fromJson(JsonObject.mapFrom(object).encode(), Cliente.class);

		Cliente cliente = new Cliente();
		cliente.setId(jsonToObj.id);
		cliente.setNome(jsonToObj.nome);
		cliente.setEndereco(jsonToObj.endereco);
		cliente.setTelefone(jsonToObj.telefone);

		System.out.println(JsonObject.mapFrom(object).encode());
		System.out.println(jsonToObj.id);
		System.out.println(jsonToObj.nome);

		ClienteDAO clienteDAO = new ClienteDAO();
		if (jsonToObj.id == null) {
			clienteDAO.save(cliente);
		} else {
			clienteDAO.update(cliente);
		}

		ctx.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).end(JsonObject.mapFrom(save(merged)).encode());
	}

	void handlePatchArray(RoutingContext ctx) {
		LOGGER.log(Level.INFO, "TODO: Patch de vários objetos em uma única transação {0}", ctx);
	}

	private void handleDelete(RoutingContext ctx) throws ClassNotFoundException, SQLException {
		String id = ctx.request().getParam("id");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(id));

		Cliente cliente = new Cliente();
		cliente.setId(id);
		ClienteDAO clienteDAO = new ClienteDAO();
		clienteDAO.excluir(id);

		this.delete(id);

		ctx.response().setStatusCode(204).putHeader(CONTENT_TYPE, APPLICATION_JSON).end();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Class<A> getResourceClass() {
		ParameterizedType param = (ParameterizedType) this.getClass().getGenericSuperclass();
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
