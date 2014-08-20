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
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MCommand;
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

public class TestMergeBindingContexts extends TestMerge {

	// IDs for the objects merged from model to master.
	 static final String BINDING_CTX_MAIN_ID = "org.eclipse.ui.contexts.dialogAndWindow";
	 static final String BINDING_CTX_WINDOW_ID = "org.eclipse.ui.contexts.window";
	 static final String BINDING_CTX_DIALOG_ID = "org.eclipse.ui.contexts.dialog";
	
	// This context is added in local in the dialog context (as a child of BINDING_CTX_DIALOG_ID)
	 static final String BINDING_CTX_LOCAL_WIZARD_ID = "com.opcoach.e4.modelmerger.test.wizardContext";
	
	// This context is added in local in the dialog context (as a sibling of BINDING_CTX_WINDOW_ID)
	 static final String BINDING_CTX_LOCAL_IN_OTHER_ID = "com.opcoach.e4.modelmerger.test.inOther";
	
	// This context is added in local as a top level context (sibling of BINDING_CTX_MAIN_ID)
//	private static final String BINDING_CTX_LOCAL_ID = "com.opcoach.e4.modelmerger.test.localBindingContext";
//	private static final String BINDING_CTX_LOCAL_CHILD_ID = "com.opcoach.e4.modelmerger.test.childContextOfTopContext";
	
	
	//..................................................................................................
	// The loadModel method does not read several binding context at the top level... I don't know why...
	// So these test only control that a child of dialog and window can be added in the master model.	
	//..................................................................................................
	
	
	
	/*@Test
	public void testMergePreConditions()
	{
		// This test just control if the two binding contexts in master and model are like expected.
		
		for (MBindingContext mbc : master.getBindingContexts())
			System.out.println("--> Master Binding context id  = " + mbc.getElementId());

		for (MBindingContext mbc : model.getBindingContexts())
			System.out.println("--> Local Binding context id  = " + mbc.getElementId());

		
		// Check master model contents
		MBindingContext masterBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_MAIN_ID);
	    MBindingContext dialogBc = (MBindingContext) searchElementById(masterBc.getChildren(), BINDING_CTX_DIALOG_ID);
	    
		assertNotNull("Master model must contain the dialog and window binding context ", masterBc);
		assertNotNull("Dialog and window binding context ust contain dialog context ", dialogBc);
		assertEquals("Master dialog and window binding context must contain only 2 children", 2, masterBc.getChildren().size());
		assertEquals("Master dialog binding context must not have  children", 0, dialogBc.getChildren().size());
		assertEquals("Master model must contain only one binding context ", 1, master.getBindingContexts().size());

		// Check local model contents.
		MBindingContext localBc = (MBindingContext) searchElementById(model.getBindingContexts(), BINDING_CTX_MAIN_ID);
		assertNotNull("Local model must contain the dialog and window binding context " + BINDING_CTX_MAIN_ID, localBc);
		assertEquals("Local dialog and window binding context must contain 3 children", 3, localBc.getChildren().size());
		

		assertEquals("Local model must contains 2 binding contexts", 2, model.getBindingContexts().size());

	}
*/
	@Test
	public void testMergeBindingContextInExistingDialog() {
		
		// The CTX_MAIN_ID contains in master and local the window and dialog binding contexts
		// The BINDING_CTX_LOCAL_WIZARD_ID has been added in the local binding context as a child of BINDING_CTX_DIALOG_ID
		// We check that wizard binding is a child of dialog binding context, in the master model after merge. 
		
		// Ensure master and local binding context are as expected
		MBindingContext masterBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_MAIN_ID);
	    MBindingContext dialogBc = (MBindingContext) searchElementById(masterBc.getChildren(), BINDING_CTX_DIALOG_ID);
		assertNotNull("Master model must contain the dialog and window binding context " + BINDING_CTX_MAIN_ID, masterBc);
		assertEquals("Master dialog and window binding context must contain only 2 children", 2, masterBc.getChildren().size());
		assertEquals("Master dialog binding context must not have  children", 0, dialogBc.getChildren().size());
		assertEquals("Master model must contain only one binding context ", 1, master.getBindingContexts().size());
	
