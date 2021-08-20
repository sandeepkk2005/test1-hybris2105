/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package au.com.tti.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import au.com.tti.fulfilmentprocess.constants.TtiFulfilmentProcessConstants;

public class TtiFulfilmentProcessManager extends GeneratedTtiFulfilmentProcessManager
{
	public static final TtiFulfilmentProcessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (TtiFulfilmentProcessManager) em.getExtension(TtiFulfilmentProcessConstants.EXTENSIONNAME);
	}
	
}
