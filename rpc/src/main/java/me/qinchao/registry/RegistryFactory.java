package me.qinchao.registry;

/**
 * Created by SULVTO on 16-4-3.
 */
public class RegistryFactory {
    private static Registry registry;

    private RegistryFactory() {

    }

    private static Registry createRegistry(String registryAddress) {
        return new ZookeeperRegistry(registryAddress);
    }

    public static Registry getRegistry(String registryAddress) {
        if (registry == null) {
            registry = createRegistry(registryAddress);
        }
        return registry;
    }
}
