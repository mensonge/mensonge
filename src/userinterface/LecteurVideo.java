package userinterface;

import com.xuggle.mediatool.IMediaReader;
import javax.sound.sampled.SourceDataLine;

import java.io.File;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

public class LecteurVideo extends JPanel implements ActionListener {
	private static final long serialVersionUID = 5373991180139317820L;
	private File fichierVideo;
	private String nom;
	private JButton boutonLecture;
	private JButton boutonStop;
	private JPanel panelDuree;
	private JLabel labelDureeActuelle;
	private JLabel labelDureeMax;
	private JSlider slider;
	private JSlider sliderVolume;
	private ImageIcon imageIconPause;
	private ImageIcon imageIconStop;
	private ImageIcon imageIconLecture;
	private ImageComponent mediaPlayerComponent;
	private IMediaReader reader;
	private long duration;
	private int volume; // Entre 0 et 100
	private boolean pause;
	private boolean stop;
	private SourceDataLine mLine;
	private EmbeddedMediaPlayerComponent vidComp;
	
	private JButton boutonMarqueur1;
	private JButton boutonMarqueur2;
	private long timeMarqueur1=0 ;
	private long timeMarqueur2=0;
	
	public LecteurVideo(final File fichierVideo) {
		this.volume = 50;
		this.pause = true;
		this.stop = true;
		this.fichierVideo = fichierVideo;
		try {
			this.nom = fichierVideo.getCanonicalPath();
		} catch (IOException e) {
			GraphicalUserInterface.popupErreur(e.getMessage(), "Erreur");
		}
		initialiserComposants();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				vidComp.getMediaPlayer().playMedia(nom);
				ouvrirVideo();
			}
		});
	}

	private void initialiserComposants() {

		this.imageIconPause = new ImageIcon("images/Pause.png");
		this.imageIconStop = new ImageIcon("images/Stop.png");
		this.imageIconLecture = new ImageIcon("images/Lecture.png");

		this.labelDureeActuelle = new JLabel("00:00:00");
		this.labelDureeMax = new JLabel("00:00:00");

		this.panelDuree = new JPanel(new BorderLayout());
		this.panelDuree.add(labelDureeActuelle, BorderLayout.WEST);
		this.panelDuree.add(labelDureeMax, BorderLayout.EAST);

		this.boutonLecture = new JButton();
		this.boutonLecture.setToolTipText("Lancer");
		this.boutonLecture.setIcon(imageIconLecture);
		this.boutonLecture.addActionListener(this);
		this.boutonLecture.setEnabled(true);

		this.boutonMarqueur1 = new JButton();
		this.boutonMarqueur1.setToolTipText("Placer Marqueur 1");
		this.boutonMarqueur1.setText("Marqueur 1");
		this.boutonMarqueur1.addActionListener(this);
		this.boutonMarqueur1.setEnabled(true);
		
		this.boutonMarqueur2 = new JButton();
		this.boutonMarqueur2.setToolTipText("Placer Marqueur 2");
		this.boutonMarqueur2.setText("Marqueur 2");
		this.boutonMarqueur2.addActionListener(this);
		this.boutonMarqueur2.setEnabled(true);
		
		this.boutonStop = new JButton();
		this.boutonStop.setToolTipText("Stoper");
		this.boutonStop.setIcon(imageIconStop);
		this.boutonStop.addActionListener(this);
		this.boutonStop.setEnabled(true);

		this.sliderVolume = new JSlider(JSlider.HORIZONTAL);
		this.sliderVolume.setPaintTicks(false);
		this.sliderVolume.setPaintLabels(false);
		this.sliderVolume.setMinimum(0);
		this.sliderVolume.setMaximum(100);
		this.sliderVolume.setValue(50);
		this.sliderVolume.setToolTipText("Volume");
		this.sliderVolume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				volume = sliderVolume.getValue();
				vidComp.getMediaPlayer().setVolume(sliderVolume.getValue());
			}
		});

		this.slider = new JSlider(JSlider.HORIZONTAL);
		this.panelDuree.add(slider, BorderLayout.CENTER);

		this.slider.setPaintTicks(false);
		this.slider.setPaintLabels(false);
		this.slider.setMinimum(0);
		this.slider.setValue(0);
		this.slider.addMouseListener(new MouseSliderEventListener());

		  this.slider.setUI(new MetalSliderUI() 
		  { 
			  protected void  scrollDueToClickInTrack(int direction) { 
				  int value = slider.getValue(); 
				  if (slider.getOrientation() == JSlider.HORIZONTAL) 
				  { 
					  value =  this.valueForXPosition(slider.getMousePosition().x); 
				  } 
				  	vidComp.getMediaPlayer().setPosition((float) (((float)slider.getValue())/100.0));
				  	slider.setValue(value);
				  } 
		  } 
		);
		  this.slider.addKeyListener(new SliderKeyListener());
		 
		this.slider.addChangeListener(new ChangeListener() 
		{
			public void stateChanged(ChangeEvent e) 
			{
				float perCent=(float) (((float)slider.getValue())/100.0);
				vidComp.getMediaPlayer().setPosition(perCent);
			}
		});

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(boutonStop);
		toolBar.add(boutonLecture);
		toolBar.add(panelDuree);
		toolBar.add(sliderVolume);
				
		toolBar.add(boutonMarqueur1);
		toolBar.add(boutonMarqueur2);
		
		
		vidComp = new EmbeddedMediaPlayerComponent();
		vidComp.setVisible(true);
		vidComp.getMediaPlayer().addMediaPlayerEventListener(new PlayerEventListener());

		this.setLayout(new BorderLayout());
		this.add(vidComp, BorderLayout.CENTER);
		this.add(toolBar, BorderLayout.SOUTH);
	}

	private void ouvrirVideo() {
		try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pause = false;
		this.boutonLecture.setIcon(imageIconPause);
		this.boutonLecture.setToolTipText("Mettre en pause");
		vidComp.getMediaPlayer().setVolume(sliderVolume.getValue());
		this.duration = vidComp.getMediaPlayer().getLength() / 1000;
		this.slider.setMaximum(100);
		long duree = duration;
		int heures = (int) (duree / 3600);
		int minutes = (int) ((duree % 3600) / 60);
		int secondes = (int) ((duree % 3600) % 60);
		labelDureeMax.setText(String.format("%02d:%02d:%02d", heures, minutes,secondes));
	}

	/*
	 * private void ouvrirAudio() { if(this.reader.isOpen()) { IContainer
	 * container = reader.getContainer(); IStreamCoder audioCoder = null; int
	 * numStreams = container.getNumStreams(); int audioStreamId = -1; for(int i
	 * = 0; i < numStreams; i++) { IStream stream = container.getStream(i);
	 * IStreamCoder coder = stream.getStreamCoder(); if(coder.getCodecType() ==
	 * ICodec.Type.CODEC_TYPE_AUDIO) { audioStreamId = i; audioCoder = coder;
	 * break; } } if(audioStreamId == -1) { throw new
	 * RuntimeException("could not find audio stream"); } IMetaData options =
	 * IMetaData.make(); IMetaData unsetOptions = IMetaData.make();
	 * if(audioCoder.open(options,unsetOptions) < 0) { throw new
	 * RuntimeException("could not open audio decoder"); } AudioFormat
	 * audioFormat = new AudioFormat(audioCoder.getSampleRate(),
	 * (int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
	 * audioCoder.getChannels(), true, /* xuggler defaults to signed 16 bit
	 * samples false); DataLine.Info info = new
	 * DataLine.Info(SourceDataLine.class, audioFormat); try { this.mLine =
	 * (SourceDataLine) AudioSystem.getLine(info); this.mLine.open(audioFormat);
	 * this.mLine.start(); } catch(LineUnavailableException e) { throw new
	 * RuntimeException("could not open audio line"); } } }
	 */

	public void play() {
		if (this.vidComp.getMediaPlayer().isPlaying()) {
			this.vidComp.getMediaPlayer().pause();
		} else
			this.vidComp.getMediaPlayer().play();
		this.pause = false;
		this.stop = false;
		this.boutonLecture.setIcon(imageIconPause);
		this.boutonLecture.setToolTipText("Mettre en pause");
	}

	public void pause() 
	{
		this.vidComp.getMediaPlayer().pause();
		this.pause = true;
		boutonLecture.setIcon(imageIconLecture);
		boutonLecture.setToolTipText("Lancer");
	}

	public void stop() {
		this.vidComp.getMediaPlayer().stop();
		this.stop = true;
		this.pause = true;
		this.slider.setValue(0);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == boutonLecture) {
			if (this.pause == true) {
				this.play();
			} else {
				this.pause();
			}
		} else if (event.getSource() == boutonStop) {
			if (!this.stop) {
				if (this.pause == true) {
					this.boutonLecture.setIcon(imageIconPause);
					this.boutonLecture.setToolTipText("Mettre en pause");
				} else {
					this.boutonLecture.setIcon(imageIconLecture);
					this.boutonLecture.setToolTipText("Lancer");
				}
				this.stop();
			}
		}
		else if(event.getSource()==boutonMarqueur1)
		{
			timeMarqueur1= vidComp.getMediaPlayer().getTime();
		}
		else if(event.getSource()==boutonMarqueur2)
		{
			timeMarqueur2= vidComp.getMediaPlayer().getTime();
		}
	}

	private class PlayerEventListener implements MediaPlayerEventListener {

		@Override
		public void backward(MediaPlayer arg0) {
		}

		@Override
		public void buffering(MediaPlayer arg0, float arg1) {
		}

		@Override
		public void endOfSubItems(MediaPlayer arg0) {
		}

		@Override
		public void error(MediaPlayer arg0) {
		}

		@Override
		public void finished(MediaPlayer arg0) {
		}

		@Override
		public void forward(MediaPlayer arg0) {
		}

		@Override
		public void lengthChanged(MediaPlayer arg0, long arg1) {
		}

		@Override
		public void mediaChanged(MediaPlayer arg0, libvlc_media_t arg1,String arg2) {
		}

		@Override
		public void mediaDurationChanged(MediaPlayer arg0, long arg1) {
		}

		@Override
		public void mediaFreed(MediaPlayer arg0) {
		}

		@Override
		public void mediaMetaChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void mediaParsedChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void mediaStateChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void mediaSubItemAdded(MediaPlayer arg0, libvlc_media_t arg1) {
		}

		@Override
		public void newMedia(MediaPlayer arg0) {
		}

		@Override
		public void opening(MediaPlayer arg0) {
		}

		@Override
		public void pausableChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void paused(MediaPlayer arg0) {
		}

		@Override
		public void playing(MediaPlayer arg0) {
		}

		@Override
		public void positionChanged(MediaPlayer arg0, float time) 
		{
			duration = arg0.getTime() / 1000;
			long duree = duration;
			int heures = (int) (duree / 3600);
			int minutes = (int) ((duree % 3600) / 60);
			int secondes = (int) ((duree % 3600) % 60);
			labelDureeActuelle.setText(String.format("%02d:%02d:%02d", heures,minutes, secondes));
		}

		@Override
		public void seekableChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void snapshotTaken(MediaPlayer arg0, String arg1) {
		}

		@Override
		public void stopped(MediaPlayer arg0) {
		}

		@Override
		public void subItemFinished(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void subItemPlayed(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void timeChanged(MediaPlayer arg0, long time) 
		{ 
			slider.setValue((int)(arg0.getPosition()*100));
		}

		@Override
		public void titleChanged(MediaPlayer arg0, int arg1) {
		}

		@Override
		public void videoOutput(MediaPlayer arg0, int arg1) {
		}
	}
	private class MouseSliderEventListener implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent mE) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			int positionValue=((BasicSliderUI) slider.getUI()).valueForXPosition(e.getX());
			long duree = (long) ((positionValue/100.0)*vidComp.getMediaPlayer().getLength() /1000);
			int heures = (int) (duree / 3600);
			int minutes = (int) ((duree % 3600) / 60);
			int secondes = (int) ((duree % 3600) % 60);
			slider.setToolTipText(String.format("%02d:%02d:%02d", heures, minutes,secondes));
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}
	
private class SliderKeyListener	implements KeyListener {
		
		@Override
		public void keyTyped(KeyEvent ke) {
			if(ke.getKeyCode()==KeyEvent.VK_RIGHT)
			{
				float perCent=(float) (((float)slider.getValue())/100.0)+5;
				vidComp.getMediaPlayer().setPosition(perCent);
			}
			else if(ke.getKeyCode()==KeyEvent.VK_LEFT)
			{
				float perCent=(float) (((float)slider.getValue())/100.0)-5;
				vidComp.getMediaPlayer().setPosition(perCent);
			}
			else if(ke.getKeyCode()==0)//FIXME 0 sur mon pc KeyEvent.VK_SPACE normalement
			{
				if (vidComp.getMediaPlayer().isPlaying()) {
					pause();
				} else
					play();	
			}
		}
		
		@Override
		public void keyReleased(KeyEvent arg0) {
		}
		@Override
		public void keyPressed(KeyEvent arg0) {
		}
	}
}
