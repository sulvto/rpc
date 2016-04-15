package me.qinchao.api;

/**
 * Created by SULVTO on 16-4-15.
 */
public class RpcResponse {

    private Throwable error;
    private Object result;


    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}