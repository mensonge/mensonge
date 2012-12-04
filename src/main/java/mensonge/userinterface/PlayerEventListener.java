package mensonge.userinterface;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

public class PlayerEventListener extends MediaPlayerEventAdapter
{
	public static final ImageIcon IMG_ICON_PAUSE = new ImageIcon("images/Pause.png");
	public static final ImageIcon IMG_ICON_LECTURE = new ImageIcon("images/Lecture.png");
	public static final ImageIcon IMG_ICON_STOP = new ImageIcon("images/Stop.png");

	private JSlider slider;
	private JButton boutonLecture;
	private JLabel labelDureeActuelle;
	private JLabel labelDureeMax;

	public PlayerEventListener(JSlider slider, JButton boutonLecture, JLabel labelDureeMax, JLabel labelDureeActuelle)
	{
		this.slider = slider;
		this.boutonLecture = boutonLecture;
		this.labelDureeActuelle = labelDureeActuelle;
		this.labelDureeMax = labelDureeMax;
	}

	@Override
	public void paused(MediaPlayer arg0)
	{
		boutonLecture.setIcon(IMG_ICON_LECTURE);
		boutonLecture.setToolTipText("Lancer");
	}

	@Override
	public void playing(MediaPlayer arg0)
	{
		boutonLecture.setIcon(IMG_ICON_PAUSE);
		boutonLecture.setToolTipText("Mettre en pause");
	}

	@Override
	public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl)
	{
		slider.setValue(0);
		boutonLecture.setIcon(IMG_ICON_LECTURE);
		boutonLecture.setToolTipText("Lancer");
		labelDureeActuelle.setText("00:00:00");
	}

	@Override
	public void timeChanged(MediaPlayer mediaPlayer, long newTime)
	{
		long duree = newTime / 1000;
		int heures = (int) (duree / 3600);
		int minutes = (int) ((duree % 3600) / 60);
		int secondes = (int) ((duree % 3600) % 60);
		labelDureeActuelle.setText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
		slider.setValue((int) (newTime * SliderPositionEventListener.SLIDER_POSITION_MAX / mediaPlayer.getLength()));
	}

	@Override
	public void stopped(MediaPlayer arg0)
	{
		boutonLecture.setIcon(IMG_ICON_LECTURE);
		boutonLecture.setToolTipText("Lancer");
		slider.setValue(0);
		labelDureeActuelle.setText("00:00:00");
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
		mediaPlayer.stop();
	}
}