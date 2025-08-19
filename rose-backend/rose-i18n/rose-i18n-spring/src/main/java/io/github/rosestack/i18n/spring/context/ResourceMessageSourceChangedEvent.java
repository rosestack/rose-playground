package io.github.rosestack.i18n.spring.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

public class ResourceMessageSourceChangedEvent extends ApplicationContextEvent {

	private final Iterable<String> changedResources;

	public ResourceMessageSourceChangedEvent(ApplicationContext source, Iterable<String> changedResources) {
		super(source);
		this.changedResources = changedResources;
	}

	public Iterable<String> getChangedResources() {
		return changedResources;
	}
}
