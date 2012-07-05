package userinterface;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class LecteurVideo
{
	private File fichierVideo;
	private String nom;


	IMediaReader mediaReader;

	public LecteurVideo(File fichierVideo)
	{
		this.fichierVideo = fichierVideo;
		this.nom = fichierVideo.getName();
		this.initialiserComposant();
	}

	public void initialiserComposant()
	{
		mediaReader	= ToolFactory.makeReader(nom);
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		mediaReader.addListener(new ImageSnapListener();

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
