package mensonge.core;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

import mensonge.core.IExtraction;

/**
 * Classe gérant l'extraction d'échantillons ou intervalle d'un flux audio d'un fichier multimédia
 *
 */
public class Extraction implements IExtraction
{
	private static final int NB_SAMPLES = 1024;
	private static Logger logger = Logger.getLogger("logger");

	public static void main(String args[])
	{
		Extraction ext = new Extraction();
		logger.setLevel(Level.INFO);
		logger.log(Level.INFO, "[+] Début de l'extraction de l'intervalle");
		try
		{
			FileOutputStream dataOut = new FileOutputStream("sons/test_sortie.wav");
			byte[] e = ext.extraireIntervalle("sons/test.wmv", 0, 20000);
			dataOut.write(e, 0, e.length);
			dataOut.close();

		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		logger.log(Level.INFO, "[+] Fin de l'extraction de l'intervalle");
/*
 *
 *                double sinusoid[] = new double[1024];
 *                for(int i=0;i<1024;i++)
 *                {
 *                        sinusoid[i] = Math.sin(i);
 *                }
 *                long start = System.currentTimeMillis();
 *                double intervalles[] = ext.extraireEchantillons(new File("sons/test.wav"));
 *                //double intervalles[] = sinusoid;
 *
 *
 *
 *                FFTW3Library fftw = FFTW3Library.INSTANCE;
 *                System.out.println("Interval length: "+intervalles.length);
 *                int n0_in = 2;
 *                int n1_in = intervalles.length/2;
 *                int n_in = intervalles.length;
 *                System.out.println("N_in: "+n_in);
 *                System.out.println("N0_in: "+n0_in);
 *                System.out.println("N1_in: "+n1_in);
 *                int n_out = n0_in*(2*(n1_in/2+1));
 *                System.out.println("N_out: "+n_out);
 *
 *                int inBytes = (Double.SIZE/Byte.SIZE)*n_in;
 *                int outBytes = (Double.SIZE/Byte.SIZE)*n_out;
 *                Pointer in = fftw.fftw_malloc(new NativeLong(inBytes));
 *                Pointer out = fftw.fftw_malloc(new NativeLong(outBytes));
 *                DoubleBuffer inbuf = in.getByteBuffer(0, inBytes).asDoubleBuffer();
 *                DoubleBuffer outbuf = out.getByteBuffer(0, outBytes).asDoubleBuffer();
 *                int flags = FFTW3Library.FFTW_ESTIMATE;
 *
 *                FFTW3Library.fftw_plan planForward = fftw.fftw_plan_dft_r2c_2d(2,n1_in, inbuf, outbuf, flags); // Real to complex
 *
 *                double dest[] = new double[n_out];
 *                inbuf.put(intervalles);
 *                fftw.fftw_execute_dft_r2c(planForward,inbuf,outbuf);
 *                outbuf.get(dest);
 *                for(int i =0;i<n_out;i++)
 *                {
 *                        System.out.println(Math.round(dest[i]));
 *                }
 */
/*

npts = number of points
 *int
 * 795 octave_fftw::fft (const double *in, Complex *out, size_t npts,
 * 796                   size_t nsamples, octave_idx_type stride, octave_idx_type dist)
 * 797 {
 * 798   dist = (dist < 0 ? npts : dist);
 * 799 
 * 800   dim_vector dv (npts, 1);
 * 801   fftw_plan plan = octave_fftw_planner::create_plan (1, dv, nsamples,
 * 802                                                      stride, dist, in, out);
 * 803 
 * 804   fftw_execute_dft_r2c (plan, (const_cast<double *>(in)),
 * 805                          reinterpret_cast<fftw_complex *> (out));
 * 806 
 * 807   // Need to create other half of the transform.
 * 808 
 * 809   convert_packcomplex_1d (out, nsamples, npts, stride, dist);
 * 810 
 * 811   return 0;
 * 812 }
 *
 */
			/*
			 *System.out.println(intervalles[11000][0]);
			 *System.out.println(intervalles[11000][1]);
			 */
//		System.out.println("Done in "+(System.currentTimeMillis()-start)/1000.0+"s !");
	}

	/**
	 * Récupère l'id du premier flux audio du conteneur donné
	 * 
	 * @param container
	 *            Conteneur où récupérer l'id du premier flux audio
	 * @return L'id du premier flux audio
	 */
	private long getFirstAudioStreamId(IContainer container)
	{
		int numStreams = container.getNumStreams();
		long audioStreamId = -1;
		boolean idFound = false;
		int i = 0;
		while (i < numStreams && !idFound)
		{
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				audioStreamId = i;
				idFound = true;
			}
			i++;
		}
		return audioStreamId;
	}

