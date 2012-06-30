package core.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.jar.JarFile;

public class PluginManager
{
	private HashMap<String, Plugin> listePlugins;

	public PluginManager()
	{
		this.listePlugins = new HashMap<String, Plugin>();
	}

	public void chargerPlugins()
	{
		File dossierPlugins = new File("plugins");
		FilenameFilter filter = new FileListFilter(null, ".jar");// On ne veut que les .jar

		File[] listeFichiers = dossierPlugins.listFiles(filter);

		if(listeFichiers != null)
		{
			for (File fichier : listeFichiers)
			{
				System.out.println(fichier);
				try
				{
					// Chargement du jar en mémoire
					JarFile jar = new JarFile(fichier.getAbsolutePath());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.out.println(dossierPlugins.getName() + " n'est pas un dossier.");
		}
	}

	public HashMap<String, Plugin> getListePlugins()
	{
		return listePlugins;
	}
	public static void main(String args[])
	{
		PluginManager p = new PluginManager();
		p.chargerPlugins();
	}
}

class FileListFilter implements FilenameFilter
{
	private String start;
	private String end;

	public FileListFilter(String start, String end)
	{
		this.start = start;
		this.end = end;
	}

	public boolean accept(File dir, String name)
	{
		boolean accept = true;

		if(start != null)
		{
			accept &= name.startsWith(start);
		}

		if(end != null)
		{
			accept &= name.endsWith(end);
		}
		return accept;
	}
}
