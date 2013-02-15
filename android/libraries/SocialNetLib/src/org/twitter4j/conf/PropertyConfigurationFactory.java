/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twitter4j.conf;

/**
 * ConfigurationFactory implementation for PropertyConfiguration.
 * Currently getInstance calls concrete constructor each time. No caching at all.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class PropertyConfigurationFactory implements ConfigurationFactory {
    private static final PropertyConfiguration ROOT_CONFIGURATION;

    static {
        ROOT_CONFIGURATION = new PropertyConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getInstance() {
        return ROOT_CONFIGURATION;
    }

    // It may be preferable to cache the config instance

    /**
     * {@inheritDoc}
     */
    public Configuration getInstance(String configTreePath) {
        return new PropertyConfiguration(configTreePath);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        // nothing to do for property based configuration
    }
}