package io.github.rosestack.spring.boot.security.jwt.loader;

/**
 * 可缓存的密钥加载器接口
 *
 * <p>用于支持缓存刷新的密钥加载器，如：
 * <ul>
 *   <li>KeystoreKeyLoader - 支持密钥库缓存刷新</li>
 *   <li>JwksKeyLoader - 支持JWKS缓存刷新</li>
 * </ul>
 * </p>
 */
public interface CacheableKeyLoader extends KeyLoader {

    /**
     * 刷新缓存
     * 强制重新加载密钥，清除旧缓存
     *
     * @throws Exception 刷新失败时抛出异常
     */
    void refreshCache() throws Exception;

    /**
     * 获取缓存状态信息
     *
     * @return 缓存状态描述
     */
    default String getCacheStatus() {
        return "缓存状态未知";
    }
}
