package com.ringcentral.definitions;


/**
 * Query parameters for operation listPagingGroupDevices
 */
public class ListPagingGroupDevicesParameters {
    /**
     * Indicates the page number to retrieve. Only positive number values are accepted
     * Default: 1
     */
    public Long page;
    /**
     * Indicates the page size (number of items)
     * Default: 100
     */
    public Long perPage;

    public ListPagingGroupDevicesParameters page(Long page) {
        this.page = page;
        return this;
    }

    public ListPagingGroupDevicesParameters perPage(Long perPage) {
        this.perPage = perPage;
        return this;
    }
}
