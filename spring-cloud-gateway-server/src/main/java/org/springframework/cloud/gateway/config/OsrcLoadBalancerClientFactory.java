/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

/**
 * A factory that creates client, load balancer and client configuration instances. It
 * creates a Spring ApplicationContext per client name, and extracts the beans that it
 * needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Olga Maciaszek-Sharma
 */
public class OsrcLoadBalancerClientFactory extends LoadBalancerClientFactory {

	private static final Log log = LogFactory.getLog(LoadBalancerClientFactory.class);

	public OsrcLoadBalancerClientFactory(LoadBalancerClientsProperties properties) {
		super(properties);
	}

	@Override
	public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
		ReactorServiceInstanceLoadBalancer instance = getInstance(serviceId, ReactorServiceInstanceLoadBalancer.class);
		if (instance == null) {
			// ID such as :runtime-osrc-a61e221b1adb366265e0a9c0faa08a70,find running
			// instance domains generate instance

		}
		return instance;
	}

}
