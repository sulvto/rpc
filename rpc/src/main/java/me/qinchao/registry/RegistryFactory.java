package me.qinchao.registry;

/**
 * Created by SULVTO on 16-4-3.
 */
public class RegistryFactory {
    private static Registry registry;

    private RegistryFactory() {

    }

    private static Registry createRegistry(String registryAddrss) {
        return new ZookeeperRegistry(registryAddrss);
    }

    public static Registry getRegistry(String registryAddrss) {
        if (registry == null) {
            registry = createRegistry(registryAddrss);
        }
        return registry;
    }
}
