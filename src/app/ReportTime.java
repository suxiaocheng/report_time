package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import config.Config;
import debug.Log;
import jaco.mp3.player.MP3Player;
import tool.SyncTime;

public class ReportTime {

	private static final String[] SoundNetworkConnect = { Config.MUSIC_STORAGE_PATH + "start.mp3",
			Config.MUSIC_STORAGE_PATH + "NetworkConnect.mp3" };
	private static final String[] SoundNetworkDisconnect = { Config.MUSIC_STORAGE_PATH + "start.mp3",
			Config.MUSIC_STORAGE_PATH + "NetworkDisconnect.mp3" };
	private static final String[] SoundSequenceNumber = { Config.MUSIC_STORAGE_PATH + "0.mp3",
			Config.MUSIC_STORAGE_PATH + "1.mp3", Config.MUSIC_STORAGE_PATH + "2.mp3",
			Config.MUSIC_STORAGE_PATH + "3.mp3", Config.MUSIC_STORAGE_PATH + "4.mp3",
			Config.MUSIC_STORAGE_PATH + "5.mp3", Config.MUSIC_STORAGE_PATH + "6.mp3",
			Config.MUSIC_STORAGE_PATH + "7.mp3", Config.MUSIC_STORAGE_PATH + "8.mp3",
			Config.MUSIC_STORAGE_PATH + "9.mp3", };
	private static final String[] SoundMultiSequenceNumber = { Config.MUSIC_STORAGE_PATH + "10.mp3",
			Config.MUSIC_STORAGE_PATH + "20.mp3", Config.MUSIC_STORAGE_PATH + "30.mp3",
			Config.MUSIC_STORAGE_PATH + "40.mp3", Config.MUSIC_STORAGE_PATH + "50.mp3", };
	private static final String[] SoundSep = { Config.MUSIC_STORAGE_PATH + "hour.mp3",
			Config.MUSIC_STORAGE_PATH + "minute.mp3", };

	private static final String[] SoundNetworkState = { Config.MUSIC_STORAGE_PATH + "NetworkConnect.mp3",
			Config.MUSIC_STORAGE_PATH + "NetworkDisconnect.mp3" };
	private static final String[] SoundNotify = { Config.MUSIC_STORAGE_PATH + "start.mp3" };

	private static final String[] SoundTimeTitle = { Config.MUSIC_STORAGE_PATH + "time.mp3" };

	private static final String[] SoundFavoriteMusic = { Config.MUSIC_STORAGE_PATH + "Home.mp3" };

	private static final boolean bDebug = false;// false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* sync time first */
		boolean bStatus;
		boolean bPreNetworkStatus = true;

		Config.dumpSystemInfo();

