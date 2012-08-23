package userinterface;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

import java.io.File;

import java.awt.Dimension;
import javax.swing.JFrame;

public class LecteurVideo
{
	private File fichierVideo;/*Le fichier video*/
	private String nom;/*Le nom du fichier video*/
	ImageComponent mediaPlayerComponent = new ImageComponent();

	public LecteurVideo(File fichierVideo)
	{
		this.fichierVideo = fichierVideo;
		this.nom = fichierVideo.getName();
		this.initialiserComposant();

		JFrame aFrame = new JFrame();
		aFrame.setPreferredSize(new Dimension(640, 500));
		aFrame.add(mediaPlayerComponent);
		aFrame.pack();
		aFrame.setLocationRelativeTo(null);
		aFrame.setVisible(true);

	}

	public void initialiserComposant()
	{
		DecodeAndPlayVideo decodeAndPlayVideo =new DecodeAndPlayVideo(mediaPlayerComponent);
		decodeAndPlayVideo.PlayVideo(nom);
	}

	public void play()
	{
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

}
