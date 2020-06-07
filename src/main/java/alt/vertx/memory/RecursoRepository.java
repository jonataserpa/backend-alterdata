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

import alt.vertx.ddd.ApiRecursoRepository;
import alt.vertx.ddd.Many;
import alt.vertx.ddd.QuerySpec;
import io.vertx.ext.web.Router;

/**
 * @copyright Alterdata Software
 * @author boletta
 * @since 18/11/2019
 * @description MemRepository em br.com.alterdata.vertx.ofy
 */
public abstract class RecursoRepository<A extends Identifiable> extends ApiRecursoRepository<A> {

    private static final Logger LOGGER = Logger.getLogger(RecursoRepository.class.getName());

    private Map<String, A> store = new ConcurrentHashMap<>();

    public RecursoRepository(String path, Router router) {
        super(path, router);
    }

    @Override
    public A findOne(String id) {
        return store.get(id);
    }

    @Override
    public List<A> findMany(List<String> ids) {
        return store.values()
                    .stream()
                    .filter(predicate -> ids.contains(predicate.getId()))
                    .collect(Collectors.toList());
    }

    @Override
    public Many<A> findMany(QuerySpec spec) {
        spec.convertValuesTypesToFieldsTypesFrom(getResourceClass());

        Predicate<A> predicate = p -> true;

        Long cursor = 0l;
        for (Entry<String, Object> entry : spec.values.entrySet()) {
            if (entry.getKey()
                     .equals("cursor")) {
                cursor = Long.valueOf(entry.getValue()
                                           .toString());
            } else {
                predicate = predicate.and(item -> {
                    try {
                        var fieldValue = item.getClass()
                                             .getDeclaredField(entry.getKey())
                                             .get(item);
                        var entryValue = entry.getValue();
                        return Objects.equals(entryValue, fieldValue);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, e.getMessage(), e);
                        return true;
                    }
                });
            }
        }

        var list = store.values()
                        .stream()
                        .filter(predicate)
                        .skip(cursor)
                        .limit(25)
                        .collect(Collectors.toList());

        cursor = cursor + 25;

        return new Many<>(list, cursor.toString());
    }

    @Override
    public A create(A obj) {
        var id = UUID.randomUUID()
                     .toString();
        obj.setId(id);

        store.put(id, obj);

        return obj;
    }

    @Override
    public A save(A obj) {
        store.remove(obj.getId());

        store.put(obj.getId(), obj);

        return obj;
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

}
