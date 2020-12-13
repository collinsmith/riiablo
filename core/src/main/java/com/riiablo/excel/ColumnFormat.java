package com.riiablo.excel;

public class ColumnFormat extends RuntimeException {
  final String columnText;
  final int columnIndex;

  ColumnFormat(NumberFormatException t, CharSequence columnText, int columnIndex) {
    this(t.getMessage(), columnText, columnIndex);
    initCause(t);
  }

  ColumnFormat(CharSequence message, CharSequence columnText, int columnIndex) {
    super(message == null ? null : message.toString());
    this.columnText = columnText.toString();
    this.columnIndex = columnIndex;
  }

  public String columnText() {
    return columnText;
  }

  public int columnIndex() {
    return columnIndex;
  }
}
