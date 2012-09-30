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
import com.xuggle.xuggler.video.IConverter;
import com.xuggle.xuggler.video.ConverterFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.xuggle.xuggler.IAudioSamples;

import java.awt.image.BufferedImage;

public class DecodeVideo extends Thread implements Runnable
{
	private IPacket packet;
	private IStreamCoder videoCoder;
	private ImageComponent mScreen;
	private IVideoResampler resampler;
	private boolean ready;
	public DecodeVideo(IStreamCoder videoCoder,ImageComponent mScreen)
	{
		this.videoCoder = videoCoder;
		this.mScreen = mScreen;
		this.resampler = null;
		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
		{
			resampler = IVideoResampler.make(videoCoder.getWidth(),
					videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
			if (resampler == null)
			{
				throw new RuntimeException("could not create color space resampler for: ");
			}
		}
		this.ready = true;
	}
	public void setPacket(IPacket packet)
	{
		this.packet = packet;
	}
	public boolean isReady()
	{
		return ready;
	}
	public void run()
	{
		this.ready = false;
		long firstTimestampInStream = Global.NO_PTS;
		long systemClockStartTime = 0;
		IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),videoCoder.getWidth(), videoCoder.getHeight());
		BufferedImage javaImage = new BufferedImage(videoCoder.getWidth(),videoCoder.getHeight(), BufferedImage.TYPE_3BYTE_BGR); 
		IConverter converter = ConverterFactory.createConverter(javaImage, IPixelFormat.Type.BGR24); 
		int offset = 0;
		while(offset < packet.getSize())
		{
			int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
			if(bytesDecoded < 0)
			{
				throw new RuntimeException("got error decoding video in");
			}
			offset += bytesDecoded;
			if(picture.isComplete())
			{
				IVideoPicture newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),picture.getWidth(), picture.getHeight());
				if(resampler.resample(newPic, picture) < 0)
				{
					throw new RuntimeException("could not resample video from");
				}
				if(newPic.getPixelType() != IPixelFormat.Type.BGR24)
				{
					throw new RuntimeException("could not decode video as BGR 24 bit data in: ");
				}

				if(firstTimestampInStream == Global.NO_PTS)
				{
					firstTimestampInStream = picture.getTimeStamp();
					systemClockStartTime = System.currentTimeMillis();
				}
				else
				{
					long systemClockCurrentTime = System.currentTimeMillis();
					long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime - systemClockStartTime;
					long millisecondsStreamTimeSinceStartOfVideo = (picture.getTimeStamp() - firstTimestampInStream) / 1000;
					final long millisecondsTolerance = 0; // and we give ourselfs 50 ms of tolerance
					final long millisecondsToSleep =
						(millisecondsStreamTimeSinceStartOfVideo
						 - (millisecondsClockTimeSinceStartofVideo
							 + millisecondsTolerance));
					if(millisecondsToSleep > 0)
					{
						try
						{
							Thread.sleep(millisecondsToSleep);
						}
						catch (InterruptedException e)
						{
							return;
						}
					}
				}
				javaImage = converter.toImage(newPic);
				mScreen.setImage(javaImage);
				mScreen.repaint();
			}
		}
		this.ready = true;
	}
}
