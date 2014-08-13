package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.*;

import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.junit.Test;

public class TestMergeCommands extends TestMerge
{

	// IDs for the objects merged from model to master.
	private static final String CMD_EXIT_ID = "org.eclipse.ui.file.exit";
	private static final String CMD_EXIT_NAME = "quitCommand";
	private static final String CMD_EXIT_OVERRIDEN_NAME = "Overriden Quit Command";
	private static final String CMD_EXIT_OVERRIDEN_DESC = "Overriden";

	private static final String CMD_LOCAL_ID = "com.opcoach.command.local";
	private static final String CMD_LOCAL_NAME = "Local Command";
	private static final String CMD_LOCAL_DESC = "A local command";

	private static final String CMD_LOCAL_WITH_CAT_ID = "com.opcoach.command.localWithCategory";

	@Test
	public void testMergeOverriddenCommand()
	{
		MCommand masterQuitCmd = (MCommand) searchElementById(master.getCommands(), CMD_EXIT_ID);
		assertNotNull("Master model must contain the quit command " + CMD_EXIT_ID, masterQuitCmd);
		assertEquals("Master quit command name must be " + CMD_EXIT_NAME, CMD_EXIT_NAME, masterQuitCmd.getCommandName());
		assertNotNull("Local  model must contain the quit command " + CMD_EXIT_ID,
				searchElementById(model.getCommands(), CMD_EXIT_ID));

		merger.mergeModels(master, model);

		// The quit command in master must be overriden.
		MCommand qCmd = (MCommand) searchElementById(master.getCommands(), CMD_EXIT_ID);
		assertEquals("The name of quit command must be now " + CMD_EXIT_OVERRIDEN_NAME, CMD_EXIT_OVERRIDEN_NAME,qCmd.getCommandName());
		assertEquals("The description of quit command must be now " + CMD_EXIT_OVERRIDEN_DESC, CMD_EXIT_OVERRIDEN_DESC,qCmd.getDescription()
				);

	}

	@Test
	public void testMergeLocalCommandWithoutCategory()
	{
		MCommand cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_ID);
		assertNull("Master model must not contain the local command " + CMD_LOCAL_ID, cmd);
		assertNotNull("Local model must contain the local command " + CMD_LOCAL_ID,
				searchElementById(model.getCommands(), CMD_LOCAL_ID));

		merger.mergeModels(master, model);

		// The local command in master must now exist.
		cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_ID);
		assertNotNull("Master model must now contain the local command " + CMD_LOCAL_ID, cmd);
		assertEquals("Name of local cmd must be  " + CMD_LOCAL_NAME, CMD_LOCAL_NAME, cmd.getCommandName());
		assertEquals("Description of local cmd must be " + CMD_EXIT_OVERRIDEN_DESC, CMD_LOCAL_DESC, cmd.getDescription());

	}

	@Test
	public void testMergeLocalCommandWithLocalCategory()
	{
		MCommand cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_CAT_ID);
		assertNull("Master model must not contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID, cmd);
		assertNotNull("Local model must contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID,
				searchElementById(model.getCommands(), CMD_LOCAL_WITH_CAT_ID));

		merger.mergeModels(master, model);

		// The local command in master must now exist.
		cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_CAT_ID);
		assertNotNull("Master model must now contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID, cmd);
		MCategory cat = cmd.getCategory();
		MCategory masterCat = (MCategory) searchElementById(master.getCategories(), TestMergeCategories.CATEGORY_ID);
		assertEquals("Category of added command must be the same than in master model", cat, masterCat);
	}

}
