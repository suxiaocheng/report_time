package app;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import config.Config;
import debug.Log;
import tool.SyncTime;

public class ReportTime {

	private static final String[] SoundNetworkConnect = {
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "start.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "NetworkConnect.wav" };
	private static final String[] SoundNetworkDisconnect = {
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "start.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "NetworkDisconnect.wav" };
	private static final String[] SoundSequenceNumber = { Config.FILE_STORAGE_PATH + "Music" + File.separator + "0.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "1.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "2.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "3.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "4.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "5.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "6.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "7.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "8.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "9.wav", };
	private static final String[] SoundMultiSequenceNumber = {
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "10.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "20.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "30.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "40.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "50.wav", };
	private static final String[] SoundSep = { Config.FILE_STORAGE_PATH + "Music" + File.separator + "hour.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "minute.wav", };

	private static final String[] SoundNetworkState = {
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "NetworkConnect.wav",
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "NetworkDisconnect.wav" };
	private static final String[] SoundNotify = { Config.FILE_STORAGE_PATH + "Music" + File.separator + "start.wav" };

	private static final String[] SoundTimeTitle = { Config.FILE_STORAGE_PATH + "Music" + File.separator + "time.wav" };

	private static final String[] SoundFavoriteMusic = {
			Config.FILE_STORAGE_PATH + "Music" + File.separator + "Home.wav" };

	private static final boolean bDebug = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* sync time first */
		boolean bStatus;
		boolean bPreNetworkStatus = true;
		int iCount = 0;
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
			iCount++;
			if ((iCount % 6) == 0) {
				playNotify();
			}
		}
		playNetworkConnect();

		if (bDebug) {
			playFavorite();
		}

		bStatus = SyncTime.waitNetworkTimeSync();
		if (bStatus == false) {
			return;
		}
		System.out.println("Sync time sucessfully");

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
				if (minute == 0) {
					int hour = calendar.get(Calendar.HOUR_OF_DAY);
					if ((hour == 7) || (hour == 23)) {
						playFavorite();
					} else if (hour > 7) {
						playCurrentTime();
					}
				}
			} else {
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

	public static void playFavorite() {
		AudioPlayer ap = new AudioPlayer(SoundFavoriteMusic);
		Thread t = new Thread(ap);
		t.start();
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
