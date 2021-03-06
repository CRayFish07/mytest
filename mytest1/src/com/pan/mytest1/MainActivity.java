package com.pan.mytest1;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pan.mytest1.utils.AppManager;
import com.pan.mytest1.utils.BlurUtils;
import com.pan.mytest1.utils.LruCacheUtils;
import com.pan.mytest1.utils.NetTool;

public class MainActivity extends Activity {

	public static Context mContext;
	private static final String SDCardPath = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	private AppManager appManager;
	private LruCacheUtils mCacheUtils;
	private RelativeLayout rLayout;
	protected Context context;
	protected SharedPreferences sp;
	private boolean isblur = false;
	private TextView networktype ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = MainActivity.this;
		context = MainActivity.this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		appManager = new AppManager(mContext);
		sp = getSharedPreferences("shenma", MODE_PRIVATE);
		sp.edit().putString("open_blur", "开").commit();
		sp.edit().putString("wallpaperFileName", "4.jpg").commit();
		mCacheUtils = LruCacheUtils.getInstance();
		findViews();

	}

	/**
	 * 点击按钮来切换高斯模糊图片跟清晰图片
	 * 
	 * @param v
	 */
	public void changeImage(View v) {

		String fileName = sp.getString("wallpaperFileName", null);
		if (isblur) {
			// 更换背景
			System.out.println(fileName);
			if (fileName != null && !"".equals(fileName)) {
				changeBackImage(fileName);
			}
		} else {
			Bitmap bmp = mCacheUtils.getBitmapFromMemCache(SDCardPath + "/"
					+ fileName);
			if (bmp == null) {
				Bitmap tempBmp = BitmapFactory.decodeFile(SDCardPath + "/"
						+ fileName);
				mCacheUtils.addBitmapToMemoryCache(SDCardPath + "/" + fileName,
						tempBmp);
			}
			rLayout.setBackgroundDrawable(new BitmapDrawable(getResources(),
					bmp));
		}
		isblur = !isblur;
	}

	private void findViews() {
		rLayout = (RelativeLayout) findViewById(R.id.r1_bg);
		networktype = (TextView) findViewById(R.id.networktype);
		Bitmap bmp = mCacheUtils.getBitmapFromMemCache(String
				.valueOf(R.drawable.bg));
		if (bmp == null) {
			bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.main_bg);
			mCacheUtils.addBitmapToMemoryCache(
					String.valueOf(R.drawable.main_bg), bmp);
		}
		rLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
	}

	private void changeBackImage(String fileName) {
		if (fileName == null)
			return;
		File picFile = new File(SDCardPath+"/"+fileName);
		if (picFile.exists()) {
			Bitmap bmp = null;
			if ("开".equals(sp.getString("open_blur", "关"))) {
				// 开启高斯模糊背景效果
				bmp = mCacheUtils.getBitmapFromMemCache(SDCardPath + "/"
						+ fileName + "_blur");
				if (bmp == null) {
					try {
						Bitmap tempBmp = mCacheUtils
								.getBitmapFromMemCache(SDCardPath + "/"
										+ fileName);
						if (tempBmp == null) {
							tempBmp = BitmapFactory.decodeFile(context
									.getFilesDir().getAbsolutePath()
									+ "/"
									+ fileName);
							mCacheUtils.addBitmapToMemoryCache(context
									.getFilesDir().getAbsolutePath()
									+ "/"
									+ fileName, tempBmp);
						}
						bmp = BlurUtils.doBlur(tempBmp, 7, false);
						mCacheUtils.addBitmapToMemoryCache(context
								.getFilesDir().getAbsolutePath()
								+ "/"
								+ fileName + "_blur", bmp);
					} catch (OutOfMemoryError oom) {
						mCacheUtils.clearAllImageCache();
						Bitmap tempBmp = mCacheUtils
								.getBitmapFromMemCache(SDCardPath + "/"
										+ fileName);
						if (tempBmp == null) {
							tempBmp = BitmapFactory.decodeFile(context
									.getFilesDir().getAbsolutePath()
									+ "/"
									+ fileName);
							mCacheUtils.addBitmapToMemoryCache(context
									.getFilesDir().getAbsolutePath()
									+ "/"
									+ fileName, tempBmp);
						}
						bmp = BlurUtils.doBlur(tempBmp, 7, false);
						mCacheUtils.addBitmapToMemoryCache(context
								.getFilesDir().getAbsolutePath()
								+ "/"
								+ fileName + "_blur", bmp);
					}
				}
				rLayout.setBackgroundDrawable(new BitmapDrawable(
						getResources(), bmp));

			} else {
				bmp = mCacheUtils.getBitmapFromMemCache(SDCardPath + "/"
						+ fileName);
				if (bmp == null) {
					try {
						bmp = BitmapFactory.decodeFile(SDCardPath + "/"
								+ fileName);
						mCacheUtils.addBitmapToMemoryCache(context
								.getFilesDir().getAbsolutePath()
								+ "/"
								+ fileName, bmp);
					} catch (OutOfMemoryError oom) {
						mCacheUtils.clearAllImageCache();
						bmp = BitmapFactory.decodeFile(SDCardPath + "/"
								+ fileName);
						mCacheUtils.addBitmapToMemoryCache(context
								.getFilesDir().getAbsolutePath()
								+ "/"
								+ fileName, bmp);
					}
				}
				rLayout.setBackgroundDrawable(new BitmapDrawable(
						getResources(), bmp));
			}
		}
	}
	public void getNetworkType(View v)
	{
		NetTool mNetTool = new NetTool(mContext);
		String netStatus = mNetTool.getNetworkType();
		boolean netState = mNetTool.isNetWorkAvailable(mContext);
		System.out.println("网络类型:"+netStatus);
		if (netState) {
			networktype.setText(netStatus);
		}
		else
		{
			/**以下是跳转到系统界面的模块
			 * Settings.ACTION_SETTINGS 设置
			 * Settings.ACTION_SOUND_SETTINGS 声音设置
			 * Settings.ACTION_SYNC_SETTINGS 账户同步
			 * Settings.ACTION_ACCESSIBILITY_SETTINGS 辅助功能
			 * Settings.ACTION_ADD_ACCOUNT 显示添加账户创建
			 * Settings.ACTION_AIRPLANE_MODE_SETTINGS 飞行模式
			 * Settings.ACTION_WIRELESS_SETTINGS 无线网络
			 * Settings.ACTION_WIFI_SETTINGS wifi设置
			 * Settings.ACTION_APN_SETTINGS apn设置
			 * {
			 *  Uri packageURI = Uri.parse("package:" + "com.tencent.WBlog");
               Intent intent =  new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageURI); 根据包名跳转到系统自带的应用程序信息
			 * }
			 * Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS 开发人员选项
			 * Settings.ACTION_APPLICATION_SETTINGS 应用程序列表
			 * Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS 所有应用程序列表
			 * Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS 已安装应用程序列表
			 * Settings.ACTION_BLUETOOTH_SETTINGS 蓝牙设置界面
			 * Settings.ACTION_DATA_ROAMING_SETTINGS 移动网络设置界面
			 * Settings.ACTION_DATA_ROAMING_SETTINGS 日期和时间设置界面
			 * Settings.ACTION_DEVICE_INFO_SETTINGS 手机状态
			 * Settings.ACTION_INPUT_METHOD_SETTINGS 语言和输入
			 * Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS 多国语言选择
			 * Settings.ACTION_INTERNAL_STORAGE_SETTINGS 存储设置
			 * Settings.ACTION_MEMORY_CARD_SETTINGS 存储设置
			 * Settings.ACTION_LOCALE_SETTINGS 语言选择
			 * Settings.ACTION_LOCATION_SOURCE_SETTINGS 位置服务
			 * Settings.ACTION_NETWORK_OPERATOR_SETTINGS 选择网络运营商
			 * Settings.ACTION_NFCSHARING_SETTINGS NFC共享设置 api14以上
			 * Settings.ACTION_NFC_SETTINGS NFC设置 api16以上
			 * Settings.ACTION_PRIVACY_SETTINGS 备份和重置界面
			 * Settings.ACTION_QUICK_LAUNCH_SETTINGS 快速启动
			 * Settings.ACTION_SEARCH_SETTINGS 搜索设置
			 * Settings.ACTION_SECURITY_SETTINGS 安全设置
			 * Settings.ACTION_USER_DICTIONARY_SETTINGS 用户字典
			 * Settings.ACTION_WIFI_IP_SETTINGS IP设定
			 * Settings.ACTION_WIFI_SETTINGS wifi列表
			 * Settings.ACTION_DREAM_SETTINGS
			 */
			startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		}
	}
	public void installapk(View v)
	{
		String apkPath = SDCardPath + File.separator + "HiMarket.apk";
		System.out.println(apkPath);
		// 安装程序
		File apkFile = new File(apkPath);
		if (apkFile.exists()) {
			appManager.install(apkPath);
		} else {
			System.out.println(apkPath + "不存在");
		}
	}
}
