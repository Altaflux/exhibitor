package com.netflix.exhibitor.core.embedded.instance;

import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

/**
 * Interface to apply to ZooKeeper instances, this exposes the basic functionality needed
 * for the ZooKeeper instance handler to start or stop ZooKeeper.
 */
public interface ZooKeeperInstance {

    /**
     * Start the ZooKeeper server with the given properties
     *
     * @param quorumPeerConfig the QuorumConfig properties to use for the instance
     * @throws Exception if an error occurred during start-up
     */
    void startZooKeeper(QuorumPeerConfig quorumPeerConfig) throws Exception;

    /**
     * Shut down current ZooKeeper instance.
     */
    void shutDownZooKeeper();

}
