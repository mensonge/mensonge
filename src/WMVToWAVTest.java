import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;

class WMVToWAVTest
{
	public static void main(String[] args)
	{
		String filePath = "sons/Primus_John_the_Fisherman.wmv";
		IContainer containerInput = IContainer.make();

		if(containerInput.open(filePath, IContainer.Type.READ, null) < 0)
			throw new RuntimeException("Impossible d'ouvrir le fichier : " + filePath);

		int numStreams = containerInput.getNumStreams();
		int audioStreamId = -1;
		IStreamCoder audioCoder = null;

		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = containerInput.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if(coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				// Récupération du numéro du flux audio
				audioStreamId = i;
				audioCoder = coder;
				break;
			}
		}

		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if(audioStreamId == -1)
			throw new RuntimeException("Impossible de trouver un flux audio dans le fichier : " + filePath);
		if(audioCoder.open(options, unsetOptions) < 0)
			throw new RuntimeException("Impossible d'ouvrir le flux audio du fichier : " + filePath);

		int channels = audioCoder.getChannels();

		IMediaWriter writer = ToolFactory.makeWriter("sons/test.wav");
		int streamIndex = writer.addAudioStream(0, 0, ICodec.ID.CODEC_ID_PCM_S16LE, channels,
				audioCoder.getSampleRate());
		System.out.println("[i] Fichier wmv : " + containerInput.getURL());
		System.out.println("[i] Durée vidéo wmv : " + containerInput.getDuration());
		System.out.println("[i] Échantilonnage wmv : " + audioCoder.getSampleRate());
		System.out.println("[i] Débit wmv : " + audioCoder.getBitRate());
		System.out.println("[i] Codec wmv : " + audioCoder.getCodecID());
		System.out.println("[i] Nb canaux wmv : " + audioCoder.getChannels());
		System.out.print("[i] Décodage en cours...");

		long debutMilli = 0;// Défini où l'on commencer, borne incluse
		long finMilli = 22000;// Défini où l'on s'arrête, borne exclue
		containerInput.seekKeyFrame(0, debutMilli, 0);// Position le curseur pour dire où l'on doit commencer à
														// récupérer les paquets

		IPacket packet = IPacket.make();
		int offset = 0;
		int bytesDecoded = 0;
		long milliSecondes = 0;
		while(containerInput.readNextPacket(packet) >= 0 && milliSecondes < finMilli)
		{
			if(packet.getStreamIndex() == audioStreamId)
			{
				// Si on récupère un paquet correspondant au flux audio
				offset = 0;
				IAudioSamples samples = IAudioSamples.make(1024, channels, IAudioSamples.Format.FMT_S16);

				while(offset < packet.getSize())
				{
					bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
					// On ne peut obtenir le timestamp actuel que si on a décodé les samples
					// Global.DEFAULT_PTS_PER_SECOND est en microsecondes pas en milli !

					milliSecondes = samples.getPts() / (Global.DEFAULT_PTS_PER_SECOND / 1000);
					if(milliSecondes > finMilli)
					{
						// On arrete dès que le temps est dépassé
						break;
					}
					if(bytesDecoded < 0)
						throw new RuntimeException("Impossible de décoder : " + filePath);

					offset += bytesDecoded;
					if(samples.isComplete())
					{
						writer.encodeAudio(streamIndex, samples);
					}
				}
			}
		}
		writer.close();
		audioCoder.close();
		containerInput.close();
		System.out.println(" Done !");
	}
}