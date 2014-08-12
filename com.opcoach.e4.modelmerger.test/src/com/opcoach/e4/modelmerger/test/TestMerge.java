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

public class TestMerge
{

	// IDs for the objects merged from model to master.
	private static final String ADDON_ID = "com.opcoach.e4.modelmerger.test.addon.1";
	private static final String CATEGORY_ID = "com.opcoach.e4.modelmerger.test.modelCategory";

	/** The master model into merge is done */
	protected MApplication master;
	/** the source model to be merged in master (ie, the future fragment) */

	protected MApplication model;

	private ResourceSetImpl resourceSetImpl;

	protected E4ModelMerger merger;
	protected EModelService ms;

	public TestMerge()
	{
		init();
	}

	@SuppressWarnings("restriction")
	@Before
	public void setUp() throws Exception
	{

		// Read both models
		master = loadModel("master.e4xmi");
		model = loadModel("model.e4xmi");

		master.setContext(E4Application.createDefaultContext());
		model.setContext(E4Application.createDefaultContext());

		merger = ContextInjectionFactory.make(E4ModelMerger.class, master.getContext());

		ms = master.getContext().get(EModelService.class);

	}

	private MApplication loadModel(String localPath)
	{

		MApplication result = null;

		try
		{
			URI localURI = URI.createPlatformPluginURI("com.opcoach.e4.modelmerger.test/" + localPath, false);
			Resource resource = resourceSetImpl.createResource(localURI);

			resource.load(null);
			result = (MApplication) resource.getContents().get(0);

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	@SuppressWarnings("restriction")
	void init()
	{
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());

		resourceSetImpl.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI, ApplicationPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI, CommandsPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI, BasicPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI, AdvancedPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

	}

	@After
	public void tearDown() throws Exception
	{
		master = model = null;
	}

	protected MApplicationElement searchElementById(List<?> list, String id)
	{
		return merger.searchInList(list,id);
	}

	protected boolean isElementInList(List<?> list, String id)
	{
		return merger.isIdInlist(list, id);
	
	}

}
