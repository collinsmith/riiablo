package com.riiablo.table;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;

public final class TsvTranslators {
  private TsvTranslators() {}

  public static final CharSequenceTranslator TSV_ESCAPER = new TsvEscaper();

  /** Comma character. */
  private static final char TSV_DELIMITER = '\t';
  /** Quote character. */
  private static final char TSV_QUOTE = '"';
  /** Quote character converted to string. */
  private static final String TSV_QUOTE_STR = String.valueOf(TSV_QUOTE);
  /** Escaped quote string. */
  private static final String TSV_ESCAPED_QUOTE_STR = TSV_QUOTE_STR + TSV_QUOTE_STR;
  /** TSV key characters in an array. */
  private static final char[] TSV_SEARCH_CHARS = new char[] {
      TSV_DELIMITER, TSV_QUOTE, CharUtils.CR, CharUtils.LF
  };

  public static String escapeTsv(String input) {
    return TSV_ESCAPER.translate(input);
  }

  public static final class TsvEscaper extends CharSequenceTranslator {
    @Override
    public int translate(CharSequence input, int index, Writer out) throws IOException {
      if (index != 0) {
        throw new IllegalArgumentException(TsvEscaper.class.getSimpleName()
            + ".translate(final CharSequence input, final int index, final Writer out) "
            + "can not handle a non-zero index.");
      }

      translateWhole(input, out);
      return Character.codePointCount(input, index, input.length());
    }

    void translateWhole(final CharSequence input, final Writer out) throws IOException {
      final String inputSting = input.toString();
      if (StringUtils.containsNone(inputSting, TSV_SEARCH_CHARS)) {
        out.write(inputSting);
      } else {
        // input needs quoting
        out.write(TSV_QUOTE);
        out.write(StringUtils.replace(inputSting, TSV_QUOTE_STR, TSV_ESCAPED_QUOTE_STR));
        out.write(TSV_QUOTE);
      }
    }
  }
}
