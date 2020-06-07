package alt.vertx.memory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import alt.backend.Cliente;
import alt.vertx.ddd.ApiClienteRepository;
import alt.vertx.ddd.Many;
import alt.vertx.ddd.QuerySpec;
import io.vertx.ext.web.Router;

/**
 * @copyright Alterdata Software
 * @author boletta
 * @since 18/11/2019
 * @description MemRepository em br.com.alterdata.vertx.ofy
 */
@Path("cliente")
public abstract class ClienteRepository<A extends Identifiable> extends ApiClienteRepository<Cliente> {

	private static final Logger LOGGER = Logger.getLogger(ClienteRepository.class.getName());

	private Map<String, Cliente> store = new ConcurrentHashMap<>();

	public ClienteRepository(String path, Router router) {
		super(path, router);
	}

	@Override
	public Cliente findOne(String id) {
		return store.get(id);
	}

	@Override
	public List<Cliente> findMany(List<String> ids) {
		return store.values().stream().filter(predicate -> ids.contains(predicate.getId()))
				.collect(Collectors.toList());
	}

	@Override
	public Many<Cliente> findMany(QuerySpec spec) {
		spec.convertValuesTypesToFieldsTypesFrom(getResourceClass());

		Predicate<Cliente> predicate = p -> true;

		Long cursor = 0l;
		for (Entry<String, Object> entry : spec.values.entrySet()) {
			if (entry.getKey().equals("cursor")) {
				cursor = Long.valueOf(entry.getValue().toString());
			} else {
				predicate = predicate.and(item -> {
					try {
						var fieldValue = item.getClass().getDeclaredField(entry.getKey()).get(item);
						var entryValue = entry.getValue();
						return Objects.equals(entryValue, fieldValue);
					} catch (Exception e) {
						LOGGER.log(Level.WARNING, e.getMessage(), e);
						return true;
					}
				});
			}
		}

		var list = store.values().stream().filter(predicate).skip(cursor).limit(25).collect(Collectors.toList());

		cursor = cursor + 25;

		return new Many<>(list, cursor.toString());
	}

	@Override
	public Cliente create(Cliente cliente) {
		var id = UUID.randomUUID().toString();
		cliente.setId(id);
		store.put(id, cliente);

		return cliente;
	}

	@Override
	public Cliente save(Cliente cliente) {
		store.remove(cliente.getId());

		store.put(cliente.getId(), cliente);

		return cliente;
	}

	@Override
	public void delete(String id) {
		store.remove(id);
	}

}
