package waveForm.core;

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

import waveForm.userinterface.DrawXYGraph;

public class WaveForm implements Plugin
{
	private static Logger logger = Logger.getLogger("waveForm");
	private boolean isActive = false;

	private void drawGraph(final double[][] echantillons, final float sampleRate)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				final DrawXYGraph graph = new DrawXYGraph("Variation d'amplitudes", "Variation d'amplitudes",
						"Temps (Seconde)", "Amplitude");
				XYSeries seriesChannelOne = new XYSeries("Canal 0");
				XYSeries seriesChannelTwo = new XYSeries("Canal 1");
				for (int j = 0; j < echantillons.length; j++)
				{
					seriesChannelOne.add(j / sampleRate, echantillons[j][0]);
					seriesChannelTwo.add(j / sampleRate, echantillons[j][1]);
				}
				XYDataset xyDatasetChannelOne = new XYSeriesCollection(seriesChannelOne);
				XYDataset xyDatasetChannelTwo = new XYSeriesCollection(seriesChannelTwo);
				graph.addDataset(xyDatasetChannelOne);
				graph.addDataset(xyDatasetChannelTwo);
				graph.display();
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
		return "Variation d'amplitudes";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
}
