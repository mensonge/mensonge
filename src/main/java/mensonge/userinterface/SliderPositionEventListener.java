package mensonge.userinterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import mensonge.core.tools.Utils;

import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Listener pour les sliders de position des lecteurs
 * 
 */
public class SliderPositionEventListener extends MouseAdapter
{
	private JSlider slider;
	private MediaPlayer mediaPlayer;
	private JLabel labelDureeActuelle;

	/**
	 * Créé un nouveau listener pour le slider permettant de changer le label de la position actuelle et le changement
	 * de position de la vidéo
	 * 
	 * @param slider
	 *            Slider à écouter
	 * @param labelDureeActuelle
	 *            Label de la position actuelle sur la vidéo en durée hh:mm:ss
	 * @param mediaPlayer
	 *            MediaPlayer de ma vidéo
	 */
	public SliderPositionEventListener(JSlider slider, JLabel labelDureeActuelle, MediaPlayer mediaPlayer)
	{
		this.labelDureeActuelle = labelDureeActuelle;
		this.slider = slider;
		this.mediaPlayer = mediaPlayer;
	}

	/**
	 * Récupère la valeur à l'abscisse donnée
	 * 
	 * @param x
	 *            Abscisse sur le slider
	 * @return La valeur à l'abscisse donnée
	 */
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

	/**
	 * Défini le nouveau temps où l'on se trouve dans la vidéo en fonction de l'endroit cliqué
	 * 
	 * @param x
	 *            Abscisse sur le slider où l'utilisateur à cliqué
	 */
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