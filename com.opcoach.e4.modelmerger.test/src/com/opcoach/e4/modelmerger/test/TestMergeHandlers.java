package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.junit.Test;

public class TestMergeHandlers extends TestMerge
{

	// IDs for the objects merged from model to master.
	private static final String CMD_COPY_ID = "org.eclipse.ui.edit.copy";
	private static final String CMD_PASTE_ID = "org.eclipse.ui.edit.paste";

	// This handler is defined in model and override the copy command (provided by model too).
	private static final String HDL_COPY_LOCAL_ID = "org.eclipse.ui.edit.copy.handler1";
	private static final String HDL_PASTE_LOCAL_ID = "org.eclipse.ui.edit.paste.handler1";


	@Test
	public void testMergeNewHandler()
	{
		
		// Handler is defined in sub model with its related command. 
		// The command does not exist yet in master. 
		// This test check that both are copied in master. 
		
		
		// Master model does not contain copy command an copy handler.
		MCommand copyMasterCmd = (MCommand) searchElementById(master.getCommands(), CMD_COPY_ID);
		assertNull("Master model must not yet contain the copy command " + CMD_COPY_ID, copyMasterCmd);
		MHandler copyMasterHdl = (MHandler) searchElementById(master.getHandlers(), HDL_COPY_LOCAL_ID);
		assertNull("Master model must not yet contain the copy handler " + HDL_COPY_LOCAL_ID, copyMasterHdl);
		
		// Local model must contain cmd and handler
		assertNotNull("Local  model must contain the copy command  " + CMD_COPY_ID,
				searchElementById(model.getCommands(), CMD_COPY_ID));
		assertNotNull("Local  model must contain the copy handler " + HDL_COPY_LOCAL_ID,
				searchElementById(model.getHandlers(), HDL_COPY_LOCAL_ID));

		merger.mergeModels(master, model);

		// The copy  command in master must now exist with the right binding on the new copy command created.
		 copyMasterCmd = (MCommand) searchElementById(master.getCommands(), CMD_COPY_ID);
		assertNotNull("Master model must now contain the copy command " + CMD_COPY_ID, copyMasterCmd);
		 copyMasterHdl = (MHandler) searchElementById(master.getHandlers(), HDL_COPY_LOCAL_ID);
		assertNotNull("Master model must now contain the copy handler " + HDL_COPY_LOCAL_ID, copyMasterHdl);
		assertTrue("Th command bound in the merged handler must be the copy command of master model", copyMasterCmd == copyMasterHdl.getCommand());

	}
	
	@Test
	public void testMergeOverriddeHandler()
	{
		
		// Handler is defined in sub model with its related command. 
		// The command exists in master (but no handler is 
		// This test checks that command is not copied, handler is copied and correctly bound
		
		
		// Master model does not contain copy command an copy handler.
		MCommand pasteMasterCmd = (MCommand) searchElementById(master.getCommands(), CMD_PASTE_ID);
		assertNotNull("Master model must already contain the paste command " + CMD_PASTE_ID, pasteMasterCmd);
		MHandler pasteMasterHdl = (MHandler) searchElementById(master.getHandlers(), HDL_PASTE_LOCAL_ID);
		assertNull("Master model must not yet contain the paste handler " + HDL_PASTE_LOCAL_ID, pasteMasterHdl);
		
		// Local model must contain cmd and handler
		assertNotNull("Local  model must contain the paste command  " + CMD_PASTE_ID,
				searchElementById(model.getCommands(), CMD_PASTE_ID));
		assertNotNull("Local  model must contain the paste handler " + HDL_PASTE_LOCAL_ID,
				searchElementById(model.getHandlers(), HDL_PASTE_LOCAL_ID));

		merger.mergeModels(master, model);

		// The paste command in master must now exist with the right binding on the old paste command found.
		pasteMasterHdl = (MHandler) searchElementById(master.getHandlers(), HDL_PASTE_LOCAL_ID);
		assertNotNull("Master model must now contain the paste handler " + HDL_PASTE_LOCAL_ID, pasteMasterHdl);
		assertTrue("Th command bound in the merged handler must be the paste command of master model", pasteMasterCmd == pasteMasterHdl.getCommand());

	}


}
