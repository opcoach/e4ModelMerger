package com.opcoach.e4.modelmerger;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * This class merges a model into a master model. It applies the rules of merge
 * described in the doc/*.odt document
 */
public class E4ModelMerger
{

	@Inject
	EModelService ms;

	@Inject
	Logger log;

	public void mergeModels(MApplication master, MApplication model)
	{
		mergeAddons(master, model);
		mergeCommandCategories(master, model); // Categories are used by
												// commands.
		mergeCommands(master, model); // Commands are used by handlers and
										// bindings
		mergeHandlers(master, model);
	}

	/**
	 * Merging addons must : Check if addon is not present in master model (ID
	 * checking) If not, add this addon in the model
	 * 
	 * @param master
	 *            the target model
	 * @param model
	 *            the source model
	 */
	public void mergeAddons(MApplication master, MApplication model)
	{
		for (MAddon addon : model.getAddons())
		{
			if (!isIdInlist(master.getAddons(), addon.getElementId()))
			{
				// Can add this addon in master model after clone
				MAddon cAddon = cloneAndBind(addon, master);
				master.getAddons().add(cAddon);
			}
		}
	}

	/**
	 * Merging command categories must : Check if categories is not present in
	 * master model (ID checking) If not, add this command category in the model
	 * 
	 * @param master
	 * @param model
	 */
	public void mergeCommandCategories(MApplication master, MApplication model)
	{
		for (MCategory cat : model.getCategories())
		{
			if (!isIdInlist(master.getCategories(), cat.getElementId()))
			{
				// Can add this category in master model after clone
				MCategory cCat = cloneAndBind(cat, master);
				master.getCategories().add(cCat);
			}
		}
	}

	/**
	 * Merging commands must : Check if command is not present in master model
	 * (ID checking) If not, add this command in the model If present, override
	 * the command data with the sub model
	 * 
	 * @param master
	 * @param model
	 */
	public void mergeCommands(MApplication master, MApplication model)
	{
		for (MCommand cmd : model.getCommands())
		{
			MCommand masterCmd = (MCommand) searchInList(master.getCommands(), cmd.getElementId());
			if (masterCmd == null)
			{
				// Can add this command in master model after clone
				MCommand cCmd = cloneAndBind(cmd, master);
				master.getCommands().add(cCmd);
			} else
			{
				// The command exists, must merge sub model data in the master
				// command
				// But must also check if commands have same content (nb of
				// parameters for instance).
				if (checkCompliance(cmd, masterCmd))
					copyAndbind(cmd, masterCmd, master);
			}
		}
	}

	/**
	 * Merging handlers must : Check if handler d is not present in master model
	 * (ID checking) If not, add this handler in the model If present, override
	 * the handler data with the sub model and check compliance
	 * 
	 * @param master
	 * @param model
	 */
	public void mergeHandlers(MApplication master, MApplication model)
	{
		for (MHandler hdl : model.getHandlers())
		{
			MHandler masterHdl = (MHandler) searchInList(master.getHandlers(), hdl.getElementId());
			if (masterHdl == null)
			{
				// Can add this command in master model after clone
				MHandler cHdl = cloneAndBind(hdl, master);
				master.getHandlers().add(cHdl);
			} else
			{
				// The handler alreday exists with this ID, must merge sub model
				// data in the master
				// But inform user with a info message.
				log.info("The handler (id ='" + hdl.getElementId() + "') in model fragment (id='"
						+ hdl.getContributorURI() + "') already exist in master model. Data will be overriden ");

				copyAndBind(hdl, masterHdl, master);
			}
		}
	}

	/**
	 * This method check if two objects are compliant to be merged.
	 * 
	 * @Exception throws a mergeException if something is wrong.
	 * */
	private boolean checkCompliance(MCommand cmd, MCommand masterCmd)
	{
		// Check nb of parameters
		if (cmd.getParameters().size() != masterCmd.getParameters().size())
			throw new E4ModelMergeException(cmd, masterCmd, "The two commands have not the same number of parameters");

		// Then, check if parameters have the same ids.
		for (int i = 0; i < cmd.getParameters().size(); i++)
		{
			MCommandParameter p1 = cmd.getParameters().get(i);
			MCommandParameter p2 = masterCmd.getParameters().get(i);

			String p1ID = p1.getElementId();
			String p2ID = p2.getElementId();

			// CHeck non null parameters
			if ((p1ID == null) || (p2ID == null))
				throw new E4ModelMergeException(p1, p2, "Parameters must have a non null ID to be merged");

			// Check similar IDs in each command
			if (!p1ID.equals(p2ID))
				throw new E4ModelMergeException(p1, p2,
						"parameters must have the same IDs in master command to be merged");

		}

		return true;
	}

	private boolean checkAllNullOrEquals(String s1, String s2)
	{
		return ((s1 == null) && (s2 == null)) || ((s1 != null) && s1.equals(s2)) || ((s2 != null) && s2.equals(s1));
	}

