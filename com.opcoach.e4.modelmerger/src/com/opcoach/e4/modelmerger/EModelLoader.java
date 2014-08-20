package com.opcoach.e4.modelmerger;

import org.eclipse.e4.ui.model.application.MApplication;

public interface EModelLoader
{
	/**
	 * Load an application model from a plugin path
	 * @param pluginPath : must be written like this : pluginID/localPath
	 *                     for instance : com.company.project.component/myModel.e4xmi
	 * @return the application model or null if nothing found.
	 */
	public MApplication loadModel(String pluginPath);

}
