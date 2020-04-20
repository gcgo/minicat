package com.gcgo.utils;

import com.gcgo.server.HttpServlet;
import com.gcgo.server.Request;
import com.gcgo.server.Response;
import com.gcgo.server.mapper.Context;
import com.gcgo.server.mapper.Mapper;
import com.gcgo.server.mapper.Wrapper;

import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class RequestProcessor extends Thread {
    private Socket socket;
    private Mapper mapper;

    public RequestProcessor(Socket socket, Mapper mapper) {
        this.socket = socket;
        this.mapper = mapper;
    }

    @Override
    public void run() {
        System.out.println("执行线程为："+Thread.currentThread().getName());
        InputStream inputStream = null;
        HttpServlet servlet = null;
        try {
            inputStream = socket.getInputStream();
            //封装request和response
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());
            //从mapper里找Servlet
            String requestUrl = request.getUrl();//  /demo1/demo1test
            String[] split = requestUrl.split("/");
            if (split.length > 2) {
                String contextName = split[1];
                String servletName = split[2];
                List<Context> contextList = mapper.getHost().getContextList();
                for (Context context : contextList) {
                    if (context.getAppName().equals(contextName)) {
                        List<Wrapper> wrapperList = context.getWrapperList();
                        for (Wrapper wrapper : wrapperList) {
                            if (wrapper.getServletMap().containsKey(servletName)) {
                                servlet = wrapper.getServletMap().get(servletName);
                                break;
                            }
                        }
                        if (servlet != null) break;
                    }
                }
            }
            //处理静态资源
            if (servlet == null) {
                response.outputHtml(requestUrl);
            } else {
                //处理动态资源
                servlet.service(request, response);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
