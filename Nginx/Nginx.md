# Nginx 配置反向代理后怎么获得客户端真实的IP
nginx配置反向代理后，在应用中取得的ip都是反向代理服务器的ip，取得的域名也是反向代理配置的url的域名，解决该问题，需要在nginx反向代理配置中添加一些配置信息，目的将客户端的真实ip和域名传递到应用程序中。  
nginx反向代理配置时，一般会添加下面的配置
```
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header REMOTE-HOST $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
```
取得真实IP地址
```
public static String getClientIp(HttpServletRequest request) {
    String ips = request.getHeader("x-forwarded-for");
    if (ips == null || ips.length() == 0 || "unknown".equalsIgnoreCase(ips)) {
        ips = request.getHeader("Proxy-Client-IP");
    }
    if (ips == null || ips.length() == 0 || "unknown".equalsIgnoreCase(ips)) {
        ips = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ips == null || ips.length() == 0 || "unknown".equalsIgnoreCase(ips)) {
        ips = request.getRemoteAddr();
    }

    String[] ipArray = ips.split(",");
    String clientIp = null;
    for (String ip : ipArray) {
        if (!("unknown".equalsIgnoreCase(ip))) {
            clientIp = ip;
            break;
        }
    }
    return clientIp;
}
```