	public boolean isIdInlist(List<?> eltList, String id)
	{
		MApplicationElement result = searchInList(eltList, id);
		return result != null;
	}

	public MApplicationElement searchInList(List<?> eltList, String id)
	{
		MApplicationElement result = null;
		for (Object e : eltList)
		{
			MApplicationElement ae = (MApplicationElement) e;
			if ((e instanceof MApplicationElement) && ae.getElementId().equals(id))
			{
				result = ae;
				break;
			}
		}
		return result;
	}

	/**
	 * This clone and bind method clones a source Addon and bind it to the
	 * master objects
	 * 
	 * @param source
	 *            the source addon to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MAddon cloneAndBind(MAddon source, MApplication master)
	{
		// Clone step
		MAddon result = ms.createModelElement(MAddon.class);
		copyContributionData(source, result);

		// Binding step (nothing to bind for addons)

		return result;

	}

	/**
	 * This clone and bind method clones a source Category and bind it to the
	 * master objects
	 * 
	 * @param source
	 *            the source object to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MCategory cloneAndBind(MCategory source, MApplication master)
	{
		// Clone step
		MCategory result = ms.createModelElement(MCategory.class);
		copyApplicationData(source, result);

		result.setDescription(source.getDescription());
		result.setName(source.getName());

		// Binding step (nothing to bind)

		return result;

	}

	/**
	 * This clone and bind method clones a source Command and bind it to the
	 * master objects
	 * 
	 * @param source
	 *            the source object to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MCommand cloneAndBind(MCommand source, MApplication master)
	{
		// Clone step
		MCommand result = ms.createModelElement(MCommand.class);
		copyAndbind(source, result, master);

		return result;
	}

	public void copyAndbind(MCommand source, MCommand target, MApplication master)
	{
		// Clone step
		copyApplicationData(source, target);

		target.setDescription(source.getDescription());
		target.setCommandName(source.getCommandName());

		// Add the parameters if any
		for (MCommandParameter p : source.getParameters())
		{
			MCommandParameter cp = cloneAndBind(p, master);
			target.getParameters().add(cp);
		}

		// Binding step (must bind category).
		if (source.getCategory() != null)
		{
			MCategory cat = (MCategory) searchInList(master.getCategories(), source.getCategory().getElementId());
			target.setCategory(cat);
		}

	}

	/**
	 * This clone and bind method clones a source Parameter and bind it to the
	 * master objects
	 * 
	 * @param source
	 *            the source object to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MCommandParameter cloneAndBind(MCommandParameter source, MApplication master)
	{
		// Clone step
		MCommandParameter result = ms.createModelElement(MCommandParameter.class);
		copyApplicationData(source, result);

		result.setName(source.getName());
		result.setTypeId(source.getTypeId());
		result.setOptional(source.isOptional());

		// Binding step (no binding for parameters).

		return result;

	}

	/**
	 * This clone and bind method clones a source Handler and bind it to the
	 * master objects
	 * 
	 * @param source
	 *            the source handler to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MHandler cloneAndBind(MHandler source, MApplication master)
	{
		// Clone step
		MHandler result = ms.createModelElement(MHandler.class);
		copyAndBind(source, result, master);

		return result;

	}

	public void copyAndBind(MHandler source, MHandler target, MApplication master)
	{
		// Clone step
		copyContributionData(source, target);

		// Binding step (must bind commands found in the model).
		if (source.getCommand() != null)
		{
			MCommand cmd = (MCommand) searchInList(master.getCommands(), source.getCommand().getElementId());
			if (cmd != null)
			{
				// Ok, we can bind !
				target.setCommand(cmd);
			} else
			{
				// Hmmm there is an handler without command found in master ->
				// Merge Exception
				throw new E4ModelMergeException(source, target,
						"The command bound in source does not exist in master model ");
			}
		} else
		{
			// There is an handler, but no command defined inside --> Just log a
			// warning
			log.warn("The handler (id ='" + source.getElementId() + "') in model fragment (id='"
					+ source.getContributorURI() + "') is not bound to a command ");
		}

	}

	/**
	 * Copy the Application Element data (used during clone)
	 * 
	 * @param source
	 *            the source element
	 * @param target
	 *            the target element
	 */
	private void copyApplicationData(MApplicationElement source, MApplicationElement target)
	{
		target.setContributorURI(source.getContributorURI());
		target.setElementId(source.getElementId());
		// Copy persisted state
		for (Map.Entry<String, String> entry : source.getPersistedState().entrySet())
		{
			target.getPersistedState().put(entry.getKey(), entry.getValue());
		}

		target.getTags().addAll(source.getTags());
	}

	/**
	 * Copy the Contribution Element data (used during clone), call ancestor
	 * (application element)
	 * 
	 * @param source
	 *            the source element
	 * @param target
	 *            the target element
	 */
	private void copyContributionData(MContribution source, MContribution target)
	{
		// Copy ancestor data
		copyApplicationData(source, target);
		target.setContributionURI(source.getContributionURI());
	}
}
