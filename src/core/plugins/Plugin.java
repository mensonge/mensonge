package core.plugins;

import java.io.File;
import java.util.ArrayList;

public interface Plugin
{
	public void lancer(ArrayList<File> listeFichiersSelectionnes);
	public void stopper();
	public String getNom();
	public boolean isActive();
}
