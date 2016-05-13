package me.qinchao;

import me.qinchao.api.ProtocolConfig;
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

    private ProtocolConfig protocolConfig;
    private RegistryConfig registryConfig;

    public ServiceHandler() {

    }

    public ServiceHandler(ProtocolConfig protocolConfig, RegistryConfig registryConfig) {
        this.protocolConfig = protocolConfig;
        this.registryConfig = registryConfig;
    }

    void export(Object service) {
        protocol.export(service, protocolConfig);
        RegistryFactory.getRegistry(registryConfig.getAddress()).register(registryConfig);
    }

    Object refer() {
        List<RegistryConfig> subscribe = RegistryFactory.getRegistry(registryConfig.getAddress()).subscribe(registryConfig.getServiceName());
        if (subscribe.size() < 1) {
            return null;
        }

        RegistryConfig registryConfig = subscribe.get(0);
        return protocol.refer(this.registryConfig.getReferenceClass(), registryConfig);
    }


    public ProtocolConfig getProtocolConfig() {
        return protocolConfig;
    }

    public void setProtocolConfig(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }
}
