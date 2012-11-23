package mensonge.core.plugins;

import java.io.File;
import java.util.List;

import mensonge.core.IExtraction;

/**
 * Interface représentant un plugin
 */
public interface Plugin
{
	void lancer(IExtraction extraction, List<File> listeFichiersSelectionnes);
	void stopper();
	String getNom();
	boolean isActive();
}
