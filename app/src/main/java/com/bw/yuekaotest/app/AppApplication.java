package com.bw.yuekaotest.app;


import android.app.Application;

import com.bw.yuekaotest.db.SQLHelper;

import org.xutils.x;

public class AppApplication extends Application {
	private static AppApplication mAppApplication;
	private SQLHelper sqlHelper;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		x.Ext.init(this);
		mAppApplication = this;
	}

	/** ��ȡApplication */
	public static AppApplication getApp() {
		return mAppApplication;
	}
	
	/** ��ȡ���ݿ�Helper */
	public SQLHelper getSQLHelper() {
		if (sqlHelper == null)
			sqlHelper = new SQLHelper(mAppApplication);
		return sqlHelper;
	}
	
	/** �ݻ�Ӧ�ý���ʱ����� */
	public void onTerminate() {
		if (sqlHelper != null)
			sqlHelper.close();
		super.onTerminate();
	}

	public void clearAppCache() {
	}
}
