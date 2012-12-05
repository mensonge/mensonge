package mensonge.userinterface;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.core.BaseDeDonnees.LigneEnregistrement;


public class DialogueAjouterEnregistrement extends JDialog implements ActionListener
{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String CREER_CAT = "Creer Catégorie";
	
	private static final String CREER_SUJ = "Creer Sujet";

	private JComboBox comboCategorie = new JComboBox();
	private JLabel labelCategorie = new JLabel("Liste des catégories");

	private JComboBox comboSujet = new JComboBox();
	private JLabel labelSujet = new JLabel("Liste des sujets");

	private JLabel labelNom = new JLabel("Nom : ");
	private JTextField champsNom = new JTextField("                                            ");

	private JButton envoyer = new JButton("Valider");
	private JButton annuler = new JButton("Annuler");

	private byte[] enregistrement;

	private BaseDeDonnees bdd;

	public DialogueAjouterEnregistrement(JFrame parent, String title, boolean modal, BaseDeDonnees bdd,
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
			LinkedList<LigneEnregistrement> liste = bdd.getListeCategorie();
			for(LigneEnregistrement ligne : liste)
			{
				comboCategorie.addItem(ligne.getNomCat());
			}

			liste = bdd.getListeSujet();
			for(LigneEnregistrement ligne : liste)
			{
				comboSujet.addItem(ligne.getNomSuj());
			}
		}
		catch (Exception e)
		{
			GraphicalUserInterface.popupErreur("Absence de sujet ou de categorie dans la base");
		}
		
		comboCategorie.addItem(CREER_CAT);
		comboSujet.addItem(CREER_SUJ);

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

		c.gridx = 0;
		c.gridy++;
		pan.add(envoyer, c);
		c.gridx = 1;
		pan.add(annuler, c);

		this.setContentPane(pan);
		this.setLocationRelativeTo(null);
		this.setTitle("Ajouter un enregistrement");
		this.setSize(350, 300);
		activer();
	}

	public void activer()
	{
		this.setVisible(true);
	}

	public void valider()
	{
		ByteBuffer bb = ByteBuffer.allocate(8);	
		bb.put(enregistrement, 28, 4);
		bb.put(enregistrement, 40, 4);
		bb.rewind();
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int octetParSeconde = bb.getInt();
		int dataSize = bb.getInt();
		int duree = (int)(dataSize/octetParSeconde);
		String nomCat = nomCategorie();
		String nomSuj = nomSujet();
		try
		{
			this.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			int idCat = this.bdd.getCategorie(nomCat);
			int idSuj = this.bdd.getSujet(nomSuj);
			this.bdd.ajouterEnregistrement(champsNom.getText(), duree, idCat, enregistrement, idSuj);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		this.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.setVisible(false);
	}

	public String nomCategorie()
	{
		String nom = (String) comboCategorie.getSelectedItem();
		if(nom.equals(CREER_CAT))
		{
			String name = null;
			while(name == null || name.equals(""))
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
				GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
			}
			nom = name;
		}
		return nom;
	}
	
	public String nomSujet()
	{
		String nom = (String) comboSujet.getSelectedItem();
		if(nom.equals(CREER_SUJ))
		{
			String name = null;
			while(name == null || name.equals(""))
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
				GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
			}
			nom = name;
		}
		return nom;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == this.envoyer)
		{
			this.valider();
		}
		else if(e.getSource() == this.annuler)
		{
			this.setVisible(false);
		}
	}
}
