package com.opcoach.e4.modelmerger;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class E4ModelMerger {

	@Inject EModelService ms;
	
	public void mergeModels(MApplication master, MApplication model)
	{
		mergeAddons(master, model);
	}
	
	/** Merging addons must : 
	 *  Check if addon is not present in master model
	 *  If not, add this addon in the model
	 * @param master
	 * @param model
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
		// Clone part
		MAddon result = ms.createModelElement(MAddon.class);
		result.setContributionURI(source.getContributionURI());
		result.setContributorURI(source.getContributorURI());
		result.setElementId(source.getElementId());

		// Binding part (nothing to bind for addons)
		
		return result;
		
	}
}
