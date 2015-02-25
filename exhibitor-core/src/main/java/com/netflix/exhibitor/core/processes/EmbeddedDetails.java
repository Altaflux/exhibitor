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

import com.netflix.exhibitor.core.Exhibitor;
import com.netflix.exhibitor.core.config.EncodedConfigParser;
import com.netflix.exhibitor.core.config.InstanceConfig;
import com.netflix.exhibitor.core.config.StringConfigs;

import java.io.File;
import java.util.Properties;


class EmbeddedDetails {

    final File dataDirectory;
    final File logDirectory;
    final File configDirectory;
    final Properties properties;

    public EmbeddedDetails(Exhibitor exhibitor) {
        InstanceConfig config = exhibitor.getConfigManager().getConfig();

        this.dataDirectory = new File(config.getString(StringConfigs.ZOOKEEPER_DATA_DIRECTORY));
        String logDirectory = config.getString(StringConfigs.ZOOKEEPER_LOG_DIRECTORY);
        this.logDirectory = (logDirectory.trim().length() > 0) ? new File(logDirectory) : this.dataDirectory;
        this.configDirectory = new File(
                exhibitor.getConfigManager().getConfig().getString(StringConfigs.ZOOKEEPER_INSTALL_DIRECTORY));

        properties = new Properties();
        if ( isValid() )
        {
            EncodedConfigParser parser = new EncodedConfigParser(exhibitor.getConfigManager().getConfig().getString(StringConfigs.ZOO_CFG_EXTRA));
            for ( EncodedConfigParser.FieldValue fv : parser.getFieldValues() )
            {
                properties.setProperty(fv.getField(), fv.getValue());
            }
            properties.setProperty("dataDir", dataDirectory.getPath());
            properties.setProperty("dataLogDir", this.logDirectory.getPath());
        }
    }

    public boolean isValid() {
        return isValidPath(dataDirectory)
                && isValidPath(configDirectory)
                && isValidPath(logDirectory);
    }

    private boolean isValidPath(File directory) {
        return directory.getPath().length() > 0;
    }
}
