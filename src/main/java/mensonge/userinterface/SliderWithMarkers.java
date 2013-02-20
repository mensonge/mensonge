package mensonge.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.LinkedList;

import javax.swing.JSlider;

import uk.co.caprica.vlcj.player.MediaPlayer;

import mensonge.core.Annotation;

public class SliderWithMarkers extends JSlider
{
	public static final int OFFSET_MARKER = 5;

	private static final long serialVersionUID = 1L;
	private static final int NB_SIDES_POLYGON = 3;
	private float position1 = -1.0f;
	private float position2 = -1.0f;
	private MediaPlayer mediaPlayer;
	private LinkedList<Annotation> listOfAnnots;

	public SliderWithMarkers(int orientation,MediaPlayer mediaPlayer)
	{
		super(orientation);
		this.setOpaque(false);
		this.setPaintTicks(false);
		this.setPaintLabels(false);
		this.setSnapToTicks(false);
		this.setMajorTickSpacing(1);
		this.setMinorTickSpacing(0);
		this.setMinimum(0);
		this.setValue(0);
		this.setMaximum(1);
		this.listOfAnnots = new LinkedList<Annotation>();
		this.mediaPlayer = mediaPlayer;
	}

	public void setMarkerOneAt(float position)
	{
		position1 = Math.max(position, 0);
		//	System.out.println(getTimeFrame(mediaPlayer.getTime()));
		System.out.println("1 :: "+getTimeFrame(Math.round(Math.floor(mediaPlayer.getTime()*mediaPlayer.getFps()))));
		this.repaint();

	}

	public void setMarkerTwoAt(float position)
	{
		position2 = Math.max(position, 0);
		System.out.println("2 :: "+getTimeFrame(Math.round(Math.floor(mediaPlayer.getTime()*mediaPlayer.getFps()))));
		this.repaint();
	}

	public void addAnnots(Annotation annot)
	{
		this.listOfAnnots.add(annot);
	}

	public	long getTimeFrame(long frame)
	{
			System.out.println("Dans getTimeFrame : "+ Math.round(Math.floor(1000*((float)frame/(float)mediaPlayer.getLength()))));
		return Math.round(Math.floor(1000*((float)frame/(float)mediaPlayer.getLength())));
		//		return Math.round(Math.floor(time*mediaPlayer.getFps()/1000));
	}

	protected void paintComponent(Graphics g)
	{
		int sliderHeight = getHeight();
		int sliderWidth = getWidth();
		if (position1 >= 0)
		{
			g.setColor(Color.RED);
			int[] polygoneX1 = new int[NB_SIDES_POLYGON];
			int[] polygoneY1 = new int[NB_SIDES_POLYGON];
			int pos = Math.round(position1 * sliderWidth);
			if (pos + OFFSET_MARKER * 2 > sliderWidth)
			{
				pos = sliderWidth - OFFSET_MARKER * 2;
			}

			polygoneX1[0] = pos;
			polygoneX1[1] = pos + OFFSET_MARKER;
			polygoneX1[2] = pos + OFFSET_MARKER * 2;
			polygoneY1[0] = sliderHeight;
			polygoneY1[1] = sliderHeight - 7;
			polygoneY1[2] = sliderHeight;
			g.fillPolygon(polygoneX1, polygoneY1, polygoneX1.length);
			g.fillRect(pos + OFFSET_MARKER, 0, 1, sliderHeight);

		}
		if (position2 >= 0)
		{
			g.setColor(Color.BLUE);
			int[] polygoneX2 = new int[NB_SIDES_POLYGON];
			int[] polygoneY2 = new int[NB_SIDES_POLYGON];
			int pos = Math.round(position2 * sliderWidth);
			if (pos + OFFSET_MARKER * 2 > sliderWidth)
			{
				pos = sliderWidth - OFFSET_MARKER * 2;
			}

			polygoneX2[0] = pos;
			polygoneX2[1] = pos + OFFSET_MARKER;
			polygoneX2[2] = pos + OFFSET_MARKER * 2;
			polygoneY2[0] = 0;
			polygoneY2[1] = 7;
			polygoneY2[2] = 0;
			g.fillPolygon(polygoneX2, polygoneY2, polygoneY2.length);
			g.fillRect(pos + OFFSET_MARKER, 0, 1, sliderHeight);
		}
		for(Annotation annot : this.listOfAnnots)
		{
			//			int pos = Math.round(annot.getDebut() * sliderWidth);

		}
		super.paintComponent(g);
	}
}
