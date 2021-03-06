package com.mzz.esdemo.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The type Log aspect.
 *
 * @author Zero
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    private static final int MAX_LENGTH = 400;

    @Pointcut("execution(* com.mzz.esdemo.service.*.*(..))" +
            "|| execution(* com.mzz.esdemo.controller.*.*(..))")
    public void pointcutConfig() {
    }

    @Around(value = "pointcutConfig()")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        StopWatch stopWatch = StopWatch.createStarted();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.toShortString();
        Object result;
        try {
            result = joinPoint.proceed(args);
        } catch (Throwable e) {
            log.error("[ERROR_LOGGER]:: method={}; inArgs={}; exception={}", methodName, joinArgs(args), e);
            throw e;
        }

        if (log.isInfoEnabled()) {
            log.info("[METHOD_LOGGER]:: method={}; elapsedTime={}ms; inArgs={}; result={}", methodName,
                    stopWatch.getTime(TimeUnit.MILLISECONDS), joinArgs(args), substring(result));
        }
        return result;
    }

    private static String substring(Object obj) {
        String str = String.valueOf(obj);
        return str.length() > MAX_LENGTH ? StringUtils.substring(str, 0, MAX_LENGTH) : str;
    }

    private String joinArgs(Object[] args) {
        if (args == null) {
            return "";
        }
        return Arrays.stream(args)
                .map(LogAspect::substring)
                .collect(Collectors.joining(", ", "(", ")"));
    }

}
