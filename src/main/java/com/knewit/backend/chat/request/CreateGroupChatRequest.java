package com.knewit.backend.chat.request;

import java.util.List;

public class CreateGroupChatRequest {

    private String groupName;

    private List<String> usernames;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}