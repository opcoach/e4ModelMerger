package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.junit.Test;

public class TestMergeBindingTables extends TestMerge
{

	static final String BINDING_TABLE_LOCAL_ID = "org.eclipse.ui.contexts.dialogAndWindow";

	@Test
	public void testMergeKeyBindingInBindingTable()
	{

		// After the merge, there must be a key binding with M1+# and 2
		// parameters in the org.eclipse.ui.contexts.dialog binding context

		merger.mergeModels(master, model);

		// Search for the binding table for org.eclipse.ui.contexts.dialog
		// context
		MBindingContext dctx = (MBindingContext) searchElementById(master.getBindingContexts().get(0).getChildren(),
				TestMergeBindingContexts.BINDING_CTX_DIALOG_ID);
		MBindingTable table = null;
		for (MBindingTable mbt : master.getBindingTables())
		{

			if (mbt.getBindingContext() == dctx)
			{
				table = mbt;
				break;
			}
		}

		assertNotNull("There must be a binding table for the context org.eclipse.ui.contexts.dialog", table);
		// This binding table must have a key binding for M1+# with 2
		// parameters.

		MKeyBinding kb = null;
		for (MKeyBinding mkb : table.getBindings())
		{
			if ("M1+#".equals(mkb.getKeySequence()))
			{
				kb = mkb;
				break;
			}
		}

		assertNotNull("There must be a key binding with key sequence M1+#", kb);
		assertEquals("The key binding must have 2 parameters", 2, kb.getParameters().size());

	}

}
