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
import com.xuggle.xuggler.IMetaData;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.xuggle.xuggler.IAudioSamples;

import java.awt.image.BufferedImage;

public class DecodeAndPlayVideo implements Runnable
{
	private ImageComponent mScreen;
	private IStreamCoder videoCoder;
	private IStreamCoder audioCoder;
	private SourceDataLine mLine;
	private IContainer container;
	private IContainer containerAudio;
	private int videoStreamId;
	private int audioStreamId;
	private DecodeAudio decodeAudio;
	private DecodeVideo decodeVideo;

	public DecodeAndPlayVideo(ImageComponent mScreen,String filename)
	{
		this.mScreen = mScreen;
		this.container = IContainer.make();
		this.videoStreamId = -1;
		this.audioStreamId = -1;

		if (container.open(filename, IContainer.Type.READ, null) < 0)
		{
			throw new IllegalArgumentException("could not open file: " + filename);
		}

		int numStreams = container.getNumStreams();

		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				this.audioStreamId = i;
				this.audioCoder = coder;
			}
			else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
			{
				this.videoStreamId = i;
				this.videoCoder = coder;
			}
		}

		if (videoStreamId == -1)
		{
			throw new RuntimeException("could not find video stream in container: "+ filename);
		}
		if (audioStreamId == -1)
		{
			throw new RuntimeException("could not find audio stream in container: "+ filename);
		}
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if (videoCoder.open(options,unsetOptions) < 0)
		{
			throw new RuntimeException("could not open video decoder for container: "+ filename);
		}
		if (audioCoder.open(options,unsetOptions) < 0)
		{
			throw new RuntimeException("could not open audio decoder for container: "+ filename);
		}
		this.decodeAudio = new DecodeAudio(audioCoder);
		this.decodeVideo = new DecodeVideo(videoCoder,mScreen);
	}

	public void run()
	{
		IPacket packet = IPacket.make();
		while (container.readNextPacket(packet) >= 0)
		{
			if (packet.getStreamIndex() == videoStreamId)
			{
				decodeVideo.setPacket(packet);
				decodeVideo.run();
			}
			else if(packet.getStreamIndex() == audioStreamId)
			{
				decodeAudio.setPacket(packet);
				decodeAudio.run();
			}
		}
	}

	public void close()
	{
		if (videoCoder != null)
		{
			videoCoder.close();
			videoCoder = null;
		}
		if (audioCoder != null)
		{
			audioCoder.close();
			audioCoder = null;
		}
		if (container != null)
		{
			container.close();
			container = null;
		}
	}
}
