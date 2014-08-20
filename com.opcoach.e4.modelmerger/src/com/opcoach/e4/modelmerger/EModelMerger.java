package com.opcoach.e4.modelmerger;

import java.net.URL;

import org.eclipse.e4.ui.model.application.MApplication;

public interface EModelMerger extends EModelLoader
{
	/**
	 * Merge a model into a master model. For duplicate elements, the model
	 * values override the master values.
	 * 
	 * @param master
	 *            the result model
	 * @param model
	 *            the source model
	 */
	public void mergeModels(MApplication master, MApplication model);

	/**
	 * Merge a model into a master model. For duplicate elements, the model
	 * values override the master values.
	 * 
	 * @param master
	 *            the result model
	 * @param modelPath
	 *            : a plugin path to the model. For instance :
	 *            com.company.project.component/myModel.e4xmi
	 */
	public void mergeModels(MApplication master, String modelPath);

}
