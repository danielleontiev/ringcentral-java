package com.ringcentral.definitions;


public class PerformanceCallsTimersResponseOptionsCallsDurationByQueueSla
{
    /**
     * Enum: Sum, Average, Max, Min, Percent
     */
    public String aggregationType;
    public PerformanceCallsTimersResponseOptionsCallsDurationByQueueSla aggregationType(String aggregationType)
    {
        this.aggregationType = aggregationType;
        return this;
    }

    /**
     * Enum: Hour, Day, Week, Month
     */
    public String aggregationInterval;
    public PerformanceCallsTimersResponseOptionsCallsDurationByQueueSla aggregationInterval(String aggregationInterval)
    {
        this.aggregationInterval = aggregationInterval;
        return this;
    }
}