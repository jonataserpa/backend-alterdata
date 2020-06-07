package alt.vertx.ddd;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kong.unirest.GenericType;
import kong.unirest.ObjectMapper;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 29/10/2019
 * @description GsonObjectMapper em alt.vertx.ddd
 */
public class GsonObjectMapper implements ObjectMapper {

    final Gson gson = Converters.registerAll(new GsonBuilder())
                                .create();

    @Override
    public <T> T readValue(String value, Class<T> valueType) {
        return gson.fromJson(value, valueType);
    }

    @Override
    public <T> T readValue(String value, GenericType<T> genericType) {
        return gson.fromJson(value, genericType.getType());
    }

    @Override
    public String writeValue(Object value) {
        return gson.toJson(value);
    }
}