		MBindingContext localBc = (MBindingContext) searchElementById(model.getBindingContexts(), BINDING_CTX_MAIN_ID);
		assertNotNull("Local model must contain the dialog and window binding context " + BINDING_CTX_MAIN_ID, localBc);
		assertEquals("Local dialog and window binding context must contain 3 children", 3, localBc.getChildren().size());
		
		
		
	//	assertEquals("Local model must contain two binding context ", 2, model.getBindingContexts().size());
	
		merger.mergeModels(master, model);

		// Now the master model must contain a new sub context sibling of dialog
	    masterBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_MAIN_ID);
	    dialogBc = (MBindingContext) searchElementById(masterBc.getChildren(), BINDING_CTX_DIALOG_ID);
		assertEquals("Master dialog binding context must now have 1 children", 1, dialogBc.getChildren().size());
		assertEquals("The ID of the child's dialog binding context must be wizard ID", BINDING_CTX_LOCAL_WIZARD_ID, dialogBc.getChildren().get(0).getElementId());
	
	}

	@Test
	public void testMergeBindingContextInDialogAndWindow() {
		
		// The CTX_MAIN_ID contains in master and local the window and dialog binding contexts
		// The BINDING_CTX_LOCAL_IN_OTHER_ID has been added in the local binding context
		// We check that master CTX_MAIN_ID has now the new child BINDING_CTX_LOCAL_IN_OTHER_ID 
	
		// Ensure master and local binding context are as expected
		MBindingContext masterBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_MAIN_ID);
		assertNotNull("Master model must contain the dialog and window binding context " + BINDING_CTX_MAIN_ID, masterBc);
		assertEquals("Master dialog and window binding context must contain only 2 children", 2, masterBc.getChildren().size());
		assertEquals("Master model must contain only one binding context ", 1, master.getBindingContexts().size());
		
		merger.mergeModels(master, model);

		// Now the master model must contain a new child in the CTX_MAIN_ID
	    masterBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_MAIN_ID);
		assertEquals("Master dialog and window binding context must now contain 3 children", 3, masterBc.getChildren().size());
		assertEquals("The ID of the children's dialog binding context must be OTHER_ID", BINDING_CTX_LOCAL_IN_OTHER_ID, masterBc.getChildren().get(2).getElementId());

	}

	/* This test is removed, while I can not know how to read a model with several bindings at the top level (only one is
	 * bound in the xmi (see the application/@bindingContexts root attribute !).
	
	@Test
	public void testMergeBindingContextAtTopLevel() {
		
		// The master model contains only one main Binding Context (with ID = CTX_MAIN_ID)
		// The BINDING_CTX_LOCAL_ID has been added in the local binding context at the top level
		// We check that the master model has now 2 children and the new added children has also one child 
	
		// Ensure master and local binding context are as expected
		assertEquals("Master model must contain only one binding context ", 1, master.getBindingContexts().size());
		assertEquals("Local model must contain two binding contexts at top level ", 2, model.getBindingContexts().size());
		
		merger.mergeModels(master, model);

		// Now the master model must contain a new child at the top level
		assertEquals("Master model must now contain two binding contexts ", 2, master.getBindingContexts().size());
		
		MBindingContext addedBc = (MBindingContext) searchElementById(master.getBindingContexts(), BINDING_CTX_LOCAL_ID);
		assertNotNull("The master model must contain a Binding Context with id = " + BINDING_CTX_LOCAL_ID, addedBc);
		assertEquals("The added binding context must have one child ", 1, addedBc.getChildren().size());
		assertEquals("The child of the added binding context must have good ID ", BINDING_CTX_LOCAL_CHILD_ID, addedBc.getChildren().get(0).getElementId());

	}
	 */


}
