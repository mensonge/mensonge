package fondamentale.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import mensonge.core.IExtraction;
import mensonge.core.plugins.Plugin;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

//import fondamentale.userinterface.DrawXYGraph;
import fondamentale.userinterface.Fenetre;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Fondamentale implements Plugin
{
	private static Logger logger = Logger.getLogger("Fondamentale");
	private boolean isActive = false;
	private static final int NB_SAMPLES = 60000;
	private static final int NB_CYCLES = 10;
	private static final int SIZE_BLOC = 10;
	private double retour[];

	private double[] drawGraph(final double[][] echantillons, final float sampleRate)
	{

		// 1/NB_SAMPLES = frequence
		double[] samplesFFT = new double[echantillons.length];
		this.retour = new double[echantillons[0].length];

		for (int j = 0; j < echantillons[0].length; j++)
		{
			for (int i = 0; i < echantillons.length; i++)
			{
				samplesFFT[i] = echantillons[i][j];
			}

			final DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
			fft.realForward(samplesFFT);

			int index = indexBlocMax(SIZE_BLOC, samplesFFT);
			double tableauTmp[] = new double[SIZE_BLOC];
			System.arraycopy(samplesFFT, index, tableauTmp, 0, SIZE_BLOC);
			index += indexMax(tableauTmp);
			double fondamentale = sampleRate * (index / 2 - 1) / samplesFFT.length;
			retour[j] = fondamentale;
		}
		return this.retour;
	}

	@Override
	public void lancer(IExtraction extraction, List<File> listSelectedFiles)
	{
		this.isActive = true;
		List<double[]> resultat = new LinkedList<double[]>();
		int nbColonne = 0;
		if (!listSelectedFiles.isEmpty())
		{
			for (File file : listSelectedFiles)
			{
				try
				{
					AudioInputStream inputAIS = AudioSystem.getAudioInputStream(file);
					AudioFormat audioFormat = inputAIS.getFormat();
					double[][] echantillons = extraction.extraireEchantillons(file.getCanonicalPath());
					double[] tabTmp = this.drawGraph(echantillons, audioFormat.getSampleRate());
					resultat.add(tabTmp);
					
					if(tabTmp.length > nbColonne)
					{
						nbColonne = tabTmp.length;
					}
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
			JTable tableau = Fondamentale.creerTableau(resultat, listSelectedFiles, nbColonne);
			new Fenetre(tableau);
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
	
	public static int indexBlocMax(int tailleBloc, double[] samples)
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
	
	public static int indexMax(double[] samples)
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
	
	public static JTable creerTableau(List<double[]> liste, List<File> fichier, int nbColonne)
	{
		String[] titre = Fondamentale.creerTitre(nbColonne);
		Object[][] data = new Object[fichier.size()][nbColonne + 1];
		int i = 0;
		
		for(double[] canauxFonda : liste)
		{
			data[i][0] = fichier.get(i); 
			for(int j = 0; j < canauxFonda.length; j++)
			{
				data[i][j + 1] = canauxFonda[j];
			}
			i++;
		}
		return new JTable(data, titre);
	}
	
	public static String[] creerTitre(int nbColonne)
	{
		String[] retour = new String[nbColonne + 1];
		retour[0] = "Fichier";
		for(int i = 0; i < nbColonne; i++)
		{
			retour[i + 1] = "Canal " + (i + 1);
		}
		return retour;
	}
}
