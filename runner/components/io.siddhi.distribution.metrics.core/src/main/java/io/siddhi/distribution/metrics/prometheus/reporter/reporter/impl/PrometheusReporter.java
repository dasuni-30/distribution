/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.metrics.prometheus.reporter.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;
import io.siddhi.core.exception.ConnectionUnavailableException;
import org.apache.log4j.Logger;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;
import org.wso2.carbon.metrics.core.reporter.impl.AbstractReporter;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to prometheus
 */
public class PrometheusReporter extends AbstractReporter implements ScheduledReporter {

    private final MetricRegistry metricRegistry;
    private final MetricFilter metricFilter;
    private final long pollingPeriod;
    private PromReporter prometheusReporter;
    private CollectorRegistry collectorRegistry=new CollectorRegistry();
    private HTTPServer server;
    private String serverURL = "http://localhost:1234";


    private static final Logger log = Logger.getLogger(PrometheusReporter.class);

    public PrometheusReporter(String name, MetricRegistry metricRegistry,
                              MetricFilter metricFilter, long pollingPeriod) {
        super(name);
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (prometheusReporter != null) {
            prometheusReporter.report();
        }

    }

    @Override
    public void startReporter() {
        prometheusReporter = PromReporter.forRegistry(metricRegistry).filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        prometheusReporter.start(pollingPeriod, TimeUnit.SECONDS);

        collectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));
        URL target;
        try {
            target = new URL(serverURL);
            initiateServer(target.getHost(), target.getPort(),collectorRegistry);
            log.info("Prometheus Server has successfully connected at " + serverURL);
        } catch (MalformedURLException | ConnectionUnavailableException e) {
            System.out.println("MalformedURLException");
        }

    }

    @Override
    public void stopReporter() {
        if (prometheusReporter != null) {
            destroy();
            disconnect();
            prometheusReporter.stop();
            prometheusReporter = null;

        }

    }

    private void initiateServer(String host, int port, CollectorRegistry collectorRegistry) throws ConnectionUnavailableException {
        try {
            InetSocketAddress address = new InetSocketAddress(host, port);
            server = new HTTPServer(address, collectorRegistry);
        } catch (IOException e) {
            if (!(e instanceof BindException && e.getMessage().equals("Address already in use"))) {
                log.error("Unable to establish connection for Prometheus  \'\' at " + serverURL);
                throw new ConnectionUnavailableException("Unable to establish connection for Prometheus \'\' at "
                        + serverURL, e);
            }
        }
    }

    public void disconnect() {
        if (server != null) {
            server.stop();
            log.info("Prometheus Server successfully stopped at " + serverURL);
        }
    }

    public void destroy() {
        if (CollectorRegistry.defaultRegistry != null) {
            CollectorRegistry.defaultRegistry.clear();
        }
    }

}