		IndicateBackground indicateBackground = new IndicateBackground(SoundNotify[0], 6);
		Thread indicateThread = new Thread(indicateBackground);
		indicateThread.start();
		while (true) {
			bStatus = SyncTime.isConnect();
			if (bStatus == true) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		indicateBackground.setQuitFlag();
		try {
			indicateThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (bDebug) {
			// playFavorite();
		} else {
			playFavorite();
			playNetworkConnect();
		}

		bStatus = SyncTime.waitNetworkTimeSync();
		if (bStatus == false) {
			return;
		}
		System.out.println("Sync time sucessfully");

		// Create thread to get temperature period.
		GetTemperature getTemperature = new GetTemperature(1000, true);
		Thread getTempThread = new Thread(getTemperature);
		getTempThread.start();

		/* Wakeup per minute */
		Calendar calendar;
		while (true) {
			if (!bDebug) {
				calendar = Calendar.getInstance();
				int waitSecond = 60 - calendar.get(Calendar.SECOND);
				if (waitSecond > 0) {
					try {
						Thread.sleep(waitSecond * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				calendar = Calendar.getInstance();
				int minute = calendar.get(Calendar.MINUTE);
				if ((minute % 15) == 0) {
					int hour = calendar.get(Calendar.HOUR_OF_DAY);
					if (minute == 0) {
						if (hour >= Config.INDICATE_TIME) {
							playCurrentTime();
							switch (hour) {
							case 7:
								playFavorite();
								break;
							case 8:
								playFavorite();
								break;
							case 23:
								playFavorite();
								break;
							default:
								break;
							}
						}
					} else {
						if (hour >= Config.INDICATE_TIME) {
							playCurrentTime();
						}
					}
				}
			} else {
				playFavorite();
				playCurrentTime();
			}

			bStatus = SyncTime.isConnect();
			if (bStatus == false) {
				playNetworkDisconnect();
				bPreNetworkStatus = false;
			} else {
				if (bPreNetworkStatus == false) {
					bPreNetworkStatus = true;
					playNetworkConnect();
				}
			}
		}
	}

	public static int writeCmd2File(String cmd) {
		File f = new File(Config.FILE_STORAGE_PATH + "cmd.sh");
		String header = new String("#!/bin/sh\n");
		String end = new String("\n");
		if (f.exists()) {
			f.delete();
		}
		int ret = 0;
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(header.getBytes());
			out.write(cmd.getBytes());
			out.write(end.getBytes());
			out.close();

			f.setExecutable(true, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = -1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = -2;
		}

		return ret;
	}

	public static void playFavorite() {
		java.util.Random r = new java.util.Random();
		File root;
		root = new File(Config.FAVORITE_STORAGE_PATH);
		List<File> filelist = new ArrayList<File>();
		File random_file = null;
		if (root != null) {
			for (File f : root.listFiles()) {
				if (f.isDirectory() == false) {
					if (f.getName().endsWith("mp3") == true) {
						filelist.add(f);
					}
				}
			}
		}

		if (filelist.size() > 0) {
			random_file = filelist.get(r.nextInt(filelist.size()));
			if (random_file.canRead() == true) {
				if (Config.USE_FFPLAY2PLAY_FAV) {
					String command = "screen -dms mplayer mplayer " + "\"" + Config.FAVORITE_STORAGE_PATH
							+ random_file.getName() + "\"";
					int min_count = 0;

					Log.d("Going to exec: " + command);
					writeCmd2File(command);

					Config.runCommand(Config.FILE_STORAGE_PATH + "cmd.sh");
				} else {
					MP3Player player = new MP3Player(random_file);
					player.play();
					while (player.isStopped() == false) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static void playNotify() {
		AudioPlayer ap = new AudioPlayer(SoundNotify);
		Thread t = new Thread(ap);
		t.start();
	}

	public static void playNetworkConnect() {
		AudioPlayer ap = new AudioPlayer(SoundNetworkConnect);
		Thread t = new Thread(ap);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void playNetworkDisconnect() {
		AudioPlayer ap = new AudioPlayer(SoundNetworkDisconnect);
		Thread t = new Thread(ap);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void playCurrentTime() {
		Calendar calendar = Calendar.getInstance();
		List<String> timeinfo = new ArrayList<String>();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		timeinfo.add(SoundTimeTitle[0]);
		if (hour < 10) {
			timeinfo.add(SoundSequenceNumber[hour]);
		} else {
			timeinfo.add(SoundMultiSequenceNumber[hour / 10 - 1]);
			if ((hour % 10) > 0) {
				timeinfo.add(SoundSequenceNumber[hour % 10]);
			}
		}
		timeinfo.add(SoundSep[0]);
		if (minute != 0) {
			if (minute < 10) {
				timeinfo.add(SoundSequenceNumber[minute]);
			} else {
				timeinfo.add(SoundMultiSequenceNumber[minute / 10 - 1]);
				if ((minute % 10) > 0) {
					timeinfo.add(SoundSequenceNumber[minute % 10]);
				}
			}
			timeinfo.add(SoundSep[1]);
		}

		String[] strInfo = new String[timeinfo.size()];
		int pos = 0;
		for (String str : timeinfo) {
			strInfo[pos++] = str;
		}

		AudioPlayer ap = new AudioPlayer(strInfo);
		Thread t = new Thread(ap);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
