package coeffCepstraux.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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

import coeffCepstraux.userinterface.DrawXYGraph;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Fondamentale implements Plugin
{
	private static Logger logger = Logger.getLogger("Fondamentale");
	private boolean isActive = false;
	private static final int NB_SAMPLES = 60000;
	private static final int NB_CYCLES = 10;
	private static final int SIZE_BLOC = 10;

	private void drawGraph(final double[][] echantillons, final float sampleRate)
	{

		// 1/NB_SAMPLES = frequence
		final double[] samples = new double[echantillons.length];
		final double[] samplesFFT = new double[echantillons.length];


		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < echantillons.length; i++)
				{
					samples[i] = echantillons[i][0];
					samplesFFT[i] = echantillons[i][0];
				}

				final DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
				fft.realForward(samplesFFT);
				
				int index = indexBlocMax(SIZE_BLOC, samplesFFT);
				double tableauTmp[] = new double[SIZE_BLOC];
				System.arraycopy(samplesFFT, index, tableauTmp, 0, SIZE_BLOC);
				index += indexMax(tableauTmp);

				final DrawXYGraph graphCepstre = new DrawXYGraph("Fondamentale", "Fondamentale", "Quéfrence (Hz)", "Amplitude");
				XYSeries series2 = new XYSeries("Fondamentale");
				for (int j = 0; j < samplesFFT.length; j++)
				{
					if(j != index)
					{
						series2.add(sampleRate * (j / 2 - 1) / samplesFFT.length, 0);
					}
					else
					{
						series2.add(sampleRate * (j / 2 - 1) / samplesFFT.length, samplesFFT[j]);
					}
						
				}
				XYDataset xyDataset2 = new XYSeriesCollection(series2);
				graphCepstre.addDataset(xyDataset2);
				graphCepstre.display();
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
		return "Fondamentale";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}
	
	public int indexBlocMax(int tailleBloc, double[] samples)
	{
		int retour = 0;
		double max = 0, tmp = 0;
		for(int i = 0; i < tailleBloc; i++)
		{
			max += samples[i];
		}
		
		for(int i = tailleBloc; i < samples.length; i += tailleBloc)
		{
			
			tmp = 0;
			for(int j = 0; j < tailleBloc && i+j < samples.length; j++)
			{
				if(i+j >= samples.length)
				{
					break;
				}
				tmp += samples[i+j];
			}
			if(tmp > max)
			{
				max = tmp;
				retour = i;
			}
		}
		return retour;
	}
	
	public int indexMax(double[] samples)
	{
		int retour = 0;
		double max = samples[0];
		for(int i = 0; i < samples.length; i++)
		{
			if(samples[i] > max)
			{
				max = samples[i];
				retour = i;
			}
		}
		return retour;
	}
}
