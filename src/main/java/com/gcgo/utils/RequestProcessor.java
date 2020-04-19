package com.gcgo.utils;

import com.gcgo.server.HttpServlet;
import com.gcgo.server.Request;
import com.gcgo.server.Response;

import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

public class RequestProcessor extends Thread {
    private Socket socket;
    private Map<String, HttpServlet> servletMap;

    public RequestProcessor(Socket socket, Map<String, HttpServlet> servletMap) {
        this.socket = socket;
        this.servletMap = servletMap;
    }

    @Override
    public void run() {
        System.out.println("执行线程为："+Thread.currentThread().getName());
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
            //封装request和response
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());
            //处理静态资源
            if (servletMap.get(request.getUrl()) == null) {
                response.outputHtml(request.getUrl());
            } else {
                //处理动态资源
                HttpServlet httpServlet = servletMap.get(request.getUrl());
                httpServlet.service(request, response);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
