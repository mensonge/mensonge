package mensonge.userinterface;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoResampler;

public class Sound extends JPanel
{
	private static final long serialVersionUID = 5373991180139317820L;
	private File fichierVideo;
	private JButton boutonLecture;

	private JSlider slider;
	private IMediaReader reader;
	private long duration;
	private double volume; // Entre 0 et 1
	private boolean pause;
	private boolean stop;
	private SourceDataLine mLine;
	private JSlider sliderAvance;

	public Sound(File fichierVideo, JSlider slider, double vol)
	{
		this.volume = vol;
		this.pause = true;
		this.stop = true;
		this.fichierVideo = fichierVideo;
		this.sliderAvance = slider;

		try
		{
			this.reader = ToolFactory.makeReader(this.fichierVideo.getCanonicalPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		this.reader.open();

		this.duration = this.reader.getContainer().getDuration() / 1000;
		this.sliderAvance.setMaximum((int) duration);
		this.sliderAvance.setMinimum(0);
		this.sliderAvance.setValue(0);

		this.ouvrirAudio();
		this.ajoutListener();
	}

	public boolean isPause()
	{
		return pause;
	}

	public void setPause(boolean pause)
	{
		this.pause = pause;
	}

	private void ajoutListener()
	{
		if (this.reader.isOpen())
		{
			MediaListenerAdapter adapter = new LecteurEvent();
			this.reader.addListener(adapter);
		}
	}

	private void ouvrirAudio()
	{
		if (this.reader.isOpen())
		{
			IContainer container = reader.getContainer();
			IStreamCoder audioCoder = null;
			int numStreams = container.getNumStreams();
			int audioStreamId = -1;
			for (int i = 0; i < numStreams; i++)
			{
				IStream stream = container.getStream(i);
				IStreamCoder coder = stream.getStreamCoder();
				if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
				{
					audioStreamId = i;
					audioCoder = coder;
					break;
				}
			}
			if (audioStreamId == -1)
			{
				throw new RuntimeException("could not find audio stream");
			}
			IMetaData options = IMetaData.make();
			IMetaData unsetOptions = IMetaData.make();
			if (audioCoder.open(options, unsetOptions) < 0)
			{
				throw new RuntimeException("could not open audio decoder");
			}
			AudioFormat audioFormat = new AudioFormat(audioCoder.getSampleRate(),
					(int) IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()), audioCoder.getChannels(),
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
	}

	public void play()
	{
		this.pause = false;
		this.stop = false;

		if (!this.reader.isOpen())
		{
			this.ouvrirAudio();
		}
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (reader.readPacket() == null && !stop)
				{
					while (pause)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
			}
		}).start();
	}

	public void pause()
	{
		this.pause = true;
	}

	public void stop()
	{
		this.stop = true;
		this.pause = true;
		if (this.reader.isOpen())
		{
			this.reader.close();
			this.mLine.close();
		}
		this.sliderAvance.setValue(0);
	}

	public double getVolume()
	{
		return volume;
	}

	public void setVolume(double volume)
	{
		this.volume = volume;
	}

	class LecteurEvent extends MediaListenerAdapter
	{
		private IVideoResampler videoResampler = null;
		private int width = 0;// mediaPlayerComponent.getWidth();
		private int height = 0;// mediaPlayerComponent.getHeight();

		@Override
		public void onAudioSamples(IAudioSamplesEvent event)
		{
			long millisecondes = event.getTimeStamp(TimeUnit.MICROSECONDS) / 1000;
			sliderAvance.setValue((int) (millisecondes));

			IAudioSamples samples = event.getAudioSamples();
			ShortBuffer buffer = samples.getByteBuffer().asShortBuffer();
			for (int i = 0; i < buffer.limit(); ++i)
			{
				buffer.put(i, (short) (buffer.get(i) * volume));
			}
			byte[] rawBytes = samples.getData().getByteArray(0, samples.getSize());
			mLine.write(rawBytes, 0, samples.getSize());
		}
	}
}