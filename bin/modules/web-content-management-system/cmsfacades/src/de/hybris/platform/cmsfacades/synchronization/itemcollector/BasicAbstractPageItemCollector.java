/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.cmsfacades.synchronization.itemcollector;

import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.relations.ContentSlotForPageModel;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminContentSlotService;
import de.hybris.platform.cmsfacades.common.itemcollector.ItemCollector;
import de.hybris.platform.cmsfacades.synchronization.cache.SynchronizationCacheService;
import de.hybris.platform.core.model.ItemModel;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * Collects the direct content slots of a given {@link AbstractPageModel}.
 */
public class BasicAbstractPageItemCollector implements ItemCollector<AbstractPageModel>
{
	private CMSAdminContentSlotService cmsAdminContentSlotService;
	private SynchronizationCacheService synchronizationCacheService;

	@Override
	public List<? extends ItemModel> collect(final AbstractPageModel item)
	{
		return getSynchronizationCacheService().getOrSetItemListCache( //
				() -> getCmsAdminContentSlotService().getContentSlotRelationsByPageId(item.getUid(), item.getCatalogVersion()) //
						.stream() //
						.map(ContentSlotForPageModel::getContentSlot) //
						.collect(Collectors.toList()), //
				"BasicAbstractPageItemCollector", item);
	}

	protected CMSAdminContentSlotService getCmsAdminContentSlotService()
	{
		return cmsAdminContentSlotService;
	}

	@Required
	public void setCmsAdminContentSlotService(final CMSAdminContentSlotService cmsAdminContentSlotService)
	{
		this.cmsAdminContentSlotService = cmsAdminContentSlotService;
	}

	public SynchronizationCacheService getSynchronizationCacheService()
	{
		return synchronizationCacheService;
	}

	@Required
	public void setSynchronizationCacheService(SynchronizationCacheService synchronizationCacheService)
	{
		this.synchronizationCacheService = synchronizationCacheService;
	}
}
