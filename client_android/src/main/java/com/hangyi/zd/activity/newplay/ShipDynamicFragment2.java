package com.hangyi.zd.activity.newplay;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.eyunda.main.view.DialogUtil;
import com.eyunda.third.ApplicationConstants;
import com.eyunda.third.ApplicationUrls;
import com.eyunda.third.GlobalApplication;
import com.eyunda.third.activities.map.PolylineOverlayManager;
import com.eyunda.third.loaders.Data_loader;
import com.eyunda.tools.CalendarUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hangyi.tools.ParseJson;
import com.hangyi.tools.Util;
import com.hangyi.zd.ClearService;
import com.hangyi.zd.R;
import com.hangyi.zd.activity.ContentFragment;
import com.hangyi.zd.activity.NewContentFragment;
import com.hangyi.zd.activity.dialog.CustomDialog;
import com.hangyi.zd.activity.gridviewpage.AppAdapter;
import com.hangyi.zd.activity.newplay.CommonVideoView.CommonVideoChangLintener;
import com.hangyi.zd.domain.ShipCKGpssData;
import com.hangyi.zd.domain.ShipControlData;
import com.hangyi.zd.domain.ShipGpsData;
import com.hangyi.zd.domain.ShipModelCode;
import com.hangyi.zd.domain.ShipModelData;
import com.hangyi.zd.domain.ShipModelNoData;
import com.hangyi.zd.domain.ShipOneCKHcData;
import com.hangyi.zd.domain.UserPowerData;
import com.hangyi.zd.domain.UserPowerShipData;
import com.ta.util.http.AsyncHttpResponseHandler;

