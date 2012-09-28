package userinterface;

import core.BaseDeDonnees.BaseDeDonnees;
import core.BaseDeDonnees.DBException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;

import userinterface.OngletLecteur;

public class GraphicalUserInterface extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 5373991180139317820L;

	private JTabbedPane onglets;

	private JMenuItem aideAPropos;
	private JMenuItem fichierFermer;
	private JMenuItem fichierOuvrir;
	private JMenuItem baseExporter;
	private JMenuItem baseImporter;

	private ModeleTableau modeleTableau;

	private JScrollPane scrollPane;

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
		
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFichier);
		menuBar.add(menuOutils);
		menuBar.add(menuAide);

		/*
		 * Création du tableau des enregistrement
		 */
		//Création des colonnes
		modeleTableau = new ModeleTableau();
		modeleTableau.addColumn("Nom");
		modeleTableau.addColumn("Categorie");
		modeleTableau.addColumn("Durée");
		modeleTableau.addColumn("Taille");

		//ajout des ligne
		remplirTableauEnregistrement();
		JTable table = new JTable(modeleTableau);

		//transformation en scrollPane
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(270, 800));
		scrollPane.setAutoscrolls(true);
		
		Box boxLabel = Box.createVerticalBox();
		boxLabel.add(Box.createVerticalStrut(1));
		boxLabel.add(new JLabel("Liste des enregistrements"));
		boxLabel.add(Box.createVerticalStrut(2));
		boxLabel.add(new JSeparator(SwingConstants.HORIZONTAL));
		boxLabel.add(Box.createVerticalStrut(5));

		JPanel panelEnregistrements = new JPanel(new BorderLayout());
		panelEnregistrements.add(boxLabel,BorderLayout.NORTH);
		panelEnregistrements.add(scrollPane,BorderLayout.CENTER);

		/*
		 * Conteneur
		 */
		JPanel conteneur = new JPanel(new BorderLayout());
		conteneur.add(onglets,BorderLayout.CENTER);
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
		if (event.getID() == WindowEvent.WINDOW_CLOSING)
		{
			this.quitter();
		}
		else
			super.processWindowEvent(event);
	}

	public void createTableauEnregistrement()
	{
		
	}
	public void remplirTableauEnregistrement()
	{
		try
		{
			Object tab[] = new Object[5];
			ResultSet rs = bdd.getListeEnregistrement();
			while(rs.next())
			{
				
				tab[0] = rs.getString("nom");
				tab[1] = rs.getString("nomcat");
				tab[2] = rs.getInt("duree");
				tab[3] = rs.getInt("taille");
				tab[4] = rs.getInt("id");
				modeleTableau.addRow(tab);
			}
		} 
		catch(Exception e)
		{
			popup("Erreur lors du chargement des enregistrement.", "Erreur");
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
					popup("[-] Erreur lors de la creation: " + e1.getMessage(), "Erreur");
				}
				//creation de la base
				//System.out.println("[i]Base cree.");
			}
			else
			{
				popup("[-]Erreur lors de la connexion. " + e.getMessage(), "Erreur");
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
 	public static void popup(String message, String title)
	{
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}
	@Override
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
							popup(e.getMessage(), "Erreur");
						}
					}
				}
				catch (Exception e1)
				{
					popup(e1.getMessage(), "Erreur");
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
					System.out.println(fileChooser.getSelectedFile().getCanonicalPath());
				}
				catch (Exception e1)
				{
					popup(e1.getMessage(), "Erreur");
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
					modeleTableau.getDataVector().removeAllElements();//Vide le tableau
					remplirTableauEnregistrement();//On le rerempli
				}
				catch (Exception e1)
				{
					popup(e1.getMessage(), "Erreur");
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

