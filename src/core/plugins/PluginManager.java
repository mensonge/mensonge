package plugins;

import java.util.HashMap;

public class PluginManager
{
	private HashMap<String,Plugin> listePlugins;
	
	public PluginManager()
	{
		this.listePlugins = new HashMap<String, Plugin>();
	}
	public void chargerPlugins()
	{

	}
	public HashMap<String,Plugin> getListePlugins()
	{
		return listePlugins;
	}
}
