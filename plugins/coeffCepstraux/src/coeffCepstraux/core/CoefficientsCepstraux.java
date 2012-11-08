package coeffCepstraux.core;


import java.io.File;
import java.util.ArrayList;

import coeffCepstraux.core.IExtraction;
import coeffCepstraux.core.plugins.Plugin;

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
