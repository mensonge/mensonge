package coeffCepstraux.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import coeffCepstraux.userinterface.DrawXYGraph;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class CoefficientsCepstraux implements Plugin
{
	private static Logger logger = Logger.getLogger("coeffCepstraux");
	private boolean isActive = false;
	private List<DrawXYGraph> graphsList = new ArrayList<DrawXYGraph>();
	private List<XYSeries> seriesList = new ArrayList<XYSeries>();

	private void drawGraph(final double[][] echantillons, final float sampleRate, final String fileName)
	{
		double[] samples = new double[echantillons.length];
		double[] samplesFFT = new double[echantillons.length];
		double[] samplesCepstre = new double[echantillons.length];

		for (int i = 0; i < echantillons.length; i++)
		{
			samples[i] = echantillons[i][0];
		}
		System.arraycopy(samples, 0, samplesFFT, 0, samples.length);

		DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
		fft.realForward(samplesFFT);
		for (int i = 0; i < samplesFFT.length; i++)
		{
			samplesCepstre[i] = Math.log(Math.abs(samplesFFT[i]));
		}
		samples = null;
		samplesFFT = null;
		fft.realInverse(samplesCepstre, true);
		final DrawXYGraph graphCepstre = new DrawXYGraph("Cepstre - "+fileName, "Cepstre - "+fileName, "Quéfrence (Hz)", "Amplitude");

		graphsList.add(graphCepstre);
		final XYSeries series = new XYSeries("Cepstre");
		seriesList.add(series);
		for (int j = 0; j < samplesCepstre.length; j++)
		{
			series.add(sampleRate * (j / 2 - 1) / samplesCepstre.length, samplesCepstre[j]);
		}

		XYDataset xyDataset2 = new XYSeriesCollection(series);
		graphCepstre.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				graphCepstre.removeAll();
				graphCepstre.dispose();
				series.clear();
				Runtime.getRuntime().gc();
			}
		});
		graphCepstre.addDataset(xyDataset2);
		graphCepstre.display();
		fft = null;
		samplesCepstre = null;
		xyDataset2 = null;
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
		for(DrawXYGraph graph : graphsList)
		{
			graph.removeAll();
			graph.dispose();
		}
		graphsList.clear();
		for(XYSeries series : seriesList)
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
		return "Cepstre";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
