package coeffCepstraux.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import coeffCepstraux.userinterface.DrawTimeGraph;
import coeffCepstraux.userinterface.DrawXYGraph;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class CoefficientsCepstraux implements Plugin
{
	private static Logger logger = Logger.getLogger("coeffCepstraux");
	private boolean isActive = false;
	private DrawXYGraph graph;
	private static final int NB_SAMPLES = 60000;
	private static final int NB_CYCLES = 5;

	private void drawGraph(final double[][] echantillons, final float sampleRate)
	{

		// 1/NB_SAMPLES = frequence
		final double[] samples = new double[echantillons.length];
		final double[] samplesFFT = new double[echantillons.length];

		/*
		 * final double phaseMultiplier = 2 * Math.PI * NB_CYCLES / NB_SAMPLES; for (int i = 0; i < NB_SAMPLES; i++) {
		 * final double cycleX = i * phaseMultiplier; final double sineResult = Math.sin(cycleX); samples[i] =
		 * sineResult; samplesFFT[i] = sineResult; }
		 */
		for (int i = 0; i < echantillons.length; i++)
		{
			samples[i] = echantillons[i][0];
			samplesFFT[i] = echantillons[i][0];
		}

		DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
		fft.realForward(samplesFFT);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				graph = new DrawXYGraph("Variation d'amplitudes", "Variation d'amplitudes", "Temps (Secondes)",
						"Amplitudes");
				final DrawXYGraph graphFFT = new DrawXYGraph("FFT", "FFT", "Fréquence (Hz)", "Amplitudes");

				Thread t1 = new Thread(new Runnable()
				{
					// Juste en affichant les millisecondes
					@Override
					public void run()
					{
						XYSeries series = new XYSeries("Canal 0");
						for (int j = 0; j < samples.length; j++)
						{
							series.add(j / sampleRate, samples[j]);
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
						XYSeries series2 = new XYSeries("FFT");
						for (int j = 0; j < samples.length; j++)
						{
							series2.add(sampleRate * (j / 2 - 1) / samples.length, Math.abs(samplesFFT[j]));

						}
						XYDataset xyDataset2 = new XYSeriesCollection(series2);
						graphFFT.addDataset(xyDataset2);
						graphFFT.display();

					}
				});
				t1.run();
				t2.run();
				try
				{
					t1.join();
					t2.join();

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
				AudioInputStream inputAIS = AudioSystem.getAudioInputStream(test);
				AudioFormat audioFormat = inputAIS.getFormat();
				double[][] echantillons = extraction.extraireEchantillons(test.getCanonicalPath());
				this.drawGraph(echantillons, audioFormat.getSampleRate());
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
