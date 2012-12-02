package coeffCepstraux.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import coeffCepstraux.userinterface.DrawXYGraph;

public class CoefficientsCepstraux implements Plugin
{
	private static Logger logger = Logger.getLogger("coeffCepstraux");
	private boolean isActive = false;
	private DrawXYGraph graph;

	private void drawGraph(final double[][] echantillons)
	{
		/*
		 * XYSeries series = new XYSeries("Canard !"); final double phaseMultiplier = 2 * Math.PI * 5 / 500;
		 * 
		 * for (int i = 0; i < 500; i++) { final double cycleX = i * phaseMultiplier; final double sineResult =
		 * Math.sin(cycleX); series.add(cycleX, sineResult); }
		 */

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				graph = new DrawXYGraph("Canard ?", "Variation d'amplitudes");
				Thread t1 = new Thread(new Runnable()
				{
					
					@Override
					public void run()
					{
						XYSeries series = new XYSeries("Canal " + 0);
						for (int j = 0; j < echantillons.length; j++)
						{
							series.add(j, echantillons[j][0]);

						}
						XYDataset xyDataset = new XYSeriesCollection(series);
						graph.addDataset(xyDataset);
					}
				});
				Thread t2 = new Thread(new Runnable()
				{
					
					@Override
					public void run()
					{
						XYSeries series = new XYSeries("Canal " +1);
						for (int j = 0; j < echantillons.length/4; j++)
						{
							series.add(j*44100, echantillons[j][1]);

						}
						XYDataset xyDataset = new XYSeriesCollection(series);
						graph.addDataset(xyDataset);
					}
				});
				t1.run();
			//	t2.run();
				try
				{
					t1.join();
				//	t2.join();
				}
				catch (InterruptedException e)
				{
					logger.log(Level.WARNING, e.getMessage());
				}
				graph.display();
			}
		});
	}

	@Override
	public void lancer(IExtraction extraction, List<File> listeFichiersSelectionnes)
	{
		this.isActive = true;
		if (!listeFichiersSelectionnes.isEmpty())
		{
			File test = listeFichiersSelectionnes.get(0);
			try
			{
				double[][] echantillons = extraction.extraireEchantillons(test.getCanonicalPath());
				this.drawGraph(echantillons);

			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, e.getMessage());
			}
			catch (UnsupportedAudioFileException e)
			{
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		this.isActive = false;
	}

	@Override
	public void stopper()
	{
		this.isActive = false;
		if(graph != null)
		{
			graph.dispose();
		}
	}

	@Override
	public String getNom()
	{
		return "Coefficients cepstraux";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
