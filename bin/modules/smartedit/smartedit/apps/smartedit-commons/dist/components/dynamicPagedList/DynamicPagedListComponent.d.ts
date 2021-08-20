import './dynamicPageList.scss';
import { ChangeDetectorRef, EventEmitter, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { LogService, Page, RestServiceFactory } from '@smart/utils';
import { DynamicPagedListApi, DynamicPagedListColumnKey, DynamicPagedListConfig } from './interfaces';
export declare class DynamicPagedListComponent implements OnInit, OnChanges {
    private logService;
    private restServiceFactory;
    private cdr;
    config: DynamicPagedListConfig;
    mask: string;
    getApi: EventEmitter<DynamicPagedListApi>;
    onItemsUpdate: EventEmitter<Page<any>>;
    internalSortBy: string;
    columnSortMode: string;
    currentPage: number;
    ready: boolean;
    totalItems: number;
    items: any[];
    columns: DynamicPagedListColumnKey[];
    private oldMask;
    private api;
    constructor(logService: LogService, restServiceFactory: RestServiceFactory, cdr: ChangeDetectorRef);
    ngOnInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    orderByColumn(event: {
        $columnKey: DynamicPagedListColumnKey;
        $columnSortMode: string;
    }): void;
    loadItems(): Promise<Page<any>>;
    onCurrentPageChange(newCurrentPage: number): void;
    private _validateInput;
    private _buildColumnData;
}
