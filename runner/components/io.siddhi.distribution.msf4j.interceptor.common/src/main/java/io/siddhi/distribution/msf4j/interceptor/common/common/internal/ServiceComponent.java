/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.distribution.msf4j.interceptor.common.common.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants.SP_AUTH_NAMESPACE;
import static org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants.STREAMLINED_AUTH_NAMESPACE;

/**
 * component to get the registered IdPClient OSGi service.
 */
@Component(
        name = "ServiceComponent",
        service = ServiceComponent.class,
        immediate = true
)
public class ServiceComponent {

    @Activate
    protected void start(BundleContext bundleContext) throws ConfigurationException {
        ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
        IdPClientConfiguration idPClientConfiguration;

        if (configProvider.getConfigurationObject(STREAMLINED_AUTH_NAMESPACE) != null) {
            idPClientConfiguration = configProvider.getConfigurationObject(
                    STREAMLINED_AUTH_NAMESPACE, IdPClientConfiguration.class);
        } else if (configProvider.getConfigurationObject(SP_AUTH_NAMESPACE) != null) {
            idPClientConfiguration = configProvider.getConfigurationObject(IdPClientConfiguration.class);
        } else {
            idPClientConfiguration = new IdPClientConfiguration();
        }

        String enableInterceptor = idPClientConfiguration.getRestAPIAuthConfigs().getAuthEnable();
        boolean isInterceptorEnabled = Boolean.parseBoolean(enableInterceptor);
        DataHolder.getInstance().setInterceptorEnabled(isInterceptorEnabled);

        List<Pattern> excludeURI = idPClientConfiguration.getRestAPIAuthConfigs().getExclude().stream().map((glob) -> {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < glob.length(); ++i) {
                final char c = glob.charAt(i);
                switch (c) {
                    case '*':
                        out.append(".*");
                        break;
                    case '?':
                        out.append('.');
                        break;
                    case '.':
                        out.append("\\.");
                        break;
                    case '\\':
                        out.append("\\\\");
                        break;
                    default:
                        out.append(c);
                }
            }
            return Pattern.compile(out.toString());
        }).collect(Collectors.toList());
        DataHolder.getInstance().setExcludeURLList(excludeURI);
    }

    @Deactivate
    protected void stop() {
    }


    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.analytics.idp.client.core.api.IdPClient",
            service = IdPClient.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterIdP"
    )
    protected void registerIdP(IdPClient client) {
        DataHolder.getInstance().setIdPClient(client);
    }

    protected void unregisterIdP(IdPClient client) {
        DataHolder.getInstance().setIdPClient(null);
    }
}
