package com.github.xjs.audit.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class LogUtil {
    public static void debug(Logger logger, String msg, Object ... args){
        if(logger.isDebugEnabled()){
            logger.debug(msg, args);
        }
    }
}
