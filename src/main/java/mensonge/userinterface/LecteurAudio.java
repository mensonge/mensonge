package mensonge.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

/**
 * Classe gérant un lecteur audio
 *
 */
public class LecteurAudio extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 5373991180139317820L;

	private JButton boutonStop;

	private JButton boutonLecture;
	private JLabel labelDureeActuelle;

	private JLabel labelDureeMax;
	private JSlider slider;

	private MediaPlayer mediaPlayer;

	private MediaPlayerFactory factory;

	/**
	 * Créé un nouveau lecteur audio avec une barre de controle
	 */
	public LecteurAudio()
	{
		factory = new MediaPlayerFactory();
		this.mediaPlayer = factory.newHeadlessMediaPlayer();
		this.addControls();
		this.mediaPlayer.addMediaPlayerEventListener(new PlayerEventListener(slider, boutonLecture, labelDureeMax,
				labelDureeActuelle));

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

	/**
	 * Ajoute la barre de controle du lecteur audio
	 */
	private void addControls()
	{
		this.labelDureeActuelle = new JLabel("00:00:00");
		this.labelDureeMax = new JLabel("00:00:00");

		this.boutonLecture = new JButton();
		this.boutonLecture.setIcon(PlayerEventListener.IMG_ICON_LECTURE);
		this.boutonLecture.addActionListener(this);
		this.boutonLecture.setEnabled(true);

		this.boutonStop = new JButton();
		this.boutonStop.setIcon(PlayerEventListener.IMG_ICON_STOP);
		this.boutonStop.addActionListener(this);
		this.boutonStop.setEnabled(true);

		this.slider = new JSlider(JSlider.HORIZONTAL);
		this.slider.setPaintTicks(false);
		this.slider.setPaintLabels(false);
		this.slider.setMinimum(0);
		this.slider.setValue(0);
		this.slider.setMaximum(100);

		JSlider sliderVolume = new JSlider(JSlider.HORIZONTAL);
		sliderVolume.setPaintTicks(false);
		sliderVolume.setPaintLabels(false);
		sliderVolume.setMinimum(0);
		sliderVolume.setMaximum(100);
		sliderVolume.setValue(100);
		sliderVolume.setToolTipText("Volume");
		sliderVolume.setMinimumSize(new Dimension(100, 30));
		sliderVolume.setMaximumSize(new Dimension(100, 30));
		sliderVolume.setPreferredSize(new Dimension(100, 30));
		sliderVolume.addMouseListener(new SliderVolumeListener(sliderVolume, this.mediaPlayer));	


		SliderPositionEventListener sliderListener = new SliderPositionEventListener(this.slider,
				this.labelDureeActuelle, this.mediaPlayer);
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

	/**
	 * Lance la lecture du fichier actuellement chargé
	 */
	public void play()
	{
		this.mediaPlayer.play();
	}

	/**
	 * Lance la lecture d'un fichier audio
	 * @param filePath Chemin du fichier à lire
	 */
	public void play(final String filePath)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mediaPlayer.playMedia(filePath);
			}
		});
	}

	/**
	 * Charge un fichier audio
	 * @param filePath Chemin du fichier audio
	 */
	public void load(final String filePath)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mediaPlayer.startMedia(filePath);
				mediaPlayer.pause();
			}
		});
	}

	/**
	 * Stop la lecture du fichier audio
	 */
	public void stop()
	{
		this.mediaPlayer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == boutonLecture)
		{
			if (this.mediaPlayer.isPlaying())
			{
				this.mediaPlayer.pause();
			}
			else
			{
				this.mediaPlayer.play();
			}
		}
		else if (event.getSource() == boutonStop)
		{
			this.mediaPlayer.stop();
		}
	}

}