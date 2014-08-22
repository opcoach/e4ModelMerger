package com.opcoach.e4.modelmerger.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.opcoach.e4.modelmerger.E4ModelMergeException;
import com.opcoach.e4.modelmerger.EModelMerger;

/**
 * This class merges a model into a master model. It applies the rules of merge
 * described in the doc/*.odt document This is another merger written with
 * reflective API.
 */
public class E4ReflectiveModelMerger extends E4ModelLoaderImpl implements EModelMerger
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
		// Must get EClass to find some references that has not been
		// generated...
		EClass applicationEClass = getEClass(master);

		mergeElements(MAddon.class, ApplicationPackageImpl.Literals.APPLICATION__ADDONS, master, model);
		mergeElements(MCategory.class, ApplicationPackageImpl.Literals.APPLICATION__CATEGORIES, master, model);
		mergeElements(MCommand.class, ApplicationPackageImpl.Literals.APPLICATION__COMMANDS, master, model);

		mergeElements(MHandler.class, CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS, master, model);
		mergeElements(MBindingContext.class, CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, master, model);
		mergeElements(MBindingTable.class, CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES,
				master, model);
		
	}

	@SuppressWarnings("unchecked")
	public <T extends MApplicationElement> void mergeElements(Class<T> elementType, EReference ref,
			MApplication master, MApplication model)
	{
		List<T> contents = (List<T>) getValue(model, ref);
		for (T elt : contents)
		{
			MApplicationElement objectInMaster = searchInList((List<T>) getValue(master, ref), elt.getElementId());
			if (objectInMaster == null)
			{
				T clonedElt = ms.createModelElement(elementType);
				copyData(elt, clonedElt);

				((List<T>) getValue(master, ref)).add(clonedElt);
			} else
			{
				// There is already an object with this ID
				// override the data
				copyData(elt, objectInMaster);
			}
		}
		// Put objects in the cache
		fillCache((List<T>) getValue(master, ref));

	}

	/**
	 * This method merges the sourceList in targetList. It searches if object is
	 * already present in targetList, if yes it just copy the data, if not it
	 * clone the object and add it in target
	 * 
	 * @param sourceList
	 *            the source list
	 * @param targetList
	 *            the target list
	 * @param ref
	 *            the EReference which got the lists (to have type and
	 *            information)
	 */
	@SuppressWarnings("unchecked")
	public <T extends MApplicationElement> void mergeElementLists(List<T> sourceList, List<T> targetList)
	{
		for (T elt : sourceList)
		{
			MApplicationElement objectInMaster = searchInList(targetList, elt.getElementId());
			if (objectInMaster == null)
			{
				EObject clonedElt = EcoreUtil.copy((EObject)elt);

				targetList.add((T)clonedElt);
			} else
			{
				// There is already an object with this ID
				// override the data
				copyData(elt, objectInMaster);
			}
		}
		// Put objects in the cache
		fillCache(targetList);

	}

	/**
	 * This method copy only data... Associatino Reference will be bound later.
	 * Composition are cloned too
	 */
	public <T extends MApplicationElement> void copyData(T source, T target)
	{
		EClass sourceClass = getEClass(source);
		if (sourceClass == null)
			throw new E4ModelMergeException("Unable to call the eClass method on these objects " + source.toString());

		// Set all attributes values
		for (EAttribute a : sourceClass.getEAllAttributes())
		{
			if (a.isChangeable())
			{
				Object sourceValue = getValue(source, a);
				setValue(target, a, sourceValue);
			}
		}

		// Clone children or bind values
		for (EReference ref : sourceClass.getEAllReferences())
		{
			if (ref.isContainment())
			{
				if (ref.isMany())
				{
					
					mergeElementLists((List<T>) getValue(source, ref), (List<T>) getValue(target, ref));
			
				} else
				{
					// Clone only one object
					MApplicationElement sourceObject = (MApplicationElement) getValue(source, ref);
					MApplicationElement clonedChild = (MApplicationElement) EcoreUtil.create(ref.getEReferenceType());
					copyData(sourceObject, clonedChild);
					setValue(target, ref, clonedChild);
				}

			} else
			{
				// This is a reference to another object already created. We
				// must find the similar object with same ID
				if (ref.isMany())
				{
					System.out.println("A IMPLEMENTER");
				} else
				{
					// Clone only one object
					MApplicationElement sourceObject = (MApplicationElement) getValue(source, ref);
					String elementIdToBindTo = (sourceObject == null) ? null : sourceObject.getElementId();
					if (elementIdToBindTo != null)
					{
						MApplicationElement targetChild = cache.get(elementIdToBindTo);
						setValue(target, ref, targetChild);
					}
				}
			}
		}

	}

	/**
	 * 
	 * @param obj
	 *            the model object for getting the value
	 * @param feature
	 *            the feature in object to be extracted.
	 * @return
	 */
	public EClass getEClass(Object obj)
	{
		EClass result = null;
		// Search for the eGet method on obj.
		try
		{
			Method m = obj.getClass().getMethod("eClass");
			try
			{
				result = (EClass) m.invoke(obj);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * 
	 * @param obj
	 *            the model object for getting the value
	 * @param feature
	 *            the feature in object to be extracted.
	 * @return
	 */
	public Object getValue(Object obj, EStructuralFeature feature)
	{
		Object result = null;
		// Search for the eGet method on obj.
		try
		{
			Method m = obj.getClass().getMethod("eGet", EStructuralFeature.class);
			try
			{
				result = m.invoke(obj, feature);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * 
	 * @param obj
	 *            the model object for getting the value
	 * @param feature
	 *            the feature in object to be extracted.
	 * @return
	 */
	public void setValue(Object obj, EStructuralFeature feature, Object newValue)
	{
		// Search for the eSet method on obj.
		try
		{
			Method m = obj.getClass().getMethod("eSet", EStructuralFeature.class, Object.class);
			try
			{
				m.invoke(obj, feature, newValue);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void fillBindingContextCache(MBindingContext bc)
	{
		cache.put(bc.getElementId(), bc);
		for (MBindingContext child : bc.getChildren())
			fillBindingContextCache(child);
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
			if ((ae.getElementId() != null) && (ae.getElementId().equals(id)))
			{
				result = ae;
				break;
			}
		}
		return result;
	}

	/** Fill a local cache with the objects contained in the list */
	private <T extends MApplicationElement> void fillCache(List<T> eltList)
	{
		for (MApplicationElement e : eltList)
		{
			if (e.getElementId() != null)
			{
				MApplicationElement old = cache.put(e.getElementId(), e);
				if (old != null)
					log.warn("There are 2 objects in master model with the same ID : " + e.getElementId());
			}
		}
	}

	@Override
	public void mergeModels(MApplication master, String modelPath)
	{
		mergeModels(master, loadModel(modelPath));
	}
}
