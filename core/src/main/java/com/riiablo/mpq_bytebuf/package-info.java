/**
 * Implementation of MPQ archive decoding backed by netty ByteBuf and wrapping a
 * mapped byte buffer. Should be a bit faster than original implementation using
 * java nio ByteBuffer and byte[], but mainly ByteBuf provides more seamless
 * decoding for certain raw files and a much easier interface to work with for
 * file decoding. Intended successor to com.riiablo.mpq
 */
package com.riiablo.mpq_bytebuf;
