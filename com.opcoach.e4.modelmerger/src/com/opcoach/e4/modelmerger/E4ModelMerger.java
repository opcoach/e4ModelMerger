package com.opcoach.e4.modelmerger;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class E4ModelMerger
{

	@Inject
	EModelService ms;

	public void mergeModels(MApplication master, MApplication model)
	{
		mergeAddons(master, model);
		mergeCommandCategories(master, model); // Categories are used by
												// commands.
		mergeCommands(master, model);
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
			MCommand masterCmd = (MCommand) searchInList(master.getCommands(),cmd.getElementId());
			if (masterCmd == null)
			{
				// Can add this command in master model after clone
				MCommand cCmd = cloneAndBind(cmd, master);
				master.getCommands().add(cCmd);
			} else
			{
				// The command exists, must merge sub model data in the master
				// command
				copyAndbind(cmd, masterCmd, master);
			}
		}
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

		// Add the parameters is any
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
