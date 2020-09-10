package com.riiablo.attrs;

import java.util.Arrays;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class UpdateSequence {
  private static final Logger log = LogManager.getLogger(UpdateSequence.class);

  private static final Pool<UpdateSequence> POOL = Pools.get(UpdateSequence.class, 16);
  static UpdateSequence obtain() {
    return POOL.obtain();
  }

  private static final int MAX_SEQUENCE_LENGTH = 32;

  private final StatListRef[] sequence = new StatListRef[MAX_SEQUENCE_LENGTH];
  private AttributesUpdater updater;
  private int sequenceLength;
  private boolean sequencing;

  private Attributes attrs;
  private Attributes opBase;
  private CharStats.Entry charStats;

  public Attributes apply() {
    final StatListRef[] sequence = this.sequence;
    for (int i = 0; i < sequenceLength; i++) {
      final StatListRef seq = sequence[i];
      updater.add(attrs, seq);
    }

    updater.apply(attrs, charStats, opBase);
    final Attributes attrs = this.attrs;
    clear();
    POOL.free(this);
    return attrs;
  }

  UpdateSequence reset(
      final AttributesUpdater updater,
      final Attributes attrs,
      final int listFlags,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    if (sequencing) {
      throw new IllegalStateException("sequence locked, must apply current sequence");
    }

    this.updater = updater;
    this.attrs = attrs.reset();
    this.opBase = opBase;
    this.charStats = charStats;
    return addAll(attrs, listFlags);
  }

  void clear() {
    sequencing = false;
    Arrays.fill(sequence, 0, sequenceLength, null);
    sequenceLength = 0;
    this.attrs = null;
    this.charStats = null;
    this.updater = null;
  }

  public UpdateSequence add(StatListRef stats) {
    if (log.traceEnabled()) log.traceEntry("add(stats: {})", stats);
    if (sequenceLength >= MAX_SEQUENCE_LENGTH) {
      throw new IndexOutOfBoundsException(
          "sequenceLength(" + sequenceLength + ") >= MAX_SEQUENCE_LENGTH(" + MAX_SEQUENCE_LENGTH + ")");
    }

    sequence[sequenceLength++] = stats;
    return this;
  }

  public UpdateSequence addAll(Attributes attrs, final int listFlags) {
    if (log.traceEnabled()) log.traceEntry("addAll(attrs: {}, listFlags: {})", attrs, listFlags);
    if (!attrs.type().updatable()) return this;
    if (!attrs.type().isValid(listFlags)) return this;

    final StatList list = attrs.list();
    for (int i = 0, s = list.numLists(); i < s; i++) {
      if (((listFlags >> i) & 1) == 1) {
        add(list.get(i));
      }
    }

    return this;
  }
}