	/**
	 * Extrait les échantillons audio d'un fichier multimédia
	 * 
	 * @param fichier
	 *            Fichier multimédia où extraire les échantillons du premier flux audio trouvé
	 * @return Un tableau de double contenant les échantillons
	 */
	public double[][] extraireEchantillons(String filePath)
	{
		IContainer containerInput = IContainer.make();

		if (containerInput.open(filePath, IContainer.Type.READ, null) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le fichier " + filePath);
			return null;
		}

		long audioStreamId = this.getFirstAudioStreamId(containerInput);

		if (audioStreamId == -1)
		{
			logger.log(Level.WARNING, "Impossible de trouver un flux audio dans le fichier " + filePath);
			return null;
		}
		IStreamCoder audioCoderInput = containerInput.getStream(audioStreamId).getStreamCoder();
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if (audioCoderInput.open(options, unsetOptions) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le flux audio du fichier " + filePath);
			return null;
		}
		int nbChannels = audioCoderInput.getChannels();
		ID codec = audioCoderInput.getCodec().getID();
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

		IPacket packetInput = IPacket.make();
		while (containerInput.readNextPacket(packetInput) >= 0)
		{
			if (packetInput.getStreamIndex() == audioStreamId)
			{
				IAudioSamples samples = IAudioSamples.make(NB_SAMPLES, nbChannels);
				int offset = 0;
				while (offset < packetInput.getSize())
				{
					int bytesDecoded = audioCoderInput.decodeAudio(samples, packetInput, offset);
					if (bytesDecoded < 0)
					{
						logger.log(Level.WARNING, "Erreur de décodage du fichier " + filePath);
					}
					offset += bytesDecoded;
					if (samples.isComplete())
					{
						byteOutput.write(samples.getData().getByteArray(0, samples.getSize()), 0, samples.getSize());
					}
				}
			}
		}
		audioCoderInput.close();
		containerInput.close();

		byte[] audioBytes = byteOutput.toByteArray();
		int bitsBySample = 16;
		int nbSamples = audioBytes.length / nbChannels;
		double doubleArray[] = new double[nbSamples];
		// TODO ne gere que le PCM signé BE/LE 16 bit, faire le reste (8, 24 et 32)
		if (codec.toString().endsWith("BE"))// Big endian
		{
			for (int i = 0; i < nbSamples; i++)
			{
				int msb = audioBytes[2 * i];
				int lsb = audioBytes[2 * i + 1];
				doubleArray[i] = ((msb << 8) | (0xff & lsb)) / 32768.0d;
				// Si c'est du 16bit ça ira de -32768 à +32767 donc pour avoir des double on divise par 32768 ça ira
				// donc de -1 à +1
			}
		}
		else
		{
			for (int i = 0; i < nbSamples; i++)
			{
				int lsb = audioBytes[2 * i];
				int msb = audioBytes[2 * i + 1];
				doubleArray[i] = ((msb << 8) | (0xff & lsb)) / 32768.0d;
				// Si c'est du 16bit ça ira de -32768 à +32767 donc pour avoir des double on divise par 32768 ça ira
				// donc de -1 à +1
			}
		}
		if ((nbSamples % nbChannels) != 0)
		{
			logger.log(Level.WARNING,"Les données audio ne correspondent pas au nombre de canaux");
			return null;
		}
		int nbSamplesChannel = nbSamples / nbChannels;
		return reshape(doubleArray,nbChannels,nbSamplesChannel);
	}

