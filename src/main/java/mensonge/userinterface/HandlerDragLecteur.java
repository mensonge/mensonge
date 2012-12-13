package mensonge.userinterface;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;

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
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return false;
		}
		return true;
	}

	public boolean importData(TransferHandler.TransferSupport support)
	{

		Transferable data = support.getTransferable();
		String str = "";
		try
		{
			str = (String) data.getTransferData(DataFlavor.stringFlavor);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		String[] tab = str.split("file://");
		for (int i = 0; i < tab.length; i++)
		{
			tab[i] = tab[i].trim();
			if (!tab[i].equals(""))
			{
				tab[i] = tab[i].replaceAll("\\%20", " ");
				File f = new File(tab[i]);
				if (f.canRead() && f.exists())
				{
					this.fenetre.ajouterOnglet(new OngletLecteur(f, this.bdd, this.fenetre));
				}
			}
		}

		return false;
	}

	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
}
