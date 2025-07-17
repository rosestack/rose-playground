package io.github.rose.i18n;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.rose.i18n.util.I18nResourceUtils;

/**
 * 装饰器：为 I18nMessageSource 添加消息监听能力，并自动监听文件变化。
 */
public class ObservableI18nMessageSource implements ReloadedResourceMessageSource {
    private final ReloadedResourceMessageSource delegate;
    private final List<MessageSourceChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Path> watchPaths = new ArrayList<>();
    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running = false;

    public ObservableI18nMessageSource(ReloadedResourceMessageSource delegate) {
        this.delegate = delegate;
        delegate.getInitializeResources().forEach(resource -> watchPaths.add(Path.of(resource)));
    }

    public void addListener(MessageSourceChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageSourceChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return delegate.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        return delegate.getMessage(code, args, locale);
    }

    private void startWatching() {
        if (watchPaths.isEmpty()) {
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Set<Path> parentDirs = new HashSet<>();
            for (Path path : watchPaths) {
                Path parent = path.getParent();
                if (parent != null && parentDirs.add(parent)) {
                    parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                }
            }
            running = true;
            watchThread = new Thread(this::watchLoop, "I18nFileWatchThread");
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            // 可选：日志记录
        }
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.take();
                Path dir = (Path) key.watchable();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path changed = dir.resolve((Path) event.context());
                    if (watchPaths.contains(changed)) {
                        // 通知所有监听器
                        for (MessageSourceChangeListener listener : listeners) {
                            listener.onMessagesReloaded(changed.toFile().getAbsolutePath(), this); // locale 可根据实际实现传递
                        }
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 可选：日志记录
            }
        }
    }

    public void stopWatching() {
        running = false;
        if (watchThread != null) watchThread.interrupt();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void init() {
        startWatching();
    }

    @Override
    public void destroy() {
        stopWatching();
    }

    @Override
    public void initializeResource(String resource) {
        delegate.initializeResource(resource);
    }

    @Override
    public Set<String> getInitializeResources() {
        return delegate.getInitializeResources();
    }
}