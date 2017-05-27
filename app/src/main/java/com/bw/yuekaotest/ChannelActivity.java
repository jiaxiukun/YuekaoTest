package com.bw.yuekaotest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bw.yuekaotest.adapter.DragAdapter;
import com.bw.yuekaotest.adapter.OtherAdapter;
import com.bw.yuekaotest.app.AppApplication;
import com.bw.yuekaotest.bean.Beans;
import com.bw.yuekaotest.bean.ChannelItem;
import com.bw.yuekaotest.bean.ChannelManage;
import com.bw.yuekaotest.view.DragGrid;
import com.bw.yuekaotest.view.OtherGridView;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ???????
 * @Author RA
 * @Blog http://blog.csdn.net/vipzjyno1
 */
public class ChannelActivity extends Activity implements OnItemClickListener {
	/** ????????GRIDVIEW */
	private DragGrid userGridView;
	/** ?????????GRIDVIEW */
	private OtherGridView otherGridView;
	/** ?????????????????????????? */
	DragAdapter userAdapter;
	/** ?????????????????? */
	OtherAdapter otherAdapter;
	/** ?????????? */
	ArrayList<ChannelItem> otherChannelList = new ArrayList<ChannelItem>();
	/** ????????? */
	ArrayList<ChannelItem> userChannelList = new ArrayList<ChannelItem>();
	/** ???????????????????????????????????????????????????????????????????????????? */	
	boolean isMove = false;
	private AlertDialog.Builder builder;
	private AlertDialog.Builder b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subscribe_activity);
		initView();

		setDialog();
		initData();

	}

	private void get() {
		RequestParams params = new RequestParams("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");
		params.addQueryStringParameter("username","abc");
		params.addQueryStringParameter("password","123");
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//解析result
				Log.d("ChannelActivity", "请求成功");
				String substring = result.substring(0, result.length() - 1);
				Gson gson=new Gson();
				Beans beans = gson.fromJson(substring, Beans.class);
				List<Beans.AppBean> app = beans.getApp();
				String url = app.get(1).getUrl();
				download(url);
				Log.d("ChannelActivity", url);

			}
			//请求异常后的回调方法
			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}
			//主动调用取消请求的回调方法
			@Override
			public void onCancelled(CancelledException cex) {
			}
			@Override
			public void onFinished() {
			}
		});
	}

	private void download(String url) {
		RequestParams params = new RequestParams(url);
//自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
		params.setSaveFilePath(Environment.getExternalStorageDirectory()+"/myapp/");
//自动为文件命名
		params.setAutoRename(true);
		x.http().post(params, new Callback.ProgressCallback<File>() {
			@Override
			public void onSuccess(File result) {
				//apk下载完成后，调用系统的安装方法
				Log.d("ChannelActivity", "下载成功");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
				ChannelActivity.this.startActivity(intent);
			}
			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}
			@Override
			public void onCancelled(CancelledException cex) {
			}
			@Override
			public void onFinished() {
			}
			//网络请求之前回调
			@Override
			public void onWaiting() {
			}
			//网络请求开始的时候回调
			@Override
			public void onStarted() {
			}
			//下载的时候不断回调的方法
			@Override
			public void onLoading(long total, long current, boolean isDownloading) {
				//当前进度和文件总大小
				Log.d("JAVA","current："+ current +"，total："+total);
			}
		});
	}

	public void setDialog(){
		b = new AlertDialog.Builder(this);
		b.setTitle("版本更新");
		b.setMessage("现在检测到新版本，是否更新?");
		b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				get();
			}
		});
		final String[] itmes = {"wifi","手机流量"};
		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("网络选择");
		builder.setSingleChoiceItems(itmes, -1, new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    b.show();
					dialog.cancel();
				}else{
					startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				}

			}
		});
	}
	
	/** ?????????*/
	private void initData() {
	    userChannelList = ((ArrayList<ChannelItem>) ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).getUserChannel());
	    otherChannelList = ((ArrayList<ChannelItem>)ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).getOtherChannel());
	    userAdapter = new DragAdapter(this, userChannelList);
	    userGridView.setAdapter(userAdapter);
	    otherAdapter = new OtherAdapter(this, otherChannelList);
	    otherGridView.setAdapter(this.otherAdapter);
	    //????GRIDVIEW??ITEM????????
	    otherGridView.setOnItemClickListener(this);
	    userGridView.setOnItemClickListener(this);

		otherGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				builder.show();
				return false;
			}
		});
	}
	
	/** ?????????*/
	private void initView() {
		userGridView = (DragGrid) findViewById(R.id.userGridView);
		otherGridView = (OtherGridView) findViewById(R.id.otherGridView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** GRIDVIEW?????ITEM??????????  */
	@Override
	public void onItemClick(AdapterView<?> parent, final View view, final int position,long id) {
		//?????????????????????????????????????????
		if(isMove){
			return;
		}
		switch (parent.getId()) {
		case R.id.userGridView:
			//position? 0??1 ????????????????
			if (position != 0 && position != 1) {
				final ImageView moveImageView = getView(view);
				if (moveImageView != null) {
					TextView newTextView = (TextView) view.findViewById(R.id.text_item);
					final int[] startLocation = new int[2];
					newTextView.getLocationInWindow(startLocation);
					final ChannelItem channel = ((DragAdapter) parent.getAdapter()).getItem(position);//???????????????
					otherAdapter.setVisible(false);
					//??????????
					otherAdapter.addItem(channel);
					new Handler().postDelayed(new Runnable() {
						public void run() {
							try {
								int[] endLocation = new int[2];
								//???????????
								otherGridView.getChildAt(otherGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
								MoveAnim(moveImageView, startLocation , endLocation, channel,userGridView);
								userAdapter.setRemove(position);
							} catch (Exception localException) {
							}
						}
					}, 50L);
				}
			}
			break;
		case R.id.otherGridView:
			final ImageView moveImageView = getView(view);
			if (moveImageView != null){
				TextView newTextView = (TextView) view.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
				final ChannelItem channel = ((OtherAdapter) parent.getAdapter()).getItem(position);
				userAdapter.setVisible(false);
				//??????????
				userAdapter.addItem(channel);
				new Handler().postDelayed(new Runnable() {
					public void run() {
						try {
							int[] endLocation = new int[2];
							//???????????
							userGridView.getChildAt(userGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
							MoveAnim(moveImageView, startLocation , endLocation, channel,otherGridView);
							otherAdapter.setRemove(position);
						} catch (Exception localException) {
						}
					}
				}, 50L);
			}
			break;
		default:
			break;
		}
	}
	/**
	 * ???ITEM???????
	 * @param moveView
	 * @param startLocation
	 * @param endLocation
	 * @param moveChannel
	 * @param clickGridView
	 */
	private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final ChannelItem moveChannel,
			final GridView clickGridView) {
		int[] initLocation = new int[2];
		//????????????VIEW??????
		moveView.getLocationInWindow(initLocation);
		//?????????VIEW,????????????????
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
		//???????????
		TranslateAnimation moveAnimation = new TranslateAnimation(
				startLocation[0], endLocation[0], startLocation[1],
				endLocation[1]);
		moveAnimation.setDuration(300L);//???????
		//????????
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);//??????????????View?????????????????
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				isMove = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				moveViewGroup.removeView(mMoveView);
				// instanceof ???????2????????????????????????DragGrid????OtherGridView
				if (clickGridView instanceof DragGrid) {
					otherAdapter.setVisible(true);
					otherAdapter.notifyDataSetChanged();
					userAdapter.remove();
				}else{
					userAdapter.setVisible(true);
					userAdapter.notifyDataSetChanged();
					otherAdapter.remove();
				}
				isMove = false;
			}
		});
	}
	
	/**
	 * ????????VIEW????????ViewGroup????????
	 * @param viewGroup
	 * @param view
	 * @param initLocation
	 * @return
	 */
	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}
	
	/**
	 * ?????????ITEM?????ViewGroup????????
	 */
	private ViewGroup getMoveViewGroup() {
		ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(this);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}
	
	/**
	 * ????????Item????View??
	 * @param view
	 * @return
	 */
	private ImageView getView(View view) {
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(cache);
		return iv;
	}
	
	/** ?????????????????????  */
	private void saveChannel() {
		ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).deleteAllChannel();
		ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).saveUserChannel(userAdapter.getChannnelLst());
		ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).saveOtherChannel(otherAdapter.getChannnelLst());
	}
	
	@Override
	public void onBackPressed() {
		saveChannel();
		super.onBackPressed();
	}
}
