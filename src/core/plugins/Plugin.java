package core.plugins;

import core.IExtraction;
import java.io.File;
import java.util.ArrayList;

/**
 * Interface représentant un plugin
 */
public interface Plugin
{
	public void lancer(IExtraction extraction, ArrayList<File> listeFichiersSelectionnes);
	public void stopper();
	public String getNom();
	public boolean isActive();
}
