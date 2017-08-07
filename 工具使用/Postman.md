## 注意点：
  * Postman中对于无 Value 的Key默认是不传的，所以可以对这类的Key加个空格，强制Postman将这个参数传递到服务器. eg.  
  Get las.secoo.com/api/comment/show_product_comment?pageSize=10&productBrandId=29&type=0&page=1&filter=0&productCategoryId=40&productId=18377538
  ```
  User-Agent:android
  app-ver:5.8.0
  //sysver:4.4.2
  //imei:868131010842395
  //platform-type:1
  device-id:868131010842395_84%3AEF%3A18%3ADA%3ACE%3AAB
  //device_id:868131010842395_84%3AEF%3A18%3ADA%3ACE%3AAB
  screen-height:1280
  //mac:84%3AEF%3A18%3ADA%3ACE%3AAB
  //app-id:644873678
  channel:baiduzhushou
  screen-width:720
  upk:
  ```
  当时由于upk没有对应的值，导致Postman没有将这个参数传递到服务器端，致使服务器报错.(导致抓包时，该接口是有数据返回的，但是在Postman中却报500错误)