	/**
	 * Transforme un vecteur (tableau à une dimension) en tableau à 2 dimension
	 * @param doubleArray Vecteur qui sera restructuré
	 * @param n Nombre de lignes
	 * @param m Nombre de colonnes
	 * @return Un tableau à 2 dimensions fait à partir du vecteur en entrée 
	 */
	private double[][] reshape(double doubleArray[], int n, int m)
	{
		double reshapeArray[][] = new double[n][m];
		int k = 0;
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
			{
				reshapeArray[i][j] = doubleArray[k++];
			}
		}
		return reshapeArray;
	}
	
	/**
	 * Extrait le flux audio d'un fichier multimédia et le converti en WAV, format PCM Signé 16 bit little endian
	 * @param fichier Fichier multimédia où extraire l'intervalle défini du premier flux audio trouvé
	 * @param debut La borne de début de l'intervalle en millisecondes où commencer l'extraction 
	 * @param fin La borne de fin de l'intervalle en millisecondes où terminer l'extraction
	 * @return Un tableau d'octet contenant le fichier WAV
	 */
	public byte[] extraireIntervalle(String filePath, long debut, long fin)
	{
		IContainer containerInput = IContainer.make();

		if (containerInput.open(filePath, IContainer.Type.READ, null) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le fichier " + filePath);
			return null;
		}

		long audioStreamId = this.getFirstAudioStreamId(containerInput);
		if (audioStreamId == -1)
		{
			logger.log(Level.WARNING, "Impossible de trouver un flux audio dans le fichier " + filePath);
			return null;
		}
		IStreamCoder audioCoderInput = containerInput.getStream(audioStreamId).getStreamCoder();
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if (audioCoderInput.open(options, unsetOptions) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le flux audio du fichier " + filePath);
			return null;
		}

		IContainer containerOutput = IContainer.make();
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		IContainerFormat formatOutput = IContainerFormat.make();
		formatOutput.setOutputFormat("wav", null, null);
		if (containerOutput.open(byteOutput, formatOutput) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le conteneur de sortie");
			return null;
		}
		IStream stream = containerOutput.addNewStream(ICodec.ID.CODEC_ID_PCM_S16LE);

		IStreamCoder audioCoderOutput = stream.getStreamCoder();
		audioCoderOutput.setBitRateTolerance(audioCoderInput.getBitRateTolerance());
		audioCoderOutput.setSampleRate(audioCoderInput.getSampleRate());
		audioCoderOutput.setChannels(audioCoderInput.getChannels());
		audioCoderOutput.setBitRate(audioCoderInput.getBitRate());
		if (audioCoderOutput.open(options, unsetOptions) < 0)
		{
			logger.log(Level.WARNING, "Impossible d'ouvrir le flux audio du conteneur de sortie");
			return null;
		}

		if (containerOutput.writeHeader() < 0)
		{
			logger.log(Level.WARNING, "Impossible d'écrire l'en-tête");
			return null;
		}

		IPacket packetInput = IPacket.make();
		IPacket packetOutput = IPacket.make();
		long actuelMicroSecondes = 0;
		long debutMicroSecondes = debut * 1000;
		long finMicroSecondes = fin * 1000;
		int lastPosOut = 0;
		containerInput.seekKeyFrame(0, debutMicroSecondes, 0);
		while (containerInput.readNextPacket(packetInput) >= 0 && actuelMicroSecondes <= finMicroSecondes)
		{
			if (packetInput.getStreamIndex() == audioStreamId)
			{
				int offset = 0;
				IAudioSamples samples = IAudioSamples.make(NB_SAMPLES, audioCoderInput.getChannels(),
						IAudioSamples.Format.FMT_S16);

				while (offset < packetInput.getSize() && actuelMicroSecondes <= finMicroSecondes)
				{
					int bytesDecoded = audioCoderInput.decodeAudio(samples, packetInput, offset);
					// On ne peut obtenir le timestamp actuel que si on a décodé les samples
					// Global.DEFAULT_PTS_PER_SECOND est en microsecondes pas en milli !

					actuelMicroSecondes = samples.getPts() / Global.DEFAULT_PTS_PER_SECOND;
					if (actuelMicroSecondes <= finMicroSecondes)
					{
						if (bytesDecoded < 0)
						{
							logger.log(Level.WARNING, "Erreur de décodage du fichier " + filePath);
						}
						offset += bytesDecoded;
						if (samples.isComplete())
						{
							int samplesConsumed = 0;
							while (samplesConsumed < samples.getNumSamples())
							{
								int retVal = audioCoderOutput.encodeAudio(packetOutput, samples, samplesConsumed);
								if (retVal <= 0)
								{
									logger.log(Level.WARNING, "Impossible d'encoder le flux audio");
									return null;
								}
								samplesConsumed += retVal;
								if (packetOutput.isComplete())
								{
									packetOutput.setPosition(lastPosOut);
									packetOutput.setStreamIndex(stream.getIndex());
									lastPosOut += packetOutput.getSize();
									containerOutput.writePacket(packetOutput);
								}
							}
						}
					}
				}
			}
		}
		if (containerOutput.writeTrailer() < 0)
		{
			logger.log(Level.WARNING, "Impossible d'écrire la finalisation du fichier");
			return null;
		}
		audioCoderOutput.close();
		audioCoderInput.close();
		containerOutput.close();
		containerInput.close();
		return byteOutput.toByteArray();
	}
}
