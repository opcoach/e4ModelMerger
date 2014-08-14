package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.*;

import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.junit.Test;

import com.opcoach.e4.modelmerger.E4ModelMergeException;

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

	private static final String CMD_LOCAL_WITH_PARAM_ID = "com.opcoach.command.localWithParameters";
	private static final String PARAM1_ID = "com.opcoach.e4.modelmerger.test.commandparameter1";
	private static final String PARAM1_TYPEID = "param1.typeID";
	private static final String PARAM1_NAME = "Param1";
	private static final String PARAM2_ID = "com.opcoach.e4.modelmerger.test.commandparameter2";
	private static final String PARAM2_NAME = "Param2";
	private static final String PARAM2_TYPEID = "param2.typeID";

	// This command will be present in master and local, but each contains
	// different parameters
	private static final String CMD_WITH_DIFFERENT_PARAMS_ID = "com.opcoach.command.cmdWithDiffParameters";

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
		assertEquals("The name of quit command must be now " + CMD_EXIT_OVERRIDEN_NAME, CMD_EXIT_OVERRIDEN_NAME,
				qCmd.getCommandName());
		assertEquals("The description of quit command must be now " + CMD_EXIT_OVERRIDEN_DESC, CMD_EXIT_OVERRIDEN_DESC,
				qCmd.getDescription());

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
		assertEquals("Description of local cmd must be " + CMD_EXIT_OVERRIDEN_DESC, CMD_LOCAL_DESC,
				cmd.getDescription());

	}

	@Test
	public void testMergeLocalCommandWithLocalCategory()
	{
		MCommand cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_CAT_ID);
		assertNull("Master model must not yet contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID, cmd);
		assertNotNull("Local model must contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID,
				searchElementById(model.getCommands(), CMD_LOCAL_WITH_CAT_ID));

		merger.mergeModels(master, model);

		// The local command in master must now exist.
		cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_CAT_ID);
		assertNotNull("Master model must now contain the local command with category : " + CMD_LOCAL_WITH_CAT_ID, cmd);
		MCategory cat = cmd.getCategory();
		MCategory masterCat = (MCategory) searchElementById(master.getCategories(), TestMergeCategories.CATEGORY_ID);
		assertTrue("Category of added command must be the same than in master model", cat == masterCat);
	}

	@Test
	public void testMergeLocalCommandWithParameters()
	{
		MCommand cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_PARAM_ID);
		assertNull("Master model must not yet contain the local command with parameters : " + CMD_LOCAL_WITH_PARAM_ID,
				cmd);
		assertNotNull("Local model must contain the local command with parameters : " + CMD_LOCAL_WITH_PARAM_ID,
				searchElementById(model.getCommands(), CMD_LOCAL_WITH_PARAM_ID));

		merger.mergeModels(master, model);

		// The local command in master must now exist.
		cmd = (MCommand) searchElementById(master.getCommands(), CMD_LOCAL_WITH_PARAM_ID);
		assertNotNull("Master model must now contain the local command with category : " + CMD_LOCAL_WITH_PARAM_ID, cmd);
		// Check its parameters
		assertEquals("The merged command must contain 2 parameters ", 2, cmd.getParameters().size());
		MCommandParameter p1 = cmd.getParameters().get(0);
		MCommandParameter p2 = cmd.getParameters().get(1);
		assertEquals("The first parameter of merged command must have same ID ", PARAM1_ID, p1.getElementId());
		assertEquals("The first parameter of merged command must have same name ", PARAM1_NAME, p1.getName());
		assertEquals("The first parameter of merged command must have same typeID ", PARAM1_TYPEID, p1.getTypeId());
		assertTrue("The first parameter of merged command is not optional ", !p1.isOptional());

		assertEquals("The second parameter of merged command must have same ID ", PARAM2_ID, p2.getElementId());
		assertEquals("The second parameter of merged command must have same name ", PARAM2_NAME, p2.getName());
		assertEquals("The second parameter of merged command must have same typeID ", PARAM2_TYPEID, p2.getTypeId());
		assertTrue("The second parameter of merged command is not optional ", p2.isOptional());

	}

	@Test(expected = E4ModelMergeException.class)
	public void testMergeLocalCommandWithNotCompliantParameters()
	{
		// In fragment we have a command with not the same number of parameters
		// than in command found in master, while ID of commands are the same.

		model = loadModel("modelWithUncompliantCmdParameters.e4xmi");
		
		assertNotNull("Master model must  contain the local command with parameters : " + CMD_WITH_DIFFERENT_PARAMS_ID,
				searchElementById(master.getCommands(), CMD_WITH_DIFFERENT_PARAMS_ID));
		assertNotNull("Local model must contain the local command with parameters : " + CMD_WITH_DIFFERENT_PARAMS_ID,
				searchElementById(model.getCommands(), CMD_WITH_DIFFERENT_PARAMS_ID));

		merger.mergeModels(master, model);

		// It must throw an exception : the two commands exists in both model, but nb of parameter is different. 

	}

}
