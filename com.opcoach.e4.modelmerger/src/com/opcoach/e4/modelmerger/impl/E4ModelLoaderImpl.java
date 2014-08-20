package com.opcoach.e4.modelmerger.impl;

import java.io.IOException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.opcoach.e4.modelmerger.EModelLoader;

public class E4ModelLoaderImpl implements EModelLoader
{
	private ResourceSetImpl resourceSetImpl;

	public E4ModelLoaderImpl()
	{
		super();
		init();
	}



	@Override
	public MApplication loadModel(String pluginPath)
	{

		MApplication result = null;

		try
		{
			// URI localURI = URI.createPlatformPluginURI("com.opcoach.e4.modelmerger.test/" + localPath, false);
			URI localURI = URI.createPlatformPluginURI(pluginPath, false);
			Resource resource = resourceSetImpl.createResource(localURI);

			resource.load(null);
			result = (MApplication) resource.getContents().get(0);
			
			IEclipseContext defaultContext = E4Application.createDefaultContext();
			result.setContext(defaultContext);
			defaultContext.set(MApplication.class, result);

			/*ModelAssembler contribProcessor = ContextInjectionFactory.make(ModelAssembler.class,
					defaultContext);
			contribProcessor.processModel(true);
			 */

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


}
