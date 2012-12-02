package mensonge.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

public class LecteurAudio extends JPanel
{
	private static final long serialVersionUID = 5373991180139317820L;

	private double volume; // Entre 0 et 1
	private boolean pause;
	private JButton boutonStop;

	private JButton boutonLecture;
	private JLabel labelDureeActuelle;

	private ImageIcon imageIconStop;
	private ImageIcon imageIconLecture;
	private JLabel labelDureeMax;
	private JSlider slider;
	private JSlider sliderVolume;

	private MediaPlayer mediaPlayer;

	private MediaPlayerFactory factory;

	public LecteurAudio()
	{
		factory = new MediaPlayerFactory();
		this.mediaPlayer = factory.newHeadlessMediaPlayer();
		this.pause = true;
		this.addControls();
		this.mediaPlayer.addMediaPlayerEventListener(new PlayerEventListener(slider, boutonLecture, labelDureeMax, labelDureeActuelle));

	}
	
	/**
	 * Ferme le lecteur audio proprement en fermant les instances de mediaPlayer
	 */
	public void close()
	{
		this.mediaPlayer.stop();
		this.mediaPlayer.release();
		this.factory.release();
	}
	
	private void addControls()
	{
		this.imageIconStop = new ImageIcon("images/Stop.png");
		this.imageIconLecture = new ImageIcon("images/Lecture.png");

		this.labelDureeActuelle = new JLabel("00:00:00");
		this.labelDureeMax = new JLabel("00:00:00");

		this.boutonLecture = new JButton();
		this.boutonLecture.setToolTipText("Lancer");
		this.boutonLecture.setIcon(imageIconLecture);
		this.boutonLecture.setEnabled(true);

		this.boutonStop = new JButton();
		this.boutonStop.setToolTipText("Stoper");
		this.boutonStop.setIcon(imageIconStop);
		this.boutonStop.setEnabled(true);

		this.slider = new JSlider(JSlider.HORIZONTAL);
		this.slider.setPaintTicks(false);
		this.slider.setPaintLabels(false);
		this.slider.setMinimum(0);
		this.slider.setValue(0);
		this.slider.setMaximum(100);

		this.sliderVolume = new JSlider(JSlider.HORIZONTAL);
		this.sliderVolume.setPaintTicks(false);
		this.sliderVolume.setPaintLabels(false);
		this.sliderVolume.setMinimum(0);
		this.sliderVolume.setMaximum(100);
		this.sliderVolume.setValue(50);
		this.sliderVolume.setToolTipText("Volume");
		this.sliderVolume.setMinimumSize(new Dimension(100, 30));
		this.sliderVolume.setMaximumSize(new Dimension(100, 30));
		this.sliderVolume.setPreferredSize(new Dimension(100, 30));
		
		SliderPositionEventListener sliderListener = new SliderPositionEventListener(this.slider, this.mediaPlayer);
		this.slider.addMouseListener(sliderListener);
		this.slider.addMouseMotionListener(sliderListener);
		
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
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new JLabel(new ImageIcon("images/Volume.png")));
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(sliderVolume);
		toolBar.add(Box.createHorizontalStrut(5));

		this.setLayout(new GridLayout(2, 1));
		this.add(panelDuree);
		this.add(toolBar);
		
		ActionMap actionMap = this.getActionMap();
		InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space bar");
		actionMap.put("space bar", new AbstractAction()
		{
			private static final long serialVersionUID = -7449791455625215682L;

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (mediaPlayer.isPlaying())
				{
					mediaPlayer.pause();
				}
				else
				{
					mediaPlayer.play();
				}
			}
		});
	}

	public boolean isPause()
	{
		return pause;
	}

	public void setPause(boolean pause)
	{
		this.pause = pause;
	}

	public void play()
	{
		this.mediaPlayer.play();
		this.pause = false;
	}

	public void play(final String filePath)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mediaPlayer.playMedia(filePath);
			}
		});
		this.pause = false;
	}

	public void stop()
	{
		this.mediaPlayer.stop();
		this.pause = true;
	}

	public void pause()
	{
		this.mediaPlayer.pause();
		this.pause = true;
	}

	public double getVolume()
	{
		return volume;
	}

	public void setVolume(double volume)
	{
		this.volume = volume;
	}

}