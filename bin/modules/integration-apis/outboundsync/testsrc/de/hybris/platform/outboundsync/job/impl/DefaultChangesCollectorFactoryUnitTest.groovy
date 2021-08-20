/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.outboundsync.job.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.outboundsync.job.FilteringService
import de.hybris.platform.outboundsync.job.ItemChangeSender
import de.hybris.platform.outboundsync.model.OutboundSyncCronJobModel
import de.hybris.platform.outboundsync.model.OutboundSyncStreamConfigurationModel
import org.junit.Test
import spock.lang.Specification

@UnitTest
class DefaultChangesCollectorFactoryUnitTest extends Specification {
    def factory = new DefaultChangesCollectorFactory(itemChangeSender: Stub(ItemChangeSender), filteringService: Stub(FilteringService))

    @Test
    def "creates new instance of streaming changes collector"() {
        when:
        def collector = factory.createCountingCollector(cronJob(), streamConfiguration())

        then:
        StreamingChangesCollector.isCase collector
    }

    private OutboundSyncStreamConfigurationModel streamConfiguration() {
        Stub(OutboundSyncStreamConfigurationModel)
    }

    private OutboundSyncCronJobModel cronJob() {
        Stub(OutboundSyncCronJobModel)
    }
}
