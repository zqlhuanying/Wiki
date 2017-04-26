<!-- MDTOC maxdepth:6 firsth1:1 numbering:0 flatten:0 bullets:1 updateOnSave:1 -->

   - [Postman 400 Bad Request](#postman-400-bad-request)   
   - [@RequestBody](#requestbody)   
   - [如何多次消费 Request Body 中的数据(具体可见 ValidateFilter)](#如何多次消费-request-body-中的数据具体可见-validatefilter)   
   - [Problem](#problem)   

<!-- /MDTOC -->
## Postman 400 Bad Request
* 主要原因：在 Spring 中使用 Jackson 来解析 Json 数据，当 Json 转换成 Model 时，如果有字段无法解析，会报 HttpMessageNotReadableException 异常，在 Postman 就会表现成 Bad Request

## @RequestBody
1. 在[参数请求转发](参数请求转发.md)中提到参数解析的接口是：HandlerMethodArgumentResolver，对于不同的注解会采用不同的参数解析器进行解析，@RequestBody的参数解析器是：RequestResponseBodyMethodProcessor。
2. @RequestBody 会读取请求中的请求体来消费数据，因为流中的数据被消费一次后，再次读取就读不到内容了。所以 the body can be consumed only once。
3. 但是这并不是表示 @RequestBody 注解在 Controller 中只能使用一次，只不过只有第一个 @RequestBody 才有数据，其他读取的内容均是空（当然也有方法可以实现多次去消费 body 中的数据）。所以如果在进入 Controller 之前，有 Interceptor 或 Filter 提前把 Request Body 中的数据消费了，那么此时 @RequestBody 也是读取不到数据的。
```Java
@Override
protected <T> Object readWithMessageConverters(NativeWebRequest webRequest,
    MethodParameter methodParam,  Type paramType) throws IOException, HttpMediaTypeNotSupportedException {

  // 获取 HttpServletRequest 对象
  final HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

  // HttpServletRequest 是无法直接获取 body 中的数据的，因此封装成 HttpInputMessage
  HttpInputMessage inputMessage = new ServletServerHttpRequest(servletRequest);

  RequestBody annot = methodParam.getParameterAnnotation(RequestBody.class);
  if (!annot.required()) {
    // 获取 body 数据
    InputStream inputStream = inputMessage.getBody();
    if (inputStream == null) {
      return null;
    }
    else if (inputStream.markSupported()) {
      inputStream.mark(1);
      if (inputStream.read() == -1) {
        return null;
      }
      inputStream.reset();
    }
    else {
      final PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
      int b = pushbackInputStream.read();
      if (b == -1) {
        return null;
      }
      else {
        pushbackInputStream.unread(b);
      }
      inputMessage = new ServletServerHttpRequest(servletRequest) {
        @Override
        public InputStream getBody() throws IOException {
          // Form POST should not get here
          return pushbackInputStream;
        }
      };
    }
  }

  return super.readWithMessageConverters(inputMessage, methodParam, paramType);
}
```
父类中的 readWithMessageConverters
```Java
protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage,
    MethodParameter methodParam, Type targetType) throws IOException, HttpMediaTypeNotSupportedException {

  MediaType contentType;
  try {
    // 获取媒体类型 如 application/json
    contentType = inputMessage.getHeaders().getContentType();
  }
  catch (InvalidMediaTypeException ex) {
    throw new HttpMediaTypeNotSupportedException(ex.getMessage());
  }

  if (contentType == null) {
    contentType = MediaType.APPLICATION_OCTET_STREAM;
  }

  Class<?> contextClass = methodParam.getDeclaringClass();
  Map<TypeVariable, Type> map = GenericTypeResolver.getTypeVariableMap(contextClass);
  Class<T> targetClass = (Class<T>) GenericTypeResolver.resolveType(targetType, map);

  // 查找合适的转换器来转换数据
  for (HttpMessageConverter<?> converter : this.messageConverters) {
    if (converter instanceof GenericHttpMessageConverter) {
      GenericHttpMessageConverter genericConverter = (GenericHttpMessageConverter) converter;
      if (genericConverter.canRead(targetType, contextClass, contentType)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Reading [" + targetType + "] as \"" +
          contentType + "\" using [" + converter + "]");
        }
        // Json 数据会进入这里，开始使用 Jackson 去解析 Json 数据
        return genericConverter.read(targetType, contextClass, inputMessage);
      }
    }
    if (targetClass != null) {
      if (converter.canRead(targetClass, contentType)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Reading [" + targetClass.getName() + "] as \"" +
          contentType + "\" using [" + converter + "]");
        }
        // String类型会进入到这里
        return ((HttpMessageConverter<T>) converter).read(targetClass, inputMessage);
      }
    }
  }

  throw new HttpMediaTypeNotSupportedException(contentType, allSupportedMediaTypes);
}
```

## 如何多次消费 Request Body 中的数据(具体可见 ValidateFilter)
1. body 的数据来源于 request.getInputStream，只能消费一次。  
多次消费思路：将 InputStream 的数据保存在变量 body 中，并重写 getInputStream 方法，每次都从变量 body 中恢复流数据。  
2. Using the **HttpServletRequest** object, you can get access to the URL the client used to make the request, the method used (GET, POST, PUT, etc), the query string, and headers.    
3. Getting the **RequestBody** may be a bit trickier and may require using the **HttpServletRequestWrapper** object. Since the request body can only be read once, you'll need to extend the wrapper to access it so that your target controller can still access it later to deserialize your JSON into POJO objects.  
Example  
```Java
public class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, String> customHeaders = new HashMap<>();
    private final byte[] body;

    /**
     * construct a wrapper for this request
     */
    public MyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 读取流数据并保存到 body 变量中
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

    // 重写 getInputStream 方法，每次都从变量 body 中来恢复流数据
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
```
最后在拦截器或过滤器中将这个 HttpServletRequest 继续传递下去，这样每次调用 getInputStream 时就都能获得流数据了。这样就算使用多个 @RequestBody 也都能获得 body 中的数据。
```Java
// requestWrapper 即上面 MyHttpServletRequestWrapper 对象
chain.doFilter(requestWrapper, response); // Goes to default servlet.
```

## Problem
1. 在使用多个 @RequestBody 时，因为每个 model or simple java type 的字段是不同的，但是一次请求会将多个 model 中的字段一并上传（即 union（model1，model2，...）），这样就很容易出现 400 Bad Request。  
**解决方案：**在每个 model 中添加 @JsonIgnoreProperties(ignoreUnknown = true) 注解，这样当某些字段无法解析时，就会被忽略，而不是报错，只需要能获取到当前 model 所需的数据即可。
