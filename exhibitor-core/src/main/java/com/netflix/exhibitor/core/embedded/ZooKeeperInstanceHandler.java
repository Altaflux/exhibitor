package com.netflix.exhibitor.core.embedded;


import com.google.common.base.Preconditions;
import com.netflix.exhibitor.core.embedded.instance.EmbeddedQuorumZooKeeper;
import com.netflix.exhibitor.core.embedded.instance.EmbeddedZooKeeperStandAlone;
import com.netflix.exhibitor.core.embedded.instance.ZooKeeperInstance;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Properties;

public class ZooKeeperInstanceHandler {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperInstanceHandler.class);
    private ZooKeeperInstance zooKeeperInstance;

    /**
     * Start the ZooKeeper server with the given properties
     *
     * @param zooKeeperProperties the zookeeper properties to use for the instance
     * @throws Exception if an error occurred during start-up
     */
    public void startZooKeeper(Properties zooKeeperProperties) throws Exception {

        Preconditions.checkNotNull(zooKeeperProperties, "ZooKeeper properties are null, system won't be able to start");
        //Just in case shut down previous instance, it should be down already or never created at this moment.

        try {
            stopZooKeeper();
        } catch (Exception e) {
            logger.warn("Error shutting down previous down previous instance, this should not happen but is not grave", e);
        }

        configureDataLogDir(zooKeeperProperties);

        final QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        quorumConfiguration.parseProperties(zooKeeperProperties);

        if (quorumConfiguration.getServers().size() > 0) {
            zooKeeperInstance = new EmbeddedQuorumZooKeeper();
            zooKeeperInstance.startZooKeeper(quorumConfiguration);
        } else {
            zooKeeperInstance = new EmbeddedZooKeeperStandAlone();
            zooKeeperInstance.startZooKeeper(quorumConfiguration);
        }
    }

    /**
     * Creates the Data Log Dir directory, this is not created automatically by ZooKeeper
     *
     * @param zooKeeperProperties the zookeeper properties to use for the instance
     * @throws Exception if directory could not be created
     */
    protected void configureDataLogDir(Properties zooKeeperProperties) throws Exception {
        String dataLogDir = zooKeeperProperties.getProperty("dataLogDir");
        if (dataLogDir != null) {
            File dataLogDirFile = new File(dataLogDir);
            if (!dataLogDirFile.exists()) {
                logger.info("ZooKeeper DataLog directory created?: " + dataLogDirFile.mkdirs());
            }
        }
    }

    /**
     * Shut down current ZooKeeper instance.
     */
    @PreDestroy
    public void stopZooKeeper() {
        try {
            if (zooKeeperInstance != null) {
                zooKeeperInstance.shutDownZooKeeper();
                zooKeeperInstance = null;
            }
        } catch (Exception e) {
            logger.warn("Error shutting down previous instance, this should not happen but its not grave", e);
        }
    }
}