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
	private static final Logger LOGGER = Logger.getLogger("cacheUtils");
	private File cacheDirectory;

	/**
	 * Création d'un nouveau répertoire de cache
	 */
	public Cache()
	{
		cacheDirectory = new File(CACHE_DIRECTORY_NAME);

		if (cacheDirectory.exists() && !cacheDirectory.isDirectory() && !cacheDirectory.delete())
		{
			LOGGER.log(Level.WARNING, "Impossible de supprimer le fichier portant le même nom que le dossier de cache");

		}
		if (!cacheDirectory.exists() && !cacheDirectory.mkdir())
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
	public File get(String fileName)
	{
		File cacheFile = new File(cacheDirectory, fileName);
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
	public boolean exists(String fileName)
	{
		return new File(cacheDirectory, fileName).exists();
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
	public void createFile(String fileName, byte[] content) throws IOException
	{
		File newFile = new File(cacheDirectory, fileName);
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
	public long purge()
	{
		long length = 0;
		if (cacheDirectory.exists() && cacheDirectory.isDirectory())
		{
			for (File file : cacheDirectory.listFiles())
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
	public long getSize()
	{
		long length = 0;
		if (cacheDirectory.exists() && cacheDirectory.isDirectory())
		{
			for (File file : cacheDirectory.listFiles())
			{
				length += file.length();
			}
		}
		return length;
	}
}
