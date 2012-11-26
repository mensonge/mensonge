package mensonge.core.plugins;

import java.io.File;
import java.util.List;

import mensonge.core.IExtraction;

/**
 * Interface représentant un plugin
 */
public interface Plugin
{
	/**
	 * Lance le plugin
	 * @param extraction Instance de la classe d'extraction utilisé par le plugin
	 * @param listeFichiersSelectionnes Ensemble des fichiers selectionnés qui seront traités par le plugin
	 */
	void lancer(IExtraction extraction, List<File> listeFichiersSelectionnes);
	
	/**
	 * Arrête le plugin
	 */
	void stopper();
	
	/**
	 * Récupère le nom du plugin
	 * @return Nom du plugin
	 */
	String getNom();
	/**
	 * Détermine si un plugin est lancé ou non
	 * @return Vrai si le plugin est lancé, faux sinon
	 */
	boolean isActive();
}
