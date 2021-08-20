/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.odata2services.odata.persistence;

import static de.hybris.platform.odata2services.odata.persistence.ItemLookupRequest.itemLookupRequestBuilder;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.impl.DefaultPersistenceContext;
import de.hybris.platform.integrationservices.item.IntegrationItem;
import de.hybris.platform.integrationservices.model.TypeAttributeDescriptor;
import de.hybris.platform.integrationservices.search.ItemSearchRequest;
import de.hybris.platform.integrationservices.search.ItemSearchRequestBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;

import com.google.common.base.Preconditions;

/**
 * Request which contains an item for persistence.
 */
public class StorageRequest extends CrudRequest implements PersistenceContext
{
	private final DefaultPersistenceContext persistenceContext;

	private String postPersistHook;
	private String prePersistHook;

	StorageRequest(final DefaultPersistenceContext ctx, final EdmEntitySet entitySet) throws EdmException
	{
		Preconditions.checkArgument(ctx != null, "DefaultPersistenceContext cannot be null");
		Preconditions.checkArgument(entitySet != null, "EdmEntitySet cannot be null");
		Preconditions.checkArgument(entitySet.getEntityType() != null, "EdmEntityType cannot be null");

		persistenceContext = ctx;
		setEntitySet(entitySet);
		setEntityType(entitySet.getEntityType());
	}

	public static StorageRequestBuilder storageRequestBuilder()
	{
		return new StorageRequestBuilder();
	}

	@Nonnull
	@Override
	public IntegrationItem getIntegrationItem()
	{
		return persistenceContext.getIntegrationItem();
	}

	public String getPrePersistHook()
	{
		return prePersistHook != null ? prePersistHook : "";
	}

	public String getPostPersistHook()
	{
		return postPersistHook != null ? postPersistHook : "";
	}

	public DefaultPersistenceContext getPersistenceContext()
	{
		return persistenceContext;
	}

	@Nonnull
	@Override
	public Locale getAcceptLocale()
	{
		return persistenceContext.getAcceptLocale();
	}

	protected void setPostPersistHook(final String postPersistHook)
	{
		this.postPersistHook = postPersistHook;
	}

	protected void setPrePersistHook(final String prePersistHook)
	{
		this.prePersistHook = prePersistHook;
	}

	/**
	 * Creates an {@link ItemLookupRequest} from this {@link StorageRequest}
	 *
	 * @return the newly constructed ItemLookupRequest
	 * @throws EdmException if encounters an OData problem
	 * @deprecated replaced with {@link #toItemSearchRequest()}
	 */
	@Deprecated(since = "2105", forRemoval = true)
	public ItemLookupRequest toLookupRequest() throws EdmException
	{
		return itemLookupRequestBuilder()
				.withAcceptLocale(getAcceptLocale())
				.withEntitySet(getEntitySet())
				.withIntegrationObject(getIntegrationObjectCode())
				.withODataEntry(getODataEntry())
				.withServiceRoot(getServiceRoot())
				.withContentType(getContentType())
				.withRequestUri(getRequestUri())
				.withIntegrationItem(getIntegrationItem())
				.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public ItemSearchRequest toItemSearchRequest()
	{
		final ItemSearchRequestBuilder searchRequestBuilder = new ItemSearchRequestBuilder();
		return searchRequestBuilder
				.withIntegrationItem(getIntegrationItem())
				.withLocale(getAcceptLocale())
				.build();
	}

	@Nonnull
	@Override
	public Locale getContentLocale()
	{
		return persistenceContext.getContentLocale();
	}

	@Override
	public PersistenceContext getReferencedContext(final TypeAttributeDescriptor attribute)
	{
		return persistenceContext.getReferencedContext(attribute);
	}

	@Override
	public Collection<PersistenceContext> getReferencedContexts(final TypeAttributeDescriptor attribute)
	{
		return persistenceContext.getReferencedContexts(attribute);
	}

	@Override
	public boolean isReplaceAttributes()
	{
		return persistenceContext.isReplaceAttributes();
	}

	@Override
	public boolean isItemCanBeCreated()
	{
		return persistenceContext.isItemCanBeCreated();
	}

	@Override
	public Optional<PersistenceContext> getSourceContext()
	{
		return persistenceContext.getSourceContext();
	}

	@Override
	public Optional<ItemModel> getContextItem()
	{
		return persistenceContext.getContextItem();
	}

	@Override
	public void putItem(final ItemModel item)
	{
		persistenceContext.putItem(item);
	}

	@Nonnull
	@Override
	public PersistenceContext getRootContext()
	{
		return persistenceContext.getRootContext();
	}

	/**
	 * @deprecated Please use {@link de.hybris.platform.odata2services.odata.persistence.StorageRequestBuilder} instead.
	 */
	@Deprecated(since = "21.05", forRemoval = true)
	public static class StorageRequestBuilder extends de.hybris.platform.odata2services.odata.persistence.StorageRequestBuilder
	{
		StorageRequestBuilder()
		{
			super();
		}
	}
}
