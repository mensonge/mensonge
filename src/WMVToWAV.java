import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IMetaData;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;

class WMVToWAV
{
	public static void main(String[] args)
	{
		String filePath = "sons/PRIMUS_John_THe_Fisherman.wmv";
		IContainer containerInput = IContainer.make();

		if(containerInput.open(filePath, IContainer.Type.READ, null) < 0)
			throw new RuntimeException("Impossible d'ouvrir le fichier : " + filePath);

		int numStreams = containerInput.getNumStreams();

		int videoStreamId = -1;
		IStreamCoder videoCoder = null;
		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = containerInput.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if(coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
			{
				videoStreamId = i;
				videoCoder = coder;
				break;
			}
		}
		IMetaData options = IMetaData.make();
		IMetaData unsetOptions = IMetaData.make();
		if(videoStreamId == -1)
			throw new RuntimeException("Impossible de trouver un flux audio dans le fichier : " + filePath);
		if(videoCoder.open(options, unsetOptions) < 0)
			throw new RuntimeException("Impossible d'ouvrir le flux audio du fichier : " + filePath);
		/*
		 * IContainer containerOutput = IContainer.make();
		 * 
		 * if(containerOutput.open("sons/test.wav", IContainer.Type.WRITE, null) < 0) { throw new
		 * Exception("Impossible d'ouvrir le fichier"); } IStream stream =
		 * containerOutput.addNewStream(ICodec.ID.CODEC_ID_PCM_S16LE);
		 * 
		 * IStreamCoder coder = stream.getStreamCoder();
		 * 
		 * coder.setSampleRate(audioCoder.getSampleRate()); coder.setChannels(audioCoder.getChannels()); coder.setBitRate(64000); if(coder.open() < 0) throw new
		 * RuntimeException("could not open coder");
		 * 
		 * if(containerOutput.writeHeader() < 0) throw new RuntimeException("Problème header");
		 */
		IMediaWriter writer = ToolFactory.makeWriter("sons/test2.wmv");
		int streamIndex = writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_WMV2,videoCoder.getWidth(), videoCoder.getHeight());
		System.out.println("Durée vidéo wmv : "+containerInput.getDuration());
		System.out.println("Échantilonnage wmv : "+videoCoder.getSampleRate());
		System.out.println("Débit wmv : "+videoCoder.getBitRate());
		System.out.println("Codec wmv : "+videoCoder.getCodecID());
		System.out.println("Nb canaux wmv : "+videoCoder.getChannels());

		IPacket packet = IPacket.make();
		
		int offset = 0;
		int bytesDecoded = 0;
		while(containerInput.readNextPacket(packet)  >= 0)
		{
			if(packet.getStreamIndex() == videoStreamId)
			{
				// containerOutput.writePacket(packet);
				offset = 0;
				IVideoPicture pictures = IVideoPicture
						.make(videoCoder.getPixelType(),videoCoder.getWidth(),videoCoder.getHeight());

				while(offset < packet.getSize())
				{
					bytesDecoded = videoCoder.decodeVideo(pictures, packet, offset);
					if(bytesDecoded < 0)
						throw new RuntimeException("Impossible de décoder : " + filePath);

					offset += bytesDecoded;
					if(pictures.isComplete())
					{
						writer.encodeVideo(streamIndex, pictures);
					}
				}
			}
		}
		/*
		 * if(containerOutput.writeTrailer() < 0) throw new RuntimeException();containerOutput.close();
		 */
		IContainer containerOutput = IContainer.make();

		if(containerOutput.open("sons/test2.wmv", IContainer.Type.READ, null) < 0)
			throw new RuntimeException("Impossible d'ouvrir le fichier : " + filePath);
		IStreamCoder videoCoderOutput = null;
		numStreams = containerOutput.getNumStreams();
		for (int i = 0; i < numStreams; i++)
		{
			IStream stream = containerOutput.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if(coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
			{
				videoStreamId = i;
				videoCoderOutput = coder;
				break;
			}
		}
		if(videoStreamId == -1)
			throw new RuntimeException("Impossible de trouver un flux audio dans le fichier : " + filePath);
		if(videoCoderOutput.open(options, unsetOptions) < 0)
			throw new RuntimeException("Impossible d'ouvrir le flux audio du fichier : " + filePath);
		System.out.println("Durée fichier wav : "+containerOutput.getDuration());
		System.out.println("Échantilonnage wav : "+videoCoderOutput.getSampleRate());
		System.out.println("Débit wav : "+videoCoderOutput.getBitRate());
		System.out.println("Codec wav : "+videoCoderOutput.getCodecID());
		System.out.println("Nb canaux wav : "+videoCoderOutput.getChannels());

		videoCoderOutput.close();
		videoCoder.close();
		containerOutput.close();
		containerInput.close();
		System.out.println("Done !");
	}
}