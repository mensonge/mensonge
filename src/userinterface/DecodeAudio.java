package userinterface;

import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.Utils;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IAudioSamples;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class DecodeAudio extends Thread implements Runnable
{
	private IStreamCoder audioCoder;
	private SourceDataLine mLine;
	private IPacket packet;

	public void setPacket(IPacket packet)
	{
		this.packet = packet;
	}
	public DecodeAudio(IStreamCoder audioCoder)
	{
		this.audioCoder = audioCoder;
		AudioFormat audioFormat = new AudioFormat(audioCoder.getSampleRate(),
				(int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
				audioCoder.getChannels(),
				true, /* xuggler defaults to signed 16 bit samples */
				false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try
		{
			this.mLine = (SourceDataLine) AudioSystem.getLine(info);
			this.mLine.open(audioFormat);
			this.mLine.start();
		}
		catch (LineUnavailableException e)
		{
			throw new RuntimeException("could not open audio line");
		}
	}

	public void run()
	{
		IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
		int offset = 0;
		while(offset < packet.getSize())
		{
			int bytesDecoded2 = audioCoder.decodeAudio(samples, packet, offset);
			if (bytesDecoded2 < 0)
				throw new RuntimeException("got error decoding audio in: ");
			offset += bytesDecoded2;
			if (samples.isComplete())
			{
				byte[] rawBytes = samples.getData().getByteArray(0, samples.getSize());
				mLine.write(rawBytes, 0, samples.getSize());
			}
		}
	}
}
