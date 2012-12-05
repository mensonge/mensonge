package mensonge.core;

import java.io.File;

public class Utils
{
	private Utils()
	{
		// Permet d'empêcher l'instanciation de cette classe Utilitaire
	}
	
	public static String humanReadableByteCount(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
		{
			return bytes + " o";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
	}

	public static long getCacheSize()
	{
		long length = 0;
		File cacheDirectory = new File("cache");
		if (cacheDirectory.exists() && cacheDirectory.isDirectory())
		{
			for (File file : cacheDirectory.listFiles())
			{
				length += file.length();
			}
		}
		return length;
	}

	public static long getDBSize()
	{
		File dbFile = new File("LieLab.db");
		if (dbFile.exists() && dbFile.isFile())
		{
			return dbFile.length();
		}
		return 0;
	}
}
