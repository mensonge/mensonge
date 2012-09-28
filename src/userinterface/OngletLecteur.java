package userinterface;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import com.xuggle.xuggler.*;
public class OngletLecteur extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -2623351725072404024L;

	private String nom;

	private JButton boutonLecture;
	private JSlider slider;

	private File fichierVideo;
    private LecteurVideo leLecteur;

	public OngletLecteur(File fichierVideo)
	{
		this.fichierVideo = fichierVideo;
		this.nom = fichierVideo.getName();

		this.initialiserComposants();
	}

	public void initialiserComposants()
	{
		boutonLecture = new JButton();
		boutonLecture.setToolTipText("Lancer");
		boutonLecture.setIcon(new ImageIcon("images/Lecture.png"));
		boutonLecture.addActionListener(this);
		boutonLecture.setEnabled(true);

		slider = new JSlider();
		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setValue(0);
		slider.setPaintTicks(false);
		slider.setPaintLabels(false);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(boutonLecture);
		toolBar.add(slider);

		leLecteur=new LecteurVideo(this.fichierVideo);

		this.setLayout(new BorderLayout());
		this.add(leLecteur);
		this.add(toolBar, BorderLayout.SOUTH);
	}

	public String getNom()
	{
		return nom;
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(event.getSource() == boutonLecture)
		{
			Thread t2 = new Thread(new Runnable(){
				@SuppressWarnings( "static-addccess" )
				public void run(){
	           		leLecteur.play();
				}
			});
			t2.run();


		}
	}

	public void fermerOnglet()
	{

	}
}
