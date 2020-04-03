/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import com.alibaba.fastjson.JSON;
import com.github.danielwegener.logback.kafka.KafkaAppender;
import com.github.danielwegener.logback.kafka.delivery.AsynchronousDeliveryStrategy;
import com.github.danielwegener.logback.kafka.keying.NoKeyKeyingStrategy;
import com.github.xjs.audit.config.DataAuditProperties;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 日志输出到kafka
 *
 * @author 605162215@qq.com
 * @date 2019/12/9 14:11
 **/
public class KafkaAuditLogbackService implements IAuditLogService {
    /**
     * kafka appender的名字
     * */
    public static final String KAFKA_APPENDER_NAME = "ActionAuditKafkaAppender";
    /**
     * rolling file appender的名字
     * */
    public static final String FILE_APPENDER_NAME = "ActionAuditFileAppender";
    /**
     * topic的名字
     * */
    public static final String KAFKA_TOPIC_NAME = "topic_audit";
    /**
     * 不需要等待broker确认
     * */
    public static final int ACK = 0;
    /**
     * 不重试
     */
    public static final int RETRY = 0;

    /**
     * 累计1秒再批量发送
     * */
    public static final int LINGER = 1000;

    /**
     * 最多阻塞1秒
     * */
    public static final int BLOCK = 1000;
    /**
     * 请求超时
     * */
    public static final int REQUEST_TIMEOUT = 10000;



    /**
     * 输出log
     * */
    private Logger kafkaLogger;

    /**
     * 配置信息
     * */
    private DataAuditProperties properties;


    public KafkaAuditLogbackService(DataAuditProperties properties){
        this.properties = properties;
    }

    public void send(Object data){
        if(data == null){
            return;
        }
        String msg = JSON.toJSONString(data);
        Logger logger = getLogger();
        logger.info(msg);
    }

    private Logger getLogger(){
        Logger logger = this.kafkaLogger;
        if(logger == null){
            synchronized (KafkaAuditLogbackService.class){
                if(logger == null){
                    logger = createKafkaLogger();
                    this.kafkaLogger = logger;
                }
            }
        }
        return logger;
    }

    private Logger createKafkaLogger(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        KafkaAppender kafkaAppender = getKafkaAppender(context, KAFKA_APPENDER_NAME, FILE_APPENDER_NAME);
        kafkaAppender.start();
        Logger logger = context.getLogger(KAFKA_APPENDER_NAME);
        logger.setAdditive(false);
        logger.addAppender(kafkaAppender);
        return logger;
    }

    private KafkaAppender getKafkaAppender(LoggerContext context, String kafkaName, String fileAppenderName){
        KafkaAppender kafkaAppender = new KafkaAppender();
        kafkaAppender.setTopic(KAFKA_TOPIC_NAME);
        kafkaAppender.setKeyingStrategy(new NoKeyKeyingStrategy());
        kafkaAppender.setDeliveryStrategy(new AsynchronousDeliveryStrategy());
        kafkaAppender.addProducerConfigValue("bootstrap.servers", properties.getKafka().getBootstrapServers());
        //不需要等待broker确认
        kafkaAppender.addProducerConfigValue("acks", ACK);
        //不重试
        kafkaAppender.addProducerConfigValue("retries", RETRY);
        //累计1秒再批量发送
        kafkaAppender.addProducerConfigValue("linger.ms", LINGER);
        //最多阻塞1秒
        kafkaAppender.addProducerConfigValue("max.block.ms", BLOCK);
        //请求超时
        kafkaAppender.addProducerConfigValue("request.timeout.ms", REQUEST_TIMEOUT);
        kafkaAppender.setContext(context);
        kafkaAppender.setName(kafkaName);
        //设置级别
        setLogLevel(kafkaAppender, Level.INFO);
        //设置格式
        setEncoder(context, kafkaAppender, "%m");
        //设置fallback
        RollingFileAppender fileAppender = getFileAppender(context, fileAppenderName);
        fileAppender.start();
        kafkaAppender.addAppender(fileAppender);
        return kafkaAppender;
    }

    private RollingFileAppender getFileAppender(LoggerContext context, String appenderName){
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setContext(context);
        fileAppender.setName(appenderName);
        //设置级别
        setLogLevel(fileAppender, Level.INFO);
        //设置格式
        setEncoder(context, fileAppender, "%m%n");
        //设置文件
        File logFile = LogFileUtil.getLogFile(properties);
        fileAppender.setFile(OptionHelper.substVars(logFile.getAbsolutePath(), context));
        //设置文件创建时间及大小的类
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        //文件名格式
        File rollingFile = new File(logFile.getParentFile(), getRollingFileName(logFile.getName()));
        String fp = OptionHelper.substVars(rollingFile.getAbsolutePath(),context);
        //最大日志文件大小
        policy.setMaxFileSize(FileSize.valueOf("256MB"));
        //设置文件名模式
        policy.setFileNamePattern(fp);
        //设置最大历史记录为10条
        policy.setMaxHistory(4);
        //总大小限制
        policy.setTotalSizeCap(FileSize.valueOf("1GB"));
        //设置父节点是appender
        policy.setParent(fileAppender);
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        policy.setContext(context);
        policy.start();
        fileAppender.setRollingPolicy(policy);
        return fileAppender;
    }

    private void setLogLevel(Appender appender, Level level){
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(level);
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();
        appender.addFilter(levelFilter);
    }

    private void setEncoder(LoggerContext context, Appender appender, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        encoder.setPattern(pattern);
        encoder.start();
        if (appender instanceof ConsoleAppender) {
            ((ConsoleAppender) appender).setEncoder(encoder);
        } else if (appender instanceof KafkaAppender) {
            ((KafkaAppender) appender).setEncoder(encoder);
        }else if (appender instanceof RollingFileAppender) {
            ((RollingFileAppender) appender).setEncoder(encoder);
        }
    }

    private String getRollingFileName(String logFileName){
        int dot = logFileName.indexOf(".");
        if(dot > 0){
            String pre = logFileName.substring(0, dot);
            String extension = logFileName.substring(dot+1);
            return pre + "_%d{yyyy-MM-dd}.%i." + extension;
        }else{
            return logFileName + "_%d{yyyy-MM-dd}.%i";
        }
    }
}
