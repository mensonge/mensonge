package userinterface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import core.BaseDeDonnees.BaseDeDonnees;
import core.BaseDeDonnees.DBException;

public class DialogueAjouterEnregistrement extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox comboCategorie = new JComboBox();
	private JLabel labelCategorie = new JLabel("Liste des categories");
	
	private JComboBox comboSujet = new JComboBox();
	private JLabel labelSujet = new JLabel("Liste des Sujets");
	
	private JLabel labelNom = new JLabel("Nom : ");
	private JTextField champsNom = new JTextField("                                            ");
	
	private JButton envoyer = new JButton("Valider");
	private JButton annuler = new JButton("Annuler");
	private RetourAjoutEnregistrement retour = null;
	
	private byte[] enregistrement;
	
	private BaseDeDonnees bdd; 
	
	public DialogueAjouterEnregistrement(JFrame parent, String title, boolean modal, BaseDeDonnees bdd, byte[] enregistrement)
	{
		super(parent, title, modal);
		this.enregistrement = enregistrement;
		this.bdd = bdd;
		
		JPanel pan = new JPanel(), p1 = new JPanel(), p2 = new JPanel(), p3 = new JPanel(), p4 = new JPanel();
		pan.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		this.annuler.addMouseListener(new BouttonAnnulerListener());
		
		
		try
		{
			ResultSet rs = bdd.getListeCategorie();
			while(rs.next())
			{
				comboCategorie.addItem(rs.getString("nomCat"));
			}
			rs.close();
			
			rs = bdd.getListeSujet();
			while(rs.next())
			{
				comboSujet.addItem(rs.getString("nomSuj"));
			}
			rs.close();
		}
		catch (Exception e)
		{
			
		}
		
		c.gridx = 0;
		c.gridy = 0;
		pan.add(labelNom, c);
		c.gridx = 1;
		pan.add(champsNom, c);
		
		c.gridx = 0;
		c.gridy ++;
		pan.add(labelSujet, c);
		c.gridx = 1;
		pan.add(comboSujet, c);
		
		c.gridx = 0;
		c.gridy ++;
		pan.add(labelCategorie, c);
		c.gridx = 1;
		pan.add(comboCategorie, c);
		
		c.gridx = 0;
		c.gridy ++;
		pan.add(envoyer, c);
		c.gridx = 1;
		pan.add(annuler, c);
		
		this.setContentPane(pan);
		this.setLocationRelativeTo(null);
		this.setTitle("Ajouter un enregistrement");
		this.setSize(350, 300);
	}

	public RetourAjoutEnregistrement activer()
	{
		this.setVisible(true);
		return retour;
	}
	public class BouttonAnnulerListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{
			retour = null;
			setVisible(false);
		}
	}
	public class BouttonValiderListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{
			retour = new RetourAjoutEnregistrement(champsNom.getText(), (String)comboCategorie.getSelectedItem(), (String)comboSujet.getSelectedItem(), 10, enregistrement);
			
			int duree = 10;
			
			
			try
			{
				int idCat = bdd.getCategorie((String)comboCategorie.getSelectedItem());
				int idSuj = bdd.getSujet((String)comboSujet.getSelectedItem());
				bdd.ajouterEnregistrement(champsNom.getText(), duree, idCat, enregistrement, idSuj);
			}
			catch (Exception e1)
			{
				
			}
			
			setVisible(false);
		}
	}
}
