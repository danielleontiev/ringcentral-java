package com.ringcentral.definitions;


/**
 * Status information (reason, comment, lifetime). Returned for 'Disabled' status only
 */
public class AccountStatusInfo {
    /**
     * A free-form user comment, describing the status change reason
     */
    public String comment;
    /**
     * Type of suspension
     * Enum: SuspendedVoluntarily, SuspendedInvoluntarily, UserResumed
     */
    public String reason;
    /**
     * Date until which the account will get deleted. The default value is 30 days since current date
     */
    public String till;

    public AccountStatusInfo comment(String comment) {
        this.comment = comment;
        return this;
    }

    public AccountStatusInfo reason(String reason) {
        this.reason = reason;
        return this;
    }

    public AccountStatusInfo till(String till) {
        this.till = till;
        return this;
    }
}
