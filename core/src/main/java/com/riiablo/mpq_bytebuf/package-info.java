/**
 * Intended successor to com.riiablo.mpq
 *
 * Implementation of MPQ archive decoding backed by netty ByteBuf and wrapping a
 * mapped byte buffer. Significantly faster than original implementation using
 * java nio ByteBuffer and byte[], but mainly ByteBuf provides more seamless
 * decoding for certain raw files and a much easier interface to work with for
 * file decoding.
 *
 * Allows files to be lazily decoded, s.t., once the header is loaded, other
 * contents of the file can be buffered and decoded as-needed, this allows for
 * significantly faster decoding times for files which only require immediate
 * access to a subset of their data at any given time.
 */
package com.riiablo.mpq_bytebuf;
