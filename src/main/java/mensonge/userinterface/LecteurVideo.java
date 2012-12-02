package mensonge.userinterface;

import java.io.File;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class LecteurVideo extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 5373991180139317820L;
	private static final float SLIDER_POSITION_MAX = 100.0f;
	private JButton boutonLecture;
	private JButton boutonStop;
	private JLabel labelDureeActuelle;
	private JLabel labelDureeMax;
	private JSlider slider;
	private JSlider sliderVolume;
	private ImageIcon imageIconPause;
	private ImageIcon imageIconStop;
	private ImageIcon imageIconLecture;
	private boolean pause;
	private boolean stop;
	private EmbeddedMediaPlayerComponent vidComp;

	private JButton boutonMarqueur1;
	private JButton boutonMarqueur2;
	private long timeMarqueur1 = 0;
	private long timeMarqueur2 = 0;
	private Marqueur t1;
	private EmbeddedMediaPlayer mediaPlayer;

	public LecteurVideo(final File fichierVideo)
	{
		this.vidComp = new EmbeddedMediaPlayerComponent();
		this.vidComp.setVisible(true);
		this.mediaPlayer = this.vidComp.getMediaPlayer();
		this.mediaPlayer.addMediaPlayerEventListener(new PlayerEventListener());

		this.pause = true;
		this.stop = true;

		initialiserComposants();

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					mediaPlayer.setRepeat(true);
					mediaPlayer.prepareMedia(fichierVideo.getCanonicalPath());
					mediaPlayer.setVolume(sliderVolume.getValue());
				}
				catch (IOException e)
				{
					GraphicalUserInterface.popupErreur(e.getMessage(), "Erreur");
				}
			}
		});
	}

	private void initialiserComposants()
	{

		this.imageIconPause = new ImageIcon("images/Pause.png");
		this.imageIconStop = new ImageIcon("images/Stop.png");
		this.imageIconLecture = new ImageIcon("images/Lecture.png");

		this.labelDureeActuelle = new JLabel("00:00:00");
		this.labelDureeMax = new JLabel("00:00:00");

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
		this.sliderVolume.setMinimumSize(new Dimension(150, 30));
		this.sliderVolume.setMaximumSize(new Dimension(150, 30));
		this.sliderVolume.setPreferredSize(new Dimension(150, 30));
		this.sliderVolume.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				mediaPlayer.setVolume(sliderVolume.getValue());
			}
		});

		this.slider = new JSlider(JSlider.HORIZONTAL);
		this.slider.setPaintTicks(false);
		this.slider.setPaintLabels(false);
		this.slider.setMinimum(0);
		this.slider.setValue(0);
		this.slider.setMaximum((int) SLIDER_POSITION_MAX);

		SliderPositionEventListener sliderListener = new SliderPositionEventListener(this.slider, this.mediaPlayer);
		this.slider.addMouseListener(sliderListener);
		this.slider.addMouseMotionListener(sliderListener);
		this.slider.addKeyListener(sliderListener);

		JPanel panelDuree = new JPanel();
		panelDuree.setLayout(new BoxLayout(panelDuree, BoxLayout.X_AXIS));
		panelDuree.add(Box.createHorizontalStrut(5));
		panelDuree.add(labelDureeActuelle, BorderLayout.WEST);
		panelDuree.add(Box.createHorizontalStrut(5));
		panelDuree.add(slider, BorderLayout.CENTER);
		panelDuree.add(Box.createHorizontalStrut(5));
		panelDuree.add(labelDureeMax, BorderLayout.EAST);
		panelDuree.add(Box.createHorizontalStrut(5));

		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		toolBar.setFloatable(false);
		toolBar.add(boutonLecture);
		toolBar.add(boutonStop);
		toolBar.addSeparator();
		toolBar.add(boutonMarqueur1);
		toolBar.add(boutonMarqueur2);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new JLabel(new ImageIcon("images/Volume.png")));
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(sliderVolume);
		toolBar.add(Box.createHorizontalStrut(5));

		JPanel panelControls = new JPanel(new GridLayout(2, 1));
		panelControls.add(panelDuree);
		panelControls.add(toolBar);

		this.setLayout(new BorderLayout());
		this.add(vidComp, BorderLayout.CENTER);
		this.add(panelControls, BorderLayout.SOUTH);
	}

	/**
	 * Ferme le lecteur vidéo proprement en fermant les instances de mediaPlayer
	 */
	public void close()
	{
		this.mediaPlayer.stop();
		this.mediaPlayer.release();
		this.vidComp.release();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == boutonLecture)
		{
			if (this.pause)
			{
				this.mediaPlayer.play();
			}
			else
			{
				this.mediaPlayer.pause();
			}
		}
		else if (event.getSource() == boutonStop)
		{
			if (!this.stop)
			{
				this.mediaPlayer.stop();
			}
		}
		else if (event.getSource() == boutonMarqueur1)
		{
			timeMarqueur1 = mediaPlayer.getTime();
			t1 = new Marqueur(-50);
			t1.setVisible(true);
			slider.add(t1);
			t1.repaint();
		}
		else if (event.getSource() == boutonMarqueur2)
		{
			timeMarqueur2 = mediaPlayer.getTime();
		}
	}

	private class PlayerEventListener extends MediaPlayerEventAdapter
	{
		@Override
		public void paused(MediaPlayer arg0)
		{
			pause = true;
			boutonLecture.setIcon(imageIconLecture);
			boutonLecture.setToolTipText("Lancer");
		}

		@Override
		public void playing(MediaPlayer arg0)
		{
			pause = false;
			stop = false;
			boutonLecture.setIcon(imageIconPause);
			boutonLecture.setToolTipText("Mettre en pause");
		}

		@Override
		public void positionChanged(MediaPlayer event, float time)
		{
			long duree = event.getTime() / 1000;
			int heures = (int) (duree / 3600);
			int minutes = (int) ((duree % 3600) / 60);
			int secondes = (int) ((duree % 3600) % 60);
			labelDureeActuelle.setText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
			slider.setValue((int) (event.getPosition() * SLIDER_POSITION_MAX));
		}

		@Override
		public void stopped(MediaPlayer arg0)
		{
			stop = true;
			pause = true;
			boutonLecture.setIcon(imageIconLecture);
			boutonLecture.setToolTipText("Lancer");
			slider.setValue(0);
		}

		@Override
		public void lengthChanged(MediaPlayer mediaPlayer, long newLength)
		{
			long duree = mediaPlayer.getLength() / 1000;
			int heures = (int) (duree / 3600);
			int minutes = (int) ((duree % 3600) / 60);
			int secondes = (int) ((duree % 3600) % 60);
			labelDureeMax.setText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
		}

		@Override
		public void finished(MediaPlayer mediaPlayer)
		{
			stop = true;
			pause = true;
			boutonLecture.setIcon(imageIconLecture);
			boutonLecture.setToolTipText("Lancer");
		}
		
	}

	private static class SliderPositionEventListener extends MouseAdapter implements KeyListener
	{
		private JSlider slider;
		private EmbeddedMediaPlayer mediaPlayer;

		public SliderPositionEventListener(JSlider slider, EmbeddedMediaPlayer mediaPlayer)
		{
			this.slider = slider;
			this.mediaPlayer = mediaPlayer;
		}

		private int valueForXPosition(int x)
		{
			return ((BasicSliderUI) slider.getUI()).valueForXPosition(x) + 1;
		}

		@Override
		public void mouseReleased(MouseEvent event)
		{
			int value = this.valueForXPosition(event.getX());
			mediaPlayer.setPosition(value / SLIDER_POSITION_MAX);
		}

		@Override
		public void mouseMoved(MouseEvent event)
		{
			int positionValue = this.valueForXPosition(event.getX());
			long duree = (long) ((positionValue / SLIDER_POSITION_MAX) * mediaPlayer.getLength() / 1000);
			int heures = (int) (duree / 3600);
			int minutes = (int) ((duree % 3600) / 60);
			int secondes = (int) ((duree % 3600) % 60);
			slider.setToolTipText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
		}

		@Override
		public void keyTyped(KeyEvent event)
		{
			if (event.getKeyCode() == KeyEvent.VK_RIGHT)
			{
				float perCent = mediaPlayer.getPosition() * 1.1f;
				mediaPlayer.setPosition(perCent);
			}
			else if (event.getKeyCode() == KeyEvent.VK_LEFT)
			{
				float perCent = mediaPlayer.getPosition() * 0.9f;
				mediaPlayer.setPosition(perCent);
			}
			else if (event.getKeyCode() == 0)// FIXME 0 sur mon pc KeyEvent.VK_SPACE normalement
			{
				if (mediaPlayer.isPlaying())
				{
					this.mediaPlayer.pause();
				}
				else
				{
					this.mediaPlayer.play();
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e)
		{

		}

		@Override
		public void keyReleased(KeyEvent e)
		{

		}
	}
}
