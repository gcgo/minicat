package com.gcgo.server;

import com.gcgo.server.mapper.Context;
import com.gcgo.server.mapper.Host;
import com.gcgo.server.mapper.Mapper;
import com.gcgo.server.mapper.Wrapper;
import com.gcgo.utils.RequestProcessor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Bootstrap {
    //定义监听端口号
    //    private int port = 8080;
    private int port;
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

    private Mapper mapper = new Mapper();
    private String appBase = null;

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
            //            RequestProcessor requestProcessor = new RequestProcessor(socket, servletMap);
            RequestProcessor requestProcessor = new RequestProcessor(socket, mapper);
            //            requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);

        }

    }

    private void loadServlet() {
       /* //类加载器默认路径就是/target/classes
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
        }*/

        //类加载器默认路径就是/target/classes
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            //拿services标签
            List<Element> serviceElementList = rootElement.selectNodes("//services");
            for (Element serviceElement : serviceElementList) {
                //拿controller标签
                Element connector = (Element) serviceElement.selectSingleNode("connector");
                //获取端口号
                String portStr = connector.attributeValue("port");
                port = Integer.parseInt(portStr);
                //获取engine标签
                Element engine = (Element) serviceElement.selectSingleNode("engine");
                //拿host标签
                Element hostElement = (Element) engine.selectSingleNode("host");
                //获取hostName和AppBase目录位置
                String hostName = hostElement.attributeValue("name");
                appBase = hostElement.attributeValue("appBase");
                //开始设置mapper
                Host host = new Host();
                host.setHostName(hostName);
                mapper.setHost(host);
                //循环扫描webapps文件夹下所有应用
                File appsDir = new File(appBase);
                File[] files = appsDir.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {//即应用文件夹，如demo1
                        List<Context> contextList = host.getContextList();
                        Context context = new Context();
                        context.setAppName(file.getName());
                        contextList.add(context);
                        //继续深入这个应用，即context
                        //扫描web.xml
                        scanWebXml(file, context);
                    }
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void scanWebXml(File file, Context context) {
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.getName().equals("web.xml")) {
                //开始解析
                SAXReader reader = new SAXReader();
                try {
                    Document document = reader.read(new FileInputStream(file1));
                    Element rootElement = document.getRootElement();
                    //先获取所有servlet标签
                    List<Element> selectNodes = rootElement.selectNodes("//servlet");
                    Wrapper wrapper = new Wrapper();
                    Map<String, HttpServlet> servletMap = wrapper.getServletMap();
                    //调用URLClassLoader类加载器加载应用中的类
                    String classPath = appBase + "/" + context.getAppName() + "/server";
                    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{
                            new File(classPath).toURI().toURL()
                    });
                    for (Element element : selectNodes) {
                        //拿名称
                        Element servletNameElement = (Element) element.selectSingleNode("servlet-name");
                        String servletName = servletNameElement.getStringValue();
                        //拿对应类名
                        Element servletClassElement = (Element) element.selectSingleNode("servlet-class");
                        String servletClassName = servletClassElement.getStringValue();
                        //根据servletName去找servlet-mapping对应的url
                        Node servletMapping = rootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                        String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                        //缓存urlPattern和servletClass对应关系，v3.0版本用，v4.0不用
                        //                        servletMap.put(urlPattern, (HttpServlet) Class.forName(servletClass).newInstance());
                        String[] split = urlPattern.split("/");//只存最后Servlet请求名即可
                        Class<?> aClass = urlClassLoader.loadClass(servletClassName);
                        servletMap.put(split[split.length - 1], (HttpServlet) aClass.newInstance());
                    }
                    //添加wrapper进context
                    context.getWrapperList().add(wrapper);
                    urlClassLoader.close();

                } catch (DocumentException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}











































