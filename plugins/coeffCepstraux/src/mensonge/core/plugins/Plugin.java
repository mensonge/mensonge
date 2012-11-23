package mensonge.core.plugins;

import java.io.File;
import java.util.ArrayList;

import mensonge.core.IExtraction;

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
