package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class SliderPositionEventListener extends MouseAdapter
{
	public static final float SLIDER_POSITION_MAX = 100.0f;
	private JSlider slider;
	private MediaPlayer mediaPlayer;

	public SliderPositionEventListener(JSlider slider, MediaPlayer mediaPlayer)
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
}