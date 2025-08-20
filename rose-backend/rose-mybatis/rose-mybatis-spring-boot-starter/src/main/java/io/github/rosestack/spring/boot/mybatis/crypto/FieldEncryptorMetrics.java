package io.github.rosestack.spring.boot.mybatis.crypto;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 字段加解密指标埋点。
 */
public class FieldEncryptorMetrics {
    private final Timer encryptTimer;
    private final Timer decryptTimer;
    private final Counter encryptErrorCounter;
    private final Counter decryptErrorCounter;

    public FieldEncryptorMetrics(MeterRegistry registry) {
        this.encryptTimer = Timer.builder("rose.mybatis.crypto.encrypt.duration")
                .description("Duration of encrypting fields")
                .register(registry);
        this.decryptTimer = Timer.builder("rose.mybatis.crypto.decrypt.duration")
                .description("Duration of decrypting fields")
                .register(registry);
        this.encryptErrorCounter = Counter.builder("rose.mybatis.crypto.encrypt.errors")
                .description("Field crypto error count")
                .register(registry);
        this.decryptErrorCounter = Counter.builder("rose.mybatis.crypto.decrypt.errors")
                .description("Field crypto error count")
                .register(registry);
    }

    public Timer.Sample startEncrypt() {
        return Timer.start();
    }

    public void recordEncrypt(Timer.Sample sample) {
        if (sample != null) sample.stop(encryptTimer);
    }

    public Timer.Sample startDecrypt() {
        return Timer.start();
    }

    public void recordDecrypt(Timer.Sample sample) {
        if (sample != null) sample.stop(decryptTimer);
    }

    public void incrementEncryptError() {
        encryptErrorCounter.increment();
    }

    public void incrementDecryptError() {
        decryptErrorCounter.increment();
    }
}
