package core;

import core.plugins.Plugin;
import java.io.File;
import java.util.ArrayList;

public class CoefficientsCepstraux implements Plugin
{
	private boolean isActive = false;

	public void lancer(ArrayList<File> listeFichiersSelectionnes)
	{
		this.isActive = true;
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
