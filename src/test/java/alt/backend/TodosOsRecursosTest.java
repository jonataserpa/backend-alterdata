package alt.backend;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import alt.vertx.ddd.ApiRepositoryTestBase;
import alt.vertx.ddd.Integracao;
import alt.vertx.ddd.Many;
import alt.vertx.ddd.QuerySpec;
import alt.vertx.rest.NotFoundException;
import kong.unirest.GenericType;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 14/11/2019
 * @description TodosOsRecursosTest em alt.backend
 */
@Integracao
class TodosOsRecursosTest extends ApiRepositoryTestBase<Recurso> {

    static List<Recurso> createds = new ArrayList<Recurso>();

    TodosOsRecursosTest() {
        super("recursos", Recurso.class, new GenericType<List<Recurso>>() {
        }, new GenericType<Many<Recurso>>() {
        });
    }

    @AfterEach
    void afterEach() {
        for (Recurso created : createds) {
            delete(created.id.toString());
        }
        createds.clear();
    }

    Recurso create() {
        var one = new Recurso();
        one.longo = createds.size() + 1l;
        one.inteiro = createds.size() + 1;
        one.decimalGrande = new BigDecimal(createds.size() * 3.13);
        one.texto = "O texto do recurso";
        one.data = Instant.now();
        var created = create(one);
        createds.add(created);
        return created;
    }

    @Test
    void findOneTest() {
        var created = create();
        var one = findOne(created.id.toString());
        Assertions.assertNotNull(one);
        delete(one.id.toString());
    }

    @Test
    void findManyIdsTest() {
        var one = create();
        var two = create();
        var three = create();
        List<Recurso> many = findMany(one.id.toString(), two.id.toString(), three.id.toString());
        Assertions.assertEquals(3, many.size());
    }

    @Test
    void findManyTest() {
        var created = create();
        Many<Recurso> many = findMany(QuerySpec.build()
                                               .and("longo", created.longo)
                                               .and("inteiro", created.inteiro));
        Assertions.assertFalse(many.data.isEmpty());
        Assertions.assertTrue(many.data.size() == 1);
    }

    @Test
    void findManyWithCursorTest() {
        for (int i = 0; i < 26; i++) {
            create();
        }
        Many<Recurso> many = findMany(QuerySpec.build());
        Assertions.assertFalse(many.data.isEmpty());
        Assertions.assertNotNull(many.cursor);

        Many<Recurso> nextMany = findMany(QuerySpec.build()
                                                   .and("cursor", many.cursor));
        Assertions.assertFalse(nextMany.data.isEmpty());
        Assertions.assertNotNull(many.cursor);

        Assertions.assertNotEquals(many.data.get(0).id, nextMany.data.get(0).id);
    }

    @Test
    void createTest() {
        var created = create();
        Assertions.assertNotNull(created);
        Assertions.assertNotNull(created.id);
    }

    @Test
    void saveTest() {
        var created = create();

        var partial = new HashMap<String, Object>();
        partial.put("longo", 3);

        var saved = save(created.id.toString(), partial);

        Assertions.assertNotNull(saved);
        Assertions.assertNotEquals(created.longo, saved.longo);
        Assertions.assertEquals(created.inteiro, saved.inteiro);
    }

    @Test
    void deleteTest() {
        var created = create();
        delete(created.id.toString());
        Assertions.assertThrows(NotFoundException.class, () -> findOne(created.id.toString()));
    }

}
