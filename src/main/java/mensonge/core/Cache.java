package mensonge.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe gérant le dossier de cache
 * 
 */
public final class Cache
{
	private static final String CACHE_DIRECTORY_NAME = "cache";
	private static final File CACHE_DIRECTORY;
	private static final Logger LOGGER = Logger.getLogger("cacheUtils");

	private Cache()
	{
		// Permet d'empêcher l'instanciation de cette classe Utilitaire
	}

	static
	{
		CACHE_DIRECTORY = new File(CACHE_DIRECTORY_NAME);

		if (CACHE_DIRECTORY.exists() && !CACHE_DIRECTORY.isDirectory() && !CACHE_DIRECTORY.delete())
		{
			LOGGER.log(Level.WARNING, "Impossible de supprimer le fichier portant le même nom que le dossier de cache");

		}
		if (!CACHE_DIRECTORY.exists() && !CACHE_DIRECTORY.mkdir())
		{
			LOGGER.log(Level.WARNING, "Impossible de créer le dossier de cache");
		}
	}

	/**
	 * Récupère un fichier du cache
	 * 
	 * @param fileName
	 *            Nom du fichier à récupérer
	 * @return Fichier du cache ou null s'il n'existe pas
	 */
	public static File get(String fileName)
	{
		File cacheFile = new File(CACHE_DIRECTORY, fileName);
		if (cacheFile.exists())
		{
			return cacheFile;
		}
		return null;
	}

	/**
	 * Check si le fichier existe dans le cache
	 * 
	 * @param fileName
	 *            Nom du fichier
	 * @return Vrai s'il existe faux sinon
	 */
	public static boolean exists(String fileName)
	{
		return new File(CACHE_DIRECTORY, fileName).exists();
	}

	/**
	 * Créé un fichier dans le cache
	 * 
	 * @param fileName
	 *            Nom du fichier
	 * @param content
	 *            Contenu du fichier
	 * @throws IOException
	 */
	public static void createFile(String fileName, byte[] content) throws IOException
	{
		File newFile = new File(CACHE_DIRECTORY, fileName);
		if (!newFile.exists())
		{
			newFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(newFile);
		fos.write(content);
		fos.flush();
		fos.close();
	}

	/**
	 * Purge le cache
	 * 
	 * @return La taille totale des fichiers supprimés
	 */
	public static long purge()
	{
		long length = 0;
		if (CACHE_DIRECTORY.exists() && CACHE_DIRECTORY.isDirectory())
		{
			for (File file : CACHE_DIRECTORY.listFiles())
			{
				length += file.length();
				if (!file.delete())
				{
					LOGGER.log(Level.WARNING, "Impossible de supprimer " + file.getName() + " du cache");
				}
			}
		}
		return length;
	}

	/**
	 * Récupère la taille du dossier de cache (Somme de la taille des fichiers)
	 * 
	 * @return La taille du dossier de cache
	 */
	public static long getSize()
	{
		long length = 0;
		if (CACHE_DIRECTORY.exists() && CACHE_DIRECTORY.isDirectory())
		{
			for (File file : CACHE_DIRECTORY.listFiles())
			{
				length += file.length();
			}
		}
		return length;
	}
}
