package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import mensonge.core.BaseDeDonnees.BaseDeDonneesControlleur;
import mensonge.core.BaseDeDonnees.DBException;

public class AjouterSujetListener extends MouseAdapter
{
	private JPopupMenu menuClicDroit;
	private BaseDeDonneesControlleur bdd;

	public AjouterSujetListener(JPopupMenu menuClicDroit, BaseDeDonneesControlleur bdd)
	{
		this.bdd = bdd;
		this.menuClicDroit = menuClicDroit;
	}

	public AjouterSujetListener(BaseDeDonneesControlleur bdd)
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
		String option = JOptionPane.showInputDialog("Nouveau sujet");
		if (option != null && !option.isEmpty())
		{
			try
			{
				this.bdd.ajouterSujet(option);
			}
			catch (DBException e1)
			{
				GraphicalUserInterface.popupErreur("Erreur lors de l'ajout du sujet " + option + " " + e1.getMessage(),
						"Erreur");
			}
		}
	}
}