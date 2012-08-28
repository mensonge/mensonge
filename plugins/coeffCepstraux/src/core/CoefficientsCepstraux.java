package core;

import core.IExtraction;
import core.plugins.Plugin;

import java.io.File;
import java.util.ArrayList;

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
