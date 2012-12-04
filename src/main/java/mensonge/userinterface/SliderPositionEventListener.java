package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import mensonge.core.Utils;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class SliderPositionEventListener extends MouseAdapter
{
	public static final float SLIDER_POSITION_MAX = 100.0f;
	private JSlider slider;
	private MediaPlayer mediaPlayer;
	private JLabel labelDureeActuelle;

	public SliderPositionEventListener(JSlider slider, JLabel labelDureeActuelle, MediaPlayer mediaPlayer)
	{
		this.labelDureeActuelle = labelDureeActuelle;
		this.slider = slider;
		this.mediaPlayer = mediaPlayer;
	}

	private int valueForXPosition(int x)
	{
		return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		int value = this.valueForXPosition(event.getX());
		long newTime = (long) ((value / SLIDER_POSITION_MAX) * mediaPlayer.getLength());
		mediaPlayer.setTime(newTime);
		slider.setValue(value);
		labelDureeActuelle.setText(Utils.getFormattedTime(newTime/1000));
	}

	@Override
	public void mouseMoved(MouseEvent event)
	{
		slider.setToolTipText(Utils.getFormattedTime(getTimeX(event.getX())));
	}
	
	private long getTimeX(int x)
	{
		int positionValue = this.valueForXPosition(x);
		return (long) ((positionValue / SLIDER_POSITION_MAX) * mediaPlayer.getLength() / 1000);
	}
	
}