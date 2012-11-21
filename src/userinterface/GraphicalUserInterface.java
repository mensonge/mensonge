package userinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
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
import javax.swing.JTabbedPane;

import core.BaseDeDonnees.BaseDeDonnees;
import core.BaseDeDonnees.DBException;

import userinterface.OngletLecteur;
/**
 * 
 * Classe Interface graphique contenant tous les composants graphiques
 * 
 */
public class GraphicalUserInterface extends JFrame implements ActionListener {
	private static final long serialVersionUID = 5373991180139317820L;

	private JTabbedPane onglets;

	private JMenuItem aideAPropos;
	private JMenuItem fichierFermer;
	private JMenuItem fichierOuvrir;
	private JMenuItem baseExporter;
	private JMenuItem baseImporter;
	private JMenuItem baseAjouterCategorie;
	private JMenuItem baseAjouterSujet;
	private JMenuItem play;

	private PanneauArbre panneauArbre;

	private BaseDeDonnees bdd = null;

	public GraphicalUserInterface() {
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
		baseAjouterSujet = new JMenuItem("Ajouter sujet");

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
		menuBase.add(baseAjouterSujet);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFichier);
		menuBar.add(menuOutils);
		menuBar.add(menuAide);
		menuBar.add(menuBase);
		/*
		 * Conteneur
		 */
		this.panneauArbre = new PanneauArbre(bdd);
		baseAjouterCategorie.addMouseListener(panneauArbre.new AjouterCategorieEnregistrementClicDroit(null, bdd));
		baseAjouterSujet.addMouseListener(panneauArbre.new AjouterSujetClicDroit(null, bdd));
		
		JPanel conteneur = new JPanel(new BorderLayout());
		conteneur.add(onglets, BorderLayout.CENTER);
		conteneur.add(panneauArbre, BorderLayout.EAST);
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

	/**
	 * Ajoute un nouvel onglet à l'interface graphique
	 * @param onglet Onglet à ajouter
	 */
	public void ajouterOnglet(OngletLecteur onglet)
	{
		JButton boutonFermeture = new JButton(new ImageIcon(
				"images/CloseTab.png"));
		boutonFermeture.setToolTipText("Fermer cet onglet");
		boutonFermeture.setContentAreaFilled(false);
		boutonFermeture.setFocusable(false);
		boutonFermeture.setBorder(BorderFactory.createEmptyBorder());
		boutonFermeture.setBorderPainted(false);
		boutonFermeture.addActionListener(new FermetureOngletListener(
				this.onglets, onglet));

		JPanel panelFermeture = new JPanel();
		panelFermeture.setBackground(new Color(0, 0, 0, 0));
		panelFermeture.add(new JLabel(onglet.getNom()));
		panelFermeture.add(boutonFermeture);

		this.onglets.add(onglet);
		this.onglets.setTabComponentAt(this.onglets.getTabCount() - 1,
				panelFermeture);
	}

	/**
	 * Quitte l'application
	 */
	public void quitter()
	{

		System.exit(0);
	}

	protected void processWindowEvent(WindowEvent event) {
		if (event.getID() == WindowEvent.WINDOW_DEACTIVATED) {
			this.panneauArbre.getMenuClicDroit().setEnabled(false);
			this.panneauArbre.getMenuClicDroit().setVisible(false);
		}
		if (event.getID() == WindowEvent.WINDOW_CLOSING) {
			this.quitter();
		} else
			super.processWindowEvent(event);
	}

