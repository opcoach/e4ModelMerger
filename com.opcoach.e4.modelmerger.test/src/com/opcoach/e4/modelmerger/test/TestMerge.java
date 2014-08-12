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

public class TestMerge {

	// IDs for the objects merged from model to master.
	private static final String ADDON_ID = "com.opcoach.e4.modelmerger.test.addon.1";
	private static final String CATEGORY_ID = "com.opcoach.e4.modelmerger.test.modelCategory";
	
	
	
	/** The master model into merge is done */
	private MApplication master;
	/** the source model to be merged in master (ie, the future fragment) */

	private MApplication model;

	private ResourceSetImpl resourceSetImpl;
	
	private E4ModelMerger merger;
	private EModelService ms;
	

	
	public TestMerge()
	{
		init();
	}

	@SuppressWarnings("restriction")
	@Before
	public void setUp() throws Exception {

		// Read both models
		 master = loadModel("master.e4xmi");
	     model = loadModel("model.e4xmi");
	     
	     master.setContext(E4Application.createDefaultContext());
	     model.setContext(E4Application.createDefaultContext());
	     
		merger = ContextInjectionFactory.make(E4ModelMerger.class, master.getContext());
		
		ms = master.getContext().get(EModelService.class);
	     
	}

	private MApplication loadModel(String localPath) {

		MApplication result = null;

		try {
			URI localURI =  URI.createPlatformPluginURI("com.opcoach.e4.modelmerger.test/" + localPath, false);
			Resource resource = resourceSetImpl.createResource(localURI);

			resource.load(null);
			result = (MApplication) resource.getContents().get(0);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	@SuppressWarnings("restriction")
	void init() {
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new E4XMIResourceFactory());

		resourceSetImpl.getPackageRegistry().put(
				ApplicationPackageImpl.eNS_URI,
				ApplicationPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI,
				CommandsPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI,
				UiPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(MenuPackageImpl.eNS_URI,
				MenuPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI,
				BasicPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI,
				AdvancedPackageImpl.eINSTANCE);
		resourceSetImpl
				.getPackageRegistry()
				.put(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
						org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

	}

	@After
	public void tearDown() throws Exception {
		master = model = null;
	}

	@Test
	public void testModelsLoaded() {
		assertNotNull("Master model must not be null", master);
		assertNotNull("Model to merge must not be null", model);
	}

	@Test
	public void testAddonMerge() {
		
		int nbAddonInMaster = master.getAddons().size();
		boolean found = searchElementById(master.getAddons(),ADDON_ID);

		assertFalse("The model addon must not be in master", found);
		
		merger.mergeAddons(master, model);
		
		// The addon in the model must still be there
		// A clone of this addon must be in the master model. 
		// Duplicated addons in model must not be in master model. 
		assertEquals("There must be " + nbAddonInMaster+1 + " addons in master", master.getAddons().size(), nbAddonInMaster+1);
		
		// search for the added addon in master.
		
		found = searchElementById(master.getAddons(), ADDON_ID);
		assertTrue("The model addon must now be in master", found);
  
		found = searchElementById(model.getAddons(), ADDON_ID);
		assertTrue("The model addon must always be in the source model", found);
  
	
	}

	
	private boolean searchElementById(List<?> list, String id) {
		boolean found = false;
		for (Object o : list)
		{
			found =  (o instanceof MApplicationElement) && id.equals(((MApplicationElement)o).getElementId());
			if (found) break;
		}
		return found;
	}

	@Test
	public void testMergeCategories()
	{
		int nbCategoriesInMaster = master.getCategories().size();
		
		merger.mergeCommandCategories(master, model);
		
		// The category in the model must still be there
		// A clone of this category must be in the master model. 
		// Duplicated category in model must not be in master model. 
		assertEquals("There must be " + nbCategoriesInMaster+1 + " categories in master", master.getCategories().size(), nbCategoriesInMaster+1);
		
		// search for the added addon in master.
		
		boolean found = searchElementById(master.getCategories(), CATEGORY_ID);
		assertTrue("The model category must now be in master", found);
  
		found = searchElementById(model.getCategories(), CATEGORY_ID);
		assertTrue("The model category must always be in the source model", found);
  
		
	}

}
