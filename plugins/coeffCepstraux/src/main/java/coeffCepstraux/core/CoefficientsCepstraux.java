package coeffCepstraux.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import coeffCepstraux.userinterface.DrawTimeGraph;
import coeffCepstraux.userinterface.DrawXYGraph;

public class CoefficientsCepstraux implements Plugin
{
	private static Logger logger = Logger.getLogger("coeffCepstraux");
	private boolean isActive = false;
	private DrawXYGraph graph;
	private DrawTimeGraph timeGraph;

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
				graph = new DrawXYGraph("Canard ?", "Variation d'amplitudes", "Millisecondes", "Amplitudes");
				timeGraph = new DrawTimeGraph("Canard ?", "Variation d'amplitudes", "Temps", "Amplitudes");

				Thread t1 = new Thread(new Runnable()
				{
					// Juste en affichant les millisecondes
					@Override
					public void run()
					{
						XYSeries series = new XYSeries("Canal 0");
						for (int j = 0; j < echantillons.length; j++)
						{
							series.add(j / 44.1, echantillons[j][0]);

						}
						XYDataset xyDataset = new XYSeriesCollection(series);
						graph.addDataset(xyDataset);
						graph.display();

					}
				});
				Thread t2 = new Thread(new Runnable()
				{
					// Avec une échelle de temps
					@Override
					public void run()
					{
						TimeSeries timeSeries = new TimeSeries("Canal 1");
						for (int j = 0; j < echantillons.length; j++)
						{

							timeSeries.addOrUpdate(new Millisecond((int) (j / 44.1), 0, 0, 0, 2, 12, 2012),
									echantillons[j][1]);

						}
						TimeSeriesCollection timeDataset = new TimeSeriesCollection(timeSeries);
						timeGraph.addDataset(timeDataset);
						timeGraph.display();
					}
				});
				 t1.run();
				//t2.run();
				try
				{
					 t1.join();
					//t2.join();
				}
				catch (InterruptedException e)
				{
					logger.log(Level.WARNING, e.getMessage());
				}
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
		if (graph != null)
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
