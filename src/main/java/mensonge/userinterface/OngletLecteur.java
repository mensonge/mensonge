package mensonge.userinterface;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;

public class OngletLecteur extends JPanel
{
	private static final long serialVersionUID = -2623351725072404024L;

	private String nom;

	private LecteurVideo lecteurVideo;

	public OngletLecteur(File fichierVideo,BaseDeDonnees bdd,JFrame parent )
	{
		this.nom = fichierVideo.getName();

		this.lecteurVideo = new LecteurVideo(fichierVideo,fichierVideo.getAbsolutePath(),bdd,parent);

		this.setLayout(new BorderLayout());
		this.add(lecteurVideo, BorderLayout.CENTER);
	}

	public String getNom()
	{
		return nom;
	}

	public void fermerOnglet()
	{
		this.lecteurVideo.close();
	}
}
