package spectralAnalysis.core;

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

import spectralAnalysis.userinterface.DrawXYGraph;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SpectralAnalysis implements Plugin
{
	private static Logger logger = Logger.getLogger("spectralAnalysis");
	private boolean isActive = false;

	private void drawGraph(final double[][] echantillons, final float sampleRate)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				double[] samplesFFT = new double[echantillons.length];
				DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
				for (int i = 0; i < echantillons.length; i++)
				{
					samplesFFT[i] = echantillons[i][0];
				}
				fft.realForward(samplesFFT);
				final DrawXYGraph graphFFT = new DrawXYGraph("Analyse du spectre", "Analyse du spectre",
						"Fréquence (Hz)", "Amplitude");
				XYSeries series2 = new XYSeries("Spectre");
				for (int j = 0; j < samplesFFT.length; j++)
				{
					series2.add(sampleRate * (j / 2 - 1) / samplesFFT.length, Math.abs(samplesFFT[j]));

				}
				samplesFFT = null;
				fft = null;
				XYDataset xyDataset2 = new XYSeriesCollection(series2);
				graphFFT.addDataset(xyDataset2);
				graphFFT.display();
			}
		});
	}

	@Override
	public void lancer(IExtraction extraction, List<File> listSelectedFiles)
	{
		this.isActive = true;
		if (!listSelectedFiles.isEmpty())
		{
			for (File file : listSelectedFiles)
			{
				try
				{
					AudioInputStream inputAIS = AudioSystem.getAudioInputStream(file);
					AudioFormat audioFormat = inputAIS.getFormat();
					double[][] echantillons = extraction.extraireEchantillons(file.getCanonicalPath());
					this.drawGraph(echantillons, audioFormat.getSampleRate());
				}
				catch (IOException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
				catch (UnsupportedAudioFileException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
			}
		}
		this.isActive = false;
	}

	@Override
	public void stopper()
	{
		this.isActive = false;
	}

	@Override
	public String getNom()
	{
		return "Analyse du spectre";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
