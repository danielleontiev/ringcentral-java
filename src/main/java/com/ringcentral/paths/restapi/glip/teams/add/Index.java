package com.ringcentral.paths.restapi.glip.teams.add;

import com.ringcentral.RestClient;
import com.ringcentral.definitions.GlipPostMembersListBody;

public class Index {
    public RestClient rc;
    public com.ringcentral.paths.restapi.glip.teams.Index parent;

    public Index(com.ringcentral.paths.restapi.glip.teams.Index parent) {
        this.parent = parent;
        this.rc = parent.rc;
    }

    public String path() {
        return parent.path() + "/add";
    }

    /**
     * Adds members to the specified team. A team is a chat between 2 and more participants assigned with specific name.
     * HTTP Method: post
     * Endpoint: /restapi/{apiVersion}/glip/teams/{chatId}/add
     * Rate Limit Group: Medium
     * App Permission: TeamMessaging
     * User Permission: UnifiedAppDesktop
     */
    public String post(GlipPostMembersListBody glipPostMembersListBody) throws com.ringcentral.RestException, java.io.IOException {
        okhttp3.ResponseBody rb = this.rc.post(this.path(), glipPostMembersListBody, null);
        return rb.string();
    }
}
