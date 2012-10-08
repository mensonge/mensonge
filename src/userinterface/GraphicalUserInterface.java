package userinterface;


import core.BaseDeDonnees.BaseDeDonnees;
import core.BaseDeDonnees.DBException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;

import userinterface.OngletLecteur;


import java.io.*;
import sun.audio.*;


public class GraphicalUserInterface extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 5373991180139317820L;

	private JTabbedPane onglets;

	private JMenuItem aideAPropos;
	private JMenuItem fichierFermer;
	private JMenuItem fichierOuvrir;
	private JMenuItem baseExporter;
	private JMenuItem baseImporter;
	private JMenuItem baseAjouterCategorie;

	private ModeleTableau modeleTableau;

	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
	private DefaultMutableTreeNode racine;
	private JTree arbre;
	private JScrollPane scrollPane;
	
	JPopupMenu menuClicDroit = new JPopupMenu();//sers au clic droit

	private BaseDeDonnees bdd = null;

	public GraphicalUserInterface()
	{

		/*
		 * Connexion à la base
		 */
		connexionBase("LieLab.db");
		
		/*
		 * Conteneur
		 */
		onglets = new JTabbedPane();

		/*
		 * Menu
		 */
		fichierFermer = new JMenuItem("Fermer");
		fichierFermer.addActionListener(this);

		fichierOuvrir = new JMenuItem("Ouvrir");
		fichierOuvrir.addActionListener(this);
		
		baseExporter = new JMenuItem("Exporter");
		baseExporter.addMouseListener(new ExporterBaseListner(this));
		
		baseImporter = new JMenuItem("Importer");
		baseImporter.addMouseListener(new ImporterBaseListner(this));
		
		baseAjouterCategorie = new JMenuItem("Ajouter");
		baseAjouterCategorie.addMouseListener(new AjouterCategorieEnregistrementClicDroit());
		
		JMenu menuFichier = new JMenu("Fichier");
		menuFichier.add(fichierOuvrir);
		menuFichier.add(baseExporter);
		menuFichier.add(baseImporter);
		menuFichier.addSeparator();
		menuFichier.add(fichierFermer);

		aideAPropos = new JMenuItem("À propos");
		aideAPropos.addActionListener(this);

		JMenu menuOutils = new JMenu("Outils");

		JMenu menuAide = new JMenu("Aide");
		menuAide.add(aideAPropos);
		
		JMenu menuBase = new JMenu("Base");
		menuBase.add(baseAjouterCategorie);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFichier);
		menuBar.add(menuOutils);
		menuBar.add(menuAide);
		menuBar.add(menuBase);

		/*
		 * Création de l'arbre des enregistrement
		 */
		racine = new DefaultMutableTreeNode("Categorie");
		remplirArbreEnregistrement();
		arbre = new JTree(racine);
		arbre.addMouseListener(new ClicDroit());

		arbre.addTreeSelectionListener(new TreeSelectionListener(){

	         public void valueChanged(TreeSelectionEvent event)
	         {
	        	 
	            if(arbre.getLastSelectedPathComponent() != null && arbre.getLastSelectedPathComponent() instanceof Feuille)
	            {
	               infoArbre.setListeInfo(((Feuille) arbre.getLastSelectedPathComponent()).getInfo());//On informe le panneau d'information
	               infoArbre.repaint();//on le repaint
	               //System.out.println("*************************ACTION***************");
	               //for(int i = 0; i < arbre.getSelectionPaths().length; i++)
	               //{
	            	  // System.out.println(arbre.getSelectionPaths()[i].getLastPathComponent().getClass() + " " + arbre.getSelectionPaths()[i].getLastPathComponent());
	              // }
	            }
	            else
	            {
	            	infoArbre.setListeInfo(null);
	            	infoArbre.repaint();//on le repaint
	            }
	         }
	      });

		scrollPane = new JScrollPane(arbre);
		scrollPane.setPreferredSize(new Dimension(270, 800));
		scrollPane.setAutoscrolls(true);
		
		Box boxLabel = Box.createVerticalBox();
		boxLabel.add(Box.createVerticalStrut(1));
		boxLabel.add(new JLabel("Liste des enregistrements"));
		boxLabel.add(Box.createVerticalStrut(2));
		boxLabel.add(new JSeparator(SwingConstants.HORIZONTAL));
		boxLabel.add(Box.createVerticalStrut(5));

		JPanel panelEnregistrements = new JPanel(new GridLayout(2, 1));
		
		panelEnregistrements.add(scrollPane);
		panelEnregistrements.add(infoArbre);
		
		
		
		/*
		 * Conteneur
		 */
		JPanel conteneur = new JPanel(new BorderLayout());
		//conteneur.add(onglets,BorderLayout.CENTER);
		conteneur.add(panelEnregistrements,BorderLayout.EAST);
		/*
		 * Fenêtre
		 */
		this.setBackground(Color.WHITE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setTitle("LieLab");
		this.setLocationRelativeTo(null);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setContentPane(conteneur);
		this.setJMenuBar(menuBar);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		this.setEnabled(true);

	}

	public void ajouterOnglet(OngletLecteur onglet)
	{
		JButton boutonFermeture = new JButton(new ImageIcon("images/CloseTab.png"));
		boutonFermeture.setToolTipText("Fermer cet onglet");
		boutonFermeture.setContentAreaFilled(false);
		boutonFermeture.setFocusable(false);
		boutonFermeture.setBorder(BorderFactory.createEmptyBorder());
		boutonFermeture.setBorderPainted(false);
		boutonFermeture.addActionListener(new FermetureOngletListener(this.onglets, onglet));

		JPanel panelFermeture = new JPanel();
		panelFermeture.setBackground(new Color(0, 0, 0, 0));
		panelFermeture.add(new JLabel(onglet.getNom()));
		panelFermeture.add(boutonFermeture);

		this.onglets.add(onglet);
		this.onglets.setTabComponentAt(this.onglets.getTabCount() - 1, panelFermeture);
	}

	public void quitter()
	{
		System.exit(0);
	}

	protected void processWindowEvent(WindowEvent event)
	{
		if(event.getID() == WindowEvent.WINDOW_DEACTIVATED)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
		}
		if(event.getID() == WindowEvent.WINDOW_CLOSING)
		{
			this.quitter();
		}
		else
			super.processWindowEvent(event);
	}

	public void updateArbre()
	{
		viderNoeud(racine);
		remplirArbreEnregistrement();
		arbre.updateUI();
	}
	public void remplirArbreEnregistrement()
	{
		ResultSet rs_cat = null, rs_enr = null;
		try
		{
			rs_cat = bdd.getListeCategorie();
			while(rs_cat.next())
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(rs_cat.getString("nomcat"));
				rs_enr = bdd.getListeEnregistrement(rs_cat.getInt("idcat"));
				while(rs_enr.next())
				{
					Feuille f = new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"), rs_enr.getInt("taille"), rs_enr.getString("nomCat"));
					node.add(f);
					
				}
				rs_enr.close();
				racine.add(node);
			}
			rs_cat.close();
			
		} 
		catch(Exception e)
		{
			popupErreur("Erreur lors du chargement des enregistrement.", "Erreur");
		}
	}
	public void connexionBase(String fichier)
	{
		try
		{
			bdd = new BaseDeDonnees(fichier);
			bdd.connexion();//connexion et verification de la validite de la table
		}
		catch(DBException e)
		{
			int a = e.getCode();
			if(a == 2)
			{
				//popupInfo("Base en cour de creation ...");
				try
				{
					bdd.createDatabase();
				} 
				catch (DBException e1)
				{
					popupErreur("[-] Erreur lors de la creation: " + e1.getMessage(), "Erreur");
				}
				//creation de la base
				//System.out.println("[i]Base cree.");
			}
			else
			{
				popupErreur("[-]Erreur lors de la connexion. " + e.getMessage(), "Erreur");
				return;
			}
		}
	}
	

	public void viderNoeud (DefaultMutableTreeNode selectednode)
	{
        int nbchildren=selectednode.getChildCount();
        
        for (int i=0; i < nbchildren; i++)
        {
            if (selectednode.getChildAt(0).isLeaf())
            {
            	((DefaultMutableTreeNode)selectednode.getChildAt(0)).removeFromParent();
            }
            else
            {
                viderNoeud((DefaultMutableTreeNode)selectednode.getChildAt(0));
            }
        }
        if (selectednode.isRoot()==false) 
        {
        	selectednode.removeFromParent();
        }
    }
	/**
	 * Affiche une popup qui signale une erreur
	 *
	 * @param message
	 *            Le message d'erreur à afficher
	 * @param title
	 *            Le titre de la popup
	 */
 	public static void popupErreur(String message, String title)
	{
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}
 	public static void popupInfo(String message, String title)
	{
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == fichierFermer)
		{
			this.quitter();
		}
		else if (event.getSource() == aideAPropos)
		{
			JOptionPane.showMessageDialog(null, "Projet de détection de mensonge", "À propos",
					JOptionPane.PLAIN_MESSAGE);
		}
		else if (event.getSource() == fichierOuvrir)
		{
			JFileChooser fileChooser = new JFileChooser();
			IContainer containerInput = IContainer.make();
			fileChooser.showOpenDialog(this);
			if (fileChooser.getSelectedFile() != null)
			{
				try
				{
					if (containerInput.open(fileChooser.getSelectedFile().getCanonicalPath(), IContainer.Type.READ, null) < 0)
					{
						throw new Exception("Impossible d'ouvrir ce fichier, format non géré.");
					}
					else
					{
						try
						{
							containerInput.close();
							this.ajouterOnglet(new OngletLecteur(new File(fileChooser.getSelectedFile().getCanonicalPath())));
						}
						catch (IOException e)
						{
							popupErreur(e.getMessage(), "Erreur");
						}
					}
				}
				catch (Exception e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}
	
	
	class FermetureOngletListener implements ActionListener
	{
		private JTabbedPane onglets;
		private OngletLecteur onglet;
		
		public FermetureOngletListener(JTabbedPane onglets, OngletLecteur onglet)
		{
			this.onglet = onglet;
			this.onglets = onglets;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			onglet.fermerOnglet();
			onglets.remove(onglet);
			
		}	
	}
	
	class ClicDroit implements MouseListener
	{
		
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0){	}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent e)
		{
			if((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			{
				
				menuClicDroit.setEnabled(false) ;
				menuClicDroit.setVisible(false) ;
				menuClicDroit = new JPopupMenu() ;
	            JMenuItem exporter = new JMenuItem("Exporter") ;
	            JMenuItem renommer = new JMenuItem("Renommer");
	            //JMenuItem modifier = new JMenuItem("Modifier ...");
	            JMenuItem ecouter = new JMenuItem("Ecouter") ;
	            JMenuItem modifiercate = new JMenuItem("Changer categorie");
	            JMenuItem supprimer = new JMenuItem("Supprimer les enregistrements");
	            JMenuItem ajouter = new JMenuItem("Ajouter Categorie");
	            JMenuItem supprimerCategorie = new JMenuItem("Supprimer Categorie");
	            
	            exporter.addMouseListener(new ExporterEnregistrementClicDroit());
	            renommer.addMouseListener(new RenommerEnregistrementClicDroit());
	            ajouter.addMouseListener(new AjouterCategorieEnregistrementClicDroit());
	            modifiercate.addMouseListener(new ModifierCategorieEnregistrementClicDroit());
	            supprimer.addMouseListener(new SupprimerEnregistrementClicDroit());
	            supprimerCategorie.addMouseListener(new SupprimerCategorieEnregistrementClicDroit());

	            if(arbre.getSelectionPaths() != null)
	            {
	            	if(arbre.getSelectionPaths().length == 1)
		            {
	            		if(arbre.getLastSelectedPathComponent() instanceof Feuille)//Si c'est une feuille
	            		{
	            			menuClicDroit.add(exporter);
	            			menuClicDroit.add(ecouter);
	            			//menuClicDroit.add(modifier);
	            		}
	            		menuClicDroit.add(renommer);//commun au categorie et au feuille	            	
		            }
		            if(arbre.getSelectionPaths().length >= 1)
		            {
		            	if(arbre.getLastSelectedPathComponent() instanceof Feuille)
		            	{
		            		menuClicDroit.add(modifiercate) ;
		            	}
		            	menuClicDroit.add(supprimer) ;
		            }
	            }
	            menuClicDroit.add(ajouter);
	            if(arbre.getSelectionPaths() != null)
	            {
	            	menuClicDroit.add(supprimerCategorie);
	            }
	            
	            
	            menuClicDroit.setEnabled(true) ;
	            menuClicDroit.setVisible(true) ;
	           
	            menuClicDroit.show(arbre.getComponentAt(e.getXOnScreen(), e.getYOnScreen()), e.getXOnScreen(),e.getYOnScreen()); 
			}
			else
			{
				menuClicDroit.setEnabled(false) ;
				menuClicDroit.setVisible(false) ;
			}
		}

		public void mouseReleased(MouseEvent arg0) {}
		
	}
	class SupprimerEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			int option = JOptionPane.showConfirmDialog(null, 
	                  "Voulez-vous supprimer les enregistrements ?\n(Notez que les categories seront concervées)",
	                  "Suppression", 
	                  JOptionPane.YES_NO_CANCEL_OPTION, 
	                  JOptionPane.QUESTION_MESSAGE);
			if(option == JOptionPane.OK_OPTION)
			{
				for(int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						bdd.supprimerEnregistrement(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId());
					}
				}
			}
			updateArbre();
		}
		
	}
	class ExporterEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(null);
			String fichier;
			if (fileChooser.getSelectedFile() != null)
			{
				try
				{
					fichier = fileChooser.getSelectedFile().getCanonicalPath();
					int id = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
					bdd.exporter(fichier, id, 2);
				}
				catch (Exception e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
					return;
				}
			}
		}
	}
	class RenommerEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			String nom = JOptionPane.showInputDialog(null, "Entre le nouveau nom", "Renommer", JOptionPane.QUESTION_MESSAGE);
			if(nom != null && ! nom.equals(""))
			{
				try
				{
					if(arbre.getLastSelectedPathComponent() instanceof Feuille)//renommer enregistrement
					{
						bdd.modifierEnregistrementNom(((Feuille) arbre.getLastSelectedPathComponent()).getId(), nom);
					}
					else if(arbre.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode)//renommer une categorie
					{
						bdd.modifierCategorie(bdd.getCategorie(arbre.getSelectionPaths()[0].getLastPathComponent().toString()), nom);
					}
				}
				catch (DBException e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
			updateArbre();
		}
	}
	class AjouterCategorieEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			String nom = JOptionPane.showInputDialog(null, "Entrez le nom de la nouvelle catégorie", "Renommer", JOptionPane.QUESTION_MESSAGE);
			if(nom != null && ! nom.equals(""))
			{
				try
				{
					bdd.ajouterCategorie(nom);
				}
				catch (DBException e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
			updateArbre();
		}
	}
	class ModifierCategorieEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			DialogueNouvelleCategorie pop = new DialogueNouvelleCategorie(null, null, true, bdd);
			String nom = ((String)pop.activer()[0]);
			if( ! nom.equals("Ne rien changer"))
			{
				for(int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						try
						{
							bdd.modifierEnregistrementCategorie(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId(), nom);
						}
						catch (DBException e1)
						{
							popupErreur(e1.getMessage(), "Erreur");
						}
					}
				}
				updateArbre();
			}
		}
	}
	class SupprimerCategorieEnregistrementClicDroit implements MouseListener
	{
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e)
		{  	
			menuClicDroit.setEnabled(false) ;
			menuClicDroit.setVisible(false) ;
			int option = JOptionPane.showConfirmDialog(null, 
	                  "Voulez-vous supprimer les categories ?\n",
	                  "Suppression", 
	                  JOptionPane.YES_NO_CANCEL_OPTION, 
	                  JOptionPane.QUESTION_MESSAGE);
			if(option == JOptionPane.OK_OPTION)
			{
				for(int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					try
					{
						if( ! (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille))
						{
							ResultSet rs = bdd.getListeEnregistrement(bdd.getCategorie(arbre.getSelectionPaths()[i].getLastPathComponent().toString()));
							if(rs.next())
							{
								popupErreur("Une categorie peut être supprimée quand elle n'a plus d'enregistrements.", "Erreur");
							}
							else
							{
								bdd.supprimerCategorie(bdd.getCategorie(arbre.getSelectionPaths()[i].getLastPathComponent().toString()));
							}
							rs.close();
						}
					}
					catch (Exception e1)
					{
						popupErreur(e1.getMessage(), "Erreur");
					}
				}
			}
			updateArbre();
		}
	}
	
	class ExporterBaseListner extends MouseAdapter
	{
		GraphicalUserInterface fenetre;
		public ExporterBaseListner(GraphicalUserInterface g)
		{
			fenetre = g;
		}
		public void mouseReleased(MouseEvent event)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(fenetre);
			if (fileChooser.getSelectedFile() != null)
			{
				try
				{
					bdd.exporter(fileChooser.getSelectedFile().getCanonicalPath(), -1, 1);
				}
				catch (Exception e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}
	class ImporterBaseListner extends MouseAdapter
	{
		GraphicalUserInterface fenetre;
		public ImporterBaseListner(GraphicalUserInterface g)
		{
			fenetre = g;
		}
		public void mouseReleased(MouseEvent event)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(fenetre);
			String fichier;
			if (fileChooser.getSelectedFile() != null)
			{
				try
				{
					fichier = fileChooser.getSelectedFile().getCanonicalPath();
					bdd.importer(fichier);
					updateArbre();
				}
				catch (Exception e1)
				{
					popupErreur(e1.getMessage(), "Erreur");
					return;
				}
			}
		}
	}
	public static void main(String args[])
	{
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new GraphicalUserInterface();
			}
	});
}

}

