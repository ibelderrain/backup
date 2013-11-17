package com.ufasta.mobile.core.action;

import com.ufasta.mobile.core.request.Params;

public interface IActionListener {

	void onDone(ActionController.IAction action, Params bundle);

	void onProgress(ActionController.IAction action, Params bundle);

	void onError(ActionController.IAction action, Params bundle);

	void onException(ActionController.IAction action, Params bundle);
}