	public void connexionBase(String fichier) {
		try {
			bdd = new BaseDeDonnees(fichier);
			bdd.connexion();// connexion et verification de la validite de la
							// table
		} catch (DBException e) {
			int a = e.getCode();
			if (a == 2) {
				try {
					bdd.createDatabase();
				} catch (DBException e1) {
					popupErreur(
							"[-] Erreur lors de la creation: "
									+ e1.getMessage(), "Erreur");
				}
			} else {
				popupErreur(
						"[-]Erreur lors de la connexion. " + e.getMessage(),
						"Erreur");
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
	public static void popupErreur(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Affiche une popup d'information
	 * @param message L'information à afficher
	 * @param title Le titre de la popup
	 */
	public static void popupInfo(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void ecrireFichier(byte[] contenu, File fichier)
			throws Exception {
		FileOutputStream destinationFile = null;
		destinationFile = new FileOutputStream(fichier);
		destinationFile.write(contenu);
		destinationFile.flush();
		destinationFile.close();
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == fichierFermer) {
			this.quitter();
		} else if (event.getSource() == aideAPropos) {
			JOptionPane.showMessageDialog(null,
					"Projet de détection de mensonge", "À propos",
					JOptionPane.PLAIN_MESSAGE);
		} else if (event.getSource() == fichierOuvrir) {
			JFileChooser fileChooser = new JFileChooser();
			// IContainer containerInput = IContainer.make();
			fileChooser.showOpenDialog(this);
			if (fileChooser.getSelectedFile() != null) {
				try {
					try {

						this.ajouterOnglet(new OngletLecteur(new File(
								fileChooser.getSelectedFile()
										.getCanonicalPath())));
					} catch (IOException e) {
						popupErreur(e.getMessage(), "Erreur");
					}
				} catch (Exception e1) {
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}

	/**
	 * Classe Listener gérant la fermeture des onglets, qui sera ajouté à chaque onglet
	 */
	private class FermetureOngletListener implements ActionListener {
		private JTabbedPane onglets;
		private OngletLecteur onglet;

		public FermetureOngletListener(JTabbedPane onglets, OngletLecteur onglet) {
			this.onglet = onglet;
			this.onglets = onglets;
		}

		public void actionPerformed(ActionEvent e) {
			onglet.fermerOnglet();
			onglets.remove(onglet);

		}
	}

	/*
	 * class AjouterCategorieEnregistrementClicDroit implements MouseListener {
	 * public void mouseClicked(MouseEvent e){} public void
	 * mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){}
	 * public void mousePressed(MouseEvent e){} public void
	 * mouseReleased(MouseEvent e) { menuClicDroit.setEnabled(false) ;
	 * menuClicDroit.setVisible(false) ; String nom =
	 * JOptionPane.showInputDialog(null,
	 * "Entrez le nom de la nouvelle catégorie", "Renommer",
	 * JOptionPane.QUESTION_MESSAGE); if(nom != null && ! nom.equals("")) { try
	 * { bdd.ajouterCategorie(nom); } catch (DBException e1) {
	 * popupErreur(e1.getMessage(), "Erreur"); } } updateArbre(); } }
	 */
	class ExporterBaseListner extends MouseAdapter {
		GraphicalUserInterface fenetre;

		public ExporterBaseListner(GraphicalUserInterface g) {
			fenetre = g;
		}

		public void mouseReleased(MouseEvent event) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(fenetre);
			if (fileChooser.getSelectedFile() != null) {
				try {
					bdd.exporter(fileChooser.getSelectedFile()
							.getCanonicalPath(), -1, 1);
				} catch (Exception e1) {
					popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}

	class ImporterBaseListner extends MouseAdapter {
		GraphicalUserInterface fenetre;

		public ImporterBaseListner(GraphicalUserInterface g) {
			fenetre = g;
		}

		public void mouseReleased(MouseEvent event) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(fenetre);
			String fichier;
			if (fileChooser.getSelectedFile() != null) {
				try {
					fichier = fileChooser.getSelectedFile().getCanonicalPath();
					bdd.importer(fichier);
					// updateArbre();
				} catch (Exception e1) {
					popupErreur(e1.getMessage(), "Erreur");
					return;
				}
			}
		}
	}

	public static void main(String args[]) {
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GraphicalUserInterface();
			}
		});
	}

}
