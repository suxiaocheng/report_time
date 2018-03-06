package app;

import debug.Log;

public class IndicateBackground extends Thread {
	private Boolean bNeedQuit = false;
	private String[] strPlayFile = new String[1];
	private int iInterval = 10;

	public IndicateBackground(String name, int interval) {
		strPlayFile[0] = name;
		iInterval = interval;
	}

	@Override
	public void run() {
		int iTmp;
		super.run();

		while (true) {
			synchronized (bNeedQuit) {
				if (bNeedQuit == true) {
					break;
				}
			}
			iTmp = iInterval;
			while (iTmp > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				iTmp--;
				synchronized (bNeedQuit) {
					if (bNeedQuit == true) {
						break;
					}
				}
			}

			AudioPlayer ap = new AudioPlayer(strPlayFile);
			Thread t = new Thread(ap);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.d("indicate thread is quit");
	}

	public void setQuitFlag() {
		synchronized (bNeedQuit) {
			bNeedQuit = true;
		}
	}
}
