/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.outboundsync.job.impl;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

/**
 * A builder for creating immutable {@link OutboundSyncState}
 */
public class OutboundSyncStateBuilder
{
	private Integer totalItemsRequested;
	private int successCount;
	private int errorCount;
	private int unprocessedCount;
	private Date startTime;
	private Date endTime;
	private boolean aborted;
	private boolean systemicError;

	OutboundSyncStateBuilder()
	{
		// non-instantiable
	}

	/**
	 * Creates initial outbound sync job state builder instance.
	 *
	 * @return new builder instance, which is initialized with the provided total number of items to be processed by the job
	 * and job start date, which is set to the current time.
	 */
	public static OutboundSyncStateBuilder initialOutboundSyncState()
	{
		return new OutboundSyncStateBuilder();
	}

	/**
	 * Creates a new instance of this builder by initializing its state to the provided outbound sync state. Modifiable
	 * attributes can be then changed in the builder, but those attributes that never change will be carried over into the
	 * states created by this this builder.
	 *
	 * @param initialState another state to initialize this builder to
	 * @return a new builder instance initialized to the provided state
	 */
	public static OutboundSyncStateBuilder from(@NotNull final OutboundSyncState initialState)
	{
		Preconditions.checkArgument(initialState != null, "The state cannot be null");
		final Integer totalCount = initialState.getTotalItemsRequested().isPresent()
				? initialState.getTotalItemsRequested().getAsInt()
				: null;
		return new OutboundSyncStateBuilder()
				.withTotalItems(totalCount)
				.withStartTime(initialState.getStartTime())
				.withEndTime(initialState.getEndTime())
				.withErrorCount(initialState.getErrorCount())
				.withSuccessCount(initialState.getSuccessCount())
				.withUnprocessedCount(initialState.getUnprocessedCount())
				.withAborted(initialState.isAborted())
				.withSystemicError(initialState.isSystemicError());
	}

	public OutboundSyncStateBuilder withTotalItems(final Integer cnt)
	{
		if (totalItemsRequested == null)
		{
			totalItemsRequested = cnt;
		}
		return this;
	}

	public OutboundSyncStateBuilder withSuccessCount(final int successCount)
	{
		this.successCount = successCount;
		return this;
	}

	public OutboundSyncStateBuilder withErrorCount(final int errorCount)
	{
		this.errorCount = errorCount;
		return this;
	}

	public OutboundSyncStateBuilder withUnprocessedCount(final int unprocessedCount)
	{
		this.unprocessedCount = unprocessedCount;
		return this;
	}

	public OutboundSyncStateBuilder withStartTime(final Date time)
	{
		startTime = time;
		return this;
	}

	OutboundSyncStateBuilder withEndTime(final Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	public OutboundSyncStateBuilder withAborted(final boolean aborted)
	{
		this.aborted = aborted;
		return this;
	}

	public OutboundSyncStateBuilder withSystemicError(final boolean systemicError)
	{
		this.systemicError = systemicError;
		return this;
	}

	public OutboundSyncState build()
	{
		return new OutboundSyncState(totalItemsRequested, startTime, successCount, errorCount, unprocessedCount, endTime, aborted,
				systemicError);
	}
}
