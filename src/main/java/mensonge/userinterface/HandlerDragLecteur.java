package mensonge.userinterface;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;

public class HandlerDragLecteur extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private GraphicalUserInterface fenetre;
	private BaseDeDonnees bdd;

	public HandlerDragLecteur(GraphicalUserInterface fenetre, BaseDeDonnees bdd)
	{
		this.bdd = bdd;
		this.fenetre = fenetre;
	}

	public boolean canImport(TransferHandler.TransferSupport info)
	{
		if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			return false;
		}
		return true;
	}

	public boolean importData(TransferHandler.TransferSupport support)
	{

		Transferable data = support.getTransferable();
		List<File> liste = null;
		try
		{
			liste  = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		for(File fichier : liste)
		{
			if (fichier.canRead() && fichier.exists())
			{
				this.fenetre.ajouterOnglet(new OngletLecteur(fichier, this.bdd, this.fenetre));
			}
		}
		return false;
	}

	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
}
