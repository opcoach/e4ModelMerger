package com.opcoach.e4.modelmerger.test;

import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;

import com.opcoach.e4.modelmerger.EModelLoader;
import com.opcoach.e4.modelmerger.EModelMerger;
import com.opcoach.e4.modelmerger.impl.E4ModelLoaderImpl;
import com.opcoach.e4.modelmerger.impl.E4ModelMerger;

/**
 * The parent class for all tests. It initializes the 2 models (master and
 * model) for test
 */
public class TestMerge
{

	/**
	 * The master model into merge is done (ie, the future main application
	 * model)
	 */
	protected MApplication master;

	/** the source model to be merged in master (ie, the future fragment) */
	protected MApplication model;

	protected E4ModelMerger merger;
	protected EModelLoader loader;
	protected EModelService ms;

	/** Create the master and model with their appropriate contexts */
	@SuppressWarnings("restriction")
	@Before
	public void setUp() throws Exception
	{

		loader = new E4ModelLoaderImpl();
		
		// Read both models
		master = loader.loadModel("com.opcoach.e4.modelmerger.test/master.e4xmi");
		model = loader.loadModel("com.opcoach.e4.modelmerger.test/model.e4xmi");

		merger = ContextInjectionFactory.make(E4ModelMerger.class, master.getContext());

		ms = master.getContext().get(EModelService.class);
		master.getContext().set(EModelMerger.class, merger);
		master.getContext().set(EModelLoader.class, loader);

	}

	@After
	public void tearDown() throws Exception
	{
		master = model = null;
	}

	protected MApplicationElement searchElementById(List<?> list, String id)
	{
		return merger.searchInList(list, id);
	}

	protected boolean isElementInList(List<?> list, String id)
	{
		return merger.isIdInlist(list, id);

	}

}
