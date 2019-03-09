package com.riiablo.validator;

import android.support.annotation.Nullable;

public interface Validator extends Validatable {
  Validator ACCEPT_ALL                       = new AcceptAllValidator();
  Validator REJECT_ALL                       = new RejectAllValidator();
  Validator ACCEPT_NON_NULL                  = new NonNullValidator();
  Validator ACCEPT_NON_NULL_NON_EMPTY_STRING = new NonNullNonEmptyStringValidator();

  void validate(@Nullable Object obj);
}
