package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import mensonge.core.BaseDeDonnees.BaseDeDonneesControlleur;
import mensonge.core.BaseDeDonnees.BaseDeDonneesModele;
import mensonge.core.BaseDeDonnees.DBException;

public class AjouterCategorieListener extends MouseAdapter
{
	private JPopupMenu menuClicDroit;
	private BaseDeDonneesControlleur bdd;

	public AjouterCategorieListener(JPopupMenu menuClicDroit, BaseDeDonneesControlleur bdd)
	{
		this.bdd = bdd;
		this.menuClicDroit = menuClicDroit;
	}
	
	public AjouterCategorieListener(BaseDeDonneesControlleur bdd)
	{
		this.bdd = bdd;
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (menuClicDroit != null)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
		}

		String nom = JOptionPane.showInputDialog(null, "Entrez le nom de la nouvelle catégorie", "Renommer",
				JOptionPane.QUESTION_MESSAGE);
		if (nom != null && !nom.isEmpty())
		{
			try
			{
				bdd.ajouterCategorie(nom);
			}
			catch (DBException e1)
			{
				GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
			}
		}
	}
}