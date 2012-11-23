package mensonge.core;

public interface IExtraction
{
	public double[][] extraireEchantillons(String filePath);
	public byte[] extraireIntervalle(String filePath, long debut, long fin);
}
