package mensonge.userinterface;

import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.InputFormatException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import mensonge.core.Extraction;
import mensonge.core.Annotation;
import mensonge.core.database.BaseDeDonneesControlleur;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Classe gérant le lecteur vidéo
 *
 */
public class LecteurVideo extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 5373991180139317820L;
	private static final ImageIcon IMG_ICON_BLUE_MARKER = new ImageIcon("images/BlueMarker.png");
	private static final ImageIcon IMG_ICON_RED_MARKER = new ImageIcon("images/RedMarker.png");
	private static final int VOLUME_MAX = 100;
	private static final int VOLUME_DEFAULT = 100;
	private static final int SLIDER_VOLUME_WIDTH = 150;
	private static final int SLIDER_VOLUME_HEIGHT = 30;
	private static final Dimension SLIDER_VOLUME_DIMENSION = new Dimension(SLIDER_VOLUME_WIDTH, SLIDER_VOLUME_HEIGHT);
	private static final int PANEL_MARGIN = 5;

	private JButton boutonLecture;
	private JButton boutonStop;
	private JLabel labelDureeActuelle;
	private JLabel labelDureeMax;
	private SliderWithMarkers slider;
	private JSlider sliderVolume;

	private EmbeddedMediaPlayerComponent vidComp;
	private JButton boutonMarqueur1;
	private JButton boutonMarqueur2;
	private JButton boutonExtract;
	private JButton boutonAnnotation;
	private JButton boutonExportAnnotation;
	private long timeMarqueur1 = -1;
	private long timeMarqueur2 = -1;
	private BaseDeDonneesControlleur bdd;
	private MediaPlayer mediaPlayer;
	private String pathVideo = "";
	private JFrame parent;
	private Extraction extraction;

	private List<Annotation> listDannotation;
	private Annotation annotTemp;
	/**
	 * Créé un lecteur vidéo avec une barre de controle
	 *
	 * @param fichierVideo
	 *            Fichier vidéo à lire
	 * @param bdd
	 *            Base de données de l'application
	 * @param parent
	 */
	public LecteurVideo(final File fichierVideo, BaseDeDonneesControlleur bdd, JFrame parent, Extraction extraction)
	{
		this.listDannotation=new LinkedList();
		this.annotTemp=new Annotation();
		this.extraction = extraction;
		this.parent = parent;
		this.bdd = bdd;
		this.vidComp = new EmbeddedMediaPlayerComponent();
		this.vidComp.setVisible(true);
		this.mediaPlayer = this.vidComp.getMediaPlayer();

		initialiserComposants();
		this.mediaPlayer.addMediaPlayerEventListener(new PlayerEventListener(slider, boutonLecture, labelDureeMax,
				labelDureeActuelle));

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					pathVideo = fichierVideo.getCanonicalPath();
					mediaPlayer.startMedia(fichierVideo.getCanonicalPath());
					mediaPlayer.pause();
					mediaPlayer.setVolume(sliderVolume.getValue());
				}
				catch (IOException e)
				{
					GraphicalUserInterface.popupErreur(e.getMessage());
				}
			}
		});
	}

	/**
	 * Créé une barre de controle avec les boutons des marqueurs, barre de progression,...
	 */
	private void initialiserComposants()
	{
		this.labelDureeActuelle = new JLabel("00:00:00");
		this.labelDureeMax = new JLabel("00:00:00");

		this.boutonLecture = new JButton();
		this.boutonLecture.setIcon(PlayerEventListener.IMG_ICON_LECTURE);
		this.boutonLecture.addActionListener(this);
		this.boutonLecture.setEnabled(true);

		this.boutonMarqueur1 = new JButton(IMG_ICON_RED_MARKER);
		this.boutonMarqueur1.addActionListener(this);
		this.boutonMarqueur1.setEnabled(true);

		this.boutonMarqueur2 = new JButton(IMG_ICON_BLUE_MARKER);
		this.boutonMarqueur2.addActionListener(this);
		this.boutonMarqueur2.setEnabled(true);

		this.boutonExtract = new JButton();
		this.boutonExtract.setText("Extraire");
		this.boutonExtract.addActionListener(this);
		this.boutonExtract.setEnabled(true);

		this.boutonAnnotation = new JButton();
		this.boutonAnnotation.setText("Annoter");
		this.boutonAnnotation.addActionListener(this);
		this.boutonAnnotation.setEnabled(true);;

		this.boutonExportAnnotation = new JButton();
		this.boutonExportAnnotation.setText("Exporter les annotations");
		this.boutonExportAnnotation.addActionListener(this);
		this.boutonExportAnnotation.setEnabled(true);;

		this.boutonStop = new JButton();
		this.boutonStop.setIcon(PlayerEventListener.IMG_ICON_STOP);
		this.boutonStop.addActionListener(this);
		this.boutonStop.setEnabled(true);

		this.sliderVolume = new JSlider(JSlider.HORIZONTAL);
		this.sliderVolume.setPaintTicks(false);
		this.sliderVolume.setPaintLabels(false);
		this.sliderVolume.setMinimum(0);
		this.sliderVolume.setMaximum(VOLUME_MAX);
		this.sliderVolume.setValue(VOLUME_DEFAULT);
		this.sliderVolume.setMinimumSize(SLIDER_VOLUME_DIMENSION);
		this.sliderVolume.setMaximumSize(SLIDER_VOLUME_DIMENSION);
		this.sliderVolume.setPreferredSize(SLIDER_VOLUME_DIMENSION);
		SliderVolumeListener volumeListener = new SliderVolumeListener(this.sliderVolume, this.mediaPlayer);
		this.sliderVolume.addMouseListener(volumeListener);
		this.sliderVolume.addMouseMotionListener(volumeListener);

		this.slider = new SliderWithMarkers(JSlider.HORIZONTAL,this.mediaPlayer);
		SliderPositionEventListener sliderListener = new SliderPositionEventListener(this.slider,
				this.labelDureeActuelle, this.mediaPlayer);
		for (MouseListener m : this.slider.getMouseListeners())
		{
			this.slider.removeMouseListener(m);
		}
		this.slider.addMouseListener(sliderListener);
		this.slider.addMouseMotionListener(sliderListener);
		SliderWithMarkersListener sliderWithMarkersSlider = new SliderWithMarkersListener();
		this.slider.addMouseListener(sliderWithMarkersSlider);
		this.slider.addMouseMotionListener(sliderWithMarkersSlider);

		JPanel panelDuree = new JPanel();
		panelDuree.setLayout(new BoxLayout(panelDuree, BoxLayout.X_AXIS));
		panelDuree.add(Box.createHorizontalStrut(PANEL_MARGIN));
		panelDuree.add(labelDureeActuelle, BorderLayout.WEST);
		panelDuree.add(Box.createHorizontalStrut(PANEL_MARGIN));
		panelDuree.add(slider, BorderLayout.CENTER);
		panelDuree.add(Box.createHorizontalStrut(PANEL_MARGIN));
		panelDuree.add(labelDureeMax, BorderLayout.EAST);
		panelDuree.add(Box.createHorizontalStrut(PANEL_MARGIN));

		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		toolBar.setFloatable(false);
		toolBar.add(boutonLecture);
		toolBar.add(boutonStop);
		toolBar.addSeparator();
		toolBar.add(boutonMarqueur1);
		toolBar.add(boutonMarqueur2);
		toolBar.add(boutonExtract);
		toolBar.add(boutonAnnotation);
		toolBar.add(boutonExportAnnotation);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new JLabel(new ImageIcon("images/Volume.png")));
		toolBar.add(Box.createHorizontalStrut(PANEL_MARGIN));
		toolBar.add(sliderVolume);
		toolBar.add(Box.createHorizontalStrut(PANEL_MARGIN));

		JPanel panelControls = new JPanel(new GridLayout(2, 1));
		panelControls.add(panelDuree);
		panelControls.add(toolBar);

		this.setLayout(new BorderLayout());
		this.add(vidComp, BorderLayout.CENTER);
		this.add(panelControls, BorderLayout.SOUTH);

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
	 * Ferme le lecteur vidéo proprement en fermant les instances de mediaPlayer
	 */
	public void close()
	{
		this.mediaPlayer.release();
		this.vidComp.release();
	}


	public	long getFrameNum(long time)
	{
		return Math.round(Math.floor(time*mediaPlayer.getFps()/1000));
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
		else if (event.getSource() == boutonMarqueur1)
		{
			timeMarqueur1 = mediaPlayer.getTime();
			annotTemp.setDebut(mediaPlayer.getTime());
			System.out.println("time : "+mediaPlayer.getTime());
			slider.setMarkerOneAt(((float) timeMarqueur1 / (float) mediaPlayer.getLength()));
		}
		else if (event.getSource() == boutonMarqueur2)
		{
			timeMarqueur2 = mediaPlayer.getTime();
			annotTemp.setFin(mediaPlayer.getTime());
			slider.setMarkerTwoAt(((float) timeMarqueur2 / (float) mediaPlayer.getLength()));
		}
		else if (event.getSource() == boutonAnnotation)
		{
			if(annotTemp.getDebut()>=0 && annotTemp.getFin()>=0)
			{
				String nom = JOptionPane.showInputDialog(null, "Saisissez le libellé de l'annotation", "Annoter",JOptionPane.QUESTION_MESSAGE);
				if(nom != "")
				{
					long tmp;
					annotTemp.setAnnotation(nom);
					if(annotTemp.getDebut()>annotTemp.getFin())
					{
						tmp=annotTemp.getFin();
						annotTemp.setFin(annotTemp.getDebut());
						annotTemp.setDebut(tmp);
					}
					listDannotation.add(annotTemp);
					annotTemp = new Annotation();
				}
				else
				{
				GraphicalUserInterface.popupErreur("Veuillez placer les deux marqueurs avant tout");
				}
			}
			else
			{
				GraphicalUserInterface.popupErreur("Veuillez saisir un nom de libellé !");
			}
		}
		else if (event.getSource() == boutonExportAnnotation)
		{
			if(!listDannotation.isEmpty())
			{
				exportAnnotations();
			}
		}
		else if (event.getSource() == boutonExtract)
		{
			// On test si il a bien set les 2 marquers et qu'ils ne soient pas placés au même endroit
			if (timeMarqueur1 != -1 && timeMarqueur2 != -1 && (timeMarqueur1 - timeMarqueur2) != 0)
			{
				final String msgErreur = "Extraction : ";
				if (this.mediaPlayer.isPlaying())
				{
					this.mediaPlayer.pause();
				}
				try
				{
					byte[] tabOfByte = null;
					if (timeMarqueur1 < timeMarqueur2)
					{
						tabOfByte = extraction.extraireIntervalle(pathVideo, timeMarqueur1, timeMarqueur2);
					}
					else
					{
						tabOfByte = extraction.extraireIntervalle(pathVideo, timeMarqueur2, timeMarqueur1);
					}
					new DialogueAjouterEnregistrement(parent, "Ajout d'un enregistrement", true, this.bdd, tabOfByte);
				}
				catch (IllegalArgumentException e)
				{
					GraphicalUserInterface.popupErreur(msgErreur + e.getMessage());
				}
				catch (InputFormatException e)
				{
					GraphicalUserInterface.popupErreur(msgErreur + e.getMessage());
				}
				catch (IOException e)
				{
					GraphicalUserInterface.popupErreur(msgErreur + e.getMessage());
				}
				catch (EncoderException e)
				{
					GraphicalUserInterface.popupErreur(msgErreur + e.getMessage());
				}
			}
		}
	}

	private void exportAnnotations()
	{
		JFileChooser fileChooser = new JFileChooser();
		int option = fileChooser.showSaveDialog(this);
		if (option == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFiles() != null)
		{
			try
			{
				final BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.getSelectedFile().getCanonicalPath()),"UTF8"));
				dataOut.write(this.pathVideo+": \n");
				for(Annotation annotation : listDannotation)
				{
					dataOut.write(annotation.toString()+"\n");
				}
				dataOut.close();
				GraphicalUserInterface.popupInfo("Exportation réussie","Succès de l'exportation");
			}
			catch (IOException e)
			{
				GraphicalUserInterface.popupErreur(e.getMessage());
			}
		}
	}

	/**
	 * Listener du slider avec les marqueurs
	 *
	 */
	private class SliderWithMarkersListener extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			setMarkers(e);
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			setMarkers(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			setCursor(Cursor.getDefaultCursor());
		}

		private void setMarkers(MouseEvent e)
		{
			int w = slider.getWidth();
			if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0)
			{
				timeMarqueur1 = valueForXPosition(e.getX());
				annotTemp.setDebut(valueForXPosition(e.getX()));
				slider.setMarkerOneAt((float) (e.getX() - SliderWithMarkers.OFFSET_MARKER) / (float) w);
			}
			else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			{
				timeMarqueur2 = valueForXPosition(e.getX());
				annotTemp.setFin(valueForXPosition(e.getX()));
				slider.setMarkerTwoAt((float) (e.getX() - SliderWithMarkers.OFFSET_MARKER) / (float) w);
			}
		}

		private int valueForXPosition(int x)
		{
			return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
		}
	}
}
