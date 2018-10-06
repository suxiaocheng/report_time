package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import config.Config;
import debug.Log;

public class GetTemperature extends Thread {
	private Boolean bNeedQuit = false;
	private int iInterval;
	private Boolean bLogToFile = false;
	private static final String sysTemperatureDir = "/sys/class/thermal/";

	private static final String strLogDir = Config.FILE_STORAGE_PATH + "log";
	private static final String strLogName = "temperature";
	private File fLogFile;
	private File fLogDir;
	private BufferedWriter bwLogFile;
	private FileWriter fwLogFile = null;

	public GetTemperature(int interval, Boolean log2file) {
		iInterval = interval;
		bLogToFile = log2file;
		if (bLogToFile) {
			Date today = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(today);

			fLogDir = new File(strLogDir);
			if (fLogDir.exists() == true) {
				if (fLogDir.isDirectory() == false) {
					fLogDir.delete();
				}
			} else {
				fLogDir.mkdirs();
			}

			fLogFile = new File(strLogDir + File.separator + strLogName
					+ strDate + ".log");
			try {
				fwLogFile = new FileWriter(fLogFile, true);
				bwLogFile = new BufferedWriter(fwLogFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				bwLogFile = null;
			}
		} else {
			fwLogFile = null;
		}
	}

	@Override
	protected void finalize() {
		Log.d("finalize is being calling");
		try {
			if (bwLogFile != null) {
				bwLogFile.flush();
				bwLogFile.close();
				bwLogFile = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileContent(String name) {
		String sContent = null;
		BufferedReader bReader = null;

		File fContent = new File(name);
		try {
			bReader = new BufferedReader(new FileReader(fContent));
			sContent = bReader.readLine();
			bReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sContent;
	}

	public void log() {
		String strOut;
		String msg = "";
		Date today = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		strOut = dateFormat.format(today);		

		File tempDir = new File(sysTemperatureDir);
		if (tempDir.exists()) {
			String sContent;
			float fTemperature;
			File[] files = tempDir.listFiles();
			for (File file : files) {
				if (file.getAbsolutePath().contains("thermal_zone")) {
					sContent = getFileContent(file.getAbsolutePath()
							+ File.separator + "temp");
					if (sContent != null) {
						fTemperature = Integer.parseInt(sContent) / 1000;
						msg += " " + fTemperature;
					}
				}
			}
		} else {
			Log.e("Temperature dir is not exist");
			bNeedQuit = true;
		}

		tempDir = new File("/proc/loadavg");
		if (tempDir.exists() && tempDir.canRead()) {
			msg += " " + getFileContent(tempDir.getAbsolutePath());
		}

		if (bwLogFile != null) {
			try {
				bwLogFile.write(strOut + msg + "\n");
				// bwLogFile.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println(strOut + msg);
		}
	}

	@Override
	public void run() {
		int iCount = 0;
		super.run();

		while (true) {
			synchronized (bNeedQuit) {
				if (bNeedQuit == true) {
					break;
				}
			}

			while (true) {
				try {
					Thread.sleep(iInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				log();

				if (iCount++ > Config.SYNC_FILESYSTEM_TIME) {
					iCount = 0;
					if (bwLogFile != null) {
						try {
							bwLogFile.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				synchronized (bNeedQuit) {
					if (bNeedQuit == true) {
						break;
					}
				}
			}
		}
		finalize();
		Log.d("GetTemperature thread is quit");
	}

	public void setQuitFlag() {
		synchronized (bNeedQuit) {
			bNeedQuit = true;
		}
	}

	public static void main(String[] args) {
		GetTemperature getTemperature = new GetTemperature(1000, true);
		Thread getTempThread = new Thread(getTemperature);
		getTempThread.start();
		int iExecuteTime = 10;

		while (true) {
			iExecuteTime--;
			if (iExecuteTime == 0) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getTemperature.setQuitFlag();
		try {
			getTemperature.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
