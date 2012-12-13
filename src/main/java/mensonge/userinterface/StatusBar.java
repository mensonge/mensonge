package mensonge.userinterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.Timer;

public class StatusBar extends JLabel implements ActionListener
{
	private static final long serialVersionUID = 8573623540967463794L;
	private static final int STATUS_BAR_HEIGHT = 16;
	/**
	 * En millisecondes
	 */
	private static final int TIMER_DELAY = 10000;
	private Timer timer;

	public StatusBar()
	{
		super();
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
		setPreferredSize(new Dimension(this.getWidth(), STATUS_BAR_HEIGHT));
		timer = new Timer(TIMER_DELAY, this);
	}

	public void setMessage(String message)
	{
		setText(" " + message);
		repaint();
		this.timer.stop();
	}

	
	public void done()
	{
		this.timer.restart();
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		setText("");
		this.timer.stop();
	}
	
	
}
