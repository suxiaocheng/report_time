package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import debug.Log;

public class AudioPlayer extends Thread {
	private List<File> music;
	private static Boolean bNeedQuit = false;

	private AudioInputStream audioInputStream;
	private AudioFormat audioFormat;
	private SourceDataLine sourceDataLine;
	private byte tempBuffer[] = new byte[2048];

	AudioPlayer(String[] namelist) {
		music = new ArrayList<File>();

		synchronized (bNeedQuit) {
			bNeedQuit = false;
		}
		for (String name : namelist) {
			File file = new File(name);
			music.add(file);
		}
	}

	@Override
	public void run() {
		super.run();
		play();
	}

	public void setQuitFlag() {
		synchronized (bNeedQuit) {
			bNeedQuit = true;
		}
	}

	public void play() {
		File file;
		Log.d("play list: " + music.toString());
		Iterator it = music.iterator();
		while (it.hasNext()) {
			file = (File) it.next();
			try {
				audioInputStream = AudioSystem.getAudioInputStream(file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.e("File not found");
				return;
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Unsupported file");
				return;
			}
			audioFormat = audioInputStream.getFormat();

			if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				Log.d("Target: " + file + " is not pcm signed, converted");
				audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
						audioFormat.getChannels(), audioFormat.getChannels() * 2, audioFormat.getSampleRate(), false);
				audioInputStream = AudioSystem.getAudioInputStream(audioFormat, audioInputStream);
			}

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat,
					AudioSystem.NOT_SPECIFIED);
			try {
				sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
				sourceDataLine.open(audioFormat);
			} catch (LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.e("LineUnavailableException");
				return;
			}
			sourceDataLine.start();

			int cnt;
			try {
				while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
					synchronized (bNeedQuit) {
						if (bNeedQuit){
							Log.d("User force to quit");
							break;
						}
					}
					if (cnt > 0) {
						sourceDataLine.write(tempBuffer, 0, cnt);
					}
				}
				sourceDataLine.drain();
				sourceDataLine.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Reading audio input stream error\n");
			}			
			try {
				audioInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("audioInputStream close error");
			}
			synchronized (bNeedQuit) {
				if (bNeedQuit)
					break;
			}
			Log.d("Music " + file.toString() + " is end");
		}
		music.clear();
	}
}
