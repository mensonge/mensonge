package fondamentale.userinterface;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class Fenetre extends JFrame
{
	public Fenetre(JTable tableau)
	{
		JScrollPane scrollpane = new JScrollPane(tableau);
		this.getContentPane().add(scrollpane);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setSize(new Dimension(400, 200));
		this.setTitle("Plugin");
		this.setEnabled(true);
		this.setVisible(true);
	}
}
