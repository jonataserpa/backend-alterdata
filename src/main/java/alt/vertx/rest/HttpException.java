package alt.vertx.rest;

/**
 * @copyright Alterdata Software
 * @author boletta
 * @since 19/11/2019
 * @description HttpException em br.com.alterdata.vertx.rest
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }

}
