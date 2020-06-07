package alt.vertx.ddd;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;

import alt.Main;
import alt.vertx.rest.HttpStatusExceptionFactory;
import kong.unirest.GenericType;
import kong.unirest.Unirest;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 09/10/2019
 * @description ApiRepositoryTest em alt.vertx.ddd
 */
public class ApiRepositoryTestBase<A> {

    private static final String API = "http://localhost:8080/api/v1";

    private static String accessToken;

    private final String resource;
    private final Class<A> clazz;
    private final GenericType<List<A>> listType;
    private final GenericType<Many<A>> manyType;

    @BeforeAll
    static void beforeAllTests() throws ClassNotFoundException, SQLException {
        Unirest.config()
               .setObjectMapper(new GsonObjectMapper());

        Main.main(new String[0]);

        // Mais uma migalha para o pleno...
        accessToken = "";
    }

    protected ApiRepositoryTestBase(String resource, Class<A> clazz, GenericType<List<A>> listType,
            GenericType<Many<A>> manyType) {
        this.resource = resource;
        this.clazz = clazz;
        this.listType = listType;
        this.manyType = manyType;
    }

    protected A findOne(String id) {
        var response = Unirest.get(API + "/" + resource + "/" + id)
                              .header("Authorization", "Bearer " + accessToken)
                              .asObject(clazz);

        HttpStatusExceptionFactory.create(response.getStatus());

        return response.getBody();

    }

    protected List<A> findMany(String... ids) {
        var response = Unirest.get(API + "/" + resource + "/" + String.join(",", ids))
                              .header("Authorization", "Bearer " + accessToken)
                              .asObject(this.listType);

        HttpStatusExceptionFactory.create(response.getStatus());

        return response.getBody();
    }

    protected Many<A> findMany(Spec spec) {
        var response = Unirest.get(API + "/" + resource + spec.toString())
                              .header("Authorization", "Bearer " + accessToken)
                              .asObject(this.manyType);

        HttpStatusExceptionFactory.create(response.getStatus());

        return response.getBody();
    }

    protected A create(A object) {
        var response = Unirest.post(API + "/" + resource)
                              .header("Content-Type", "application/json")
                              .header("Authorization", "Bearer " + accessToken)
                              .body(object)
                              .asObject(clazz);

        HttpStatusExceptionFactory.create(response.getStatus());

        return response.getBody();

    }

    protected A save(String id, Map<String, Object> partial) {
        var response = Unirest.patch(API + "/" + resource + "/" + id)
                              .header("Content-Type", "application/json")
                              .header("Authorization", "Bearer " + accessToken)
                              .body(partial)
                              .asObject(clazz);

        HttpStatusExceptionFactory.create(response.getStatus());

        return response.getBody();
    }

    protected void delete(String id) {
        var response = Unirest.delete(API + "/" + resource + "/" + id)
                              .header("Authorization", "Bearer " + accessToken)
                              .asObject(clazz);

        HttpStatusExceptionFactory.create(response.getStatus());
    }

}
