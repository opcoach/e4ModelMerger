package com.opcoach.e4.modelmerger;

import java.util.Formatter;

import org.eclipse.e4.ui.model.application.MApplicationElement;

public class E4ModelMergeException extends RuntimeException
{

	protected MApplicationElement source;
	protected MApplicationElement target;

	private static final long serialVersionUID = -3955895178604694572L;

	public E4ModelMergeException(MApplicationElement sourceElt, MApplicationElement targetElt, String message)
	{
		super(message);
		source = sourceElt;
		target = targetElt;
	}

	@Override
	public String getMessage()
	{
		Formatter f = new Formatter();

		try
		{

			f.format("%15s%30s%30s\n", " ", "Target Object", "Source Object")
					.format("%15s%30s%30s\n", " Class ", target.getClass().getName(), source.getClass().getName())
					.format("%15s%30s%30s\n", "Id", target.getElementId(), source.getElementId());

			return super.getMessage() + "\n" + f.toString();
		} finally
		{
			f.close();
		}
	}
}
