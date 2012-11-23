package mensonge.core.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
	private static Logger logger = Logger.getLogger("logger");
	private HashMap<String, Plugin> listePlugins;

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
	public void chargerPlugins() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
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
				loader = new URLClassLoader(new URL[] { fichier.toURI().toURL() });
				jar = new JarFile(fichier.getAbsolutePath());
				enumeration = jar.entries();
				while (enumeration.hasMoreElements())
				{
					tmp = enumeration.nextElement().toString();// Le chemin du fichier
					if (tmp.length() > 6 && tmp.substring(tmp.length() - 6).compareTo(".class") == 0)
					{
						tmp = tmp.substring(0, tmp.length() - 6);// on supprime le .class
						tmp = tmp.replaceAll(File.separator, ".");// On remplace les / par des .
						/*
						 * Exemple :core/plugins/PluginManager.class deviendra core.plugins.PluginManager
						 * Comme le spécifie la convention de Java
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
				// Cela signifie qu'un Jar peut comporter plusieurs plugins !
			}
		}
		else
		{
			logger.log(Level.WARNING,dossierPlugins.getName() + " n'est pas un dossier.");
		}
	}

	/**
	 * Récupère la liste des plugins sous la forme d'une hashMap
	 * 
	 * @return HashMap contenant la liste des plugins
	 */
	public HashMap<String, Plugin> getListePlugins()
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
			p.chargerPlugins();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, Plugin> h = p.getListePlugins();
		if (h.containsKey("Coefficients cepstraux"))
		{
			System.out.println("[i] Lançement du plugin CoefficientsCepstraux");
			h.get("Coefficients cepstraux").lancer(new Extraction(), fichiers);
			System.out.println("[i] Fin du plugin CoefficientsCepstraux");
		}
		else
			System.out.println("[E] Pas de plugin CoefficientsCepstraux");
	}

	/**
	 * Classe interne permettant de filter les noms des fichiers
	 * 
	 */
	private class FileListFilter implements FilenameFilter
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