package mensonge.userinterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mensonge.core.BaseDeDonnees.BaseDeDonneesControlleur;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.core.BaseDeDonnees.LigneEnregistrement;

public final class DialogueAjouterEnregistrement extends JDialog implements ActionListener, KeyListener
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final String CREER_CAT = "Creer catégorie";

	private static final String CREER_SUJ = "Creer sujet";

	private JComboBox comboCategorie = new JComboBox();
	private JLabel labelCategorie = new JLabel("Catégorie");

	private JComboBox comboSujet = new JComboBox();
	private JLabel labelSujet = new JLabel("Sujet");

	private JLabel labelNom = new JLabel("Nom");
	private JTextField champsNom = new JTextField();

	private JButton envoyer = new JButton("Valider");
	private JButton annuler = new JButton("Annuler");

	private byte[] enregistrement;

	private BaseDeDonneesControlleur bdd;

	public DialogueAjouterEnregistrement(JFrame parent, String title, boolean modal, BaseDeDonneesControlleur bdd,
			byte[] enregistrement)
	{
		super(parent, title, modal);
		this.enregistrement = enregistrement;
		this.bdd = bdd;
		JPanel pan = new JPanel();
		pan.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		this.envoyer.addActionListener(this);
		this.annuler.addActionListener(this);

		try
		{
			List<LigneEnregistrement> liste = bdd.getListeCategorie();
			for (LigneEnregistrement ligne : liste)
			{
				comboCategorie.addItem(ligne.getNomCat());
			}

			liste = bdd.getListeSujet();
			for (LigneEnregistrement ligne : liste)
			{
				comboSujet.addItem(ligne.getNomSuj());
			}
		}
		catch (DBException e)
		{
			GraphicalUserInterface.popupErreur(e.getLocalizedMessage());
		}

		comboCategorie.addItem(CREER_CAT);
		comboSujet.addItem(CREER_SUJ);
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(4, 10, 0, 10);
		c.gridx = 0;
		c.gridy = 0;
		pan.add(labelNom, c);
		c.gridx = 1;
		pan.add(champsNom, c);

		c.gridx = 0;
		c.gridy++;
		pan.add(labelSujet, c);
		c.gridx = 1;
		pan.add(comboSujet, c);

		c.gridx = 0;
		c.gridy++;
		pan.add(labelCategorie, c);
		c.gridx = 1;
		pan.add(comboCategorie, c);

		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy++;
		pan.add(envoyer, c);
		c.gridx = 1;
		pan.add(annuler, c);

		this.champsNom.addKeyListener(this);
		this.setContentPane(pan);
		this.setLocationRelativeTo(null);
		this.setTitle("Ajouter un enregistrement");
		this.pack();
		activer();
	}

	public void activer()
	{
		this.setVisible(true);
	}

	public void valider()
	{
		if (!champsNom.getText().isEmpty())
		{
			this.setVisible(false);
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					ByteBuffer bb = ByteBuffer.allocate(8);
					bb.put(enregistrement, 28, 4);
					bb.put(enregistrement, 40, 4);
					bb.rewind();
					bb.order(ByteOrder.LITTLE_ENDIAN);
					int octetParSeconde = bb.getInt();
					int dataSize = bb.getInt();
					int duree = (int) (dataSize / octetParSeconde);
					String nomCat = nomCategorie();
					String nomSuj = nomSujet();
					try
					{
						int idCat = bdd.getCategorie(nomCat);
						int idSuj = bdd.getSujet(nomSuj);
						bdd.ajouterEnregistrement(champsNom.getText(), duree, idCat, enregistrement, idSuj);
					}
					catch (DBException e)
					{
						GraphicalUserInterface.popupErreur(e.getLocalizedMessage());
					}
				}
			}).start();
		}
		else
		{
			champsNom.requestFocus();
		}
	}

	public String nomCategorie()
	{
		String nom = (String) comboCategorie.getSelectedItem();
		if (nom.equals(CREER_CAT))
		{
			String name = null;
			while (name == null || name.equals(""))
			{
				name = JOptionPane.showInputDialog(null, "Entrez le nom de la nouvelle categorie", "Ajout",
						JOptionPane.QUESTION_MESSAGE);
			}
			try
			{
				bdd.ajouterCategorie(name);
			}
			catch (DBException e1)
			{
				GraphicalUserInterface.popupErreur(e1.getMessage());
			}
			nom = name;
		}
		return nom;
	}

	public String nomSujet()
	{
		String nom = (String) comboSujet.getSelectedItem();
		if (nom.equals(CREER_SUJ))
		{
			String name = null;
			while (name == null || name.equals(""))
			{
				name = JOptionPane.showInputDialog(null, "Entrez le nom du nouveau sujet", "Ajout",
						JOptionPane.QUESTION_MESSAGE);
			}
			try
			{
				bdd.ajouterSujet(name);
			}
			catch (DBException e1)
			{
				GraphicalUserInterface.popupErreur(e1.getMessage());
			}
			nom = name;
		}
		return nom;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.envoyer)
		{
			this.valider();
		}
		else if (e.getSource() == this.annuler)
		{
			this.setVisible(false);
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			this.valider();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}
}
