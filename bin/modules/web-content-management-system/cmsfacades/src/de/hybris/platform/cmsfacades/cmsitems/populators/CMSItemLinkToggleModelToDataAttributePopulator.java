/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.cmsfacades.cmsitems.populators;

import static de.hybris.platform.cmsfacades.constants.CmsfacadesConstants.*;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.HashMap;
import java.util.Map;


/**
 * Populator that prepares the linkToggle field and removes old external and urlLink fields
 */
public class CMSItemLinkToggleModelToDataAttributePopulator extends AbstractCMSItemPopulator
{
	@Override
	public void populate(ItemModel itemModel, Map<String, Object> itemMap) throws ConversionException
	{
		if (isAttributeAllowed(itemModel, FIELD_LINK_TOGGLE_NAME))
		{
			Map<String, Object> linkToggle = new HashMap<>();

			linkToggle.put(FIELD_EXTERNAL_NAME, itemMap.get(FIELD_EXTERNAL_NAME));
			linkToggle.put(FIELD_URL_LINK_NAME, itemMap.get(FIELD_URL_LINK_NAME));
			itemMap.put(FIELD_LINK_TOGGLE_NAME, linkToggle);

			itemMap.remove(FIELD_EXTERNAL_NAME);
			itemMap.remove(FIELD_URL_LINK_NAME);
		}
	}
}
