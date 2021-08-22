package com.xqq.oss.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class TencentCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        //判断当前系统是Windows/Linux
        return Objects.requireNonNull(conditionContext.getEnvironment().getProperty("oss.type")).contains("tencent");
    }
} 