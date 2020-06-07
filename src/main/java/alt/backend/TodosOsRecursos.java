package alt.backend;

import alt.vertx.memory.RecursoRepository;
import io.vertx.ext.web.Router;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 14/11/2019
 * @description TodosOsRecursos em alt.backend
 */
public class TodosOsRecursos extends RecursoRepository<Recurso> {

    public TodosOsRecursos(Router router) {
        super("/recursos", router);
    }

}
