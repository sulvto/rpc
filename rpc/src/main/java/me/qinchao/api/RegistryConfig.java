package me.qinchao.api;

/**
 * Created by SULVTO on 16-4-3.
 */
public class RegistryConfig {
    private String host;
    private int port;
    private String serviceName;

    public RegistryConfig(String host, int port, String serviceName) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
