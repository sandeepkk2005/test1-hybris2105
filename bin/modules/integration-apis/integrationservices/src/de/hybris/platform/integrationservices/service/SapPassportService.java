/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.integrationservices.service;

/**
 * Generates an SAP Passport based on the provided information
 */
public interface SapPassportService
{
	/**
	 * Generates an SAP Passport based on the provided info.
	 * @param integrationObjectCode The integrationObject code.
	 * @return The passport representation.
	 */
	String generate(String integrationObjectCode);
}