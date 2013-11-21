package com.ufasta.mobile.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ufasta.mobile.UFastaApp;
import com.ufasta.mobile.core.action.ActionController.IAction;
import com.ufasta.mobile.core.action.IActionListener;
import com.ufasta.mobile.core.request.Params;
import com.ufasta.mobile.ui.activities.BaseActivity;

public abstract class BaseFragment extends Fragment implements IActionListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		UFastaApp.actionController.unregister(this);
	}

	@Override
	public void onDone(IAction action, Params bundle) {
		getBaseActivity().hideProgress();
	}

	@Override
	public void onProgress(IAction action, Params bundle) {
		getBaseActivity().showProgress();
	}

	@Override
	public void onError(IAction action, Params bundle) {
		getBaseActivity().hideProgress();
	}

	@Override
	public void onException(IAction action, Params bundle) {
		getBaseActivity().hideProgress();
	}

	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

}
