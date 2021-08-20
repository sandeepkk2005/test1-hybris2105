/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.outboundservices.facade.impl;

import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * REST interface to a remote system.
 */
public interface RemoteSystemClient
{
	/**
	 * Performs POST request to the remote system.
	 * @param destination a remote system info, e.g. URL, to which the request has to be sent.
	 * @param entity an entity to use for the POST request body.
	 * @return result of the POST request.
	 */
	ResponseEntity<Map> post(@NotNull ConsumedDestinationModel destination, HttpEntity<Map<String, Object>> entity);
}
