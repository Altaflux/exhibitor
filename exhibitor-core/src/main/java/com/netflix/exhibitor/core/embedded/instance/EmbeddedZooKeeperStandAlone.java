package com.netflix.exhibitor.core.embedded.instance;

import com.netflix.exhibitor.core.embedded.EmbeddableZooKeeperMain;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedZooKeeperStandAlone implements ZooKeeperInstance {

    private EmbeddableZooKeeperMain zooKeeperServer;
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedQuorumZooKeeper.class);

    public void startZooKeeper(final QuorumPeerConfig quorumPeerConfig) throws Exception {

        zooKeeperServer = new EmbeddableZooKeeperMain();
        Thread zooKeeperThread = new Thread() {
            public void run() {
                try {
                    final ServerConfig configuration = new ServerConfig();
                    configuration.readFrom(quorumPeerConfig);
                    zooKeeperServer.runFromConfig(configuration);

                } catch (Exception e) {
                    logger.error("An error has occurred starting the Zookeeper Instance", e);
                }
            }
        };
        zooKeeperThread.setName("ZooKeeperServer Thread");
        zooKeeperThread.start();
    }

    public void shutDownZooKeeper() {
        if (zooKeeperServer != null) {
            zooKeeperServer.shutDown();
            zooKeeperServer = null;
        }
    }
}