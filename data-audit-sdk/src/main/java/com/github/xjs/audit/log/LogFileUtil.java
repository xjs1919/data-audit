package com.github.xjs.audit.log;

import com.github.xjs.audit.config.DataAuditProperties;
import com.github.xjs.audit.util.StringUtil;

import java.io.File;

public class LogFileUtil {

    public static File getLogFile(DataAuditProperties properties){
        File file = null;
        String fullFileName = properties.getKafka().getFallbackFileName();
        if(StringUtil.isEmpty(fullFileName)){
            file = new File(System.getProperty("java.io.tmpdir"), "action_audit.log");
        }else{
            file = new File(fullFileName);
        }
        File dir = file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        return file;
    }
}
