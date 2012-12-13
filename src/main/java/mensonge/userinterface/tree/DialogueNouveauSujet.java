package mensonge.userinterface.tree;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.core.BaseDeDonnees.LigneEnregistrement;
import mensonge.userinterface.GraphicalUserInterface;

public final class DialogueNouveauSujet extends JDialog
{
	private static final long serialVersionUID = 1L;

	private JComboBox comboBox = new JComboBox();
	private JLabel label = new JLabel("Liste des catégories");

	private JButton buttonValidate = new JButton("Valider");

	private Object[] retour = new Object[1];

	public DialogueNouveauSujet(JFrame parent, String title, boolean modal, BaseDeDonnees bdd)
	{
		super(parent, title, modal);
		JPanel pan = new JPanel(), j1 = new JPanel(), bouton = new JPanel();
		try
		{
			retour[0] = "Ne rien changer";
			comboBox.addItem(retour[0]);
			List<LigneEnregistrement> liste = bdd.getListeSujet();
			for (LigneEnregistrement ligne : liste)
			{
				comboBox.addItem(ligne.getNomSuj());
			}
		}
		catch (DBException e)
		{
			GraphicalUserInterface.popupErreur(e.getLocalizedMessage());
		}
		buttonValidate.addMouseListener(new ValiderListener());
		comboBox.addItemListener(new ComboBoxListener());

		j1.add(label);
		j1.add(comboBox);

		bouton.add(buttonValidate);

		pan.add(j1);
		pan.add(buttonValidate);

		this.setContentPane(pan);
		this.setLocationRelativeTo(null);
		this.setTitle("Changer de sujet");
		this.setSize(350, 120);
	}

	public Object[] activer()
	{
		this.setVisible(true);
		return retour;
	}

	private class ComboBoxListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			retour[0] = e.getItem().toString();
		}
	}

	private class ValiderListener extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			setVisible(false);
		}
	}
}
