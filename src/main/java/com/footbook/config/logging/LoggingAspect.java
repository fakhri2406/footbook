package com.footbook.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Before("execution(* com.footbook.controller..*(..)) || execution(* com.footbook.service..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering: {} with args {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.footbook.controller..*(..)) || execution(* com.footbook.service..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        log.info("Exiting: {} with result {}", joinPoint.getSignature(), result);
    }
}
