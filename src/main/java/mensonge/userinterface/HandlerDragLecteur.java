package mensonge.userinterface;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import mensonge.core.Extraction;
import mensonge.core.BaseDeDonnees.BaseDeDonneesModele;

public class HandlerDragLecteur extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("dragLecteur");
	private GraphicalUserInterface fenetre;
	private BaseDeDonneesModele bdd;
	private Extraction extraction;

	public HandlerDragLecteur(GraphicalUserInterface fenetre, BaseDeDonneesModele bdd, Extraction extraction)
	{
		this.extraction = extraction;
		this.bdd = bdd;
		this.fenetre = fenetre;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
				|| info.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support)
	{
		if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			return importDataList(support);
		}
		else if (support.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return importDataString(support);
		}
		return false;
	}

	public boolean importDataList(TransferHandler.TransferSupport support)
	{
		Transferable data = support.getTransferable();
		List<File> liste = null;

		try
		{
			liste = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
		}
		catch (UnsupportedFlavorException e)
		{
			LOGGER.log(Level.WARNING, e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			LOGGER.log(Level.WARNING, e.getLocalizedMessage());
		}

		for (File fichier : liste)
		{
			if (fichier.canRead() && fichier.exists())
			{
				this.fenetre.ajouterOnglet(fichier, this.bdd, this.fenetre, this.extraction);
			}
		}
		return false;
	}

	public boolean importDataString(TransferHandler.TransferSupport support)
	{
		Transferable data = support.getTransferable();
		String str = "";
		try
		{
			str = (String) data.getTransferData(DataFlavor.stringFlavor);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getLocalizedMessage());
		}
		String[] tab = str.split("file://");
		for (int i = 0; i < tab.length; i++)
		{
			tab[i] = tab[i].trim();
			if (!tab[i].equals(""))
			{
				tab[i] = tab[i].replaceAll("\\%20", " ");
				File fichier = new File(tab[i]);
				if (fichier.canRead() && fichier.exists())
				{
					this.fenetre.ajouterOnglet(fichier, this.bdd, this.fenetre, this.extraction);
				}
			}
		}

		return false;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
}
