package com.gcgo.server.mapper;

import java.util.ArrayList;
import java.util.List;

public class Host {
    private String hostName = null;
    private List<Context> contextList = new ArrayList<>();

    public List<Context> getContextList() {
        return contextList;
    }

    public void setContextList(List<Context> contextList) {
        this.contextList = contextList;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
