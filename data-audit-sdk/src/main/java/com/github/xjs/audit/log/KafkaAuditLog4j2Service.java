/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.log;

import com.alibaba.fastjson.JSON;
import com.github.xjs.audit.config.DataAuditProperties;
import com.github.xjs.audit.threadlocal.ThreadPoolUtil;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 日志输出到kafka同时输出到本地文件
 *
 * @author 605162215@qq.com
 * @date 2019/12/9 14:11
 **/
public class KafkaAuditLog4j2Service implements IAuditLogService {

    /**
     * LoggerContext
     * */
    private static final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

    /**
     * Logger Configuration
     * */
    private static final Configuration config = ctx.getConfiguration();

    /**
     * logger的名字
     * */
    public static final String LOGGER_NAME = "kafka-file-logger";


    /**
     * 输出log
     * */
    private Logger kafkaLogger;

    /**
     * 配置信息
     * */
    private DataAuditProperties properties;


    public KafkaAuditLog4j2Service(DataAuditProperties properties){
        this.properties = properties;
    }

    public void send(Object data){
        if(data == null){
            return;
        }
        String msg = JSON.toJSONString(data);
        final Logger logger = getLogger();
        //log4j-kafka只能发同步消息
        ThreadPoolUtil.execute(()->{
            logger.info(msg);
        });
    }

    private Logger getLogger(){
        Logger logger = this.kafkaLogger;
        if(logger == null){
            synchronized (KafkaAuditLog4j2Service.class){
                if(logger == null){
                    logger = createLogger();
                    this.kafkaLogger = logger;
                }
            }
        }
        return logger;
    }

    private Logger createLogger(){
        Appender kafkaAppender = createKafkaAppender();
        Appender fileAppender = createRollingFileAppender();
        config.addAppender(kafkaAppender);
        config.addAppender(fileAppender);
        AppenderRef kafkaRef = AppenderRef.createAppenderRef(KafkaAuditLogbackService.KAFKA_APPENDER_NAME, Level.INFO, null);
        AppenderRef fileRef = AppenderRef.createAppenderRef(KafkaAuditLogbackService.FILE_APPENDER_NAME, Level.INFO, null);
        AppenderRef[] refs = new AppenderRef[]{kafkaRef, fileRef};
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, LOGGER_NAME, "true", refs, null, config, null);
        loggerConfig.addAppender(kafkaAppender, Level.INFO, null);
        loggerConfig.addAppender(fileAppender, Level.INFO, null);
        config.addLogger(LOGGER_NAME, loggerConfig);
        ctx.updateLoggers();
        return LogManager.getLogger(LOGGER_NAME);
    }

    private Appender createKafkaAppender(){
        List<Property> list = new ArrayList<Property>();
        list.add(Property.createProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG ,properties.getKafka().getBootstrapServers()));
        list.add(Property.createProperty(ProducerConfig.ACKS_CONFIG , ""+KafkaAuditLogbackService.ACK));
        list.add(Property.createProperty(ProducerConfig.RETRIES_CONFIG , ""+KafkaAuditLogbackService.RETRY));
        list.add(Property.createProperty(ProducerConfig.LINGER_MS_CONFIG , ""+KafkaAuditLogbackService.LINGER));
        list.add(Property.createProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG , ""+KafkaAuditLogbackService.BLOCK));
        list.add(Property.createProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG ,""+KafkaAuditLogbackService.REQUEST_TIMEOUT));
        Property[] props = list.toArray(new Property[list .size()]);
        Layout<String> layout = PatternLayout.newBuilder().withConfiguration(config).withPattern("%msg").build();
        Appender appender = KafkaAppender.createAppender(layout, null, KafkaAuditLogbackService.KAFKA_APPENDER_NAME ,true , KafkaAuditLogbackService.KAFKA_TOPIC_NAME , props , config,null);
        appender.start();
        return appender;
    }

    private Appender createRollingFileAppender(){
        File logFile = LogFileUtil.getLogFile(properties);
        String logFileName = logFile.getName();
        int dot = logFileName.lastIndexOf(".");
        String prefix = dot>0?logFileName.substring(0, dot):logFileName;
        String suffix = dot>0?logFileName.substring(dot+1):null;
        //7个滚动日志文件
        DefaultRolloverStrategy strategy = DefaultRolloverStrategy.newBuilder().withMax("4").withMin("1").withConfig(config).build();
        Layout layout = PatternLayout.newBuilder().withConfiguration(config).withPattern("%msg%n").build();
        RollingFileAppender appender = RollingFileAppender.createAppender(
                logFile.getAbsolutePath(),
                logFile.getParentFile().getAbsolutePath()+ File.pathSeparator + prefix + ".%d{yyyy-MM-dd}.%i" + (suffix==null?"":"."+suffix),
                "true",
                KafkaAuditLogbackService.FILE_APPENDER_NAME,
                "true","8192", "true",
                SizeBasedTriggeringPolicy.createPolicy("256M"),
                strategy, layout, null, "true", "false", null, config);
        appender.start();
        return appender;
    }
}
