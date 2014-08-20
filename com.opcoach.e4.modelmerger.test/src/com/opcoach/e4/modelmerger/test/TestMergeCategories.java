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

import com.opcoach.e4.modelmerger.impl.E4ModelMerger;

public class TestMergeCategories extends TestMerge {

	// IDs for the objects merged from model to master.
     static final String CATEGORY_ID = "com.opcoach.e4.modelmerger.test.modelCategory";
	
	
	@Test
	public void testMergeCategories()
	{
		int nbCategoriesInMaster = master.getCategories().size();
		
		merger.mergeModels(master, model);
		
		// The category in the model must still be there
		// A clone of this category must be in the master model. 
		// Duplicated category in model must not be in master model. 
		assertEquals("There must be " + nbCategoriesInMaster+1 + " categories in master", master.getCategories().size(), nbCategoriesInMaster+1);
		
		// search for the added addon in master.
		
		boolean found = isElementInList(master.getCategories(), CATEGORY_ID);
		assertTrue("The model category must now be in master", found);
  
		found = isElementInList(model.getCategories(), CATEGORY_ID);
		assertTrue("The model category must always be in the source model", found);
  
		
	}

}
