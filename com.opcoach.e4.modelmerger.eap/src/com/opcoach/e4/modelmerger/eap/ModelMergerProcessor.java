package com.opcoach.e4.modelmerger.eap;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.osgi.framework.FrameworkUtil;

import com.opcoach.e4.modelmerger.EModelMerger;
import com.opcoach.e4.modelmerger.impl.E4ReflectiveModelMerger;




public class ModelMergerProcessor
{
		
	@Execute
	public void processMerge(MApplication masterApplication, IEclipseContext ctx)
	{
		
		EModelMerger merger = (EModelMerger) ContextInjectionFactory.make(E4ReflectiveModelMerger.class, ctx);
		
		// Load the model to be merged.
		String bundleId = FrameworkUtil.getBundle(getClass()).getSymbolicName();
		MApplication model = merger.loadModel(bundleId +"/modelToMerge.e4xmi");
		
		System.out.println("--->  Merging both models");
		merger.mergeModels(masterApplication, model);
	}
	

}
