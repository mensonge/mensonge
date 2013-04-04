package mensonge.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JSlider;

import mensonge.core.Annotation;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class SliderWithMarkers extends JSlider
{
	public static final int OFFSET_MARKER = 5;

	private static final long serialVersionUID = 1L;
	private static final int NB_SIDES_POLYGON = 3;
	private float position1 = -1.0f;
	private float position2 = -1.0f;
	private MediaPlayer mediaPlayer;
	private Map<Annotation, Color> annotations;

	private JLabel labelAnnotation;

	public SliderWithMarkers(int orientation, MediaPlayer mediaPlayer, JLabel labelAnnotation)
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
		this.annotations = new HashMap<Annotation, Color>();
		this.mediaPlayer = mediaPlayer;
		this.labelAnnotation = labelAnnotation;
	}

	public void setMarkerOneAt(float position)
	{
		position1 = Math.max(position, 0);
		this.repaint();

	}

	public void setMarkerTwoAt(float position)
	{
		position2 = Math.max(position, 0);
		this.repaint();
	}

	public void addAnnotation(Annotation annot)
	{
		Color randomColor = generateRandomColor(new Color(255, 0, 10));
		while (annotations.containsValue(randomColor))
		{
			randomColor = generateRandomColor(new Color(255, 0, 10));
		}
		this.annotations.put(annot, randomColor);
		this.repaint();
	}

	public long getFrameNum(long time)
	{
		return Math.round(Math.floor(time * mediaPlayer.getFps() / 1000));
	}

	public Color generateRandomColor(Color mix)
	{
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		if (mix != null)
		{
			red = (red + mix.getRed()) / 2;
			green = (green + mix.getGreen()) / 2;
			blue = (blue + mix.getBlue()) / 2;
		}

		Color color = new Color(red, green, blue);
		return color;
	}

	protected void paintComponent(Graphics g)
	{
		int sliderHeight = getHeight();
		int sliderWidth = getWidth();
		long lastFrame = getFrameNum(mediaPlayer.getLength());
		long actualFrame = getFrameNum(this.getValue());
        labelAnnotation.setText("");
		for (Entry<Annotation, Color> entry : this.annotations.entrySet())
		{
			Annotation annotation = entry.getKey();
			//System.out.println(annotation);
			Color color = entry.getValue();
			g.setColor(color);
			int x1 = Math.round(((float) annotation.getDebut() / (float) lastFrame) * sliderWidth);
			int x2 = Math.round(((float) annotation.getFin() / (float) lastFrame) * sliderWidth);
			g.fillRect(x1 + OFFSET_MARKER, 0, x2 - x1, sliderHeight);
			if(actualFrame >= annotation.getDebut() && actualFrame <= annotation.getFin())
			{
				labelAnnotation.setText("<html><b>"+annotation.getAnnotation()+"</b></html>");
				labelAnnotation.setForeground(color);
			}
        }
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
		super.paintComponent(g);
	}
}
