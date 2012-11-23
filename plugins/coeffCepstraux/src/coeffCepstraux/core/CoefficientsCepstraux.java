package coeffCepstraux.core;


import java.io.File;
import java.util.ArrayList;

import mensonge.core.plugins.Plugin;

import mensonge.core.IExtraction;

public class CoefficientsCepstraux implements Plugin
{
	private boolean isActive = false;

	public void lancer(IExtraction extraction, ArrayList<File> fichiers)
	{
		this.isActive = true;
		this.isActive = false;
	}
	public void stopper()
	{
		this.isActive = false;
	}
	public String getNom()
	{
		return "Coefficients cepstraux";
	}
	public boolean isActive()
	{
		return isActive;
	}
}
