package me.qinchao.registry;

import me.qinchao.api.RegistryConfig;

import java.util.List;

/**
 * Created by SULVTO on 16-4-3.
 */
public interface Registry {

    void register(String host,int port,String serviceName);

    List<RegistryConfig> subscribe(String serviceName);
}
