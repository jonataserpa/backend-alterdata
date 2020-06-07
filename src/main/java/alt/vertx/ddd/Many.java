package alt.vertx.ddd;

import java.util.List;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 03/12/2019
 * @description Many em alt.vertx.ddd
 */
public class Many<A> {

    public final List<A> data;
    public final String cursor;

    public Many(List<A> data, String cursor) {
        super();
        this.data = data;
        this.cursor = cursor;
    }

}
