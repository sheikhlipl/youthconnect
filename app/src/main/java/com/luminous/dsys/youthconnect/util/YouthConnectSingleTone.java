package com.luminous.dsys.youthconnect.util;

import android.content.Context;

import com.luminous.dsys.youthconnect.pojo.Feedback;
import com.luminous.dsys.youthconnect.pojo.ReportForm;

import java.util.ArrayList;
import java.util.List;

public class YouthConnectSingleTone {

	private static YouthConnectSingleTone instance = null;
	public List<Feedback> submitedReport = null;
	public List<Feedback> expierdReport = null;
	public List<Feedback> rejectedReport = null;
	public List<Feedback> pendingReport = null;
	public List<Feedback> allReport = null;
	public ReportForm mReportForm = null;
	public boolean isBackFromSubmit = false;
	public boolean isCameraCaptureActivityFinish = false;

	public Context context = null;

	public YouthConnectSingleTone(){
		submitedReport = new ArrayList<Feedback>();
		expierdReport = new ArrayList<Feedback>();
		rejectedReport = new ArrayList<Feedback>();
		pendingReport = new ArrayList<Feedback>();
		allReport = new ArrayList<Feedback>();
		mReportForm = new ReportForm();
	}
	
	public static YouthConnectSingleTone getInstance(){
		if(instance == null){
			instance = new YouthConnectSingleTone();
		}
		
		return instance;
	}
}