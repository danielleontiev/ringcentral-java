package com.ringcentral.definitions;


public class PerformanceCallsAggregatesRequest {
    /**
     * Required
     */
    public AggregatePerformanceCallsGrouping grouping;
    /**
     * Required
     */
    public PerformanceCallsTimeSettings timeSettings;
    /**
     *
     */
    public PerformanceCallsFilters additionalFilters;
    /**
     * Required
     */
    public PerformanceCallsResponseDataOptions responseOptions;

    public PerformanceCallsAggregatesRequest grouping(AggregatePerformanceCallsGrouping grouping) {
        this.grouping = grouping;
        return this;
    }

    public PerformanceCallsAggregatesRequest timeSettings(PerformanceCallsTimeSettings timeSettings) {
        this.timeSettings = timeSettings;
        return this;
    }

    public PerformanceCallsAggregatesRequest additionalFilters(PerformanceCallsFilters additionalFilters) {
        this.additionalFilters = additionalFilters;
        return this;
    }

    public PerformanceCallsAggregatesRequest responseOptions(PerformanceCallsResponseDataOptions responseOptions) {
        this.responseOptions = responseOptions;
        return this;
    }
}
