/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.exhibitor.core.processes;

import com.google.common.io.Files;
import com.netflix.exhibitor.core.Exhibitor;
import com.netflix.exhibitor.core.activity.ActivityLog;

import com.netflix.exhibitor.core.config.IntConfigs;
import com.netflix.exhibitor.core.embedded.ZooKeeperInstanceHandler;
import com.netflix.exhibitor.core.state.ServerSpec;
import com.netflix.exhibitor.core.state.ServerType;
import com.netflix.exhibitor.core.state.UsState;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.server.PurgeTxnLog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

/**
 * ProcessOperations designed to work on an embedded Zookeeper. All operations are
 * done locally through the ZooKeeper instance handler,
 */
public class EmbeddedZooKeeperProcessOperation implements ProcessOperations {

    private final Exhibitor exhibitor;

    private ZooKeeperInstanceHandler zooKeeperInstanceHandler;

    /**
     * Create an EmbeddedZooKeeperProcessOperation from an Exhibitor instance.
     * @param exhibitor the Exhibitor instance
     * @param zooKeeperInstanceHandler the instance handler
     */
    public EmbeddedZooKeeperProcessOperation(Exhibitor exhibitor, ZooKeeperInstanceHandler zooKeeperInstanceHandler) {
        this.exhibitor = exhibitor;
        this.zooKeeperInstanceHandler = zooKeeperInstanceHandler;

    }

    /**
     * Start the embedded instance.
     *
     * @throws Exception errors
     */
    @Override
    public void startInstance() throws Exception {

        EmbeddedDetails details = new EmbeddedDetails(exhibitor);

        zooKeeperInstanceHandler.startZooKeeper(createNProperties(details));
    }

    /**
     * Kill the embedded instance.
     *
     * @throws Exception errors
     */
    @Override
    public void killInstance() throws Exception {
        zooKeeperInstanceHandler.stopZooKeeper();
    }

    @Override
    public void cleanupInstance() throws Exception {
        EmbeddedDetails details = new EmbeddedDetails(exhibitor);
        if (!details.isValid()) {
            return;
        }

        PurgeTxnLog.purge(details.logDirectory, details.dataDirectory,
                exhibitor.getConfigManager().getConfig().getInt(
                        IntConfigs.CLEANUP_MAX_FILES));
    }



    private Properties createNProperties(EmbeddedDetails details) throws IOException {
        UsState usState = new UsState(exhibitor);

        File idFile = new File(details.dataDirectory, "myid");
        if (usState.getUs() != null) {
            Files.createParentDirs(idFile);
            String id = String.format("%d\n", usState.getUs().getServerId());
            Files.write(id.getBytes(), idFile);
        } else {
            exhibitor.getLog().add(ActivityLog.Type.INFO, "Starting in standalone mode");
            if (idFile.exists() && !idFile.delete()) {
                exhibitor.getLog().add(ActivityLog.Type.ERROR, "Could not delete ID file: " + idFile);
            }
        }

        Properties localProperties = new Properties();
        localProperties.putAll(details.properties);

        localProperties.setProperty("clientPort", Integer.toString(usState.getConfig().getInt(IntConfigs.CLIENT_PORT)));

        String portSpec = String.format(":%d:%d", usState.getConfig().getInt(IntConfigs.CONNECT_PORT), usState.getConfig().getInt(IntConfigs.ELECTION_PORT));
        for (ServerSpec spec : usState.getServerList().getSpecs()) {
            localProperties.setProperty("server." + spec.getServerId(), spec.getHostname() + portSpec + spec.getServerType().getZookeeperConfigValue());
        }

        if ((usState.getUs() != null) && (usState.getUs().getServerType() == ServerType.OBSERVER)) {
            localProperties.setProperty("peerType", "observer");
        }


        OutputStream out = null;
        try {
            File configFile = new File(details.configDirectory, "zoo.cfg");

            Files.createParentDirs(configFile);
            out = new BufferedOutputStream(new FileOutputStream(configFile));

            localProperties.store(out, "Auto-generated by Exhibitor - " + new Date());
        } catch (IOException e) {
            exhibitor.getLog().add(ActivityLog.Type.ERROR, "Could not write zoo.cfg file to directory", e);
        } finally {
            CloseableUtils.closeQuietly(out);
        }

        return localProperties;
    }
}
