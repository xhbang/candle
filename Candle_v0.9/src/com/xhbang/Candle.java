package com.xhbang;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Candle extends Activity {

	private static final int MODE_LONGLIGHT = 0;
	private static final int MODE_FLASH = 1;
	private static final int MODE_SOS = 2;
	private static final int MODE_CANDLE=3;	
	private static final int MODE_BATTERY=4;
	private static final int MODE_HELP = 5;
	
	
	//not yet developed,it's a fun function,right?I'll make it in the next version
	
	//颜色菜单
	
	private static final int M_BLUE=6;
	private static final int M_GREEN=7;
	private static final int M_YELLOW=8;
	private static final int M_ORANGE=9;
	private static final int M_CYAN=10;
	private static final int M_BALCK=11;
	
	
	private int flashColor=Color.BLACK;
	private PowerManager.WakeLock mWakeLock;
	private WindowManager.LayoutParams lp;
	private View mWindow;
	
	/**
	 * 0: 长亮
	 * 1: 闪闪
	 * 2: SOS
	 * 3：烛光
	 * 4：电池
	 * 5：帮助|关于
	 */
	private int mCurrentMode = -1;

	private int mFlag;
	private FlashHandler mFlashHandler;
	private SOSHandler mSOSHandler;	
	private int mSOSIdx;
	private int[] mSOSCode = new int[] {
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			900,
			//
			900,
			300,
			900,	// ---	O
			300,
			900,
			//
			900,
			//
			300,
			300,
			300,	// ...	S
			300,
			300,
			300,
			//
			2100
	};
	
	//电池服务
	private int intLevel;
	private int intScale; 
	private BroadcastReceiver mBatInfoReceiver=new BroadcastReceiver() 
	  {  
	    public void onReceive(Context context, Intent intent) 
	    { 
	      String action = intent.getAction();  
	      if (Intent.ACTION_BATTERY_CHANGED.equals(action)) 
	      { 
	        intLevel = intent.getIntExtra("level", 0);  
	        intScale = intent.getIntExtra("scale", 100); 
	        onBatteryInfoReceiver(intLevel,intScale);
	      }  
	    } 
	  };

	//捕捉
	public void onBatteryInfoReceiver(int intLevel, int intScale) 
	  {
	    final Dialog d = new Dialog(Candle.this);
	    d.setTitle("电池信息"); 
	    d.setContentView(R.layout.mydialog); 
	    
	    Window window = d.getWindow(); 
	    window.setFlags 
	    ( 
	      WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
	      WindowManager.LayoutParams.FLAG_BLUR_BEHIND 
	    );
	    
	    //窗口设置
	    TextView mTextView = (TextView)d.findViewById(R.id.myTextView); 
	    mTextView.setText 
	    ( 
	      getResources().getText(R.string.str_dialog_body)+ 
	      String.valueOf(intLevel * 100 / intScale) + "%" 
	    );
	    
	    Button mButton = (Button)d.findViewById(R.id.myButton); 
	    mButton.setOnClickListener(new Button.OnClickListener() 
	    { 
	      @Override 
	      public void onClick(View v) 
	      { 
	        unregisterReceiver(mBatInfoReceiver); 
	        d.dismiss(); 
	      } 
	    }); 
	    d.show(); 
	  } 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		setFullscreen();		
		mWindow = findViewById(R.id.window);		
		// 默认长亮
		modelonglight();		
		mWakeLock = getWakeLock();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		SubMenu sub=menu.addSubMenu("颜色");
		sub.add(0, M_BALCK,0, R.string.str_black);
		sub.add(0,M_GREEN,0,R.string.str_green);
		sub.add(0,M_BLUE, 0, R.string.str_blue);	
		sub.add(0, M_CYAN, 0, R.string.str_cyan);
		sub.add(0, M_YELLOW, 0, R.string.str_yellow);
		sub.add(0, M_ORANGE, 0, R.string.str_orange);		
				
		
		MenuItem longlight = menu.add(0, MODE_LONGLIGHT, 0, "长亮");
		MenuItem flash = menu.add(0, MODE_FLASH, 0, "闪闪");		
		MenuItem sos = menu.add(0, MODE_SOS, 0, "SOS");	
		MenuItem candle=menu.add(0, MODE_CANDLE, 0, "烛光");
		MenuItem battery=menu.add(0, MODE_BATTERY, 0, "电池");
		MenuItem help = menu.add(0, MODE_HELP, 0, "帮助");	
		help.setIcon(android.R.drawable.ic_menu_help);
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		mCurrentMode = item.getItemId();
		
		switch (item.getItemId()) {
		case MODE_LONGLIGHT:
			modelonglight();
			return true;
		case MODE_FLASH:
			modeFlash();
			return true;
		case MODE_SOS:
			modeSOS();
			return true;
		case MODE_BATTERY:		//attention here
			registerReceiver 
		    ( 
		      mBatInfoReceiver, 
		      new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
		    ); 
			return true;
		case MODE_CANDLE:			//尚未开发
			return true;
		case MODE_HELP:
			modeHelp();
			return true;
		case M_BALCK:
			flashColor=Color.BLACK;
			mWindow.setBackgroundColor(flashColor);
			break;
		case M_GREEN:
			flashColor=Color.GREEN;
			mWindow.setBackgroundColor(flashColor);
			break;
		case M_BLUE:
			flashColor=Color.BLUE;					
			mWindow.setBackgroundColor(flashColor);
			return true;
		case M_CYAN:
			flashColor=Color.CYAN;
			mWindow.setBackgroundColor(flashColor);
			return true;
		case M_ORANGE:
			//
			return true;
		case M_YELLOW:
			flashColor=Color.YELLOW;
			mWindow.setBackgroundColor(flashColor);
			return true;
		}	
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		brightnessDefault();
		mWakeLock.release();		
		super.onStop();
	}

	//u can change color here
	public void modelonglight() {
		mFlag = 1;
		mWindow.setBackgroundColor(Color.WHITE);
		brightnessMax();
	}
	
	public void modeHelp() {
		new AlertDialog.Builder(Candle.this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle("About")
        .setMessage(R.string.app_about)
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .create().show();
	}
	
	// ---------------------------------------------------------------------------- Flash
	public void modeFlash() {
		mFlag = 1;
		mFlashHandler = new FlashHandler();
		updateFlashUI();
	}
	
	private void updateFlashUI() {
		
		if (mCurrentMode != MODE_FLASH) {
			return;
		}
		
		mFlashHandler.sleep(500);
		
		if (mFlag == 1) {
			mWindow.setBackgroundColor(flashColor);
			mFlag = 0;
		} else {
			mWindow.setBackgroundColor(Color.WHITE);
			mFlag = 1;
		}
	}
	
	//SOS Morse Code
	public void modeSOS() {
		mFlag = 1;
		mSOSIdx = 0;
		mSOSHandler = new SOSHandler();
		updateSOSUI();
	}
	
	private void updateSOSUI() {
		if (mCurrentMode != MODE_SOS) {
			return;
		}
		
		mSOSHandler.sleep(mSOSCode[mSOSIdx]);
		
		if (mSOSIdx + 1 == mSOSCode.length) {
			mSOSIdx = 0;
			mFlag = 1;
		} else {
			mSOSIdx++;
		}
		
		if (mFlag == 1) {
			mWindow.setBackgroundColor(flashColor);
			mFlag = 0;
		} else {
			mWindow.setBackgroundColor(Color.WHITE);
			mFlag = 1;
		}
	}
	

	private void setFullscreen() {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	
	private PowerManager.WakeLock getWakeLock() {
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);		
		PowerManager.WakeLock w = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Candle");
		w.acquire();		
		return w;
	}
	
	private void brightnessMax() {  
	    lp = getWindow().getAttributes();  
	    lp.screenBrightness = 1.0f;  
	    getWindow().setAttributes(lp);  
	}  	
	private void brightnessDefault() {  
	    lp = getWindow().getAttributes();  
	    lp.screenBrightness = -1.0f;  
	    getWindow().setAttributes(lp);  
	} 
	
	class FlashHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Candle.this.updateFlashUI();
		}
		
		public void sleep(long delayMillis) {
			removeMessages(0);			
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
	
	class SOSHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Candle.this.updateSOSUI();
		}
		
		public void sleep(long delayMillis) {
			removeMessages(0);
			
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
}