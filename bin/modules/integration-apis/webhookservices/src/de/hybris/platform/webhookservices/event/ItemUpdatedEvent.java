/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.webhookservices.event;

import de.hybris.platform.outboundservices.event.EventType;
import de.hybris.platform.outboundservices.event.impl.DefaultEventType;
import de.hybris.platform.tx.AfterSaveEvent;

import java.util.Objects;

/**
 * A {@link WebhookEvent}  that indicates an item update in the platform
 */
public class ItemUpdatedEvent extends BaseWebhookEvent
{
	private static final EventType UPDATED_EVENT = new DefaultEventType("Updated");

	/**
	 * Instantiates an ItemUpdatedEvent
	 *
	 * @param event The {@link AfterSaveEvent} that is wrapped.
	 */
	public ItemUpdatedEvent(final AfterSaveEvent event)
	{
		super(event.getPk(), UPDATED_EVENT);
	}

	@Override
	public String toString()
	{
		return "ItemUpdatedEvent{pk='" + this.getPk() + "', type='" + this.getEventType() + "'}";
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final ItemUpdatedEvent that = (ItemUpdatedEvent) o;
		return this.getPk().equals(that.getPk());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getEventType(), getPk());
	}
}

