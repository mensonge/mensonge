package coeffCepstraux.core;

import java.io.File;

public interface IExtraction
{
	public double[][] extraireEchantillons(File fichier);
	public byte[] extraireIntervalle(File fichier, long debut, long fin);
}
