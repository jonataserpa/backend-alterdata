package alt.vertx.ddd;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Target;

/**
 * Anotação somente para indicar que o campo é público e não necessita de acessores (get/set).
 * 
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 05/02/2020
 * @description PublicField em alt.vertx.ddd
 */
@Target(FIELD)
public @interface PublicField {

}
