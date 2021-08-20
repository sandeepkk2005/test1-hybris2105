/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package au.com.tti.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import au.com.tti.core.constants.TtiCoreConstants;
import au.com.tti.core.setup.CoreSystemSetup;


/**
 * Do not use, please use {@link CoreSystemSetup} instead.
 * 
 */
public class TtiCoreManager extends GeneratedTtiCoreManager
{
	public static final TtiCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (TtiCoreManager) em.getExtension(TtiCoreConstants.EXTENSIONNAME);
	}
}
