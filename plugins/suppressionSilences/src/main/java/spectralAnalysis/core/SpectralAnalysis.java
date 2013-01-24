package spectralAnalysis.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import mensonge.core.IExtraction;
import mensonge.core.database.DBException;
import mensonge.core.database.IBaseDeDonnees;
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
	private List<DrawXYGraph> graphsList = new ArrayList<DrawXYGraph>();
	private List<XYSeries> seriesList = new ArrayList<XYSeries>();

	private void drawGraph(final double[][] echantillons, final float sampleRate, final String fileName)
	{
		double[] hamming = hamming(echantillons.length);
		double[] samplesFFT = new double[echantillons.length];
		DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
		for (int i = 0; i < echantillons.length; i++)
		{
			samplesFFT[i] = echantillons[i][0] * hamming[i];
		}
		fft.realForward(samplesFFT);
		final DrawXYGraph graphFFT = new DrawXYGraph("Analyse du spectre - " + fileName, "Analyse du spectre - "
				+ fileName, "Fréquence (Hz)", "Amplitude");
		graphsList.add(graphFFT);
		final XYSeries series = new XYSeries("Spectre");
		seriesList.add(series);
		for (int j = 0; j < samplesFFT.length; j++)
		{
			series.add(sampleRate * (j / 2 - 1) / samplesFFT.length, Math.abs(samplesFFT[j]));

		}
		XYDataset xyDataset2 = new XYSeriesCollection(series);
		graphFFT.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				graphFFT.removeAll();
				graphFFT.dispose();
				series.clear();
				Runtime.getRuntime().gc();
			}
		});
		graphFFT.addDataset(xyDataset2);
		graphFFT.display();
		samplesFFT = null;
		fft = null;
	}

	private static double[] hamming(int length)
	{
		double[] window = new double[length];
		int m = length / 2;
		double r = Math.PI * 2 / length;
		for (int n = -m; n < m; n++)
		{
			window[m + n] = 0.54 + 0.46 * Math.cos(n * r);
		}
		return window;
	}

	@Override
	public void lancer(IExtraction extraction, Map<Integer, File> listSelectedFiles, IBaseDeDonnees bdd)
	{
		this.isActive = true;
		AudioInputStream inputAIS = null;
		if (!listSelectedFiles.isEmpty())
		{
			for (Integer id : listSelectedFiles.keySet())
			{
				try
				{
					File file = listSelectedFiles.get(id);
					inputAIS = AudioSystem.getAudioInputStream(file);
					AudioFormat audioFormat = inputAIS.getFormat();
					double[][] echantillons = extraction.extraireEchantillons(file.getCanonicalPath());
					this.drawGraph(echantillons, audioFormat.getSampleRate(), bdd.getNomEnregistrement(id));
					echantillons = null;
				}
				catch (IOException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
				catch (UnsupportedAudioFileException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
				catch (DBException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
			}
		}
		this.isActive = false;
		Runtime.getRuntime().gc();
	}

	@Override
	public void stopper()
	{
		for (DrawXYGraph graph : graphsList)
		{
			graph.removeAll();
			graph.dispose();
		}
		graphsList.clear();
		for (XYSeries series : seriesList)
		{
			series.clear();
		}
		seriesList.clear();
		Runtime.getRuntime().gc();
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
