package io.github.rose.core.lang.functional;

import io.github.rose.core.lang.functional.core.Try;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Try 类的测试
 * 
 * @author rose
 */
public class TryTest {
    
    @Test
    public void testSuccess() {
        Try<String> success = Try.success("hello");
        assertTrue(success.isSuccess());
        assertFalse(success.isFailure());
        assertEquals("hello", success.get());
        assertEquals("hello", success.getOrElse("default"));
    }
    
    @Test
    public void testFailure() {
        RuntimeException error = new RuntimeException("test error");
        Try<String> failure = Try.failure(error);
        assertFalse(failure.isSuccess());
        assertTrue(failure.isFailure());
        assertEquals(error, failure.getError());
        assertEquals("default", failure.getOrElse("default"));
    }
    
    @Test
    public void testOfWithSuccess() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"));
        assertTrue(result.isSuccess());
        assertEquals(123, result.get());
    }
    
    @Test
    public void testOfWithFailure() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("abc"));
        assertTrue(result.isFailure());
        assertTrue(result.getError() instanceof NumberFormatException);
    }
    
    @Test
    public void testOfCheckedWithSuccess() {
        Try<Integer> result = Try.ofChecked(() -> Integer.parseInt("123"));
        assertTrue(result.isSuccess());
        assertEquals(123, result.get());
    }
    
    @Test
    public void testOfCheckedWithFailure() {
        Try<Integer> result = Try.ofChecked(() -> Integer.parseInt("abc"));
        assertTrue(result.isFailure());
        assertTrue(result.getError() instanceof NumberFormatException);
    }
    
    @Test
    public void testMap() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
                                .map(value -> value * 2);
        assertTrue(result.isSuccess());
        assertEquals(246, result.get());
    }
    
    @Test
    public void testMapChecked() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
                                .mapChecked(value -> Integer.parseInt(String.valueOf(value) + "0"));
        assertTrue(result.isSuccess());
        assertEquals(1230, result.get());
    }
    
    @Test
    public void testFlatMap() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
                                .flatMap(value -> Try.of(() -> value * 2));
        assertTrue(result.isSuccess());
        assertEquals(246, result.get());
    }
    
    @Test
    public void testRecover() {
        Try<Integer> result = Try.of(() -> Integer.parseInt("abc"))
                                .recover(error -> 0);
        assertTrue(result.isSuccess());
        assertEquals(0, result.get());
    }
    
    @Test
    public void testToOptional() {
        Try<Integer> success = Try.success(123);
        assertTrue(success.toOptional().isPresent());
        assertEquals(123, success.toOptional().get());
        
        Try<Integer> failure = Try.failure(new RuntimeException("error"));
        assertFalse(failure.toOptional().isPresent());
    }
}