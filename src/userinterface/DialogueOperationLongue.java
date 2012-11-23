package userinterface;

import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class DialogueOperationLongue extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7204918681564497456L;

	private Image image = null;

	public DialogueOperationLongue(JFrame parent, String title, boolean modal)
	{
		super(parent, title, modal);
		image = getToolkit().getImage("loading.gif");
		PaneauDialogueOperation pan = new PaneauDialogueOperation(image);

		this.setContentPane(pan);
		this.setLocationRelativeTo(null);
		this.setTitle("Chargement");
		this.setSize(100, 100);
	}

	public void exporterBase()
	{
		this.setVisible(true);
		try
		{
			Thread.sleep(1000 * 3);
		}
		catch (InterruptedException e)
		{
		}
		this.setVisible(false);
		return;
	}
}
