```Java

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author jiqi
 * @since 2016/9/18
 */
@Component("validateFilter")
public class ValidateFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(ValidateFilter.class);

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String SIGN_KEY = "sglyqfh,sskmyyq.";
    private static final String LOGIN_PATH = "/user/login";
    private static final String UID = "uid";

    @SuppressWarnings("all")
    @Autowired
    private UserService userExportService;

    private String[] passURIs = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String passURIsString = filterConfig.getInitParameter("passURIs");
        if (!StringUtils.isBlank(passURIsString)) {
            passURIs = passURIsString.split(",");
        }
    }

    private boolean isPassURI(String uri) {
        if (this.passURIs != null && this.passURIs.length > 0) {
            for (String passURI : passURIs) {
                if (uri.startsWith(passURI)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {

            // 1.h5页面不需要验证
            HttpServletRequest req = (HttpServletRequest) request;

            String requestURI = req.getRequestURI();
            if (isPassURI(requestURI)) {
                chain.doFilter(request, response); // Goes to default servlet.
                return;
            }

            // 2.获取输入数据（json数据或者表单数据）
            MyHttpServletRequestWrapper requestWrapper = new MyHttpServletRequestWrapper(req);
            String parameterInput = "";
            boolean isGetMethod = false;
            if ("GET".equalsIgnoreCase(req.getMethod())) { // get请求
                if(null != req.getQueryString()) {
                    parameterInput = URLDecoder.decode(req.getQueryString(), DEFAULT_CHARSET);
                }
                isGetMethod = true;
            } else {  // post请求
                try {
                    parameterInput = requestWrapper.getBody();
                    //InputStream inputStream = request.getInputStream();
                    //parameterInput = IOUtils.toString(inputStream, DEFAULT_CHARSET);
                } catch (Exception e) {
                    response.getWriter().write(JSON.toJSONString(
                            new BaseResponse<>(new MapiException(MapiErrorCode.REQUEST_PARMA_FAILED))));
                    return;
                }
            }

            // 3.md5校验
            String userToken = req.getHeader("userToken");
            String sign = req.getHeader("security");
            String mySign;
            if (userToken == null) {
                mySign = MD5Encrypt.md5(parameterInput + SIGN_KEY);
            } else {
                mySign = MD5Encrypt.md5(parameterInput + userToken + SIGN_KEY);
            }

            if (!mySign.equals(sign)) {
                logger.warn("validateSign failed, sign:" + sign + ",mysign:" + mySign);
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.addHeader("sign", mySign);
                response.getWriter().write(JSON.toJSONString(
                        new BaseResponse<>(new MapiException(MapiErrorCode.SIGN_FAILED.getCode(), "sign验证不通过"))));
                return;
            }

            // 4.user/login不校验token
            if (!requestURI.equals(LOGIN_PATH)) {

                if (StringUtils.isEmpty(userToken)) {
                    if(!isGetMethod) { // 兼容post方式
                        JSONObject jsonObject = JSON.parseObject(parameterInput);
                        userToken = jsonObject.getString("token");
                    }
                    if (StringUtils.isEmpty(userToken)) {
                        response.getWriter().write(JSON.toJSONString(
                                new BaseResponse<>(MapiErrorCode.TOKEN_VALIDATE_FAILED)));
                        return;
                    }
                }

                TokenTo tokenTo = userExportService.tokenValidate(userToken).getResponseVo();
                if (tokenTo == null) {
                    response.getWriter().write(JSON.toJSONString(
                            new BaseResponse<>(MapiErrorCode.TOKEN_VALIDATE_FAILED)));
                    return;
                }

                requestWrapper.addHeader(UID, String.valueOf(tokenTo.getUid()));
                requestWrapper.addHeader("isAnonymous", String.valueOf(tokenTo.isAnonymous()));
            }
            logger.info("Token:"+userToken);

            // 实现重复消费 body
            chain.doFilter(requestWrapper, response); // Goes to default servlet.

        } catch (Exception e) {
            logger.error(String.format("validateFilter 出现异常"), e);
        }

    }

    @Override
    public void destroy() {
    }

    // 实现重复消费 body
    public class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String> customHeaders = new HashMap<>();
        private final byte[] body;

        /**
         * construct a wrapper for this request
         */
        public MyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            InputStream inputStream = request.getInputStream();
            String inputString = IOUtils.toString(inputStream, DEFAULT_CHARSET);
            body = inputString.getBytes(DEFAULT_CHARSET);
        }

        /**
         * add a header with given name and value
         */
        public void addHeader(String name, String value) {
            customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            if (customHeaders.containsKey(name)) {
                return customHeaders.get(name);
            }
            return super.getHeader(name);
        }

        /**
         * get the Header names
         */
        @Override
        public Enumeration<String> getHeaderNames() {
            // create a set of the custom header names
            Set<String> set = new HashSet<>(customHeaders.keySet());

            // now add the headers from the wrapped request object
            @SuppressWarnings("unchecked")
            Enumeration<String> e = super.getHeaderNames();
            while (e.hasMoreElements()) {
                // add the names of the request headers into the list
                String n = e.nextElement();
                set.add(n);
            }

            // create an enumeration from the set and return
            return Collections.enumeration(set);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = Collections.list(super.getHeaders(name));
            if (customHeaders.containsKey(name)) {
                values.add(customHeaders.get(name));
            }
            return Collections.enumeration(values);
        }

        public String getBody() throws Exception {
            return new String(body, DEFAULT_CHARSET);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {

            final ByteArrayInputStream bais = new ByteArrayInputStream(body);

            return new ServletInputStream() {

                @Override
                public int read() throws IOException {
                    return bais.read();
                }

                @Override
                public int available() {
                    return bais.available();
                }

                public int read(byte[] buf, int off, int len) {
                    return bais.read(buf, off, len);
                }
            };
        }
    }
}
```