import org.apache.http.cookie.Cookie;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShipDynamicFragment2 extends Fragment implements OnClickListener,
		OnMarkerClickListener, OnMapClickListener ,CommonVideoChangLintener{

	Data_loader dataLoader;
	protected DialogUtil dialogUtil;
	protected ProgressDialog dialog;
	Calendar start,end;
	private static final int START_lOADIMG = 100;
	private static final int STOP_lOADIMG = 200;
	public static final int SENDTO_CLIENT = 300;
	public static final int test = 400;

//	private ExecutorService pool;
	private ExecutorService pool_showImg;

	String shipID="",startTime="",endTime="",shipName="";
	MapView mapHistory;
	BaiduMap bmHistory;
	UiSettings uisHistory;
	float z = 13;//
	/**实际加载GPS数*/
	public static int loadedGpsDataSize=0;
	/**加载时间段分钟数*/
	public static int needLoadedMinutes=0;
	private static final int centerChange=30;
	private static final int losePointMax=10;
	public static int flag = 0;
	public static int losePoint = 0;
	private static String loseLastTime = "";
	public static boolean flagFirst = true;
	private static final int loadDays = 5;
	public static final int ParseCKJson = 11;
	public static final int ParseCKJsonIsEmpty = 12;
	public static final int ParseCKJsonIsError = 13;
	private static final int ParseJsonException = 9;
	private static final int ParseJsonIsEmpty = 8;
	private static final int ParseJsonTooMuch = 7;
	private static final int ParseJsonFailure = 6;
	private static final int playfast = 10;
	public static final int playmod = 5;
	private static final int playslo = 2;
	volatile public static int currPlayPosition = 0;
	public static final LatLng BEIJING_LATLNG = new LatLng(23.038006, 113.509988);
	List<ShipGpsData> positionDatas;
	Marker marker;
	TextOptions historyTextOption = null;
	boolean setCenterFlag = true;

	private ImageView gps;
	private ImageView videoPauseImg;
	private MySurfaceView img1,img2,img3,img4;
	private MySurfaceView img31,img32,img33;
	private TextView tv_time4,tv_speed4,tv_lat4,tv_j4,tv_w4;
	private TextView tv_time3,tv_speed3,tv_lat3,tv_j3,tv_w3;
	private LinearLayout shipImg,shipImg3,shipImg4,shipInfo3,shipInfo4;

	private CommonVideoView commonVideoView;
	private List<String> channels = new ArrayList<String>();

	List<ShipOneCKHcData> ckGpsData;
	ShipGpsData cPoint;

	ShipControlData sc;
	public static final int RShipControlData = 20;

	BitmapDescriptor mTexture = null;
	BitmapDescriptor mPurpleTexture = BitmapDescriptorFactory.fromAsset("icon_road_purple_arrow.png");
	BitmapDescriptor mGreenTexture = BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png");
	BitmapDescriptor mRedTexture = BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png");
	static Bitmap lose_gpsBitmap = BitmapFactory.decodeResource(GlobalApplication.getInstance().getResources(), R.drawable.nothing_gps);
	static Bitmap lose_imgBitmap = BitmapFactory.decodeResource(GlobalApplication.getInstance().getResources(), R.drawable.nothing_img);
	public static Bitmap home_defaultBitmap = BitmapFactory.decodeResource(GlobalApplication.getInstance().getResources(), R.drawable.home_default);
	BitmapDescriptor baseBitmapOrg = BitmapDescriptorFactory.fromResource(R.drawable.kuang2);

	private Messenger mService;
	private boolean isConn = false;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			isConn = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			isConn = true;
//			mBinderService = (ImgDownLoadService.ServiceBinder) service;
		}
	};

	private void bindServiceInvoked(){
		Intent intent = new Intent(getActivity(),ImgDownLoadService2.class);
//		Intent intent = new Intent();
//		intent.setAction("com.zlf.aidl.img");
//		intent.setPackage("com.hangyi.zd");
		getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	public static File sdCardPath = Environment.getExternalStorageDirectory();// 获取SDCard目录
	public static File cacheDir = new File(sdCardPath + ApplicationConstants.imgCachePath);
	private ViewArea3 viewArea;


	/**startTime-endTime分钟数组*/
	private String[] minArr = null;
	/**startTime-endTime GpsData数组*/
	private ShipGpsData[] gpsDataArr = null;

	private String lineType = "";

	PowerManager.WakeLock mWakeLock;
	PolylineOverlayManager polylineOverlayManager;

	private Messenger mMessenger = new Messenger(new Handler()
	{
		@Override
		public void handleMessage(Message msgFromServer)
		{
			switch (msgFromServer.what)
			{
				case SENDTO_CLIENT:
					int startTimeEndTimeMins = msgFromServer.arg1;
					LoadedList.getInstance().add(startTimeEndTimeMins);
					break;
				case test:
//					Toast.makeText(getActivity(),"test"+msgFromServer.arg1,Toast.LENGTH_LONG).show();
			}
			super.handleMessage(msgFromServer);
		}
	});

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(isAdded()){
				if (msg.what == 1) {// 初始化数据完成
					drawLines();
					commonVideoView.play();
					gpsLoadedAfterStart();

				} else if (msg.what == 2) {// 读取新坐标完成
//					commonVideoView.doCaheSeekBar();
//					doPlayListener();

					if(CommonVideoView.isPlaying){
						int lastPointIndex = currPlayPosition;
						if (lastPointIndex < gpsDataArr.length) {
							ShipGpsData currPoint = gpsDataArr[lastPointIndex];
							if(currPoint == null){
								if(losePoint >= losePointMax){
//									removeMarker();
									showLoseShipImg(minArr[lastPointIndex]);

									if(viewArea!=null)
										viewArea.playLoseImg(minArr[lastPointIndex]);
								}
								losePoint ++ ;

								lastPointIndex++;
								currPlayPosition = lastPointIndex;
							}else{
								losePoint = 0;
								loseLastTime = currPoint.getGpsTime();

								cPoint = currPoint;
								if(viewArea!=null)
									viewArea.playImg(cPoint);

								moveMarker(currPoint, currPoint);
								showShipImg(currPoint);

								if(flagFirst){
									setCenter(currPoint, z);
									flagFirst=false;
								}
								if(flag == centerChange){
									flag = 0;
									setCenter(currPoint, z);
								}
								flag++;

								lastPointIndex++;
								currPlayPosition = lastPointIndex;
							}

						}else{
//							CommonVideoView.isPlaying = false;
//							currPlayPosition = 0;
//							flagFirst=true;
//							flag = 0;
						}
					}else{
					}

				}else if(msg.what == 3){
				}else if(msg.what == ParseJsonException){
					Toast.makeText(getActivity(), "数据解析异常！请稍后再试！", Toast.LENGTH_LONG).show();
					commonVideoView.loadError();
				}else if(msg.what == ParseJsonIsEmpty){
					Toast.makeText(getActivity(), "数据加载为空！", Toast.LENGTH_LONG).show();
					commonVideoView.loadError();
				}else if(msg.what == ParseJsonTooMuch){
					Toast.makeText(getActivity(), "航线数据太多，超出显视范围，请重新选择时间段！", Toast.LENGTH_LONG).show();
					commonVideoView.loadError();
				}else if(msg.what == ParseJsonFailure){
					commonVideoView.loadError();
				}else if(msg.what == ParseCKJson){
					if(ckGpsData!=null&&ckGpsData.size()>0){
						for(ShipOneCKHcData socd:ckGpsData){
							if(socd.getList().size()>0)
								drawLines(null,socd.getList(),13);
						}
					}
				}else if(msg.what == ParseCKJsonIsEmpty){
					Toast.makeText(getActivity(), "参考航线数据为空",
							Toast.LENGTH_SHORT).show();
				}else if(msg.what == ParseCKJsonIsError){
					Toast.makeText(getActivity(), "参考航线数据解析异常",
							Toast.LENGTH_SHORT).show();
				}else if(msg.what == RShipControlData){
					if (sc!=null){
						tv_lat3.setText("船队："+sc.getContact());
						tv_lat4.setText("船队："+sc.getContact());
					}
				}
				super.handleMessage(msg);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SDKInitializer.initialize(getActivity().getApplicationContext());
		MapView.setCustomMapStylePath(ContentFragment.getAssetsCacheFile(getActivity(),"baidu_custom_config"));
		return inflater.inflate(R.layout.zd_ship_play_fragment_surface, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		dataLoader = new Data_loader();
		dialogUtil=new DialogUtil(getActivity());
//		pool = Executors.newFixedThreadPool(2);
		pool_showImg = Executors.newFixedThreadPool(6);

		//开始绑定服务
		bindServiceInvoked();
//		Intent bindIntent = new Intent(getActivity(), ImgDownLoadService.class);
//		getActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

		Intent intent = getActivity().getIntent();
		shipID = intent.getStringExtra("shipID")!=null?intent.getStringExtra("shipID"):"";
		shipName = intent.getStringExtra("shipName")!=null?intent.getStringExtra("shipName"):"";
		startTime = intent.getStringExtra("startTime")!=null?intent.getStringExtra("startTime"):"";//"yyyy-MM-dd HH:mm:ss"
		endTime = intent.getStringExtra("endTime")!=null?intent.getStringExtra("endTime"):"";//"yyyy-MM-dd HH:mm:ss"
		lineType = intent.getStringExtra(ApplicationConstants.historyLineType)!=null
				?intent.getStringExtra(ApplicationConstants.historyLineType):ApplicationConstants.historyLineNormal;

		if(lineType.equals(ApplicationConstants.historyLineNormal)){//from nomal
			mTexture = mPurpleTexture;
		}else{//from ShipPoliceActivity
			mTexture = mRedTexture;

			String startPort = intent.getStringExtra("startPort");
			String endPort = intent.getStringExtra("endPort");
			loadShipCKHX(startPort,endPort);;
		}

		this.initMap();

		Calendar now = Calendar.getInstance();
		if("".equals(startTime)||"".equals(endTime)){
			startTime = CalendarUtil.toYYYY_MM_DD_HH_MM_SS(CalendarUtil.addDays(now, -1));
			endTime = CalendarUtil.toYYYY_MM_DD_HH_MM_SS(now);
		}

		initView(startTime,endTime);
		initShipPowerChannels();
		getShipControlData();

		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

		super.onActivityCreated(savedInstanceState);
	}

	private void initView(String startTime, String endTime) {
//        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
//        Display display = manager.getDefaultDisplay();
//        display.getSize(pt);

		commonVideoView = (CommonVideoView) getActivity().findViewById(R.id.common_videoView);
		commonVideoView.init(CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(startTime),CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(endTime));
		commonVideoView.setCommonVideoChangLintener(this);

		shipInfo3 = (LinearLayout) getActivity().findViewById(R.id.shipInfo3);
		shipInfo3.setOnClickListener(this);
		shipInfo4 = (LinearLayout) getActivity().findViewById(R.id.shipInfo4);
		shipInfo4.setOnClickListener(this);

		shipImg = (LinearLayout) getActivity().findViewById(R.id.shipImg);
		shipImg3 = (LinearLayout) getActivity().findViewById(R.id.shipImg3);
		shipImg4 = (LinearLayout) getActivity().findViewById(R.id.shipImg4);
		tv_time3 = (TextView) getActivity().findViewById(R.id.tv_time3);
		tv_speed3 = (TextView) getActivity().findViewById(R.id.tv_speed3);
		tv_lat3 = (TextView) getActivity().findViewById(R.id.tv_lat3);
		tv_time4 = (TextView) getActivity().findViewById(R.id.tv_time4);
		tv_speed4 = (TextView) getActivity().findViewById(R.id.tv_speed4);
		tv_lat4 = (TextView) getActivity().findViewById(R.id.tv_lat4);
		tv_j3 = (TextView) getActivity().findViewById(R.id.tv_j3);
		tv_j4 = (TextView) getActivity().findViewById(R.id.tv_j4);
		tv_w3 = (TextView) getActivity().findViewById(R.id.tv_w3);
		tv_w4 = (TextView) getActivity().findViewById(R.id.tv_w4);

		img1 = (MySurfaceView) getActivity().findViewById(R.id.img1);
		img1.init(pool_showImg);
		img2 = (MySurfaceView) getActivity().findViewById(R.id.img2);
		img2.init(pool_showImg);
		img3 = (MySurfaceView) getActivity().findViewById(R.id.img3);
		img3.init(pool_showImg);
		img4 = (MySurfaceView) getActivity().findViewById(R.id.img4);
		img4.init(pool_showImg);
//		img1.setOnTouchListener(new MyOnTouchListener(1,img1));
//		img2.setOnTouchListener(new MyOnTouchListener(2,img2));
//		img3.setOnTouchListener(new MyOnTouchListener(3,img3));
//		img4.setOnTouchListener(new MyOnTouchListener(4,img4));

		img31 = (MySurfaceView) getActivity().findViewById(R.id.img31);
		img31.init(pool_showImg);
		img32 = (MySurfaceView) getActivity().findViewById(R.id.img32);
		img32.init(pool_showImg);
		img33 = (MySurfaceView) getActivity().findViewById(R.id.img33);
		img33.init(pool_showImg);
//		img31.setOnTouchListener(new MyOnTouchListener(1,img31));
//		img32.setOnTouchListener(new MyOnTouchListener(2,img32));
//		img33.setOnTouchListener(new MyOnTouchListener(3,img33));

		videoPauseImg = (ImageView) getActivity().findViewById(R.id.videoPauseImg);
		videoPauseImg.setOnClickListener(this);

		gps = (ImageView) getActivity().findViewById(R.id.gps);
		gps.setOnClickListener(this);
	}


	protected void getShipControlData() {

		String shipId = shipID;
		if(shipId!=null&&shipId.startsWith("0x"))
			shipId = shipId.substring(2);

		Map<String, Object> apiParams = new HashMap<String, Object>();

		dataLoader.getZd_JavaManageResult(new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String arg2) {

				try {
					if(arg2==null||"".equals(arg2)){
						//					Toast.makeText(ShipHCListIngActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
						return;
					}

					Gson gson = new Gson();
					final HashMap<String, Object> rmap = gson.fromJson(
							arg2, new TypeToken<Map<String, Object>>() {
							}.getType());
					if (rmap.get("returnCode").equals("Success")) {

						Map<String, Object> map = (Map<String, Object>) rmap.get("shipAttrData");
						if(!map.isEmpty()){
							sc = new ShipControlData(map);

							Message message = new Message();
							message.what = RShipControlData;
							handler.sendMessage(message);
						}

					} else {
						//					Toast.makeText(ShipHCListIngActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
				}

			}
			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);

			}
		}, ApplicationUrls.shipInfo2+shipId, apiParams, "get");
	}

	@Override
	public boolean loadPlayGpsData(Calendar start,Calendar end) {
		Calendar startTemp = start;

		if (start.getTimeInMillis() - end.getTimeInMillis() >= 0l) {
			Toast.makeText(getActivity(), "请设置结束时间大于开始时间！", Toast.LENGTH_SHORT)
			.show();
			return false;
		}
		if (end.getTimeInMillis() - start.getTimeInMillis() > loadDays*24*60*60*1000l) {
			Toast.makeText(getActivity(), "最多只支持加载"+loadDays+"天数据！", Toast.LENGTH_SHORT)
			.show();
			return false;
		}

		initMinArr(startTemp,end);
		endTimeChang(start, end);

		return true;
	}

	private void initMinArr(Calendar start,Calendar end) {
		this.start = start;
		this.end = end;

		long timeOne=start.getTimeInMillis();
		long timeTwo=end.getTimeInMillis();
		int minute=(int) ((timeTwo-timeOne)/(1000*60));//转化minute

		minArr = new String[minute];
		for(int i=0;i<minArr.length;i++){
			if(i == 0){
				start = CalendarUtil.getTheSecondZero(start);
				minArr[0] = CalendarUtil.toYYYY_MM_DD_HH_MM_SS(start);
			}else{
				start = CalendarUtil.addMinutes(start, 1);
				minArr[i] = CalendarUtil.toYYYY_MM_DD_HH_MM_SS(start);
			}
		}
	}

	private void initGpsDataArr2() {
		if(minArr == null)
			return;

		gpsDataArr = new ShipGpsData[minArr.length];

		ShipGpsData s = null;
		for(int i=0;i<gpsDataArr.length;i++){
			String min = minArr[i];
			Calendar minc = CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(min);

			if(s == null)
				s = GpsDataQueue.getInstance().poll();

			String doGpsTime = doGpsTime(s.getGpsTime());
			Calendar doGpsTimec = CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(doGpsTime);

			if(minc.getTimeInMillis() == doGpsTimec.getTimeInMillis()){
				gpsDataArr[i] = s;
				s = null;
				continue;
			}else if(minc.getTimeInMillis()>doGpsTimec.getTimeInMillis()){
				for(;;){
					s = GpsDataQueue.getInstance().poll();
					String doGpsTimeTemp = doGpsTime(s.getGpsTime());
					Calendar doGpsTimeTempc = CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(doGpsTimeTemp);
					if(minc.getTimeInMillis()<=doGpsTimeTempc.getTimeInMillis()){
						gpsDataArr[i] = s;
						s = null;
						break;
					}
				}
			}
		}
	}
