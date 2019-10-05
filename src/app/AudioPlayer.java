package app;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import debug.Log;
import jaco.mp3.player.MP3Player;

public class AudioPlayer extends Thread {
	private List<File> music;
	private static Boolean bNeedQuit = false;

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
		MP3Player player = new MP3Player();
		
		while (it.hasNext()) {
			file = (File) it.next();
			player.addToPlayList(file);
		}
		player.play();
		
		while(player.isStopped() == false) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		music.clear();
	}
}
