package com.ufasta.mobile.core.action;

import com.ufasta.mobile.core.action.ActionController.IAction;

public enum Actions implements IAction {

	/* List of all the actions that will be handled by the application */
	NO_ACTION;

	@Override
	public int getNumber() {
		return ordinal();
	}

	@Override
	public String getName() {
		return name();
	}
}