//	private void initGpsDataArr() {
//		if(minArr == null)
//			return;
//
//		gpsDataArr = new ShipGpsData[minArr.length];
//
//		for(int i=0;i<gpsDataArr.length;i++){
//			String min = minArr[i];
//			Calendar minc = CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(min);
//
//			for(ShipGpsData s:positionDatas){
//				String doGpsTime = doGpsTime(s.getGpsTime());
//				Calendar doGpsTimec = CalendarUtil.parseYYYY_MM_DD_HH_MM_SS(doGpsTime);
//
//				if(minc.getTimeInMillis() == doGpsTimec.getTimeInMillis()){
//					gpsDataArr[i] = s;
//					break;
//				}else if(doGpsTimec.getTimeInMillis()>minc.getTimeInMillis()){
//					break;
//				}
//			}
//		}
//	}

	//"GPSTime":"2016-08-04 16:52:25" 转成"GPSTime":"2016-08-04 16:52:00"
	private static String doGpsTime(String gpsTime){
		String[] arr = gpsTime.split(":");
		return arr[0]+":"+arr[1]+":" +"00";
	}

	private void initData(String startTime,String endTime){
		if(positionDatas==null){
			positionDatas = new ArrayList<ShipGpsData>();
		}else{
			positionDatas.clear();
		}
		GpsDataQueue.getInstance().clear();

		loadData(startTime,endTime);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initShipPowerChannels() {
		if(!"".equals(shipID)){

			if(channels.isEmpty()){
				SharedPreferences sp = GlobalApplication.getInstance().getSharedPreferences(ApplicationConstants.UserPowerData_SharedPreferences, Context.MODE_PRIVATE);
				String object = sp.getString("UserPower", "");

				UserPowerData data = null;
				if(!"".equals(object)){
					Gson gson = new Gson();
					data = gson.fromJson(object, new TypeToken<UserPowerData>() {}.getType());
				}else
					data = new UserPowerData();

				flag:
				for(UserPowerShipData upsd:data.getUserPowerShipDatas()){
					if(upsd.getShipID().equals(shipID)){
						for(ShipModelData smd:upsd.getShipModels()){
							if(smd.getModel() == ShipModelCode.five){
								for(ShipModelNoData smnd:smd.getModelNolist()){
									channels.add(smnd.getModelNo());
								}
								break flag;
							}else
								continue;
						}
					}else
						continue;
				}
				if(channels.size() == 3){
					shipImg.setVisibility(View.VISIBLE);
					shipImg3.setVisibility(View.VISIBLE);

					img31.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(0))));
					img32.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(1))));
					img33.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(2))));
				}else{
					shipImg.setVisibility(View.VISIBLE);
					shipImg4.setVisibility(View.VISIBLE);

					switch (channels.size()){
						case 1:
							img1.setVisibility(View.VISIBLE);
							img1.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(0))));
							break;
						case 2:
							img1.setVisibility(View.VISIBLE);
							img1.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(0))));
							img2.setVisibility(View.VISIBLE);
							img2.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(1))));
							break;
						case 4:
							img1.setVisibility(View.VISIBLE);
							img1.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(0))));
							img2.setVisibility(View.VISIBLE);
							img2.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(1))));
							img3.setVisibility(View.VISIBLE);
							img3.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(2))));
							img4.setVisibility(View.VISIBLE);
							img4.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(3))));
							break;
						default:
							break;
					}

