package com.gcgo.server.mapper;

import com.gcgo.server.HttpServlet;

import java.util.HashMap;
import java.util.Map;

public class Wrapper {
    private Map<String, HttpServlet> servletMap = new HashMap<>();

    public Map<String, HttpServlet> getServletMap() {
        return servletMap;
    }

    public void setServletMap(Map<String, HttpServlet> servletMap) {
        this.servletMap = servletMap;
    }
}
