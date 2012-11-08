package coeffCepstraux.core.plugins;

import java.io.File;
import java.util.ArrayList;

import coeffCepstraux.core.IExtraction;

public interface Plugin
{
	public void lancer(IExtraction extraction, ArrayList<File> listeFichiersSelectionnes);
	public void stopper();
	public String getNom();
	public boolean isActive();
}
