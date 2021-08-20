/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.webhookservices.service.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.apiregistryservices.dto.EventSourceData
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel
import de.hybris.platform.apiregistryservices.model.DestinationTargetModel
import de.hybris.platform.apiregistryservices.model.events.EventConfigurationModel
import de.hybris.platform.core.PK
import de.hybris.platform.core.model.ItemModel
import de.hybris.platform.integrationservices.model.IntegrationObjectModel
import de.hybris.platform.integrationservices.service.ItemModelSearchService
import de.hybris.platform.order.events.SubmitOrderEvent
import de.hybris.platform.outboundservices.enums.OutboundSource
import de.hybris.platform.outboundservices.event.impl.DefaultEventType
import de.hybris.platform.outboundservices.facade.OutboundServiceFacade
import de.hybris.platform.outboundservices.facade.SyncParameters
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.webhookservices.event.BaseWebhookEvent
import de.hybris.platform.webhookservices.event.WebhookEvent
import de.hybris.platform.webhookservices.filter.WebhookFilterService
import de.hybris.platform.webhookservices.model.WebhookConfigurationModel
import de.hybris.platform.webhookservices.service.WebhookConfigurationService
import org.junit.Test
import rx.Observable
import spock.lang.Specification
import spock.lang.Unroll

@UnitTest
class WebhookEventToItemSenderUnitTest extends Specification {

    private static final def SAVED_ITEM_PK = PK.fromLong 1
    private static final def SAVED_ITEM = new ItemModel()
    private static final def FILTER_SCRIPT_URI = 'script://someScriptThatDoesNotMatter'
    private static final def MATCHING_DESTINATION_TARGET_ID = 'matchingDestinationTargetId'
    private static final def NON_MATCHING_DESTINATION_TARGET_ID = 'nonMatchingDestinationTargetId'
    private static final def CREATED_EVENT_TYPE = new DefaultEventType('Created')
    private static final def UPDATED_EVENT_TYPE = new DefaultEventType('Updated')

    def outboundServiceFacade = Mock OutboundServiceFacade
    def webhookConfigurationService = Mock(WebhookConfigurationService) {
        getWebhookConfigurationsByEventAndItemModel({ it.getPk() == SAVED_ITEM_PK } as WebhookEvent, SAVED_ITEM) >> [webhookConfig()]
    }
    def modelService = Stub(ModelService)
    def itemModelSearchService = Mock(ItemModelSearchService) {
        nonCachingFindByPk(SAVED_ITEM_PK) >> Optional.of(SAVED_ITEM)
    }

    def sender = new WebhookEventToItemSender(outboundServiceFacade, webhookConfigurationService, itemModelSearchService)

    @Test
    @Unroll
    def "sender fails to instantiate because #msg with deprecated constructor"() {
        when:
        new WebhookEventToItemSender(outboundServiceFacade, webhookConfigurationService, modelService as ModelService)

        then:
        def e = thrown IllegalArgumentException
        e.message == msg

        where:
        outboundServiceFacade       | webhookConfigurationService       | modelService       | msg
        null                        | Stub(WebhookConfigurationService) | Stub(ModelService) | 'OutboundServiceFacade cannot be null'
        Stub(OutboundServiceFacade) | null                              | Stub(ModelService) | 'WebhookConfigurationService cannot be null'
        Stub(OutboundServiceFacade) | Stub(WebhookConfigurationService) | null               | 'ModelService cannot be null'
    }

    @Test
    @Unroll
    def "sender fails to instantiate because #msg"() {
        when:
        new WebhookEventToItemSender(outboundServiceFacade, webhookConfigurationService, itemModelSearchService as ItemModelSearchService)

        then:
        def e = thrown IllegalArgumentException
        e.message == msg

        where:
        outboundServiceFacade       | webhookConfigurationService       | itemModelSearchService       | msg
        null                        | Stub(WebhookConfigurationService) | Stub(ItemModelSearchService) | 'OutboundServiceFacade cannot be null'
        Stub(OutboundServiceFacade) | null                              | Stub(ItemModelSearchService) | 'WebhookConfigurationService cannot be null'
        Stub(OutboundServiceFacade) | Stub(WebhookConfigurationService) | null                         | 'ItemModelSearchService cannot be null'
    }

    @Test
    def 'item is not sent when event is not an WebhookEvent'() {
        given: 'SubmitOrderEvent occurred'
        def eventSourceData = Stub(EventSourceData) {
            getEvent() >> Stub(SubmitOrderEvent)
        }

        when:
        sender.send eventSourceData

        then:
        0 * itemModelSearchService._
        0 * webhookConfigurationService._
        0 * outboundServiceFacade._
    }

    @Test
    def 'item is not sent when the item is not found'() {
        when:
        sender.send webhookEventSourceData()

        then: "modelService didn't find the item"
        1 * itemModelSearchService.nonCachingFindByPk(SAVED_ITEM_PK) >> Optional.empty()

        and: "webhookConfigurationService and outboundServiceFacade are not invoked"
        0 * webhookConfigurationService._
        0 * outboundServiceFacade._
    }

    @Test
    def 'item is not sent when no webhook configurations are found'() {
        given: 'WebhookEvent occurred'
        def eventSourceData = webhookEventSourceData()

        when:
        sender.send eventSourceData

        then: 'no matching webhookconfiguration is found for the event and item'
        1 * webhookConfigurationService.getWebhookConfigurationsByEventAndItemModel(eventSourceData.event, SAVED_ITEM) >> []

        and: 'outboundServiceFacade is not invoked'
        0 * outboundServiceFacade._
    }

