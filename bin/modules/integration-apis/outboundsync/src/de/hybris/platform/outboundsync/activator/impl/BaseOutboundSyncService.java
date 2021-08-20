/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.outboundsync.activator.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.integrationservices.service.ItemModelSearchService;
import de.hybris.platform.integrationservices.util.Log;
import de.hybris.platform.outboundsync.activator.OutboundItemConsumer;
import de.hybris.platform.outboundsync.events.AbortedOutboundSyncEvent;
import de.hybris.platform.outboundsync.events.CompletedOutboundSyncEvent;
import de.hybris.platform.outboundsync.events.SystemErrorOutboundSyncEvent;
import de.hybris.platform.servicelayer.event.EventService;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;

/**
 * This base service provides common functionality across outbound sync services
 */
class BaseOutboundSyncService
{
	private static final Logger LOG = Log.getLogger(BaseOutboundSyncService.class);

	protected ItemModelSearchService itemModelSearchService;
	protected EventService eventService;
	protected OutboundItemConsumer outboundItemConsumer;

	/**
	 * Default constructor. Fields required set using setters
	 */
	protected BaseOutboundSyncService()
	{
		// Using setters to set the fields
	}

	/**
	 * Instantiates this base class.
	 *
	 * @param itemModelSearchService Service to search for item models
	 * @param eventService           Service to send events to update cronjob status
	 * @param outboundItemConsumer   Consumer to consume delta detect changes
	 */
	protected BaseOutboundSyncService(@NotNull final ItemModelSearchService itemModelSearchService,
	                                  @NotNull final EventService eventService,
	                                  @NotNull final OutboundItemConsumer outboundItemConsumer)
	{
		Preconditions.checkArgument(itemModelSearchService != null, "ItemModelSearchService cannot be null");
		Preconditions.checkArgument(eventService != null, "EventService cannot be null");
		Preconditions.checkArgument(outboundItemConsumer != null, "OutboundItemConsumer cannot be null");

		this.itemModelSearchService = itemModelSearchService;
		this.eventService = eventService;
		this.outboundItemConsumer = outboundItemConsumer;
	}

	/**
	 * This method checks the cronjob is in the appropriate state before calling the {@link Synchronizer}.
	 * <p>
	 * Here are the rules:
	 * <ul>
	 *     <li>If the job is aborting, an abort event is published. No synchronization will occur.
	 *     <li>If the job is in system error state, an system error event is published. No synchronization will occur.
	 *     <li>If the job is not aborted and not in system error state, synchronization will occur.
	 * </ul>
	 *
	 * @param cronJobPk       PK of the cronjob being executed
	 * @param changeItemCount The number of changed items detected
	 * @param synchronizer    Synchronizer to execute when the cronjob is in the appropriate state
	 */
	protected void syncInternal(final PK cronJobPk, final int changeItemCount, final Synchronizer synchronizer)
	{
		final var cronJobResult = getCronJob(cronJobPk);
		cronJobResult.ifPresent(cronJob -> {
			if (isCronJobAborting(cronJob))
			{
				publishAbortEvent(cronJob.getPk(), changeItemCount);
			}
			else if (isSystemError(cronJob))
			{
				publishSystemErrorEvent(cronJob.getPk(), changeItemCount);
			}
			else if (!isCronJobAborted(cronJob) && !isSystemError(cronJob))
			{
				synchronizer.sync();
			}
		});
	}

	private boolean isCronJobAborting(final CronJobModel cronJob)
	{
		return cronJob != null && Boolean.TRUE.equals(cronJob.getRequestAbort());
	}

	private boolean isCronJobAborted(final CronJobModel cronJob)
	{
		return cronJob != null && cronJob.getStatus() == CronJobStatus.ABORTED;
	}

	private boolean isSystemError(final CronJobModel cronJob)
	{
		return cronJob != null && cronJob.getResult() == CronJobResult.FAILURE;
	}

	private void publishAbortEvent(final PK cronJobPk, final int changeItemCount)
	{
		LOG.debug("Publishing aborted event");
		eventService.publishEvent(new AbortedOutboundSyncEvent(cronJobPk, changeItemCount));
	}

	/**
	 * Publish then {@link SystemErrorOutboundSyncEvent} if a system error occurs
	 *
	 * @param cronJobPk       PK of the cronjob being executed
	 * @param changeItemCount The number of changed items detected
	 */
	protected void publishSystemErrorEvent(final PK cronJobPk, final int changeItemCount)
	{
		LOG.debug("Publishing system error event");
		eventService.publishEvent(new SystemErrorOutboundSyncEvent(cronJobPk, changeItemCount));
	}

	/**
	 * Publish the {@link CompletedOutboundSyncEvent} with success set to true when the synchronization is done
	 *
	 * @param cronJobPk       PK of the cronjob being executed
	 * @param changeItemCount The number of changed items detected
	 */
	protected void publishSuccessfulCompletedEvent(final PK cronJobPk, final int changeItemCount)
	{
		LOG.debug("Publishing successful completed event");
		eventService.publishEvent(new CompletedOutboundSyncEvent(cronJobPk, true, changeItemCount));
	}

	/**
	 * Publish the {@link CompletedOutboundSyncEvent} with success set to false when the synchronization is done
	 *
	 * @param cronJobPk       PK of the cronjob being executed
	 * @param changeItemCount The number of changed items detected
	 */
	protected void publishUnSuccessfulCompletedEvent(final PK cronJobPk, final int changeItemCount)
	{
		LOG.debug("Publishing unsuccessful completed event");
		eventService.publishEvent(new CompletedOutboundSyncEvent(cronJobPk, false, changeItemCount));
	}

	/**
	 * Gets the {@link CronJobModel} from the database
	 *
	 * @param cronJobPk PK of the cronjob being executed
	 * @return The cronjob wrapped in an {@link Optional} if found, otherwise an {@link Optional#empty()}
	 */
	protected Optional<CronJobModel> getCronJob(final PK cronJobPk)
	{
		return itemModelSearchService.nonCachingFindByPk(cronJobPk);
	}

	/**
	 * The Synchronizer interface is defined to be used with the {@link #syncInternal(PK, int, Synchronizer)} method.
	 * Because subclasses may have different synchronize method that takes different input parameters, this
	 * interface provides a common method to call when synchronizing.
	 */
	protected interface Synchronizer
	{
		/**
		 * Synchronizes the change.
		 * The user can supply an implementation by using lambda expression {@code () -> synchronizeAnItem(args1,...,argsN)},
		 * where {@code synchronizeAnItem(...)} is a method in the subclass of this base class.
		 */
		void sync();
	}
}
