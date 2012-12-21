package mensonge.userinterface;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mensonge.core.Extraction;
import mensonge.core.BaseDeDonnees.BaseDeDonneesControlleur;
import mensonge.core.BaseDeDonnees.BaseDeDonneesModele;

/**
 * Classe gérant un onglet dans l'application possédant un lecteur vidéo
 * 
 */
public class OngletLecteur extends JPanel
{
	private static final long serialVersionUID = -2623351725072404024L;

	private String nom;

	private LecteurVideo lecteurVideo;

	/**
	 * Créé un nouvel onglet avec un lecteur vidéo
	 * 
	 * @param fichierVideo
	 *            Fichier vidéo
	 * @param bdd
	 *            Base de données de l'application
	 * @param parent
	 */
	public OngletLecteur(File fichierVideo, BaseDeDonneesControlleur bdd, JFrame parent, Extraction extraction)
	{
		this.nom = fichierVideo.getName();

		this.lecteurVideo = new LecteurVideo(fichierVideo, bdd, parent, extraction);

		this.setLayout(new BorderLayout());
		this.add(lecteurVideo, BorderLayout.CENTER);
	}

	/**
	 * Récupère le nom de l'onglet
	 * 
	 * @return Nom de l'onglet
	 */
	public String getNom()
	{
		return nom;
	}

	/**
	 * Appelé à la fermeture de l'onglet, permet notammenent de fermer proprement le lecteur vidéo
	 */
	public void fermerOnglet()
	{
		this.lecteurVideo.close();
	}
}
