package com.gmail.collinsmith70.unifi.widget;

public interface WidgetManager {

WidgetManager addWidget(Widget child);
boolean containsWidget(Widget child);
boolean removeWidget(Widget child);
int getNumWidgets();

}