//					for(int i=1;i<=channels.size();i++){
//						int view_id = getResources().getIdentifier("img"+i, "id",  getActivity().getPackageName());
//						ImageView view = (ImageView) getActivity().findViewById(view_id);
//						view.setOnTouchListener(new MyOnTouchListener(Integer.valueOf(channels.get(i-1)),view));
//					}
				}
			}
		}
	}

	public static String getUrl(ShipGpsData data,String string){
		String shipChannelCacheDirStr = cacheDir + "/" + data.getShipID() + "/" + string;
		String shipChannelCacheImg = shipChannelCacheDirStr+"/"+LoadImgRunnable.doGpsTime(data.getGpsTime())+".png";
		if(new File(shipChannelCacheImg).exists())
			return shipChannelCacheImg;
		else
			return null;
	}

	protected void showLoseShipImg(String time) {
		try {
			int size = channels.size();
			if(size ==0)
				return;

			if(size==3){

				tv_time3.setText("最后时间："+loseLastTime);
				tv_speed3.setText("航速：未知");
//				tv_lat3.setText("航向：未知");
				tv_j3.setText("经度：未知");
				tv_w3.setText("纬度：未知");

				img31.drawImg(img31.new DrawTask(null, time));
				img32.drawImg(img32.new DrawTask(null, time));
				img33.drawImg(img33.new DrawTask(null, time));
			}else{

				tv_time4.setText("最后时间："+loseLastTime);
				tv_speed4.setText("航速：未知");
//				tv_lat4.setText("航向：未知");
				tv_j4.setText("经度：未知");
				tv_w4.setText("纬度：未知");

				switch (size){
					case 1:
						img1.drawImg(img1.new DrawTask(null, time));
						break;
					case 2:
						img1.drawImg(img1.new DrawTask(null, time));
						img2.drawImg(img2.new DrawTask(null, time));
						break;
					case 4:
						img1.drawImg(img1.new DrawTask(null, time));
						img2.drawImg(img2.new DrawTask(null, time));
						img3.drawImg(img3.new DrawTask(null, time));
						img4.drawImg(img4.new DrawTask(null, time));
						break;
					default:
						break;
				}

//				for (int i = 1; i <= size; i++) {
//					int view_id = getResources().getIdentifier("img"+i, "id",  getActivity().getPackageName());
//					MySurfaceView view = (MySurfaceView) getActivity().findViewById(view_id);
//					view.setVisibility(View.VISIBLE);
//
//					view.drawImg(null, time);
//				}
			}

		} catch (Exception e) {
		}
	}

	private void showShipImg(ShipGpsData curShip) {

		try {
			int size = channels.size();
			if(size ==0)
				return;

			if(size==3){

				tv_time3.setText("最后时间："+curShip.getGpsTime());
				tv_speed3.setText("航速："+curShip.getGpsSpeed()+"节");
//					tv_lat3.setText("航向："+curShip.getGpsCourse());
				tv_j3.setText("经度："+curShip.getGpsLongitude());
				tv_w3.setText("纬度："+curShip.getGpsLatitude());

					String imgStr31 = getUrl(curShip,channels.get(0));
					if(imgStr31 !=null )
					{
						img31.drawImg(img31.new DrawTask(imgStr31, curShip.getGpsTime()));
					}
					else
						img31.drawImg(img31.new DrawTask(null, curShip.getGpsTime()));

					String imgStr32 = getUrl(curShip,channels.get(1));
					if(imgStr32 !=null )
					{
						img32.drawImg(img32.new DrawTask(imgStr32, curShip.getGpsTime()));
					}
					else
						img32.drawImg(img32.new DrawTask(null, curShip.getGpsTime()));

					String imgStr33 = getUrl(curShip,channels.get(2));
					if(imgStr33 !=null )
					{
						img33.drawImg(img33.new DrawTask(imgStr33, curShip.getGpsTime()));
					}
					else
						img33.drawImg(img33.new DrawTask(null, curShip.getGpsTime()));

			}else{

				tv_time4.setText("最后时间："+curShip.getGpsTime());
				tv_speed4.setText("航速："+curShip.getGpsSpeed()+"节");
//					tv_lat4.setText("航向："+curShip.getGpsCourse());
				tv_j4.setText("经度："+curShip.getGpsLongitude());
				tv_w4.setText("纬度："+curShip.getGpsLatitude());

				switch (size){
					case 1:
						String imgStr1 = getUrl(curShip,channels.get(0));
						if(imgStr1 !=null)
							img1.drawImg(img1.new DrawTask(imgStr1, curShip.getGpsTime()));
						else
							img1.drawImg(img1.new DrawTask(null, curShip.getGpsTime()));
						break;
					case 2:
						String imgStr21 = getUrl(curShip,channels.get(0));
						if(imgStr21 !=null)
							img1.drawImg(img1.new DrawTask(imgStr21, curShip.getGpsTime()));
						else
							img1.drawImg(img1.new DrawTask(null, curShip.getGpsTime()));

						String imgStr22 = getUrl(curShip,channels.get(1));
						if(imgStr22 !=null)
							img2.drawImg(img2.new DrawTask(imgStr22, curShip.getGpsTime()));
						else
							img2.drawImg(img2.new DrawTask(null, curShip.getGpsTime()));

						break;
					case 4:
						String imgStr41 = getUrl(curShip,channels.get(0));
						if(imgStr41 !=null)
							img1.drawImg(img1.new DrawTask(imgStr41, curShip.getGpsTime()));
						else
							img1.drawImg(img1.new DrawTask(null, curShip.getGpsTime()));

						String imgStr42 = getUrl(curShip,channels.get(1));
						if(imgStr42 !=null)
							img2.drawImg(img2.new DrawTask(imgStr42, curShip.getGpsTime()));
						else
							img2.drawImg(img2.new DrawTask(null, curShip.getGpsTime()));

						String imgStr43 = getUrl(curShip,channels.get(2));
						if(imgStr43 !=null)
							img3.drawImg(img3.new DrawTask(imgStr43, curShip.getGpsTime()));
						else
							img3.drawImg(img3.new DrawTask(null, curShip.getGpsTime()));

						String imgStr44 = getUrl(curShip,channels.get(3));
						if(imgStr44 !=null)
							img4.drawImg(img4.new DrawTask(imgStr44, curShip.getGpsTime()));
						else
							img4.drawImg(img4.new DrawTask(null, curShip.getGpsTime()));

						break;
					default:
						break;
				}

//					for (int i = 1; i <= size; i++) {
//						int view_id = getResources().getIdentifier("img"+i, "id",  getActivity().getPackageName());
//						MySurfaceView view = (MySurfaceView) getActivity().findViewById(view_id);
//						view.setVisibility(View.VISIBLE);
//
//						String imgStr21 = getUrl(curShip,channels.get(i-1));
//						if(imgStr21 !=null )
//						{
//							view.drawImg(imgStr21, curShip.getGpsTime());
//						}
//						else
//							view.drawImg(null, curShip.getGpsTime());
//					}
				}

		} catch (Exception e) {
		}

	}

	protected void gpsLoadedAfterStart() {

		commonVideoView.startCacheSeekBarTimer();
		startThreadLoadImg();
		PlayGpsTimer.getInstance().setHandler(handler);
    	PlayGpsTimer.getInstance().startPlayGpsTimer();
//    	modChang();
	}

	private void startThreadLoadImg() {
		try{
			Message msgFromClient = Message.obtain(null, START_lOADIMG, 0, 0);

			Bundle bundle = new Bundle();
			bundle.putString("shipId",shipID);
			bundle.putString("startTime",CalendarUtil.toYYYY_MM_DD_HH_MM_SS(start));
			bundle.putString("endTime",CalendarUtil.toYYYY_MM_DD_HH_MM_SS(end));
			ArrayList<CharSequence> chs = new ArrayList<CharSequence>();
			for(String s:channels)
				chs.add(s);
			bundle.putCharSequenceArrayList("chanls",chs);

			List<Cookie> list = GlobalApplication.getInstance().getCookies();
			String PHPSESSID = "";
			for (Cookie cookie : list) {
				if (cookie.getName().equals("PHPSESSID")) {
					PHPSESSID = cookie.getValue();
				}
			}
			bundle.putString("PHPSESSID",PHPSESSID);

			msgFromClient.setData(bundle);
			msgFromClient.replyTo = mMessenger;
			if (isConn) {
				//往服务端发送消息
				mService.send(msgFromClient);
			}
		} catch (RemoteException e){
		}
//		mBinderService.startThreadLoadImg(shipID,channels,start,end);
	}

	private void drawLines() {
//		bmHistory.clear();
//		NewContentFragment.mapAddPort(getActivity(),bmHistory);

		List<OverlayOptions> polylineOptionsList = new ArrayList<OverlayOptions>();
		if (positionDatas.size() >= 2) {
			List<LatLng> points = new ArrayList<LatLng>();
			ShipGpsData lastPoint = positionDatas.get(0);
//			LatLng first = MapConvertUtil.convertFromGPS( new LatLng(Double.parseDouble(lastPoint.getLatitude()),
//					Double.parseDouble(lastPoint.getLongitude())));
			LatLng first;
			if(lastPoint.getBdgpsLongitude()!=null&&lastPoint.getBdgpsLongitude()!=0.0)
				first = new LatLng(lastPoint.getBdgpsLatitude(),lastPoint.getBdgpsLongitude());
			else
			    first = new LatLng(lastPoint.getGpsLatitude(),lastPoint.getGpsLongitude());
			points.add(first);
			setCenter(lastPoint, z);

			for(int i=1; i<positionDatas.size();i++){
//				LatLng start =  MapConvertUtil.convertFromGPS(new LatLng(Double.parseDouble(positionDatas.get(
//						i).getLatitude()), Double.parseDouble(positionDatas.get(i)
//						.getLongitude())));

				LatLng start;
				if(positionDatas.get(i).getBdgpsLongitude()!=null&&positionDatas.get(i).getBdgpsLongitude()!=0.0)
					start = new LatLng(positionDatas.get(i).getBdgpsLatitude(),positionDatas.get(i).getBdgpsLongitude());
				else
					start = new LatLng( positionDatas.get(i).getGpsLatitude(),positionDatas.get(i).getGpsLongitude());

				points.add(start);
			}
			if (points.size() > 1) { //.color(0xAAFF0000)
				// 添加纹理图片列表
				List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
				List<Integer> textureIndexs = new ArrayList<Integer>();
				for (int i = 0; i <points.size() ; i++) {
					textureList.add(mTexture);
					textureIndexs.add(i);
				}

				OverlayOptions ooPolyline = new PolylineOptions().width(10)
						.zIndex(20).points(points).textureIndex(textureIndexs)// 点位纹理图片信息的顺序列表
						.customTextureList(textureList);// 纹理图片列表;

				polylineOptionsList.add(ooPolyline);
				polylineOverlayManager.setData(polylineOptionsList);
				polylineOverlayManager.addToMap();
			}
		}
	};

	protected void drawLines(List<ShipGpsData> shipGpsDatas, List<ShipCKGpssData> shipOneCKHcData,int z) {
		if(!isAdded())
			return;
		if (shipGpsDatas!=null&&shipGpsDatas.size() >= 2) {
			List<LatLng> points = new ArrayList<LatLng>();

			for(int i=0; i<shipGpsDatas.size();i++){

				LatLng start;
				if(shipGpsDatas.get(i).getBdgpsLongitude()!=null&&shipGpsDatas.get(i).getBdgpsLongitude()!=0.0)
					start = new LatLng(shipGpsDatas.get(i).getBdgpsLatitude(),shipGpsDatas.get(i).getBdgpsLongitude());
				else
					start = new LatLng( shipGpsDatas.get(i).getGpsLatitude(),shipGpsDatas.get(i).getGpsLongitude());

				points.add(start);
			}
			if (points.size() > 1) {
				// 添加纹理图片列表
				List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
				List<Integer> textureIndexs = new ArrayList<Integer>();
				for (int i = 0; i <points.size() ; i++) {
					textureList.add(mRedTexture);
					textureIndexs.add(i);
				}

				//.color(0xAAFF0000)
				OverlayOptions ooPolyline = new PolylineOptions().width(10)
						.zIndex(20).points(points).textureIndex(textureIndexs)// 点位纹理图片信息的顺序列表
						.customTextureList(textureList);// 纹理图片列表;;
				bmHistory.addOverlay(ooPolyline);
			}
		}else if(shipOneCKHcData!=null&&shipOneCKHcData.size() >= 2){
			List<LatLng> points = new ArrayList<LatLng>();

			for(int i=0; i<shipOneCKHcData.size();i++){

				LatLng start= new LatLng(Double.valueOf(shipOneCKHcData.get(i).getLatitude())
							,Double.valueOf(shipOneCKHcData.get(i).getLongitude()));

				points.add(start);
			}
			if (points.size() > 1) {
				OverlayOptions ooPolyline = new PolylineOptions().width(7)
						.zIndex(20).color(0xAA0000FF).points(points);
				bmHistory.addOverlay(ooPolyline);
			}
		}
	}

	private synchronized void moveMarker(ShipGpsData tpp,ShipGpsData toPoint) {
		removeMarker();
		addMarker(tpp,toPoint);
	}
	private void addMarker(ShipGpsData topPoint,ShipGpsData currPoint) {
		// 定义Maker坐标点
		LatLng first;
		if(currPoint.getBdgpsLongitude()!=null&&currPoint.getBdgpsLongitude()!=0.0)
			first = new LatLng(currPoint.getBdgpsLatitude(),currPoint.getBdgpsLongitude());
		else
		    first = new LatLng(currPoint.getGpsLatitude(),currPoint.getGpsLongitude());

		Double currPointSpeed = currPoint.getGpsSpeed()!=null?currPoint.getGpsSpeed():0;

		BitmapDescriptor bitmap = NewContentFragment.getImgByState(String.valueOf(currPointSpeed), topPoint.getGpsTime(), currPoint.getGpsTime());

		// 构建MarkerOption，用于在地图上添加Marker
		float cou = (float) (currPoint.getGpsCourse()-0.0f);
//		float cou = (float) (currPoint.getGpsCourse()-0.0f);

		bitmap = BitmapDescriptorFactory.fromBitmap(Util.first(getResources(),bitmap.getBitmap(),cou,baseBitmapOrg.getBitmap()));

		OverlayOptions option = new MarkerOptions().position(first)
				.icon(bitmap).anchor(0.5f, 0.5f).title("船名");
		marker = (Marker) (this.bmHistory.addOverlay(option));
		option = null;
		bitmap = null;
	}

	private void removeMarker() {
		if (marker != null)
			marker.remove();
	}
	@Override
	public void onPause() {
		super.onPause();
		mapHistory.onPause();
		mWakeLock.release();
	}
	@Override
	public void onResume() {
		super.onResume();
		mapHistory.onResume();
		mWakeLock.acquire();
	}


	private void initMap() {
		MapView.setCustomMapStylePath(ContentFragment.getAssetsCacheFile(getActivity(),"baidu_custom_config"));
		mapHistory = (MapView) getActivity().findViewById(R.id.mapHistory);
//		mapHistory.setCustomMapStylePath(ContentFragment.getAssetsCacheFile(getActivity(),"baidu_custom_config"));
		
		// 不显示地图缩放控件（按钮控制栏）
		mapHistory.showZoomControls(false);
		// 不显示地图上比例尺
//				mapHistory.showScaleControl(false);
		// 隐藏百度的LOGO
		View child = mapHistory.getChildAt(1);
		if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) 
			child.setVisibility(View.INVISIBLE);
		
		bmHistory = mapHistory.getMap();
		bmHistory.showMapPoi(false);// 将底图标注设置为隐藏
		
		this.uisHistory = this.bmHistory.getUiSettings();
		this.uisHistory.setOverlookingGesturesEnabled(false);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(BEIJING_LATLNG);
		this.bmHistory.setMapStatus(msu);
		
		msu = MapStatusUpdateFactory.zoomTo(z);
		this.bmHistory.setMapStatus(msu);
		this.bmHistory.setOnMarkerClickListener(this);
		this.bmHistory.setOnMapClickListener(this);
		
		bmHistory.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChange(MapStatus arg0) {
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus status) {
				z = status.zoom;                
//		    	if(Math.abs(NearActivity.this.zoom-zoom)> 0.5){
//
//		    		if(marker != null){
//		    			marker.remove();  
//		    		}
//		    		marker = BaiduMapHelper.addMarker(mBaiduMap, 
//		    				Double.parseDouble(sharedPreferences.getString("Lat", "0")), 
//		    				Double.parseDouble(sharedPreferences.getString("Lng", "0")), 
//		    				"我的位置",
//		    				R.drawable.marker_me, true);
//		    		
//		    		NearActivity.this.zoom =zoom;                    
//                    Log.d("zoom","缩放起了变化，现在缩放等级为"+zoom);                
//                }
				
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
			}  });
		
		NewContentFragment.mapAddPort(getActivity(),bmHistory);
		polylineOverlayManager = new PolylineOverlayManager(bmHistory);
	}

	class LoadShipCKHX implements Runnable{
		private String arg2;
		public LoadShipCKHX(String arg2) {
			this.arg2 = arg2;
		}

		@Override
		public void run() {
			try {
//							writeLog(start,end,arg2);
//							Util.writeFileData(start+end, arg2);

				ckGpsData = ParseJson.getCKHCGps(arg2);

				if (ckGpsData.isEmpty()) {

					Message message = new Message();
					message.what = ParseCKJsonIsEmpty;
					handler.sendMessage(message);

				}else{
					Message message = new Message();
					message.what = ParseCKJson;
					handler.sendMessage(message);

				}
			} catch (Exception e) {

				Message message = new Message();
				message.what = ParseCKJsonIsError;
				handler.sendMessage(message);
			}
		}
	}
	
	private void loadShipCKHX(final String start,final String end) {
		
		Map<String, Object> apiParams = new HashMap<String, Object>();
		dataLoader.getZd_ApiResult(new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(final String arg2) {
				super.onSuccess(arg2);
				
				if(pool_showImg!=null)
					pool_showImg.execute(new LoadShipCKHX(arg2));
			}
			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);

			}
		}, ApplicationUrls.shipCKHX+start+"&EndPort="+end, apiParams, "get");
	
	}

	class LoadData implements Runnable{
		private String arg2;
		public LoadData(String arg2) {
			this.arg2 = arg2;
		}

		@Override
		public void run() {
			List<ShipGpsData> list = new ArrayList<ShipGpsData>();
			try {
				list = ParseJson.getHistoryGps(arg2);

				if (list.isEmpty()) {
					Message message = new Message();
					message.what = ParseJsonIsEmpty;
					handler.sendMessage(message);
				} else {
					if (list.size() >= 10000) {
						Message message = new Message();
						message.what = ParseJsonTooMuch;
						handler.sendMessage(message);
					} else {
						positionDatas.addAll(list);

						initGpsDataArr2();

						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
				}
			} catch (Exception e) {
				Message message = new Message();
				message.what = ParseJsonException;
				handler.sendMessage(message);
			}
		}
	}
	
	// 初始化坐标数据
	protected synchronized void loadData(String startTime,String endTime) {
		
		Map<String, Object> apiParams = new HashMap<String, Object>();
		dataLoader.getZd_ApiResult(new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				if(!isAdded())
					return;
				dialog = dialogUtil.loading("数据加载中", "请稍候...");
			}
			@Override
			public void onSuccess(final String arg2) {
				super.onSuccess(arg2);
				if(!isAdded())
					return;
				
				if(pool_showImg!=null)
					pool_showImg.execute(new LoadData(arg2));

				dialog.dismiss();
			}
			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				if(!isAdded())
					return;

				dialog.dismiss();
				if (content != null && content.equals("can't resolve host"))
					Toast.makeText(getActivity(), "网络连接异常",
							Toast.LENGTH_LONG).show();
				else if (content != null && content.equals("socket time out")) {
					Toast.makeText(getActivity(), "连接服务器超时",
							Toast.LENGTH_LONG).show();
				} else if (content != null) {
					Toast.makeText(getActivity(), content,
							Toast.LENGTH_LONG).show();
				} else 
					Toast.makeText(getActivity(), "未知异常",
							Toast.LENGTH_LONG).show();
				
				Message message = new Message();
				message.what = ParseJsonFailure;
				handler.sendMessage(message);

			}
		}, ApplicationUrls.historyGps + shipID + "&StartTime=" + startTime.replace(" ", "%20") + "&EndTime="
					+ endTime.replace(" ", "%20"), apiParams, "get");
	}

	public void setCenter(ShipGpsData pd, float z2) {

		double lat ;
		double lng ;
		if(pd.getBdgpsLongitude()!=null&&pd.getBdgpsLongitude()!=0.0){
			lat = pd.getBdgpsLatitude();
			lng = pd.getBdgpsLongitude();
		}else{

			lat = pd.getGpsLatitude();
			lng = pd.getGpsLongitude();
		}
		
		
//		if(setCenterFlag){
			setCenter(lat, lng, z2);
			setCenterFlag = false;
//		}
		
	}

	public void setCenter(double lat, double lng, float z2) {
		// 设定中心点坐标
//		LatLng cenpt =  MapConvertUtil.convertFromGPS(new LatLng(lat, lng));
		LatLng cenpt =  new LatLng(lat, lng);
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(z2)
				.build();

		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		this.bmHistory.animateMapStatus(mMapStatusUpdate);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.videoPauseImg:
			videoPauseImg.setVisibility(View.GONE);
			commonVideoView.playPause();
			break;
		case R.id.gps:
			if(cPoint!=null)
				setCenter(cPoint, z);
			break;

		default:
			break;
		}

	}

	@Override
	public void onMapClick(LatLng arg0) {
		//showPic.setVisibility(View.GONE);
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		//showPic.setVisibility(View.VISIBLE);
		return false;
	}
	private void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
