package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Padded;

public interface Drawable extends Bounded, Padded {

  void draw(@NonNull Canvas canvas);

}
