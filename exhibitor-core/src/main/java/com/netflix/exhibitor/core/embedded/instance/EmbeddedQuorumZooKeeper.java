package com.netflix.exhibitor.core.embedded.instance;

import com.netflix.exhibitor.core.embedded.EmbeddableQuorumPeerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EmbeddedQuorumZooKeeper implements ZooKeeperInstance {

    private EmbeddableQuorumPeerMain zooKeeperServer;

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedQuorumZooKeeper.class);

    @Override
    public void startZooKeeper(final QuorumPeerConfig quorumPeerConfig) throws Exception {

        zooKeeperServer = new EmbeddableQuorumPeerMain();

        Thread zooKeeperThread;
        zooKeeperThread = new Thread() {
            public void run() {
                try {
                    zooKeeperServer.runFromConfig(quorumPeerConfig);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        zooKeeperThread.setName("ZooKeeperServer Thread");
        zooKeeperThread.setDaemon(true);
        zooKeeperThread.start();
    }

    @Override
    public void shutDownZooKeeper() {
        if (zooKeeperServer == null) {
            return;
        }
        logger.info("Shutting down ZooKeeper instance");
        zooKeeperServer.shutDown();
    }
}