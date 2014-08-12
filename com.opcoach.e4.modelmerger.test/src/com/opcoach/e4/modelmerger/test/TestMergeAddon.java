package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opcoach.e4.modelmerger.E4ModelMerger;

public class TestMergeAddon extends TestMerge {

	// IDs for the objects merged from model to master.
	private static final String ADDON_ID = "com.opcoach.e4.modelmerger.test.addon.1";
	private static final String CATEGORY_ID = "com.opcoach.e4.modelmerger.test.modelCategory";
	
	


	@Test
	public void testMergeAddon() {
		
		int nbAddonInMaster = master.getAddons().size();
		boolean found = isElementInList(master.getAddons(),ADDON_ID);

		assertFalse("The model addon must not be in master", found);
		
		merger.mergeModels(master, model);
		
		// The addon in the model must still be there
		// A clone of this addon must be in the master model. 
		// Duplicated addons in model must not be in master model. 
		assertEquals("There must be " + nbAddonInMaster+1 + " addons in master", master.getAddons().size(), nbAddonInMaster+1);
		
		// search for the added addon in master.
		
		found = isElementInList(master.getAddons(), ADDON_ID);
		assertTrue("The model addon must now be in master", found);
  
		found = isElementInList(model.getAddons(), ADDON_ID);
		assertTrue("The model addon must always be in the source model", found);
  
	
	}


}
