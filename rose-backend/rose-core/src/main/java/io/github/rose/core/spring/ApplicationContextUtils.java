package io.github.rose.core.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SpringVersion;

import java.util.List;

import static io.github.rose.core.util.BeanFactoryUtils.getBeanPostProcessors;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class ApplicationContextUtils {
    private static final Logger log = LoggerFactory.getLogger(ApplicationContextUtils.class);

    public static final String APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME = "org.springframework.context.support.ApplicationContextAwareProcessor";

    /**
     * The {@link org.springframework.context.support.ApplicationContextAwareProcessor} Class (Internal).
     *
     * @see org.springframework.context.support.ApplicationContextAwareProcessor
     */
    public static final Class<?> APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS = ClassLoaderUtils.resolveClass(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME);


    @Nonnull
    public static BeanPostProcessor getApplicationContextAwareProcessor(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        return getApplicationContextAwareProcessor(beanFactory);
    }

    /**
     * Get the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     *
     * @return the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     */
    @Nullable
    public static BeanPostProcessor getApplicationContextAwareProcessor(BeanFactory beanFactory) {
        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors(beanFactory);
        BeanPostProcessor applicationContextAwareProcessor = null;
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            if (beanPostProcessor.getClass().equals(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS)) {
                applicationContextAwareProcessor = beanPostProcessor;
                break;
            }
        }
        if (applicationContextAwareProcessor == null) {
            if (log.isWarnEnabled()) {
                log.warn("The BeanPostProcessor[class : '{}' , present : {}] was not added in the BeanFactory[{}] @ Spring Framework '{}'",
                        APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME,
                        APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS != null,
                        beanFactory,
                        SpringVersion.getVersion());
            }
        }
        return applicationContextAwareProcessor;
    }
}
