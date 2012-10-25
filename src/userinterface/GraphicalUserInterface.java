package userinterface;


import core.BaseDeDonnees.BaseDeDonnees;
import core.BaseDeDonnees.DBException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import java.io.FileOutputStream;
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
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
	
	//--------------------------------------------------------------------------------	
	//private JPopupMenu menuClicDroit = new JPopupMenu();//sers au clic droit
	//--------------------------------------------------------------------------------
	
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
		
		baseAjouterCategorie = new JMenuItem("Ajouter catégorie");
		//baseAjouterCategorie.addMouseListener(new AjouterCategorieEnregistrementClicDroit());
		
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
		 * Conteneur
		 */
		JPanel conteneur = new JPanel(new BorderLayout());
		conteneur.add(onglets,BorderLayout.CENTER);
		conteneur.add(new PanneauArbre(bdd),BorderLayout.EAST);
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
		File f = new File("tmp.wav");//on supprime le fichier temporaire
		if(f.exists())
		{
			/*
			if(lecteurSonArbre != null)
			{
				lecteurSonArbre.stop();//Stoppe le thread du son
			}
			*/
			f.delete();
		}
		System.exit(0);
	}

	protected void processWindowEvent(WindowEvent event)
	{
		if(event.getID() == WindowEvent.WINDOW_DEACTIVATED)
		{
			//menuClicDroit.setEnabled(false);
			//menuClicDroit.setVisible(false);
		}
		if(event.getID() == WindowEvent.WINDOW_CLOSING)
		{
			this.quitter();
		}
		else
			super.processWindowEvent(event);
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
 	public static void ecrireFichier(byte[] contenu, File fichier) throws Exception
 	{
 		FileOutputStream destinationFile = null;
 		destinationFile = new FileOutputStream(fichier);
 		destinationFile.write(contenu);
 		destinationFile.flush();
 		destinationFile.close();
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
	/*
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
	*/
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
					//updateArbre();
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

/*
 * Image image = null;
		image = getToolkit().getImage("loading.gif");
		if(image != null) // Si l'image existe, ...
		{
		g.drawImage(image, 200, 20, this); // ... on la dessine
		}
		*/
