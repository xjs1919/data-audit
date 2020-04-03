# 微信扫一扫关注公众号：爪哇优太儿
![扫一扫加关注](https://img-blog.csdnimg.cn/20190524100820287.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dvbGRlbmZpc2gxOTE5,size_16,color_FFFFFF,t_7)

# data-audit
数据库操作审计，记录数据库数据的变化

# 为什么要存在
如果仅仅是解析mysql的binlog，是无法获取用户和请求信息，仅仅能获取数据库数据的变化，这对于审计来说是远远不够的。

# 内部原理
通过interceptor拦截到请求，把请求信息写入到ThreadLocal，通过myabtis-plugin拦截要执行的sql，记录数据的差量到ThreadLocal，请求结束以后，把ThreadLocal里面的数据推送到kafka。

# 接入方法
## 1.添加依赖
手动下载源码，mvn clean install -DskipTests到本都maven仓库
```xml
<dependency>
  <groupId>com.github.xjs</groupId>
  <artifactId>data-audit-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```
## 2.在主类上添加@EnableDataAudit注解 
## 3.在要审计的controller方法上添加@DataAudit注解
## 4.添加sdk所需的配置类
注入IAuditUserService、IAuditDataSourceService、IAuditCacheService
## 5.添加配置项
```xml
audit:
  #app key
  appKey: 111111
  appSecret: abcdef
  kafka:
    # kafka的broker
    bootstrap-servers: "192.168.1.26:9092"
    # 如果发送kafka失败，把数据写到这个日志文件中
    fallbackFileName: /tmp/data_audit.log
```
如何接入可以参考demo的实现

# 对性能的影响
- 如果是insert，几乎没有影响，因为插入的数据就是变化的数据。
- 如果是update，需要在update之前做一次select，这样才能知道数据的变化。
- 如果是delete，需要在delete之前先做一次select，这样才能知道删除之前的数据。
- 增删改接口混合做压测，性能损耗在5%左右。
