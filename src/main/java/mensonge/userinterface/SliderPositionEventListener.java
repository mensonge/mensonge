package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import mensonge.core.tools.Utils;

import uk.co.caprica.vlcj.player.MediaPlayer;

public class SliderPositionEventListener extends MouseAdapter
{
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
		// On ne peut changer la pos qu'avec le clic gauche
		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
		{
			this.setNewTime(event.getX());
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent event)
	{
		// On ne peut changer la pos qu'avec le clic gauche
		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
		{
			this.setNewTime(event.getX());
		}
	}
	
	private void setNewTime(int x)
	{
		int value = this.valueForXPosition(x);
		long newTime = (long) value;
		mediaPlayer.setTime(newTime);
		slider.setValue(value);
		labelDureeActuelle.setText(Utils.getFormattedTime(newTime / 1000));
	}

	@Override
	public void mouseMoved(MouseEvent event)
	{
		slider.setToolTipText(Utils.getFormattedTime(valueForXPosition(event.getX()) / 1000));
	}
}