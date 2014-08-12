package com.opcoach.e4.modelmerger;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class E4ModelMerger {

	@Inject EModelService ms;
	
	public void mergeModels(MApplication master, MApplication model)
	{
		mergeAddons(master, model);
		mergeCommandCategories(master, model);  // Categories are used by commands.
	}
	
	/** Merging addons must : 
	 *  Check if addon is not present in master model (ID checking)
	 *  If not, add this addon in the model
	 * @param master the target model
	 * @param model the source model
	 */
	public void mergeAddons(MApplication master, MApplication model)
	{
		for (MAddon addon : model.getAddons())
		{
			if (!isIdInlist(addon.getElementId(), master.getAddons()))
			{
				// Can add this addon in master model after clone
				MAddon cAddon = cloneAndBind(addon, master);
				master.getAddons().add(cAddon);
			}
		}
	}
	
	
	/** Merging command categories must : 
	 *  Check if categories is not present in master model (ID checking)
	 *  If not, add this command category in the model
	 * @param master
	 * @param model
	 */
	public void mergeCommandCategories(MApplication master, MApplication model)
	{
		for (MCategory cat : model.getCategories())
		{
			if (!isIdInlist(cat.getElementId(), master.getCategories()))
			{
				// Can add this addon in master model after clone
				MCategory cCat = cloneAndBind(cat, master);
				master.getCategories().add(cCat);
			}
		}
	}
	
	public boolean isIdInlist(String id, List<?> eltList)
	{
		boolean result = false; 
		for (Object e : eltList)
		{
			if ((e instanceof MApplicationElement) && ((MApplicationElement)e).getElementId().equals(id))
			{
				result = true; 
			break;
			}
		}
		return result;
	}
	
	/** This clone and bind, clone a source Addon and bind it to the master objects
	 * by finding the corresponding elements in master model
	 * @param source the source addon to be cloned
	 * @param master the master model where bounded data should be found (may be none)
	 * @return
	 */
	public  MAddon cloneAndBind(MAddon source, MApplication master)
	{
		// Clone step
		MAddon result = ms.createModelElement(MAddon.class);
		copyContributionData(source, result);

		// Binding step (nothing to bind for addons)
		
		return result;
		
	}
	
	/** This clone and bind, clone a source Category and bind it to the master objects
	 * by finding the corresponding elements in master model
	 * @param source the source object to be cloned
	 * @param master the master model where bounded data should be found (may be none)
	 * @return
	 */
	public  MCategory cloneAndBind(MCategory source, MApplication master)
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
	 * Copy the Application Element data (used during clone)
	 * @param source the source element
	 * @param target the target element
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
	 * Copy the Contribution Element data (used during clone), call ancestor (application element)
	 * @param source the source element
	 * @param target the target element
	 */
	private void copyContributionData(MContribution source, MContribution target)
	{
		// Copy ancestor data
		copyApplicationData(source, target);
		target.setContributionURI(source.getContributorURI());
	}
}