//	private void clearImageview(){
//		int size = channels.size();
//		if(size ==0)
//			return;
//		switch (size) {
//		case 1:
//			releaseImageViewResouce(img12);
//			break;
//		case 2:
//			releaseImageViewResouce(img21);
//			releaseImageViewResouce(img22);
//			break;
//		case 3:
//			releaseImageViewResouce(img31);
//			releaseImageViewResouce(img32);
//			releaseImageViewResouce(img34);
//			break;
//		case 4:
//			releaseImageViewResouce(img41);
//			releaseImageViewResouce(img42);
//			releaseImageViewResouce(img43);
//			releaseImageViewResouce(img44);
//			break;
//
//		default:
//			break;
//		}
//	}


	@Override
	public void endTimeChang(Calendar startCalendar, Calendar endCalendar) {
		stopThreadLoadImg();
		preClose();
		
//		bmHistory.clear();
//		NewContentFragment.mapAddPort(getActivity(),bmHistory);
		polylineOverlayManager.removeFromMap();
//		shipImg.setVisibility(View.GONE);
		
		LoadedList.loadSize = 0;
		LoadedList.needTotalSize = minArr.length;
		commonVideoView.initProgressBar(minArr.length);
		ShipDynamicFragment2.currPlayPosition=0;
		ShipDynamicFragment2.flagFirst=true;
		ShipDynamicFragment2.flag = 0;
		
		initData(CalendarUtil.toYYYY_MM_DD_HH_MM_SS(startCalendar), CalendarUtil.toYYYY_MM_DD_HH_MM_SS(endCalendar));
	}

	private void stopThreadLoadImg(){

		try{
			Message msgFromClient = new Message();

			msgFromClient.what = STOP_lOADIMG;
			msgFromClient.replyTo = mMessenger;
			if (isConn) {
				//往服务端发送消息
				mService.send(msgFromClient);
			}
		} catch (RemoteException e){
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapHistory.onDestroy();
		mTexture.recycle();
		stopThreadLoadImg();

		getActivity().unbindService(connection);
		
//		mBinderService.stopThreadLoadImg();
//		getActivity().unbindService(connection);
		
		preClose();
//		if(pool!=null){
//			pool.shutdownNow();
//			pool = null;
//		}
		if(pool_showImg!=null){
			pool_showImg.shutdownNow();
			pool_showImg = null;
		}

		
		Intent intent = new Intent(getActivity(),ClearService.class); 
		getActivity().startService(intent);
	}

	private void preClose(){
		PlayGpsTimer.getInstance().stopPlayGpsTimer();
		CacheSeekBarTimer.getInstance().stopCacheSeekBarTimer();
//		PlayListenerTimer.getInstance().stopPlayListenerTimer();
	}
//
//	@Override
//	protected void onStop() {
//		super.onStop();
//		timer.cancel();
//		timer.purge();
//	}
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		initData();
//	}
//	@Override
//	protected void onResume() {
//		super.onResume();
//		initData();
//	}
	private void toggleImage(){
//		LayoutParams ps = shipPics.getLayoutParams();
//
//		if(scal == false){
//			ps.width = pt.x-30;
//			ps.height = ps.width;
//			scal = true;
//		}else{
//			scal = false;
//			ps.width = pt.x/3;
//			ps.height = ps.width;
//		}
//		shipPics.setLayoutParams(ps);
	}
	@Override
	public void fastChang() {
//		if (gpsDataArr != null && gpsDataArr.length>0) {
//			int size = gpsDataArr.length;
//			int ss = size/playfast;
//			String time = Util.secToTime(ss);
//			commonVideoView.setVideoTotalTimeText(time);
			try {
//				CacheSeekBarTimer.getInstance().resetTime(Long.valueOf(1000/playfast));
				PlayGpsTimer.getInstance().resetTime(Long.valueOf(1000/playfast));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
//		}
	}

	@Override
	public void modChang() {
//		if (gpsDataArr != null && gpsDataArr.length>0) {
//			int size = gpsDataArr.length;
//			int ss = size/playmod;
//			String time = Util.secToTime(ss);
//			commonVideoView.setVideoTotalTimeText(time);
			try {
//				CacheSeekBarTimer.getInstance().resetTime(Long.valueOf(1000/playmod));
				PlayGpsTimer.getInstance().resetTime(Long.valueOf(1000/playmod));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
//		}

	}

	@Override
	public void sloChang() {
//		if (gpsDataArr != null && gpsDataArr.length>0) {
//			int size = gpsDataArr.length;
//			int ss = size/playslo;
//			String time = Util.secToTime(ss);
//			commonVideoView.setVideoTotalTimeText(time);
			try {
//				CacheSeekBarTimer.getInstance().resetTime(Long.valueOf(1000/playslo));
				PlayGpsTimer.getInstance().resetTime(Long.valueOf(1000/playslo));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
//		}
	}

	@Override
	public void playBigImg() {
		// TODO Auto-generated method stub
//		isPlayBigImg = true;
		
	}

	@Override
	public void playSmaImg() {
		// TODO Auto-generated method stub
//		isPlayBigImg = false;
		
	} 
	private class MyOnTouchListener implements OnTouchListener {
		private int i;

		public MyOnTouchListener(int i) {
			this.i = i;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:// 手指压下屏幕
				if(cPoint!=null)
					showCustomDialog(i);
				break;
			}
			return false;
		}
	}
	private  void showCustomDialog(int i) {
		// 初始化一个自定义的Dialog
		final CustomDialog.Builder b = new CustomDialog.Builder(getActivity());
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View view = inflater.inflate(R.layout.progress_dialog, null);

		LinearLayout ll_viewArea = (LinearLayout) view.findViewById(R.id.ll_viewArea);
		LinearLayout.LayoutParams parm = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		parm.gravity = Gravity.CENTER;

		// 自定义布局控件，用来初始化并存放自定义imageView
		viewArea = new ViewArea3(b,getActivity(),cPoint, i);

		ll_viewArea.addView(viewArea, parm);

		b.setView(view);
		b.show();

		ll_viewArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewArea = null;
				b.dismiss();
			}
		});
	}

	@Override
	public void disPlayPauseImg() {
		videoPauseImg.setVisibility(View.VISIBLE);
	}

	@Override
	public void noDisPlayPauseImg() {
		videoPauseImg.setVisibility(View.GONE);
	}

}
