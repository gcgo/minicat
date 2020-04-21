# Tomcat大作业：

## 1编程题说明：

1. 运行Bootstrap.main() 启动minicat

2. 配置文件在src/main/resources/server.xml，其中appBase配置为绝对路径：

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <server>
       <services>
           <connector port="8080" />
           <engine>
               <host name="localhost" appBase="D:\workspace\minicat\src\main\resources\webapps" />
           </engine>
       </services>
   </server>
   ```

   下载运行请将appBase地址修改为项目中webapps文件夹的绝对路径即可。

3. 请求地址为：对应webapps中的demo1和demo2项目

   - localhost:8080/demo1/demo1test
   - localhost:8080/demo2/demo2test

4. 运行效果及说明详见验证资料中的视频说明

## 2简答题：

地址：

