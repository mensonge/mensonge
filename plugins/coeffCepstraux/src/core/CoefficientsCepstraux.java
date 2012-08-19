package core;

import core.plugins.Plugin;
import java.io.File;
import java.util.ArrayList;
import core.Extraction;

public class CoefficientsCepstraux implements Plugin
{
	private boolean isActive = false;

	public void lancer(ArrayList<File> fichiers)
	{
		this.isActive = true;
		if(fichiers.isEmpty())
		{
			System.out.println("[E] Aucun fichier sélectionné !");
		}
		else
		{
			double echs[][] = Extraction.extraireEchantillons(fichiers.get(0));
			System.out.println(echs[12000][0]+" "+echs[12000][1]);
		}

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
	public static void main(String args[])
	{
		ArrayList<File> fichiers = new ArrayList<File>();
		fichiers.add(new File("sons/test.wav"));
		CoefficientsCepstraux coeff = new CoefficientsCepstraux();
		System.out.println("[i] "+coeff.getNom());
		coeff.lancer(fichiers);
	}
}
