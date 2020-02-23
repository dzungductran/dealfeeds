package com.notalenthack.dealfeeds.service;

import com.notalenthack.dealfeeds.service.SettingData;
import com.notalenthack.dealfeeds.service.IStatusListener;

interface ITightwadAPI {

	void addFeed( String feed );

	void removeFeed( String feed );

	void refresh();

	void refreshFeed( String feed );

	void setSettings(in SettingData settings);

	SettingData getSettings();

	void addListener(IStatusListener listener);

	void removeListener();
}
