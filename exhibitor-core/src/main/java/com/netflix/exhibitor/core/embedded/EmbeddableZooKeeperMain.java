package com.netflix.exhibitor.core.embedded;

import org.apache.zookeeper.server.ZooKeeperServerMain;

/**
 * Extension of ZooKeeperMain to gain access to the shutdown mechanism of ZooKeeper
 */
public class EmbeddableZooKeeperMain extends ZooKeeperServerMain {

    /**
     * Shutdown ZooKeeper Server instance
     */
    public void shutDown() {
        shutdown();
    }
}