package alt.backend;

import java.math.BigDecimal;
import java.time.Instant;

import alt.vertx.ddd.PublicField;
import alt.vertx.memory.Identifiable;

/**
 * @copyright Alterdata Software
 * @author gustavo.dsn.pack
 * @since 14/11/2019
 * @description Recurso em alt.backend
 */
public class Recurso implements Identifiable {

    @PublicField
    public String id;
    @PublicField
    public Long longo;
    @PublicField
    public Integer inteiro;
    @PublicField
    public BigDecimal decimalGrande;
    @PublicField
    public String texto;
    @PublicField
    public Instant data;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
