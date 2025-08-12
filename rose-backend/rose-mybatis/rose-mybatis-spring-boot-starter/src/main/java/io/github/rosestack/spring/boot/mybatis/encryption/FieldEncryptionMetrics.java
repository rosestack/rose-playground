package io.github.rosestack.spring.boot.mybatis.encryption;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 字段加解密指标埋点。
 */
public class FieldEncryptionMetrics {
    private final Timer encryptTimer;
    private final Timer decryptTimer;
    private final Counter encryptErrorCounter;
    private final Counter decryptErrorCounter;

    public FieldEncryptionMetrics(MeterRegistry registry) {
        this.encryptTimer = Timer.builder("rose.mybatis.encryption.encrypt.duration")
                .description("Duration of encrypting fields")
                .register(registry);
        this.decryptTimer = Timer.builder("rose.mybatis.encryption.decrypt.duration")
                .description("Duration of decrypting fields")
                .register(registry);
        this.encryptErrorCounter = Counter.builder("rose.mybatis.encryption.encrypt.errors")
                .description("Field encryption error count")
                .register(registry);
        this.decryptErrorCounter = Counter.builder("rose.mybatis.encryption.decrypt.errors")
                .description("Field decryption error count")
                .register(registry);
    }

    public Timer.Sample startEncrypt() { return Timer.start(); }
    public void recordEncrypt(Timer.Sample sample) { if (sample != null) sample.stop(encryptTimer); }

    public Timer.Sample startDecrypt() { return Timer.start(); }
    public void recordDecrypt(Timer.Sample sample) { if (sample != null) sample.stop(decryptTimer); }

    public void incrementEncryptError() { encryptErrorCounter.increment(); }
    public void incrementDecryptError() { decryptErrorCounter.increment(); }
}


