package me.qinchao;

import me.qinchao.api.RegistryConfig;
import me.qinchao.protocol.Protocol;
import me.qinchao.protocol.impl.NettyProtocol;
import me.qinchao.registry.RegistryFactory;

import java.util.List;

/**
 * Created by SULVTO on 16-4-3.
 */
public class ServiceHandler {
    private Protocol protocol = new NettyProtocol();
    private String registryAddress;


    public ServiceHandler(String registryAddress) {
        this.registryAddress = registryAddress;
    }


    void export(Object service, String host, int port, String serviceName) {
        protocol.export(service, host, port);
        RegistryFactory.getRegistry(registryAddress).register(host, port, serviceName);
    }

    Object refer(Class<?> referenceClass,String serviceName) {
        List<RegistryConfig> subscribe = RegistryFactory.getRegistry(registryAddress).subscribe(serviceName);
        if (subscribe.size() < 1) {
            return null;
        }

        RegistryConfig registryConfig = subscribe.get(0);
        return protocol.refer(referenceClass, registryConfig.getHost(), registryConfig.getPort());
    }





}
