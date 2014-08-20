package com.opcoach.e4.modelmerger.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import com.opcoach.e4.modelmerger.E4ModelMergeException;
import com.opcoach.e4.modelmerger.EModelMerger;

/**
 * This class merges a model into a master model. It applies the rules of merge
 * described in the doc/*.odt document
 */
public class E4ModelMerger  extends E4ModelLoaderImpl implements EModelMerger
{

	@Inject
	EModelService ms;

	@Inject
	Logger log;

	// A private cache to find data when rebinding...
	// It contains only objects from the master model
	private Map<String, MApplicationElement> cache = new HashMap<String, MApplicationElement>();

	public void mergeModels(MApplication master, MApplication model)
	{
		mergeAddons(master, model);
		mergeCommandCategories(master, model); // Categories are used by
												// commands.
		mergeCommands(master, model); // Commands are used by handlers and
										// bindings
		mergeHandlers(master, model);
		mergeBindingContexts(master, model);
		mergeBindingTable(master, model);
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

		fillCache(master.getCategories());
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
					copyAndBind(cmd, masterCmd, master);
			}
		}

		fillCache(master.getCommands());

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
						+ model.getElementId() + "') already exists in master model. Data will be overriden ");

				copyAndBind(hdl, masterHdl, master);
			}
		}

		fillCache(master.getHandlers());

	}

	/**
	 * Merging binding contexts must : Check if binding context is not present
	 * in master model (ID checking) If not, add this binding context in the
	 * model If present, override the binding context data with the sub model
	 * Warning : a binding context can contain sub context -> we must detect if
	 * merge is compliant and deep clone the object. Then we must check if a
	 * context is already present in master model but with a different parent...
	 * 
	 * @param master
	 * @param model
	 */
	public void mergeBindingContexts(MApplication master, MApplication model)
	{
		for (MBindingContext bct : model.getBindingContexts())
		{
			MBindingContext masterCtx = (MBindingContext) searchInList(master.getBindingContexts(), bct.getElementId());
			if (masterCtx == null)
			{
				// Can add this binding context in master model after deep
				// clone...
				MBindingContext cCtx = cloneAndBind(bct, master);
				master.getBindingContexts().add(cCtx);
			} else
			{
				// The binding context already exists, must merge sub model data
				// and all the children
				copyAndBind(bct, masterCtx, master);
			}
		}

		for (MBindingContext bct : master.getBindingContexts())
			fillBindingContextCache(bct);
	}

	private void fillBindingContextCache(MBindingContext bc)
	{
		cache.put(bc.getElementId(), bc);
		for (MBindingContext child : bc.getChildren())
			fillBindingContextCache(child);
	}

	/**
	 * Merging binding tables must : Check if binding table is not present in
	 * master model (ID checking) If not, add this binding table in the model
	 * with all of its children If present, override the binding table data with
	 * the sub model
	 * 
	 * @param master
	 * @param model
	 */
	public void mergeBindingTable(MApplication master, MApplication model)
	{
		for (MBindingTable bt : model.getBindingTables())
		{
			MBindingTable masterBt = (MBindingTable) searchInList(master.getBindingTables(), bt.getElementId());
			if (masterBt == null)
			{
				// Can add this binding table in master model after deep
				// clone...
				MBindingTable cbt = cloneAndBind(bt, master);
				master.getBindingTables().add(cbt);
			} else
			{
				// The binding context already exists, must merge sub model data
				// and all the children
				copyAndBind(bt, masterBt, master);
			}
		}

		fillCache(master.getBindingTables());
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
			if ( (ae.getElementId()!=null) && (ae.getElementId().equals(id)))
			{
				result = ae;
				break;
			}
		}
		return result;
	}

	/** Fill a local cache with the objects contained in the list */
	private void fillCache(List<?> eltList)
	{
		for (Object e : eltList)
		{
			MApplicationElement ae = (MApplicationElement) e;
			cache.put(ae.getElementId(), ae);
		}
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
		copyAndBind(source, result, master);

		return result;
	}

	public void copyAndBind(MCommand source, MCommand target, MApplication master)
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
	
	public MParameter cloneAndBind(MParameter source, MApplication master)
	{
		// Clone step
		MParameter result = ms.createModelElement(MParameter.class);
		copyApplicationData(source, result);

		result.setValue(source.getValue());
		result.setName(source.getName());

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

	/**
	 * This clone and bind method clones a binding context and bind it to the
	 * master objects. This is a deep clone, and it clones also the children.
	 * 
	 * @param source
	 *            the source object to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MBindingContext cloneAndBind(MBindingContext source, MApplication master)
	{
		MBindingContext result = ms.createModelElement(MBindingContext.class);
		copyAndBind(source, result, master);

		return result;

	}
	

	public void copyAndBind(MBindingContext source, MBindingContext target, MApplication master)
	{
		// Clone step
		copyApplicationData(source, target);

		target.setName(source.getName());
		target.setDescription(source.getDescription());

		// Copy or clone also the children depending of already in master
		for (MBindingContext child : source.getChildren())
		{
			MBindingContext targetBC = (MBindingContext) searchInList(target.getChildren(), child.getElementId());

			if (targetBC != null)
			{
				// There is already a child with the same ID, must just continue
				// to copy
				copyAndBind(child, targetBC, master);
			} else
			{
				// There is no existing children in master.. must clone and add
				MBindingContext cc = cloneAndBind(child, master);
				target.getChildren().add(cc);
			}
		}

		// Binding step (no objects binding for binding context).

	}
	
	/**
	 * This clone and bind method clones a binding table and bind it to the
	 * master objects (binding contexts and commands for its children. 
	 * This is a deep clone, and it clones also the children.
	 * 
	 * @param source
	 *            the source object to be cloned
	 * @param master
	 *            the master model where bounded data should be found (may be
	 *            none)
	 * @return
	 */
	public MBindingTable cloneAndBind(MBindingTable source, MApplication master)
	{
		MBindingTable result = ms.createModelElement(MBindingTable.class);
		copyAndBind(source, result, master);

		return result;

	}
	
	public MKeyBinding cloneAndBind(MKeyBinding source, MApplication master)
	{
		MKeyBinding result = ms.createModelElement(MKeyBinding.class);
		copyAndBind(source, result, master);

		return result;

	}
	
	
	public void copyAndBind(MBindingTable source, MBindingTable target, MApplication master)
	{
		// Clone step
		copyApplicationData(source, target);

		// Copy or clone also the children depending of already in master
		for (MKeyBinding child : source.getBindings())
		{
			MKeyBinding targetKb = (MKeyBinding) searchInList(target.getBindings(), child.getElementId());

			if (targetKb != null)
			{
				// There is already a child with the same ID, must just continue
				// to copy
				copyAndBind(child, targetKb, master);
			} else
			{
				// There is no existing children in master.. must clone and add
				MKeyBinding ckb = cloneAndBind(child, master);
				target.getBindings().add(ckb);
			}
		}

		// Binding step (Must bind the table to the corresponding binding context).
		MBindingContext bc = (MBindingContext) cache.get(source.getBindingContext().getElementId());
		target.setBindingContext(bc);
	}

	public void copyAndBind(MKeyBinding source, MKeyBinding target, MApplication master)
	{
		// Clone step
		copyApplicationData(source, target);
		target.setKeySequence(source.getKeySequence());

		// Add the parameters if any
		for (MParameter p : source.getParameters())
		{
			MParameter cp = cloneAndBind(p, master);
			target.getParameters().add(cp);
		}

		// Binding step (must bind the command).
		if (source.getCommand() != null)
		{
			MCommand cmd = (MCommand) cache.get(source.getCommand().getElementId());
			target.setCommand(cmd);
		}


	}

	public void copyAndBind(MHandler source, MHandler target, MApplication master)
	{
		// Clone step
		copyContributionData(source, target);

		// Binding step (must bind commands found in the model).
		if (source.getCommand() != null)
		{
			MCommand cmd = (MCommand) cache.get(source.getCommand().getElementId());
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

	@Override
	public void mergeModels(MApplication master, String modelPath)
	{
		mergeModels(master, loadModel(modelPath));
	}
}
