package coeffCepstraux.core;

import java.io.File;
import java.util.List;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

public class CoefficientsCepstraux implements Plugin
{
	private boolean isActive = false;

	@Override
	public void lancer(IExtraction extraction, List<File> listeFichiersSelectionnes)
	{
		this.isActive = true;
		this.isActive = false;
	}

	@Override
	public void stopper()
	{
		this.isActive = false;
	}

	@Override
	public String getNom()
	{
		return "Coefficients cepstraux";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}

}
