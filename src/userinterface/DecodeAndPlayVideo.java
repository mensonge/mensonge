
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


import java.awt.image.BufferedImage;


public class DecodeAndPlayVideo {
	private ImageComponent mScreen;
	private IStreamCoder videoCoder;
	private IContainer container;

	@SuppressWarnings("deprecation")
	public DecodeAndPlayVideo(ImageComponent mScreen) {
		this.mScreen = mScreen;
	}

	@SuppressWarnings("deprecation")
	public void PlayVideo(String filename) {
		if (!IVideoResampler.isSupported(
					IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION)) {
			throw new RuntimeException("you must install the GPL version"
					+ " of Xuggler (with IVideoResampler support) for "
					+ "this demo to work");
					}

		container = IContainer.make();

		if (container.open(filename, IContainer.Type.READ, null) < 0) {
			throw new IllegalArgumentException("could not open file: " + filename);
		}

		int numStreams = container.getNumStreams();
		int videoStreamId = -1;

		for (int i = 0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoCoder = coder;
				break;
			}
		}

		if (videoStreamId == -1) {
			throw new RuntimeException("could not find video stream in container: "
					+ filename);
		}

		if (videoCoder.open() < 0) {
			throw new RuntimeException("could not open video decoder for container: "
					+ filename);
		}

		IVideoResampler resampler = null;
		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
			resampler = IVideoResampler.make(videoCoder.getWidth(),
					videoCoder.getHeight(), IPixelFormat.Type.BGR24,
					videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
			if (resampler == null) {
				throw new RuntimeException("could not create color space "
						+ "resampler for: " + filename);
			}
		}

		IPacket packet = IPacket.make();
		long firstTimestampInStream = Global.NO_PTS;
		long systemClockStartTime = 0;
		while (container.readNextPacket(packet) >= 0) {
			if (packet.getStreamIndex() == videoStreamId) {
				IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
						videoCoder.getWidth(), videoCoder.getHeight());

				int offset = 0;
				while (offset < packet.getSize()) {
					int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
					if (bytesDecoded < 0) {
						throw new RuntimeException("got error decoding video in: "
								+ filename);
					}
					offset += bytesDecoded;

					if (picture.isComplete()) {
						IVideoPicture newPic = picture;
						if (resampler != null) {
							newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),
									picture.getWidth(), picture.getHeight());
							if (resampler.resample(newPic, picture) < 0) {
								throw new RuntimeException("could not resample video from: "
										+ filename);
							}
						}
						if (newPic.getPixelType() != IPixelFormat.Type.BGR24) {
							throw new RuntimeException("could not decode video"
									+ " as BGR 24 bit data in: " + filename);
						}

						if (firstTimestampInStream == Global.NO_PTS) {
							firstTimestampInStream = picture.getTimeStamp();
							systemClockStartTime = System.currentTimeMillis();
						} else {
							long systemClockCurrentTime = System.currentTimeMillis();
							long millisecondsClockTimeSinceStartofVideo =
								systemClockCurrentTime - systemClockStartTime;
							long millisecondsStreamTimeSinceStartOfVideo =
								(picture.getTimeStamp() - firstTimestampInStream) / 1000;
							final long millisecondsTolerance = 50; // and we give ourselfs 50 ms of tolerance
							final long millisecondsToSleep =
								(millisecondsStreamTimeSinceStartOfVideo
								 - (millisecondsClockTimeSinceStartofVideo
									 + millisecondsTolerance));
							if (millisecondsToSleep > 0) {
								try {
									Thread.sleep(millisecondsToSleep);
								} catch (InterruptedException e) {
									return;
								}
							}
						}

						BufferedImage javaImage = Utils.videoPictureToImage(newPic);
						updateJavaWindow(javaImage);
					}
				}
			} else {
				do {
				} while (false);
			}

		}
		close();
	}

	private void updateJavaWindow(BufferedImage javaImage) {
		mScreen.setImage(javaImage);
	}

	public void close() {
		if (videoCoder != null) {
			videoCoder.close();
			videoCoder = null;
		}
		if (container != null) {
			container.close();
			container = null;
		}
	}
}

