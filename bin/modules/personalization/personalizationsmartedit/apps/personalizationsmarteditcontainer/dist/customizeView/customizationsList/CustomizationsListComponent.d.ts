/// <reference types="angular-translate" />
import * as angular from 'angular';
import { LoDashStatic } from 'lodash';
import { CrossFrameEventService } from 'smarteditcommons';
import { PersonalizationsmarteditContextService } from 'personalizationsmarteditcontainer/service/PersonalizationsmarteditContextServiceOuter';
import { PersonalizationsmarteditRestService } from 'personalizationsmarteditcontainer/service/PersonalizationsmarteditRestService';
import { PersonalizationsmarteditMessageHandler } from 'personalizationcommons';
import { PersonalizationsmarteditUtils } from 'personalizationcommons';
import { PersonalizationsmarteditDateUtils } from 'personalizationcommons';
import { PersonalizationsmarteditContextUtils } from 'personalizationcommons';
import { PersonalizationsmarteditPreviewService } from 'personalizationsmarteditcontainer/service/PersonalizationsmarteditPreviewService';
import { PersonalizationsmarteditCustomizeViewServiceProxy } from 'personalizationsmarteditcontainer/customizeView/PersonalizationsmarteditCustomizeViewServiceOuterProxy';
export declare class CustomizationsListComponent {
    protected $q: any;
    protected $translate: angular.translate.ITranslateService;
    protected personalizationsmarteditContextService: PersonalizationsmarteditContextService;
    protected personalizationsmarteditRestService: PersonalizationsmarteditRestService;
    protected personalizationsmarteditCommerceCustomizationService: any;
    protected personalizationsmarteditMessageHandler: PersonalizationsmarteditMessageHandler;
    protected personalizationsmarteditUtils: PersonalizationsmarteditUtils;
    protected personalizationsmarteditDateUtils: PersonalizationsmarteditDateUtils;
    protected personalizationsmarteditContextUtils: PersonalizationsmarteditContextUtils;
    protected personalizationsmarteditPreviewService: PersonalizationsmarteditPreviewService;
    protected personalizationsmarteditManager: any;
    protected personalizationsmarteditCustomizeViewServiceProxy: PersonalizationsmarteditCustomizeViewServiceProxy;
    protected systemEventService: any;
    protected crossFrameEventService: CrossFrameEventService;
    protected SHOW_TOOLBAR_ITEM_CONTEXT: any;
    protected CUSTOMIZE_VIEW_TOOLBAR_ITEM_KEY: any;
    private lodash;
    customizationsList: any[];
    private sourceContainersComponentsInfo;
    constructor($q: any, $translate: angular.translate.ITranslateService, personalizationsmarteditContextService: PersonalizationsmarteditContextService, personalizationsmarteditRestService: PersonalizationsmarteditRestService, personalizationsmarteditCommerceCustomizationService: any, personalizationsmarteditMessageHandler: PersonalizationsmarteditMessageHandler, personalizationsmarteditUtils: PersonalizationsmarteditUtils, personalizationsmarteditDateUtils: PersonalizationsmarteditDateUtils, personalizationsmarteditContextUtils: PersonalizationsmarteditContextUtils, personalizationsmarteditPreviewService: PersonalizationsmarteditPreviewService, personalizationsmarteditManager: any, personalizationsmarteditCustomizeViewServiceProxy: PersonalizationsmarteditCustomizeViewServiceProxy, systemEventService: any, crossFrameEventService: CrossFrameEventService, SHOW_TOOLBAR_ITEM_CONTEXT: any, CUSTOMIZE_VIEW_TOOLBAR_ITEM_KEY: any, lodash: LoDashStatic);
    $onInit(): void;
    initCustomization(customization: any): void;
    editCustomizationAction(customization: any): void;
    customizationRowClick(customization: any, select: boolean): void;
    customizationClick(customization: any): void;
    getSelectedVariationClass(variation: any): string;
    getSelectedCustomizationClass(customization: any): string;
    variationClick(customization: any, variation: any): void;
    hasCommerceActions(variation: any): boolean;
    getCommerceCustomizationTooltip(variation: any): string;
    getActivityStateForCustomization(customization: any): string;
    getActivityStateForVariation(customization: any, variation: any): string;
    clearAllSubMenu(): void;
    getEnablementTextForCustomization(customization: any): string;
    getEnablementTextForVariation(variation: any): string;
    isEnabled(item: any): boolean;
    getDatesForCustomization(customization: any): string;
    customizationSubMenuAction(customization: any): void;
    isCustomizationFromCurrentCatalog(customization: any): boolean;
    statusNotDeleted(variation: any): boolean;
    private matchActionForVariation;
    private numberOfAffectedComponentsForActions;
    private initSourceContainersComponentsInfo;
    private paginatedGetAndSetNumberOfAffectedComponentsForVariations;
    private getAndSetNumberOfAffectedComponentsForVariations;
    private getNumberOfAffectedComponentsForCorrespondingVariation;
    private updateCustomizationData;
    private getVisibleVariations;
    private getAndSetComponentsForVariation;
    private updatePreviewTicket;
    private refreshCustomizeContext;
}