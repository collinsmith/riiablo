package com.riiablo.table.parser;

import io.netty.util.AsciiString;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

@Deprecated
public final class RunesMapper implements com.riiablo.table.ParserMapper<RunesParser> {
  private static final Logger log = LogManager.getLogger(RunesMapper.class);

  private static final int PREFIX_LENGTH = "RUNEWORD".length();

  @Override
  public int map(RunesParser parser, final String recordName) {
    final String idString = recordName.substring(PREFIX_LENGTH);
    final int oldId = Integer.parseInt(idString);
    final int newId;
    if (oldId == 22) {
      // Delerium
      newId = 2718;
    } else if (oldId == 95) {
      // Passion / Patience duplicate
      final CharSequence Rune_Name = parser.parser().token(-1, parser.fieldIds[1]);
      newId = AsciiString.contentEqualsIgnoreCase(Rune_Name, "Passion")
          ? 95
          : 96;
    } else {
      newId = oldId;
    }

    if (oldId != newId) log.debug("{}: {} -> {}", recordName, oldId, newId);
    return newId;
  }
}
