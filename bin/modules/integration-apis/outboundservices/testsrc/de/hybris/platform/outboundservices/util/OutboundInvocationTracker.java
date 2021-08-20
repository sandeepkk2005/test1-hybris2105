/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.outboundservices.util;

import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.integrationservices.util.Log;
import de.hybris.platform.outboundservices.facade.SyncParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

/**
 * A base {@link ExternalResource} rule for keeping track of a service invocation.
 *
 * @param <T> type of the subclass of {@link OutboundInvocationTracker}
 */
public abstract class OutboundInvocationTracker<T extends OutboundInvocationTracker<T>> extends ExternalResource
{
	private static final Logger LOG = Log.getLogger(OutboundInvocationTracker.class);

	protected final Collection<Invocation> invocations;
	private final Queue<Supplier<ResponseEntity<Map>>> responseQueue;
	private Supplier<ResponseEntity<Map>> defaultResponseSupplier;

	/**
	 * Instantiates this facade, which by default, if nothing else is specified will respond with the CREATED status and an empty
	 * body.
	 */
	public OutboundInvocationTracker()
	{
		defaultResponseSupplier = this::createDefaultResponse;
		invocations = Collections.synchronizedList(new ArrayList<>());
		responseQueue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Creates an instance of the response to be used by default.
	 *
	 * @return a response that should be returned from the send invocations when explicit response was not specified.
	 */
	protected abstract ResponseEntity<Map> createDefaultResponse();

	/**
	 * Specifies to throw an exception when the send method is called.
	 *
	 * @param e an exception to throw
	 * @return tracker with the behavior applied
	 */
	public T throwException(final RuntimeException e)
	{
		return respondWith(new ExceptionResponse(e));
	}

	/**
	 * Specifies to perform an action before return the specified response from the send method.
	 *
	 * @param builder a response builder to use to create the response after performing the action {@code act}.
	 * @param act     an action to perform before the response is returned. This allows to inject time-sensitive behavior
	 *                to the test to call certain code when a desired item is being processed instead of relying on guessing the
	 *                right time to execute that code in parallel.
	 * @return a tracker with the response specified.
	 */
	public T doAndRespondWith(final ResponseEntity.BodyBuilder builder, final Runnable act)
	{
		return doAndRespondWith(builder.build(), act);
	}

	/**
	 * Specifies to perform an action before return the specified response from the send method.
	 *
	 * @param response response to return after performing the action {@code act}.
	 * @param act      an action to perform before the response is returned. This allows to inject time-sensitive behavior
	 *                 to the test to call certain code when a desired item is being processed instead of relying on guessing the
	 *                 right time to execute that code in parallel.
	 * @return a tracker with the response specified.
	 */
	public T doAndRespondWith(final ResponseEntity<Map> response, final Runnable act)
	{
		final Supplier<ResponseEntity<Map>> supplier = new EntityResponse<>(response, act);
		return respondWith(supplier);
	}

	protected T respondWith(final ResponseEntity<Map> response)
	{
		return respondWith(new EntityResponse<>(response));
	}

	protected T respondWith(final Supplier<ResponseEntity<Map>> response)
	{
		responseQueue.add(response);
		defaultResponseSupplier = response;
		return (T) this;
	}

	protected synchronized ResponseEntity<Map> internalSend(final SyncParameters parameters)
	{
		LOG.info("Sending {}", parameters);
		invocations.add(new Invocation(parameters));
		final var responseSupplier = nextResponseSupplier();
		LOG.info("Responding with {}", responseSupplier);

		return responseSupplier.get();
	}

	private Supplier<ResponseEntity<Map>> nextResponseSupplier()
	{
		final var response = responseQueue.poll();
		return response != null ? response : defaultResponseSupplier;
	}

	public int invocations()
	{
		return invocations.size();
	}

	/**
	 * Retrieves items captured by this facade.
	 *
	 * @param dest   consumed destination, to which the items should have been sent.
	 * @param ioCode code of IntegrationObject used for the items sent.
	 * @return a collection of items send to the specified destination with the specified IntegrationObject code or an empty
	 * collection, if no items were sent with the specified parameters.
	 */
	public Collection<ItemModel> itemsFromInvocationsTo(final ConsumedDestinationModel dest, final String ioCode)
	{
		return itemsFromInvocationsTo(dest.getId(), ioCode);
	}

	/**
	 * Retrieves items captured by this facade.
	 *
	 * @param dest   specifies consumed destination ID, to which the items should have been sent.
	 * @param ioCode code of IntegrationObject used for the items sent.
	 * @return a collection of items send to the specified destination with the specified IntegrationObject code or an empty
	 * collection, if no items were sent with the specified parameters.
	 */
	public Collection<ItemModel> itemsFromInvocationsTo(final String dest, final String ioCode)
	{
		return invocations.stream()
		                  .filter(inv -> inv.matches(dest, ioCode))
		                  .map(Invocation::getItemModel)
		                  .collect(Collectors.toList());
	}

	/**
	 * Retrieves integration key for the deleted items that went through this service.
	 *
	 * @param dest   specifies consumed destination ID, to which the items should have been sent.
	 * @param ioCode code of IntegrationObject used for the items sent.
	 * @return a collection of integration keys for the items deleted through this service in the specified destination or an empty
	 * collection, if no items were sent with the specified parameters.
	 */
	public List<String> keysFromInvocationsTo(final String dest, final String ioCode)
	{
		return invocations.stream()
		                  .filter(inv -> inv.matches(dest, ioCode))
		                  .map(Invocation::getIntegrationKey)
		                  .collect(Collectors.toList());
	}

	@Override
	protected void after()
	{
		responseQueue.clear();
		invocations.clear();
	}

	protected static class Invocation
	{
		private final String destination;
		private final String integrationObject;
		private final String integrationKey;
		private final ItemModel item;

		Invocation(final SyncParameters params)
		{
			destination = params.getDestinationId();
			integrationObject = params.getIntegrationObjectCode();
			item = params.getItem();
			integrationKey = params.getIntegrationKey();
		}

		ItemModel getItemModel()
		{
			return item;
		}

		String getIntegrationKey()
		{
			return integrationKey;
		}

		boolean matches(final String dest, final String ioCode)
		{
			return destination.equals(dest) && integrationObject.equals(ioCode);
		}
	}

	protected static class EntityResponse<R> implements Supplier<R>
	{
		private final R entity;
		private final Runnable action;

		public EntityResponse(final R entity)
		{
			this(entity, null);
		}

		public EntityResponse(final R e, final Runnable toDo)
		{
			entity = e;
			action = toDo;
		}

		@Override
		public R get()
		{
			if (action != null)
			{
				LOG.info("Executing {}", action);
				action.run();
			}
			return entity;
		}

		@Override
		public String toString()
		{
			return action != null
					? action + " -> " + entity.toString()
					: entity.toString();
		}
	}

	protected static class ExceptionResponse implements Supplier<ResponseEntity<Map>>
	{
		private final RuntimeException exception;
		private final Runnable action;

		public ExceptionResponse(final RuntimeException e)
		{
			exception = e;
			action = null;
		}

		public ExceptionResponse(final RuntimeException e, final Runnable act)
		{
			exception = e;
			action = act;
		}

		@Override
		public ResponseEntity<Map> get()
		{
			if (action != null)
			{
				LOG.info("Executing {}", action);
				action.run();
			}
			throw exception;
		}

		@Override
		public String toString()
		{
			return action != null
					? action + " -> " + exception.toString()
					: exception.toString();
		}
	}
}
