package com.ufasta.mobile.ui.activities;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ufasta.mobile.R;

public abstract class BaseLeftMenuActivity extends BaseNavigationActivity {

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;

	@Override
	public void onResume() {
		super.onResume();
		drawerLayout.closeDrawer(findViewById(R.id.left_menu_fragment));
	}

	@Override
	public void setContentView(int layoutResId) {
		super.setContentView(R.layout.activity_base_navigation);
		drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
		getLayoutInflater().inflate(layoutResId, (ViewGroup) findViewById(R.id.activity_content));

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, 0, 0) {

			public void onDrawerClosed(View view) {
			}

			public void onDrawerOpened(View drawerView) {
			}
		};

		drawerToggle.syncState();

		drawerLayout.setDrawerListener(drawerToggle);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (drawerLayout.isDrawerOpen(findViewById(R.id.left_menu_fragment))) {
				drawerLayout.closeDrawer(findViewById(R.id.left_menu_fragment));
			} else {
				drawerLayout.openDrawer(findViewById(R.id.left_menu_fragment));
			}
			return true;
		default:
			break;
		}
		drawerToggle.syncState();
		return super.onOptionsItemSelected(item);
	}

	public void hideLeftmenu() {
		drawerLayout.closeDrawer(findViewById(R.id.left_menu_fragment));
	}

}
