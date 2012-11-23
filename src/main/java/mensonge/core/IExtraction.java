package mensonge.core;

public interface IExtraction
{
	double[][] extraireEchantillons(String filePath);
	byte[] extraireIntervalle(String filePath, long debut, long fin);
}
