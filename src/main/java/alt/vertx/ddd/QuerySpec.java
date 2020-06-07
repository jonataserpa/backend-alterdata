package alt.vertx.ddd;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;

/**
 * Uma Spec <https://www.martinfowler.com/apsupp/spec.pdf> a partir de uma query
 * string
 * 
 * @copyright Alterdata Software
 * @author renier.dsn.pack
 * @since 31/10/2019
 * @description QuerySpec em alt.vertx.ddd
 */
public class QuerySpec implements Spec {

    public final Map<String, Object> values = new LinkedHashMap<>();
    public final List<String> sorts = new ArrayList<>();

    private QuerySpec() {
        super();
    }

    public static QuerySpec build() {
        return new QuerySpec();
    }

    public QuerySpec(String query) {
        super();

        if (Strings.isNullOrEmpty(query)) {
            return;
        }

        query = query.replace("?", "");

        if (query.isEmpty()) {
            return;
        }

        String[] saa = query.split("&");

        for (int i = 0; i < saa.length; i++) {
            String[] sab = saa[i].split("=");

            if (sab.length < 2) {
                continue;
            }
            try {
                values.put(sab[0], URLDecoder.decode(sab[1], "UTF-8"));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot decode query spec", e);
            }
        }
    }

    @Override
    public String toString() {
        if (values.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        values.forEach((k, v) -> {
            sb.append(k);
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(v.toString(), "UTF-8"));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot encode query spec", e);
            }
            sb.append("&");
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public QuerySpec and(String field, Object value) {
        values.put(field, value);
        return this;
    }

    /**
     * Converte os tipos dos valores dos itens em VALUES para os tipos homônimos dos
     * campos declarados em CLAZZ
     */
    public void convertValuesTypesToFieldsTypesFrom(Class<?> clazz) {
        for (Entry<String, Object> entry : values.entrySet()) {
            PropertyEditor propertyEditor = null;
            try {
                propertyEditor = PropertyEditorManager.findEditor(clazz.getDeclaredField(entry.getKey())
                                                                       .getType());
            } catch (NoSuchFieldException e) {
                continue;
            }
            propertyEditor.setAsText(entry.getValue()
                                          .toString());
            values.put(entry.getKey(), propertyEditor.getValue());
        }
    }

}
