package me.qinchao.api;

/**
 * Created by SULVTO on 16-4-3.
 */
public class RegistryConfig extends AbstractConfig {
    private String address;
    private String serviceName;
    private Class<?> referenceClass;

    public RegistryConfig(String registryAddrss, String serviceName) {
        this.address = registryAddrss;
        this.serviceName = serviceName;
    }

    public RegistryConfig(String host, int port, String serviceName) {
        super(host, port);
        this.serviceName = serviceName;
    }

    public RegistryConfig(String registryAddrss, String host, int port, String serviceName) {
        super(host, port);
        this.serviceName = serviceName;
        this.address = registryAddrss;
    }

    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    public void setReferenceClass(Class<?> referenceClass) {
        this.referenceClass = referenceClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
