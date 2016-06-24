package me.qinchao.registry;

import me.qinchao.api.RegistryConfig;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by SULVTO on 16-3-29.
 */
public class ZookeeperRegistry implements Registry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private String registryAddress;

    private final String ROOT = "/root";
    private ZooKeeper zkClient;
    private volatile boolean isInit = false;
    private CountDownLatch latch = new CountDownLatch(1);

    public ZookeeperRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    private void init() {
        if (zkClient == null) {

            try {
                zkClient = new ZooKeeper(registryAddress,
                        500000, new Watcher() {
                    public void process(WatchedEvent event) {
                        if (event.getState() == Event.KeeperState.SyncConnected) {
                            LOGGER.debug("已经触发了" + event.getType() + "事件！");
                            latch.countDown();
                        }
                    }
                });
                latch.await();
                createNode(ROOT, CreateMode.PERSISTENT);
                isInit = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNode(String path, CreateMode mode) {

        try {
            if (zkClient.exists(path, true) == null) {
                zkClient.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createRegistryNode(String serverAddress, String serviceName) {
        createNode(ROOT + "/" + serverAddress, CreateMode.PERSISTENT);
        createNode(ROOT + "/" + serverAddress + "/" + serviceName, CreateMode.EPHEMERAL);
    }


    @Override
    public void register(String host, int port, String serviceName) {
        if (!isInit) {
            init();
        }
        createRegistryNode(host + ":" + port, serviceName);
    }

    @Override
    public List<RegistryConfig> subscribe(String serviceName) {
        if (!isInit) {
            init();
        }
        List<RegistryConfig> serviceList = new ArrayList<>();

        try {
            List<String> children = zkClient.getChildren(ROOT, true);
            for (int i = 0; i < children.size(); i++) {
                List<String> children2 = zkClient.getChildren(ROOT + "/" + children.get(i), true);
                if (children2.contains(serviceName)) {
                    String address = children.get(i);
                    String[] split = address.split(":");
                    serviceList.add(new RegistryConfig(split[0], Integer.parseInt(split[1]), serviceName));
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serviceList;
    }
}
