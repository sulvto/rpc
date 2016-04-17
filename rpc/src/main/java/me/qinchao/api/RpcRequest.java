package me.qinchao.api;

import java.io.Serializable;

/**
 * Created by SULVTO on 16-4-15.
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 6279844628734562551L;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
}
