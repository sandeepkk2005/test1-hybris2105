/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.integrationbackoffice.widgets.modals.controllers;

import de.hybris.platform.core.Registry;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.integrationbackoffice.services.ReadService;
import de.hybris.platform.integrationservices.model.IntegrationObjectModel;
import de.hybris.platform.integrationservices.util.IntegrationObjectTestUtil;
import de.hybris.platform.integrationservices.util.IntegrationTestUtil;
import de.hybris.platform.odata2services.odata.schema.entity.PluralizingEntitySetNameGenerator;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.type.TypeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static de.hybris.platform.integrationservices.util.IntegrationTestUtil.importImpEx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataViewerControllerIntegrationTest extends ServicelayerTest
{
	private static final String TEST_NAME = "MetadataViewerController";
	private static final String IO_CODE = TEST_NAME + "_ProductIO";

	@Resource
	private TypeService typeService;
	@Resource
	private PluralizingEntitySetNameGenerator defaultPluralizingEntitySetNameGenerator;

	private final IntegrationObjectMetadataViewerController metadataViewerController = new IntegrationObjectMetadataViewerController();

	@Before
	public void setUp()
	{
		final GenericApplicationContext applicationContext = (GenericApplicationContext) Registry.getApplicationContext();
		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();

		final AbstractBeanDefinition validationDefinition = BeanDefinitionBuilder.rootBeanDefinition(ReadService.class)
		                                                                          .getBeanDefinition();
		beanFactory.registerBeanDefinition("readService", validationDefinition);
		final ReadService readService = (ReadService) Registry.getApplicationContext().getBean("readService");
		readService.setTypeService(typeService);
		metadataViewerController.setReadService(readService);
		metadataViewerController.setPluralizer(defaultPluralizingEntitySetNameGenerator);
	}

	@After
	public void tearDown()
	{
		IntegrationTestUtil.removeSafely(IntegrationObjectModel.class, object -> object.getCode().equals(IO_CODE));
	}

	private void setBasicJSONTest() throws ImpExException
	{
		importImpEx(
				"$ioCode=" + IO_CODE,
				"INSERT_UPDATE IntegrationObject; code[unique = true];",
				"                               ; $ioCode",
				"$io = integrationObject(code)[unique = true]",
				"INSERT_UPDATE IntegrationObjectItem; $io    ; code[unique = true]; type(code)    ; root[default = false] ;itemTypeMatch(code)",
				"                                   ; $ioCode; Catalog            ; Catalog       ;                       ; ALL_SUB_AND_SUPER_TYPES;  ",
				"                                   ; $ioCode; Product            ; Product       ; true                  ; ALL_SUB_AND_SUPER_TYPES;  ",
				"                                   ; $ioCode; CatalogVersion     ; CatalogVersion;                       ; ALL_SUB_AND_SUPER_TYPES;  ",
				"$integrationItem = integrationObjectItem(integrationObject(code), code)[unique = true]",
				"$attrName = attributeName[unique = true]",
				"$attrDescriptor = attributeDescriptor(enclosingType(code), qualifier)",
				"$attributeType=returnIntegrationObjectItem(integrationObject(code), code)",
				"INSERT_UPDATE IntegrationObjectItemAttribute; $integrationItem      ; $attrName     ; $attrDescriptor       ; $attributeType        ; unique[default = false]; autoCreate[default = false] ",
				"                                            ; $ioCode:Catalog       ; id            ; Catalog:id            ;                       ; true;  ",
				"                                            ; $ioCode:Product       ; code          ; Product:code          ;                       ; true;  ",
				"                                            ; $ioCode:Product       ; catalogVersion; Product:catalogVersion; $ioCode:CatalogVersion; true;  ",
				"                                            ; $ioCode:CatalogVersion; catalog       ; CatalogVersion:catalog; $ioCode:Catalog       ; true;  ",
				"                                            ; $ioCode:CatalogVersion; version       ; CatalogVersion:version;                       ; true;  "
		);
	}

	@Test
	public void testGenerateEndpointPath() throws ImpExException
	{
		setBasicJSONTest();
		IntegrationObjectModel integrationObjectModel = IntegrationObjectTestUtil.findIntegrationObjectByCode(IO_CODE);
		assertNotNull(integrationObjectModel);

		metadataViewerController.setSelectedIntegrationObject(integrationObjectModel);

		assertEquals("https://<your-host-name>/odata2webservices/" + IO_CODE + "/Products",
				metadataViewerController.generateEndpointPath());

		IntegrationTestUtil.removeSafely(IntegrationObjectModel.class, object -> object.getCode().equals(IO_CODE));
	}

	public static String loadFileAsString(final String fileLocation) throws IOException
	{
		final ClassLoader classLoader = MetadataViewerControllerIntegrationTest.class.getClassLoader();
		final URL url = classLoader.getResource(fileLocation);
		File file = null;
		if (url != null)
		{
			file = new File(url.getFile());
		}

		return Files.readString(Paths.get(file.getPath()));
	}
}