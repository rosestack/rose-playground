package com.company.todo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggingAspectConfigTest {

    private LoggingAspectConfig.LoggingAspect aspect;

    @BeforeEach
    void setUp() {
        LoggingAspectConfig cfg = new LoggingAspectConfig();
        aspect = cfg.loggingAspect(new ObjectMapper());
    }

    @Test
    void logAfterThrowing_should_not_throw() {
        JoinPoint jp = mock(JoinPoint.class);
        Signature sig = mock(Signature.class);
        when(jp.getSignature()).thenReturn(sig);
        when(sig.getDeclaringTypeName()).thenReturn("com.company.todo.Dummy");
        when(sig.getName()).thenReturn("method");

        aspect.logAfterThrowing(jp, new RuntimeException("x"));
        // no exception expected
    }

    @Test
    void logAround_should_return_result() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getDeclaringTypeName()).thenReturn("com.company.todo.Dummy");
        when(sig.getName()).thenReturn("around");
        when(pjp.getArgs()).thenReturn(new Object[] {"a", 1});
        when(pjp.proceed()).thenReturn("ok");

        Object out = aspect.logAround(pjp);
        assertThat(out).isEqualTo("ok");
    }

    @Test
    void logAround_should_wrap_illegal_argument() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature sig = mock(Signature.class);
        when(pjp.getSignature()).thenReturn(sig);
        when(sig.getDeclaringTypeName()).thenReturn("com.company.todo.Dummy");
        when(sig.getName()).thenReturn("around");
        when(pjp.getArgs()).thenReturn(new Object[] {"a"});
        when(pjp.proceed()).thenThrow(new IllegalArgumentException("bad"));

        assertThatThrownBy(() -> {
                    try {
                        aspect.logAround(pjp);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                })
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
}
