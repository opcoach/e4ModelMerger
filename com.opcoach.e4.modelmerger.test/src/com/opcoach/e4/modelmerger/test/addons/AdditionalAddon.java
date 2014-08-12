 
package com.opcoach.e4.modelmerger.test.addons;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.services.events.IEventBroker;

public class AdditionalAddon {
	@Inject
	IEventBroker eventBroker;
	
	@PostConstruct
	void hookListeners() {
		// Hook event listeners
	}
	
	@PreDestroy
	void unhookListeners() {
		// Unhook event listeners
	}
}