package com.ankurmittal.learning.storage;

public class NavListItem {
	public String option;
	public boolean selected;
	
	public NavListItem(String option, boolean selected) {
		this.option = option;
		this.selected = selected;
	}
	
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
