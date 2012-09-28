package userinterface;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

import java.io.File;
import java.io.IOException;

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JPanel;


public class LecteurVideo extends JPanel
{
	private static final long serialVersionUID = 5373991180139317820L;
	private File fichierVideo;/*Le fichier video*/
	private String nom;/*Le nom du fichier video*/
	ImageComponent mediaPlayerComponent = new ImageComponent();
	DecodeAndPlayVideo decodeAndPlayVideo ;
	public LecteurVideo(File fichierVideo)
	{
		this.fichierVideo = fichierVideo;

		try
		{
			this.nom = fichierVideo.getCanonicalPath();
		}
		catch (IOException e)
		{
			GraphicalUserInterface.popupErreur(e.getMessage());
		}


		this.setLayout(new BorderLayout());
		this.initialiserComposant();

		this.add(mediaPlayerComponent ,BorderLayout.CENTER);

	}

	public void initialiserComposant()
	{

    	 this.decodeAndPlayVideo =new DecodeAndPlayVideo(mediaPlayerComponent,nom);
	}

	public void play()
	{
		this.decodeAndPlayVideo.run();
	}

	public void pause()
	{

	}

	public void stop()
	{
	}

	public void setVolume(long volume)
	{
	}
	public DecodeAndPlayVideo getDecodeAndPlayVideo()
	{
		return this.decodeAndPlayVideo;
	}
}
