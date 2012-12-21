package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class SliderVolumeListener extends MouseAdapter
{
	private JSlider slider;
	private MediaPlayer mediaPlayer;

	public SliderVolumeListener(JSlider slider, MediaPlayer mediaPlayer)
	{
		this.slider = slider;
		this.mediaPlayer = mediaPlayer;
	}

	private int valueForXPosition(int x)
	{
		return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
	}

	private void setNewVolume(int volume)
	{
		slider.setValue(volume);
		mediaPlayer.setVolume(volume);
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		// On ne peut changer la pos qu'avec le clic gauche
		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
		{
			int value = this.valueForXPosition(event.getX());
			this.setNewVolume(value);
		}
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		// On ne peut changer la pos qu'avec le clic gauche
		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
		{
			int value = this.valueForXPosition(event.getX());
			this.setNewVolume(value);
		}
	}
}
