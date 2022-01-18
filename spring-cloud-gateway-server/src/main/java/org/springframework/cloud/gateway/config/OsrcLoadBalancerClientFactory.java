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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A factory that creates client, load balancer and client configuration instances. It
 * creates a Spring ApplicationContext per client name, and extracts the beans that it
 * needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Olga Maciaszek-Sharma
 */
//@Component
public class OsrcLoadBalancerClientFactory extends LoadBalancerClientFactory {

	private static final Log log = LogFactory
			.getLog(LoadBalancerClientFactory.class);

	public OsrcLoadBalancerClientFactory(
			LoadBalancerClientsProperties properties) {
		super(properties);
	}

	@Override
	public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
		ReactorServiceInstanceLoadBalancer instance = getInstance(serviceId,
				ReactorServiceInstanceLoadBalancer.class);
		if (instance == null) {
			// ID such as :runtime-osrc-a61e221b1adb366265e0a9c0faa08a70,find running
			// instance domains generate instance
			DefaultServiceInstance instance1 = new DefaultServiceInstance(
					"myservice-1", "myservice", "platform.osrt.com", 18900,
					false);
			DefaultServiceInstance instance2 = new DefaultServiceInstance(
					"myservice-2", "myservice", "www.osrc.com", 18900, false);
			DefaultServiceInstance[] defaultServiceInstances = new DefaultServiceInstance[] {
					instance1, instance2};

			ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSuppliers = ServiceInstanceListSuppliers
					.toProvider("myservice", defaultServiceInstances);
			instance = new RoundRobinLoadBalancer(serviceInstanceListSuppliers,
					"myservice");
		}
		return instance;
	}

	//@Bean
	//public ServiceInstanceListSupplier staticServiceInstanceListSupplier() {
	//	return ServiceInstanceListSuppliers.from("myservice",
	//			new DefaultServiceInstance("myservice-1", "myservice",
	//					"platform.osrt.com", 18900, false),
	//			new DefaultServiceInstance("myservice-1", "myservice",
	//					"www.osrc.com", 18900, false));
	//}


}
