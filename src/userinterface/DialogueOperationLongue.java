package userinterface;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;

import core.BaseDeDonnees.BaseDeDonnees;

public class DialogueOperationLongue extends JDialog
{
	Image image = null;
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
			Thread.sleep(1000*3);
		}
		catch (InterruptedException e){}
		this.setVisible(false);
		return;
	}
}
