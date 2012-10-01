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
import java.awt.event.WindowEvent;
import java.io.File;
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

	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
	private DefaultMutableTreeNode racine;
	private JTree arbre;
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
		
		remplirArbreEnregistrement();
		
		//JTable table = new JTable(modeleTableau);

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

	//public void paint(Graphics g)
	//{
		//racine.removeAllChildren();
		//racine.removeFromParent();
	//}
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


	public void remplirArbreEnregistrement()
	{
		ResultSet rs_cat = null, rs_enr = null;
		racine = new DefaultMutableTreeNode("Enregistrements");
		try
		{
			rs_cat = bdd.getListeCategorie();
			while(rs_cat.next())
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(rs_cat.getString("nomcat"));
				rs_enr = bdd.getListeEnregistrement(rs_cat.getInt("idcat"));
				while(rs_enr.next())
				{
					node.add(new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"), rs_enr.getInt("taille"), rs_enr.getString("nomCat")));
				}
				rs_enr.close();
				racine.add(node);
			}
			rs_cat.close();
			arbre = new JTree(racine);
			//Ajout d'un listner a l'arbre
			arbre.addTreeSelectionListener(new TreeSelectionListener(){

		         public void valueChanged(TreeSelectionEvent event)
		         {
		        	 
		            if(arbre.getLastSelectedPathComponent() != null && arbre.getLastSelectedPathComponent() instanceof Feuille)
		            {
		               infoArbre.setListeInfo(((Feuille) arbre.getLastSelectedPathComponent()).getInfo());//On informe le panneau d'information
		               infoArbre.repaint();//on le repaint
		            }
		            else
		            {
		            	infoArbre.setListeInfo(null);
		            	infoArbre.repaint();//on le repaint
		            }
		         }
		      });
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
	

	//TODO faire la fonction pour vider un arbre
	public void removegroupofelements (DefaultMutableTreeNode selectednode)
	{
        
        int nbchildren=selectednode.getChildCount();
        
        for (int i=0;i<nbchildren;i++) {
            
            if (selectednode.getChildAt(0).isLeaf()) {
                
                if (selectednode.isRoot())
                {
                
                    //removesimpleelement((DefaultMutableTreeNode)selectednode.getChildAt(0),true);
                }
                else
                {
                    //removesimpleelement((DefaultMutableTreeNode)selectednode.getChildAt(0),true);
                }
            }
            else
            {
                removegroupofelements ((DefaultMutableTreeNode)selectednode.getChildAt(0));
            }
        }
        
        if (selectednode.isRoot()==false) 
        {
            //removesimpleelement(selectednode,true);
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
					//modeleTableau.getDataVector().removeAllElements();//Vide le tableau
					arbre.clearSelection();
					remplirArbreEnregistrement();//On le rerempli
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


