package edu.goorm.userservice.global.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goorm.userservice.global.util.CustomIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CustomLogger {

    private static final Logger infoLogger = LogManager.getLogger("infoLogger");
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");
    private static final Logger externalLogger = LogManager.getLogger("externalLogger");

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void logRequest(
        String logType,
        String url,
        String method,
        String payload,
        HttpServletRequest request,
        int status,
        long durationMs
    ) {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("timestamp", LocalDateTime.now().toString());
            logMap.put("level", "INFO");
            logMap.put("logType", logType);
            logMap.put("traceId", MDC.get("traceId"));
            logMap.put("service", "user-service");
            logMap.put("controller", "UserController");
            logMap.put("method", method);
            logMap.put("url", url);
            logMap.put("status", status);
            logMap.put("durationMs", durationMs);
            logMap.put("userId", MDC.get("userId") != null ? MDC.get("userId") : "-");
            logMap.put("ip", CustomIpUtil.getClientIp(request));
            logMap.put("userAgent", request.getHeader("User-Agent"));

            infoLogger.info(mapper.writeValueAsString(logMap));
        } catch (Exception e) {
            errorLogger.error("Failed to log request", e);
        }
    }

    public static void logError(
        String url,
        String method,
        Exception ex,
        int status
    ) {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("timestamp", LocalDateTime.now().toString());
            logMap.put("level", "ERROR");
            logMap.put("logType", "EXCEPTION");
            logMap.put("traceId", MDC.get("traceId"));
            logMap.put("service", "user-service");
            logMap.put("url", url);
            logMap.put("method", method);
            logMap.put("status", status);
            logMap.put("userId", MDC.get("userId") != null ? MDC.get("userId") : "-");

            Map<String, Object> exceptionMap = new HashMap<>();
            exceptionMap.put("type", ex.getClass().getSimpleName());
            exceptionMap.put("message", ex.getMessage());
            StackTraceElement[] stack = ex.getStackTrace();
            String[] trace = new String[Math.min(stack.length, 5)];
            for (int i = 0; i < trace.length; i++) {
                trace[i] = stack[i].toString();
            }
            exceptionMap.put("stackTrace", trace);

            logMap.put("exception", exceptionMap);

            errorLogger.error(mapper.writeValueAsString(logMap));
        } catch (Exception e) {
            errorLogger.error("Failed to log error", e);
        }
    }

    public static void logExternalKafkaSend(
        String target,
        String key,
        String status
    ) {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("timestamp", LocalDateTime.now().toString());
            logMap.put("level", "INFO");
            logMap.put("logType", "EXTERNAL_KAFKA_SEND");
            logMap.put("traceId", MDC.get("traceId"));
            logMap.put("system", "Kafka");
            logMap.put("target", target);
            logMap.put("operation", "send");
            logMap.put("key", key);
            logMap.put("status", status);

            externalLogger.info(mapper.writeValueAsString(logMap));
        } catch (Exception e) {
            errorLogger.error("Failed to log external Kafka send", e);
        }
    }
}
