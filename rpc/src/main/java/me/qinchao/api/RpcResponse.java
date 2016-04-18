package me.qinchao.api;

import java.io.Serializable;

/**
 * Created by SULVTO on 16-4-15.
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 5650327503627569491L;
    private Exception exception;
    private Object result;


    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}