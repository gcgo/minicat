package com.gcgo.server.mapper;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private String appName = null;
    private List<Wrapper> wrapperList = new ArrayList<>();

    public List<Wrapper> getWrapperList() {
        return wrapperList;
    }

    public void setWrapperList(List<Wrapper> wrapperList) {
        this.wrapperList = wrapperList;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
