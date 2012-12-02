package mensonge.core.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensonge.core.Extraction;

/**
 * Gestionnaire des plugins
 */
public class PluginManager
{
	private static final String PLUGINS_PATH = "plugins";
	private static final String CLASS_EXTENSION = ".class";

	private static Logger logger = Logger.getLogger("logger");
	private Map<String, Plugin> listePlugins;

	/**
	 * Initialise une liste de plugins
	 */
	public PluginManager()
	{
		this.listePlugins = new HashMap<String, Plugin>();
	}

	/**
	 * Charge la liste des plugins dans le dossier PLUGINS_PATH
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void loadPlugins() throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		this.listePlugins.clear();
		File dossierPlugins = new File(PLUGINS_PATH);
		FilenameFilter filter = new FileListFilter(null, ".jar");// On ne veut que les .jar

		File[] listeFichiers = dossierPlugins.listFiles(filter);

		if (listeFichiers != null)
		{
			String tmp = "";
			Enumeration<JarEntry> enumeration;
			JarFile jar = null;
			URLClassLoader loader = null;
			Class tmpClass = null;

			for (File fichier : listeFichiers)
			{
				// Le jar n'est pas dans le classpath on doit donc créer un loader pour pouvoir lui dire où aller
				// chercher les classes
				final URL urlFichier = fichier.toURI().toURL();
				loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
				{
					@Override
					public URLClassLoader run()
					{
						return new URLClassLoader(new URL[] { urlFichier });
					}
				});
				jar = new JarFile(fichier.getAbsolutePath());
				enumeration = jar.entries();
				while (enumeration.hasMoreElements())
				{
					tmp = enumeration.nextElement().toString();// Le chemin du fichier
					if (tmp.length() > CLASS_EXTENSION.length()
							&& tmp.substring(tmp.length() - CLASS_EXTENSION.length()).compareTo(CLASS_EXTENSION) == 0)
					{
						tmp = tmp.substring(0, tmp.length() - CLASS_EXTENSION.length());// on supprime le .class
						tmp = tmp.replace("/", ".");// On remplace les / par des .
						/*
						 * Exemple :core/plugins/PluginManager.class deviendra core.plugins.PluginManager Comme le
						 * spécifie la convention de Java
						 */

						tmpClass = Class.forName(tmp, true, loader);

						for (Class inter : tmpClass.getInterfaces())
						{
							// Si la classe implémente l'interface Plugin on l'instance et on l'ajoute
							if (inter.getName().toString().equals("mensonge.core.plugins.Plugin"))
							{
								Plugin plugin = (Plugin) tmpClass.newInstance();
								this.listePlugins.put(plugin.getNom(), plugin);
							}
						}

					}
				}
				jar.close();
				// Cela signifie qu'un Jar peut comporter plusieurs plugins !
			}
		}
		else
		{
			logger.log(Level.WARNING, dossierPlugins.getName() + " n'est pas un dossier.");
		}
	}
	
	/**
	 * Stop tous les plugins
	 */
	public void unloadPlugins()
	{
		for(Plugin plugin : listePlugins.values())
		{
			plugin.stopper();
		}
	}

	/**
	 * Récupère les plugins sous la forme d'une Map
	 * 
	 * @return Map contenant la liste des plugins
	 */
	public Map<String, Plugin> getPlugins()
	{
		return listePlugins;
	}

	public static void main(String args[])
	{
		ArrayList<File> fichiers = new ArrayList<File>();
		fichiers.add(new File("sons/test.wav"));
		PluginManager p = new PluginManager();
		try
		{
			p.loadPlugins();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		catch (ClassNotFoundException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		catch (InstantiationException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		Map<String, Plugin> h = p.getPlugins();
		if (h.containsKey("Coefficients cepstraux"))
		{
			logger.log(Level.INFO, "[i] Lançement du plugin CoefficientsCepstraux");
			h.get("Coefficients cepstraux").lancer(new Extraction(), fichiers);
			logger.log(Level.INFO, "[i] Fin du plugin CoefficientsCepstraux");
		}
		else
		{
			logger.log(Level.WARNING, "[E] Pas de plugin CoefficientsCepstraux");

		}
	}

	/**
	 * Classe interne permettant de filter les noms des fichiers
	 * 
	 */
	private static class FileListFilter implements FilenameFilter
	{
		private String start;
		private String end;

		/**
		 * Initialise le filtre
		 * 
		 * @param start Ce par quoi doit commencer le nom du fichier
		 * @param end Ce par quoi doit terminer le nom du fichier
		 */
		public FileListFilter(String start, String end)
		{
			this.start = start;
			this.end = end;
		}

		@Override
		public boolean accept(File dir, String name)
		{
			boolean accept = true;

			if (start != null)
			{
				accept &= name.startsWith(start);
			}

			if (end != null)
			{
				accept &= name.endsWith(end);
			}
			return accept;
		}
	}
}