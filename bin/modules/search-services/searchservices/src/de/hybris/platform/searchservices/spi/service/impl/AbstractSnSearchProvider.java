/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.searchservices.spi.service.impl;

import de.hybris.platform.searchservices.admin.data.SnIndexConfiguration;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.spi.data.AbstractSnSearchProviderConfiguration;
import de.hybris.platform.searchservices.spi.service.SnSearchProvider;

import java.text.MessageFormat;


/**
 * Base class for search providers.
 */
public abstract class AbstractSnSearchProvider<T extends AbstractSnSearchProviderConfiguration> implements SnSearchProvider<T>
{
	@Override
	public T getSearchProviderConfiguration(final SnIndexConfiguration indexConfiguration) throws SnException
	{
		if (indexConfiguration.getSearchProviderConfiguration() == null)
		{
			throw new SnException(MessageFormat.format("Search provider configuration not set for index configuration ''{0}''",
					indexConfiguration.getId()));
		}

		return (T) indexConfiguration.getSearchProviderConfiguration();
	}

	@Override
	public T getSearchProviderConfiguration(final SnContext context)
	{
		final SnIndexConfiguration indexConfiguration = context.getIndexConfiguration();
		return (T) indexConfiguration.getSearchProviderConfiguration();
	}

	@Override
	public void exportConfiguration(final SnContext context) throws SnException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void createIndex(final SnContext context, final String indexId) throws SnException
	{
		throw new UnsupportedOperationException();
	}
}
