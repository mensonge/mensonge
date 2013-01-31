package silences.core;

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

import silences.userinterface.DrawXYGraph;

public class SuppressionSilences implements Plugin
{
	private static Logger logger = Logger.getLogger("spectralAnalysis");
	private boolean isActive = false;
	private static final double SEUIL = 0.04;
	private List<DrawXYGraph> graphsList = new ArrayList<DrawXYGraph>();
	private List<XYSeries> seriesList = new ArrayList<XYSeries>();

	private void drawGraph(final double[][] echantillons, final float sampleRate, final String fileName)
	{
		final DrawXYGraph graph = new DrawXYGraph("Variation d'amplitudes - " + fileName, "Variation d'amplitudes - "
				+ fileName, "Temps (Seconde)", "Amplitude");
		graphsList.add(graph);
		final XYSeries seriesChannelOne = new XYSeries("Canal 0");
		seriesList.add(seriesChannelOne);
		final XYSeries seriesChannelTwo = new XYSeries("Canal 1");
		seriesList.add(seriesChannelTwo);
		for (int j = 0; j < echantillons.length; j++)
		{
			seriesChannelOne.add(j / sampleRate, echantillons[j][0]);
			seriesChannelTwo.add(j / sampleRate, echantillons[j][1]);
		}
		XYDataset xyDatasetChannelOne = new XYSeriesCollection(seriesChannelOne);
		XYDataset xyDatasetChannelTwo = new XYSeriesCollection(seriesChannelTwo);
		graph.addDataset(xyDatasetChannelOne);
		graph.addDataset(xyDatasetChannelTwo);
		graph.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				graph.removeAll();
				graph.dispose();
				seriesChannelOne.clear();
				seriesChannelTwo.clear();
				Runtime.getRuntime().gc();
			}
		});
		graph.display();
	}

	private double[][] filterAndDelete(double[][] samples)
	{
		double energy;
		List<double[]> rowsToKeep = new ArrayList<double[]>(samples.length);
		for (double[] row : samples)
		{
			for (double value : row)
			{
				energy = Math.pow(Math.abs(value), 2);
				if (energy > SEUIL)
				{
					rowsToKeep.add(row);
				}
			}
		}
		int i = 0;
		double[][] filteredSamples = new double[rowsToKeep.size()][];
		for (double[] row : rowsToKeep)
		{
			filteredSamples[i++] = row;
		}
		return filteredSamples;
	}

	private double[][] filter(double[][] samples)
	{
		double energy;
		for (int i = 0; i < samples.length; i++)
		{
			for (int j = 0; j < samples[i].length; j++)
			{
				energy = Math.pow(Math.abs(samples[i][j]), 2);
				if (energy <= SEUIL)
				{
					samples[i][j] = 0;
				}
			}
		}
		return samples;
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
					this.drawGraph(filter(echantillons), audioFormat.getSampleRate(), bdd.getNomEnregistrement(id)
							+ "_filtré");
					this.drawGraph(filterAndDelete(echantillons), audioFormat.getSampleRate(), bdd.getNomEnregistrement(id)
							+ "_filtré_et_découpé");
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
		return "Suppression des silences";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
