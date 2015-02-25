package com.netflix.exhibitor.core.embedded;

import org.apache.zookeeper.server.quorum.QuorumPeerMain;

/**
 * Extension of QuorumPeerMain to gain access to the shutdown mechanism of ZooKeeper
 */
public class EmbeddableQuorumPeerMain extends QuorumPeerMain {

    /**
     * Shutdown QuorumPeer Server instance
     */
    public void shutDown() {
        quorumPeer.shutdown();
    }
}