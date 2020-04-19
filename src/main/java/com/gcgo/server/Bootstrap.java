package com.gcgo.server;

import com.gcgo.utils.RequestProcessor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Bootstrap {
    //定义监听端口号
    private int port = 8080;
    private Map<String, HttpServlet> servletMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /*启动初始化*/
    public void start() throws Exception {
        //简单打印输出
        //        ServerSocket serverSocket;
        //        Socket socket = null;
        //        try {
        //            serverSocket = new ServerSocket(port);
        //            System.out.println("minicat开始监听端口：" + port);
        //            while (true) {
        //                socket = serverSocket.accept();
        //                System.out.println("已连接");
        //                OutputStream outputStream = socket.getOutputStream();
        //                String text = "你好 minicat";
        //                String responseText = HttpProtocolUtil.
        //                        getHttpHeader200(text.getBytes().length) + text;
        //                outputStream.write(responseText.getBytes("GBK"));
        //            }
        //        } finally {
        //            socket.close();
        //        }

        //静态资源访问：
        //        ServerSocket serverSocket = new ServerSocket(port);
        //        System.out.println("minicat开始监听端口：" + port);
        //        while (true) {
        //            Socket socket = serverSocket.accept();
        //            InputStream inputStream = socket.getInputStream();
        //            //封装request和response
        //            Request request = new Request(inputStream);
        //            Response response = new Response(socket.getOutputStream());
        //
        //            response.outputHtml(request.getUrl());
        //            socket.close();
        //        }

        //动态资源访问：
        //加载解析web.xml方法
        //        loadServlet();
        //        ServerSocket serverSocket = new ServerSocket(port);
        //        System.out.println("minicat开始监听端口：" + port);
        //        while (true) {
        //            Socket socket = serverSocket.accept();
        //            InputStream inputStream = socket.getInputStream();
        //            //封装request和response
        //            Request request = new Request(inputStream);
        //            Response response = new Response(socket.getOutputStream());
        //            //处理静态资源
        //            if (servletMap.get(request.getUrl()) == null) {
        //                response.outputHtml(request.getUrl());
        //            } else {
        //                //处理动态资源
        //                HttpServlet httpServlet = servletMap.get(request.getUrl());
        //                httpServlet.service(request, response);
        //            }
        //            socket.close();
        //        }

        //多线程改造，不使用线程池
        //加载解析web.xml方法
        //        loadServlet();
        //        ServerSocket serverSocket = new ServerSocket(port);
        //        System.out.println("minicat开始监听端口：" + port);
        //        while (true) {
        //            Socket socket = serverSocket.accept();
        //            RequestProcessor requestProcessor = new RequestProcessor(socket, servletMap);
        //            requestProcessor.start();
        //
        //        }

        //线程池实现多线程
        loadServlet();

        //线程池参数定义：
        int corePoolSize = 10;
        int maximumPoolSize = 50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>(50);
        ThreadFactory factory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                factory,
                handler
        );

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("minicat开始监听端口：" + port);
        while (true) {
            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket, servletMap);
//            requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);

        }

    }

    private void loadServlet() {
        //类加载器默认路径就是/target/classes
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("web.xml");
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            //先获取所有servlet标签
            List<Element> selectNodes = rootElement.selectNodes("//servlet");
            for (Element element : selectNodes) {
                //拿名称
                Element servletNameElement = (Element) element.selectSingleNode("servlet-name");
                String servletName = servletNameElement.getStringValue();
                //拿对应类名
                Element servletClassElement = (Element) element.selectSingleNode("servlet-class");
                String servletClass = servletClassElement.getStringValue();
                //根据servletName去找servlet-mapping对应的url
                Node servletMapping = rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                //缓存urlPattern和servletClass对应关系
                servletMap.put(urlPattern, (HttpServlet) Class.forName(servletClass).newInstance());
            }

        } catch (DocumentException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
