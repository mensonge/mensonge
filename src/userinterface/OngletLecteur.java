package userinterface;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

public class OngletLecteur extends JPanel
{
	private static final long serialVersionUID = -2623351725072404024L;

	private String nom;

	private File fichierVideo;
	private LecteurVideo leLecteur;

	public OngletLecteur(File fichierVideo)
	{
		this.fichierVideo = fichierVideo;
		this.nom = fichierVideo.getName();

		this.initialiserComposants();
	}

	public void initialiserComposants()
	{
		leLecteur = new LecteurVideo(this.fichierVideo);

		this.setLayout(new BorderLayout());
		this.add(leLecteur, BorderLayout.CENTER);
	}

	public String getNom()
	{
		return nom;
	}

	public void fermerOnglet()
	{
		this.leLecteur.stop();
	}
}
