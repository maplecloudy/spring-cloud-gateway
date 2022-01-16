/*
 * Copyright 2013-2020 the original author or authors.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.event.PredicateArgsEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;

@Validated
public class OsrcDynamicRoutes {

	private final Log logger = LogFactory.getLog(getClass());
	/**
	 * List of filter definitions that are applied to every route.
	 */
	private List<FilterDefinition> defaultFilters = new ArrayList<>();

	private List<MediaType> streamingMediaTypes = Arrays.asList(MediaType.TEXT_EVENT_STREAM,
			MediaType.APPLICATION_STREAM_JSON, new MediaType("application", "grpc"),
			new MediaType("application", "grpc+protobuf"), new MediaType("application", "grpc+json"));

	private final Map<String, RoutePredicateFactory> predicates = new LinkedHashMap<>();

	/**
	 * Option to fail on route definition errors, defaults to true. Otherwise, a warning
	 * is logged.
	 */
	private boolean failOnRouteDefinitionError = true;

	private RouteDefinitionLocator routeDefinitionLocator;

	private ConfigurationService configurationService;

	private Object gatewayProperties;

	public OsrcDynamicRoutes(GatewayProperties properties, List<GatewayFilterFactory> gatewayFilters,
			List<RoutePredicateFactory> predicates, RouteDefinitionLocator routeDefinitionLocator,
			ConfigurationService configurationService) {
		this.routeDefinitionLocator = routeDefinitionLocator;
		this.configurationService = configurationService;
		this.gatewayProperties = gatewayProperties;
	}

	public List<RouteDefinition> getRds(ServerWebExchange exchange) {
		List<RouteDefinition> lrs = new ArrayList<RouteDefinition>();
		String host = exchange.getRequest().getURI().getHost();

		// 根据host获取RouteDefinition
		RouteDefinition rt = new RouteDefinition();

		String domainRouts = "id: cookie_route\\n\" + \"uri: https://example.org\\n\"\n"
				+ "      + \"predicates:\\n\" + \"  - Cookie=mycookie,mycookievalue";

		try {
			rt.setUri(new URI("adadsf"));
		}
		catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lrs;
	}

	String rdYml = "id: cookie_route\n" + "uri: https://example.org\n" + "predicates:\n"
			+ "  - Cookie=mycookie,mycookievalue";

	public String getRouteDefinitionFromHost(String host) {
		return "";
	}

	public Flux<Route> getRoutes(ServerWebExchange exchange) {
		Flux<Route> routes = Flux.fromStream(getRds(exchange).stream().map(this::convertToRoute));
		// instead of letting error bubble up, continue
		routes = routes.onErrorContinue((error, obj) -> {
			if (logger.isWarnEnabled()) {
				logger.warn("RouteDefinition id " + ((RouteDefinition) obj).getId()
						+ " will be ignored. Definition has invalid configs, " + error.getMessage());
			}
		});
		return routes.map(route -> {
			if (logger.isDebugEnabled()) {
				logger.debug("RouteDefinition matched: " + route.getId());
			}
			return route;
		});
	}

	private Route convertToRoute(RouteDefinition routeDefinition) {
		AsyncPredicate<ServerWebExchange> predicate = combinePredicates(routeDefinition);
		List<GatewayFilter> gatewayFilters = getFilters(routeDefinition);

		return Route.async(routeDefinition).asyncPredicate(predicate).replaceFilters(gatewayFilters).build();
	}

	private AsyncPredicate<ServerWebExchange> combinePredicates(RouteDefinition routeDefinition) {
		List<PredicateDefinition> predicates = routeDefinition.getPredicates();
		if (predicates == null || predicates.isEmpty()) {
			// this is a very rare case, but possible, just match all
			return AsyncPredicate.from(exchange -> true);
		}
		AsyncPredicate<ServerWebExchange> predicate = lookup(routeDefinition, predicates.get(0));

		for (PredicateDefinition andPredicate : predicates.subList(1, predicates.size())) {
			AsyncPredicate<ServerWebExchange> found = lookup(routeDefinition, andPredicate);
			predicate = predicate.and(found);
		}

		return predicate;
	}

	private List<GatewayFilter> getFilters(RouteDefinition routeDefinition) {
		List<GatewayFilter> filters = new ArrayList<>();

		// TODO: support option to apply defaults after route specific filters?
		// if (!this.gatewayProperties.getDefaultFilters().isEmpty()) {
		// filters.addAll(loadGatewayFilters(routeDefinition.getId(),
		// new ArrayList<>(this.gatewayProperties.getDefaultFilters())));
		// }
		//
		// if (!routeDefinition.getFilters().isEmpty()) {
		// filters.addAll(loadGatewayFilters(routeDefinition.getId(), new
		// ArrayList<>(routeDefinition.getFilters())));
		// }

		AnnotationAwareOrderComparator.sort(filters);
		return filters;
	}

	public List<FilterDefinition> getDefaultFilters() {
		return defaultFilters;
	}

	public void setDefaultFilters(List<FilterDefinition> defaultFilters) {
		this.defaultFilters = defaultFilters;
	}

	public List<MediaType> getStreamingMediaTypes() {
		return streamingMediaTypes;
	}

	public void setStreamingMediaTypes(List<MediaType> streamingMediaTypes) {
		this.streamingMediaTypes = streamingMediaTypes;
	}

	public boolean isFailOnRouteDefinitionError() {
		return failOnRouteDefinitionError;
	}

	public void setFailOnRouteDefinitionError(boolean failOnRouteDefinitionError) {
		this.failOnRouteDefinitionError = failOnRouteDefinitionError;
	}

	@SuppressWarnings("unchecked")
	private AsyncPredicate<ServerWebExchange> lookup(RouteDefinition route, PredicateDefinition predicate) {
		RoutePredicateFactory<Object> factory = this.predicates.get(predicate.getName());
		if (factory == null) {
			throw new IllegalArgumentException("Unable to find RoutePredicateFactory with name " + predicate.getName());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("RouteDefinition " + route.getId() + " applying " + predicate.getArgs() + " to "
					+ predicate.getName());
		}

	// @formatter:off
    Object config = this.configurationService.with(factory)
        .name(predicate.getName())
        .properties(predicate.getArgs())
        .eventFunction((bound, properties) -> new PredicateArgsEvent(
            OsrcDynamicRoutes.this, route.getId(), properties))
        .bind();
    // @formatter:on

		return factory.applyAsync(config);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("defaultFilters", defaultFilters)
				.append("streamingMediaTypes", streamingMediaTypes)
				.append("failOnRouteDefinitionError", failOnRouteDefinitionError).toString();

	}

}
