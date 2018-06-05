/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.sp.jobmanager.core.allocation;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Allocation algorithm based on CPU utilization implementation
 */
public class CPUBasedAllocationAlgorithm implements ResourceAllocationAlgorithm {
    private static final Logger logger = Logger.getLogger(CPUBasedAllocationAlgorithm.class);
    private static final double SYSTEM_CPU_WEIGHT = 1;
    private static final double PROCESS_CPU_WEIGHT = 1;

    @Override
    public ResourceNode getNextResourceNode() {
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (deploymentConfig != null && resourcePool != null) {
            if (resourcePool.getResourceNodeMap().size() >= deploymentConfig.getMinResourceCount()) {
                Iterator resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                return getMaximumResourceNode(resourceIterator);
            } else {
                logger.error("Minimum resource requirement did not match, hence not deploying the partial siddhi app ");
            }
        }
        return null;
    }

    private ResourceNode getMaximumResourceNode(Iterator resourceIterator) {
        Map<String, Double> unsortedMap = new HashMap<>();
        while (resourceIterator.hasNext()) {
            ResourceNode resourceNode = (ResourceNode) resourceIterator.next();
            if (resourceNode.isMetricsUpdated()) {
                unsortedMap.put(resourceNode.getId(), calculateWorkerResourceMeasurement(resourceNode));
            } else {
                throw new ResourceManagerException("Metrics needs to be enabled on Resource node: "
                        + resourceNode.getId() + " to be used with Allocation algorithm class: "
                        + ServiceDataHolder.getAllocationAlgorithm().getClass().getCanonicalName());
            }
        }
        Map.Entry<String, Double> node = Collections.min(unsortedMap.entrySet(),
                new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                });
        return ServiceDataHolder.getResourcePool().getResourceNodeMap().get(node.getKey());
    }

    private double calculateWorkerResourceMeasurement(ResourceNode resourceNode){
        double processCPU = resourceNode.getProcessCPU();
        double systemCPU = resourceNode.getSystemCPU();
        return (SYSTEM_CPU_WEIGHT * (1 - systemCPU)) + (PROCESS_CPU_WEIGHT * (1 - processCPU));
    }
}