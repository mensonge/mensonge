package core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class Extraction
{
	static public byte[] extraireEchantillons(File fichier)
	{
		IContainer containerInput = IContainer.make();

		try
		{
			if (containerInput.open(fichier.getCanonicalPath(), IContainer.Type.READ, null) < 0)
				throw new RuntimeException("Impossible d'ouvrir le fichier");
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}

		int numStreams = containerInput.getNumStreams();
		int audioStreamId = -1;
		IStreamCoder audioCoderInput = null;
		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = containerInput.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				audioStreamId = i;
				audioCoderInput = coder;
				break;
			}
		}
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if (audioStreamId == -1)
			throw new RuntimeException("Impossible de trouver un flux audio dans le fichier");
		if (audioCoderInput.open(options, unsetOptions) < 0)
			throw new RuntimeException("Impossible d'ouvrir le flux audio du fichier");
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream();

		IPacket packetInput = IPacket.make();
		while (containerInput.readNextPacket(packetInput) >= 0)
		{
			if (packetInput.getStreamIndex() == audioStreamId)
			{
				try
				{
					byte_out.write(packetInput.getData().getByteArray(0, packetInput.getSize()));
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		audioCoderInput.close();
		containerInput.close();
		return byte_out.toByteArray();
	}

	static public byte[] extraireIntervalle(File fichier, long debut, long fin)
	{
		IContainer containerInput = IContainer.make();

		try
		{
			if (containerInput.open(fichier.getCanonicalPath(), IContainer.Type.READ, null) < 0)
				throw new RuntimeException("Impossible d'ouvrir le fichier");
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}

		int numStreams = containerInput.getNumStreams();
		int audioStreamId = -1;
		IStreamCoder audioCoderInput = null;
		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = containerInput.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				audioStreamId = i;
				audioCoderInput = coder;
				break;
			}
		}
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if (audioStreamId == -1)
			throw new RuntimeException("Impossible de trouver un flux audio dans le fichier");
		if (audioCoderInput.open(options, unsetOptions) < 0)
			throw new RuntimeException("Impossible d'ouvrir le flux audio du fichier");

		IContainer containerOutput = IContainer.make();
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
		IContainerFormat formatOutput = IContainerFormat.make();
		formatOutput.setOutputFormat("wav", null, null);
		if (containerOutput.open(byte_out, formatOutput) < 0)
		{
			try
			{
				throw new Exception("Impossible d'ouvrir le fichier");
			} catch (Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		IStream stream = containerOutput.addNewStream(ICodec.ID.CODEC_ID_PCM_S16LE);

		IStreamCoder audioCoderOutput = stream.getStreamCoder();
		audioCoderOutput.setBitRateTolerance(audioCoderInput.getBitRateTolerance());
		audioCoderOutput.setSampleRate(audioCoderInput.getSampleRate());
		audioCoderOutput.setChannels(audioCoderInput.getChannels());
		audioCoderOutput.setBitRate(audioCoderInput.getBitRate());
		if (audioCoderOutput.open(options, unsetOptions) < 0)
			throw new RuntimeException("could not open coder");

		if (containerOutput.writeHeader() < 0)
			throw new RuntimeException("Problème header");

		IPacket packetInput = IPacket.make();
		IPacket packetOutput = IPacket.make();
		long milliSecondes = 0;
		int lastPos_out = 0;
		containerInput.seekKeyFrame(0, debut, 0);
		while (containerInput.readNextPacket(packetInput) >= 0 && milliSecondes < fin)
		{
			if (packetInput.getStreamIndex() == audioStreamId)
			{
				int offset = 0;
				IAudioSamples samples = IAudioSamples.make(2048, audioCoderInput.getChannels(),
						IAudioSamples.Format.FMT_S16);

				while (offset < packetInput.getSize())
				{
					int bytesDecoded = audioCoderInput.decodeAudio(samples, packetInput, offset);
					// On ne peut obtenir le timestamp actuel que si on a décodé les samples
					// Global.DEFAULT_PTS_PER_SECOND est en microsecondes pas en milli !

					milliSecondes = samples.getPts() / (Global.DEFAULT_PTS_PER_SECOND / 1000);
					if (milliSecondes > fin)
					{
						// On arrete dès que le temps est dépassé
						break;
					}
					if (bytesDecoded < 0)
						throw new RuntimeException("Impossible de décoder");
					offset += bytesDecoded;
					if (samples.isComplete())
					{
						int samplesConsumed = 0;
						while (samplesConsumed < samples.getNumSamples())
						{
							int retVal = audioCoderOutput.encodeAudio(packetOutput, samples, samplesConsumed);
							if (retVal <= 0)
								throw new RuntimeException("Could not encode audio");
							samplesConsumed += retVal;
							if (packetOutput.isComplete())
							{
								packetOutput.setPosition(lastPos_out);
								packetOutput.setStreamIndex(stream.getIndex());
								lastPos_out += packetOutput.getSize();
								containerOutput.writePacket(packetOutput);
							}
						}
					}
				}
			}
		}
		if (containerOutput.writeTrailer() < 0)
			throw new RuntimeException();
		audioCoderOutput.close();
		audioCoderInput.close();
		containerOutput.close();
		containerInput.close();
		return byte_out.toByteArray();
	}

	public static void main(String args[])
	{
		System.out.println("....");
		long start = System.currentTimeMillis();
		try
		{
			FileOutputStream dataOut = new FileOutputStream("sons/test_sortie.wav");
			dataOut.write(extraireEchantillons(new File("sons/test.wmv")));
			dataOut.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Done in "+(System.currentTimeMillis()-start)/1000.0+"s !");
	}
}
