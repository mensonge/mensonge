package userinterface;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import com.xuggle.xuggler.*;
public class OngletLecteur extends JPanel implements ActionListener
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
		this.add(leLecteur,BorderLayout.CENTER);
	}

	public String getNom()
	{
		return nom;
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
	}

	public void fermerOnglet()
	{
		this.leLecteur.stop();
	}
}