    @Test
    @Unroll
    def "item is sent when the event is of saved action type #saveActionType and webhook configurations are found"() {
        given: 'WebhookEvent occurred'
        def eventSourceData = webhookEventSourceData saveActionType

        and: 'WebhookConfigurations are found'
        def config1 = webhookConfig()
        def config2 = webhookConfig()

        when:
        sender.send eventSourceData

        then: 'item is sent to all webhook configurations found'
        1 * webhookConfigurationService.getWebhookConfigurationsByEventAndItemModel(eventSourceData.event, SAVED_ITEM) >> [config1, config2]
        1 * outboundServiceFacade.send(asSyncParams(config1, saveActionType)) >> Observable.empty()
        1 * outboundServiceFacade.send(asSyncParams(config2, saveActionType)) >> Observable.empty()

        where:
        saveActionType << [UPDATED_EVENT_TYPE, CREATED_EVENT_TYPE]
    }

    @Test
    def 'item is sent when a webhook filter present and it passes the item through'() {
        given: 'there is a filter that passes the item'
        sender.filterService = Stub(WebhookFilterService) {
            filter(SAVED_ITEM, FILTER_SCRIPT_URI) >> Optional.of(SAVED_ITEM)
        }

        when: 'the event is processed'
        sender.send webhookEventSourceData()

        then: 'the item is sent to the destination'
        1 * outboundServiceFacade.send(asSyncParams(webhookConfig())) >> Observable.empty()
    }

    @Test
    def 'item returned by the filter is sent to the destination'() {
        given: 'there is a filter that replaces the original item'
        def itemFromFilter = new ItemModel()
        sender.filterService = Stub(WebhookFilterService) {
            filter(SAVED_ITEM, FILTER_SCRIPT_URI) >> Optional.of(itemFromFilter)
        }
        and: 'a webhookConfiguration'
        def cfg = webhookConfig()

        when: 'the event is processed'
        sender.send webhookEventSourceData()

        then: 'the item is sent to the destination'
        1 * outboundServiceFacade.send(asSyncParams(itemFromFilter, cfg)) >> Observable.empty()
        0 * outboundServiceFacade.send(asSyncParams(SAVED_ITEM, cfg))
    }

    @Test
    def 'item is not sent when the filter prevents item from being sent'() {
        given: 'the filter blocks the item'
        sender.filterService = Stub(WebhookFilterService) {
            filter(SAVED_ITEM, FILTER_SCRIPT_URI) >> Optional.empty()
        }

        when: 'the event is processed'
        sender.send webhookEventSourceData()

        then: 'the item is not sent to the destination'
        0 * outboundServiceFacade._
    }

    @Test
    @Unroll
    def "sender is called [#calls] time when the webhook has [#destinationTargetId] and the event has [matchingDestinationTargetId] for #saveActionType"() {
        when: 'the event is processed'
        sender.send webhookEventSourceData(saveActionType, destinationTargetId)

        then: 'item is sent to all webhook configurations found'
        calls * outboundServiceFacade.send(_) >> Observable.empty()

        where:
        calls | saveActionType     | destinationTargetId
        1     | UPDATED_EVENT_TYPE | MATCHING_DESTINATION_TARGET_ID
        1     | CREATED_EVENT_TYPE | MATCHING_DESTINATION_TARGET_ID
        0     | UPDATED_EVENT_TYPE | NON_MATCHING_DESTINATION_TARGET_ID
        0     | CREATED_EVENT_TYPE | NON_MATCHING_DESTINATION_TARGET_ID
    }


    private static SyncParameters asSyncParams(WebhookConfigurationModel cfg) {
        asSyncParams(cfg, CREATED_EVENT_TYPE)
    }

    private static SyncParameters asSyncParams(WebhookConfigurationModel cfg, DefaultEventType eventType) {
        SyncParameters.syncParametersBuilder()
                .withItem(SAVED_ITEM)
                .withSource(OutboundSource.WEBHOOKSERVICES)
                .withIntegrationObject(cfg.integrationObject)
                .withDestination(cfg.destination)
                .withEventType(eventType)
                .build()
    }

    private static SyncParameters asSyncParams(ItemModel item, WebhookConfigurationModel cfg) {
        SyncParameters.syncParametersBuilder()
                .withItem(item)
                .withSource(OutboundSource.WEBHOOKSERVICES)
                .withIntegrationObject(cfg.integrationObject)
                .withDestination(cfg.destination)
                .withEventType(CREATED_EVENT_TYPE)
                .build()
    }

    private EventSourceData webhookEventSourceData(DefaultEventType eventType = CREATED_EVENT_TYPE,
                                                   String destinationTargetId = MATCHING_DESTINATION_TARGET_ID) {
        Stub(EventSourceData) {
            getEvent() >> Stub(BaseWebhookEvent) {
                getPk() >> SAVED_ITEM_PK
                getEventType() >> eventType
            }
            getEventConfig() >> Stub(EventConfigurationModel) {
                getDestinationTarget() >> Stub(DestinationTargetModel) {
                    getId() >> destinationTargetId
                }
            }
        }
    }

    private WebhookConfigurationModel webhookConfig() {
        Stub(WebhookConfigurationModel) {
            getIntegrationObject() >> Stub(IntegrationObjectModel)
            getFilterLocation() >> FILTER_SCRIPT_URI
            getDestination() >> Stub(ConsumedDestinationModel) {
                getDestinationTarget() >> Stub(DestinationTargetModel) {
                    getId() >> MATCHING_DESTINATION_TARGET_ID
                }
            }
        }
    }

}
