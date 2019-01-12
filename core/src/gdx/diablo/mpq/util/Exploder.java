package gdx.diablo.mpq.util;

/*
  Taken from: https://github.com/horschi/OpenTeufel/blob/master/src/main/java/org/openteufel/file/mpq/explode/Exploder.java

  Modifications: Removed unused variables, made the static arrays private and changed formatting, set pInPos to 1

  *************

  Sources:
  https://github.com/ladislav-zezula/StormLib/blob/master/src/pklib/explode.c
  https://github.com/toshok/scsharp/blob/master/SCSharp/SCSharp.Mpq/PKLibDecompress.cs

  https://code.google.com/p/arx-fatalis-fixed/source/browse/trunk/Sources/HERMES/explode.c?r=23
  https://github.com/dcramer/ghostplusplus-nibbits/blob/master/StormLib/stormlib/pklib/explode.c
  https://code.google.com/p/stormlibsharp/source/browse/trunk/development/stormlib/src/pklib/explode.c?r=2
  http://yumiko.svnrepository.com/TrinityCore/trac.cgi/browser/trunk/contrib/vmap_extractor_v2/stormlib/pklib/explode.c

/* pkexplode.c                                Copyright (c) ShadowFlare 2003 */
/*---------------------------------------------------------------------------*/
/* Explode function compatible with compressed data from PKWARE Data         */
/* Compression library                                                       */
/*                                                                           */
/* Author: ShadowFlare (blakflare@hotmail.com)                               */
/*                                                                           */
/* This code was created from a format specification that was posted on      */
/* a newsgroup.  No reverse-engineering of any kind was performed by         */
/* me to produce this code.                                                  */
/*                                                                           */
/* This code is free and you may perform any modifications to it that        */
/* you wish to perform, but please leave my name in the file as the          */
/* original author of the code.                                              */
/*                                                                           */
/*---------------------------------------------------------------------------*/
/*   Date    Ver   Comment                                                   */
/* --------  ----  -------                                                   */
/* 03/05/10  1.02  Fix the timing of a buffer check                          */
/* 10/24/03  1.01  Added checks for when the end of a buffer is reached      */
/*                 Extended error codes added                                */
/* 06/29/03  1.00  First version                                             */

import java.nio.ByteBuffer;

/*****************************************************************************/

public class Exploder {
    private static final int PK_LITERAL_SIZE_FIXED = 0; // Use fixed size literal bytes, used for binary data
    private static final int PK_LITERAL_SIZE_VARIABLE = 1; // Use variable size literal bytes, used for text

    private static long TRUNCATE_VALUE(long value, int bits)  {
        return ((value) & ((1 << (bits)) - 1));
    }

    // Bit sequences used to represent literal bytes
    private static short[] ChCode =
        {
            0x0490, 0x0FE0, 0x07E0, 0x0BE0, 0x03E0, 0x0DE0, 0x05E0, 0x09E0,
            0x01E0, 0x00B8, 0x0062, 0x0EE0, 0x06E0, 0x0022, 0x0AE0, 0x02E0,
            0x0CE0, 0x04E0, 0x08E0, 0x00E0, 0x0F60, 0x0760, 0x0B60, 0x0360,
            0x0D60, 0x0560, 0x1240, 0x0960, 0x0160, 0x0E60, 0x0660, 0x0A60,
            0x000F, 0x0250, 0x0038, 0x0260, 0x0050, 0x0C60, 0x0390, 0x00D8,
            0x0042, 0x0002, 0x0058, 0x01B0, 0x007C, 0x0029, 0x003C, 0x0098,
            0x005C, 0x0009, 0x001C, 0x006C, 0x002C, 0x004C, 0x0018, 0x000C,
            0x0074, 0x00E8, 0x0068, 0x0460, 0x0090, 0x0034, 0x00B0, 0x0710,
            0x0860, 0x0031, 0x0054, 0x0011, 0x0021, 0x0017, 0x0014, 0x00A8,
            0x0028, 0x0001, 0x0310, 0x0130, 0x003E, 0x0064, 0x001E, 0x002E,
            0x0024, 0x0510, 0x000E, 0x0036, 0x0016, 0x0044, 0x0030, 0x00C8,
            0x01D0, 0x00D0, 0x0110, 0x0048, 0x0610, 0x0150, 0x0060, 0x0088,
            0x0FA0, 0x0007, 0x0026, 0x0006, 0x003A, 0x001B, 0x001A, 0x002A,
            0x000A, 0x000B, 0x0210, 0x0004, 0x0013, 0x0032, 0x0003, 0x001D,
            0x0012, 0x0190, 0x000D, 0x0015, 0x0005, 0x0019, 0x0008, 0x0078,
            0x00F0, 0x0070, 0x0290, 0x0410, 0x0010, 0x07A0, 0x0BA0, 0x03A0,
            0x0240, 0x1C40, 0x0C40, 0x1440, 0x0440, 0x1840, 0x0840, 0x1040,
            0x0040, 0x1F80, 0x0F80, 0x1780, 0x0780, 0x1B80, 0x0B80, 0x1380,
            0x0380, 0x1D80, 0x0D80, 0x1580, 0x0580, 0x1980, 0x0980, 0x1180,
            0x0180, 0x1E80, 0x0E80, 0x1680, 0x0680, 0x1A80, 0x0A80, 0x1280,
            0x0280, 0x1C80, 0x0C80, 0x1480, 0x0480, 0x1880, 0x0880, 0x1080,
            0x0080, 0x1F00, 0x0F00, 0x1700, 0x0700, 0x1B00, 0x0B00, 0x1300,
            0x0DA0, 0x05A0, 0x09A0, 0x01A0, 0x0EA0, 0x06A0, 0x0AA0, 0x02A0,
            0x0CA0, 0x04A0, 0x08A0, 0x00A0, 0x0F20, 0x0720, 0x0B20, 0x0320,
            0x0D20, 0x0520, 0x0920, 0x0120, 0x0E20, 0x0620, 0x0A20, 0x0220,
            0x0C20, 0x0420, 0x0820, 0x0020, 0x0FC0, 0x07C0, 0x0BC0, 0x03C0,
            0x0DC0, 0x05C0, 0x09C0, 0x01C0, 0x0EC0, 0x06C0, 0x0AC0, 0x02C0,
            0x0CC0, 0x04C0, 0x08C0, 0x00C0, 0x0F40, 0x0740, 0x0B40, 0x0340,
            0x0300, 0x0D40, 0x1D00, 0x0D00, 0x1500, 0x0540, 0x0500, 0x1900,
            0x0900, 0x0940, 0x1100, 0x0100, 0x1E00, 0x0E00, 0x0140, 0x1600,
            0x0600, 0x1A00, 0x0E40, 0x0640, 0x0A40, 0x0A00, 0x1200, 0x0200,
            0x1C00, 0x0C00, 0x1400, 0x0400, 0x1800, 0x0800, 0x1000, 0x0000
        };

    // Lengths of bit sequences used to represent literal bytes
    private static byte[] ChBits =
        {
            0x0B, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x08, 0x07, 0x0C, 0x0C, 0x07, 0x0C, 0x0C,
            0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0D, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
            0x04, 0x0A, 0x08, 0x0C, 0x0A, 0x0C, 0x0A, 0x08, 0x07, 0x07, 0x08, 0x09, 0x07, 0x06, 0x07, 0x08,
            0x07, 0x06, 0x07, 0x07, 0x07, 0x07, 0x08, 0x07, 0x07, 0x08, 0x08, 0x0C, 0x0B, 0x07, 0x09, 0x0B,
            0x0C, 0x06, 0x07, 0x06, 0x06, 0x05, 0x07, 0x08, 0x08, 0x06, 0x0B, 0x09, 0x06, 0x07, 0x06, 0x06,
            0x07, 0x0B, 0x06, 0x06, 0x06, 0x07, 0x09, 0x08, 0x09, 0x09, 0x0B, 0x08, 0x0B, 0x09, 0x0C, 0x08,
            0x0C, 0x05, 0x06, 0x06, 0x06, 0x05, 0x06, 0x06, 0x06, 0x05, 0x0B, 0x07, 0x05, 0x06, 0x05, 0x05,
            0x06, 0x0A, 0x05, 0x05, 0x05, 0x05, 0x08, 0x07, 0x08, 0x08, 0x0A, 0x0B, 0x0B, 0x0C, 0x0C, 0x0C,
            0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
            0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
            0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D,
            0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
            0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
            0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C, 0x0C,
            0x0D, 0x0C, 0x0D, 0x0D, 0x0D, 0x0C, 0x0D, 0x0D, 0x0D, 0x0C, 0x0D, 0x0D, 0x0D, 0x0D, 0x0C, 0x0D,
            0x0D, 0x0D, 0x0C, 0x0C, 0x0C, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D, 0x0D
        };

    // Bit sequences used to represent the base values of the copy length
    private static byte[] LenCode =
        {
            0x05, 0x03, 0x01, 0x06, 0x0A, 0x02, 0x0C, 0x14, 0x04, 0x18, 0x08, 0x30, 0x10, 0x20, 0x40, 0x00
        };

    // Lengths of bit sequences used to represent the base values of the copy length
    private static byte[] LenBits =
        {
            0x03, 0x02, 0x03, 0x03, 0x04, 0x04, 0x04, 0x05, 0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x07, 0x07
        };

    // Base values used for the copy length
    private static short[] LenBase =
        {
            0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009,
            0x000A, 0x000C, 0x0010, 0x0018, 0x0028, 0x0048, 0x0088, 0x0108
        };

    // Lengths of extra bits used to represent the copy length
    private static byte[] ExLenBits =
        {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
        };

    // Bit sequences used to represent the most significant 6 bits of the copy offset
    private static byte[] OffsCode =
        {
            0x03, 0x0D, 0x05, 0x19, 0x09, 0x11, 0x01, 0x3E, 0x1E, 0x2E, 0x0E, 0x36, 0x16, 0x26, 0x06, 0x3A,
            0x1A, 0x2A, 0x0A, 0x32, 0x12, 0x22, 0x42, 0x02, 0x7C, 0x3C, 0x5C, 0x1C, 0x6C, 0x2C, 0x4C, 0x0C,
            0x74, 0x34, 0x54, 0x14, 0x64, 0x24, 0x44, 0x04, 0x78, 0x38, 0x58, 0x18, 0x68, 0x28, 0x48, 0x08,
            (byte) 0xF0, 0x70, (byte) 0xB0, 0x30, (byte) 0xD0, 0x50, (byte) 0x90, 0x10, (byte) 0xE0, 0x60, (byte) 0xA0, 0x20, (byte) 0xC0, 0x40,
            (byte) 0x80, 0x00
        };

    // Lengths of bit sequences used to represent the most significant 6 bits of the copy offset
    private static byte[] OffsBits =
        {
            0x02, 0x04, 0x04, 0x05, 0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
            0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
            0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07,
            0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08
        };

    public static int pkexplode(ByteBuffer in, ByteBuffer out) {
        if (in.remaining() < 4) {
            throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");
        }

        // Get header from compressed data
        byte nLitSize = in.get();
        byte nDictSizeByte = in.get();

        // Check for a valid compression type
        if (nLitSize != PK_LITERAL_SIZE_FIXED && nLitSize != PK_LITERAL_SIZE_VARIABLE)
            throw new IllegalArgumentException("PK_ERR_BAD_DATA: Invalid LitSize: " + nLitSize);

        // Only dictionary sizes of 1024, 2048, and 4096 are allowed.
        // The values 4, 5, and 6 correspond with those sizes
        if (4 > nDictSizeByte || nDictSizeByte > 6)
            throw new IllegalArgumentException("PK_ERR_BAD_DATA: Invalid DictSizeByte: " + nDictSizeByte);

        // Store actual dictionary size
        int nDictSize = 64 << nDictSizeByte;

        // Initialize dictionary position
        byte[] Dict = new byte[0x1000]; // Sliding dictionary used for compression and decompression
        int pDictPos = 0;

        // Initialize current dictionary size to zero
        int nCurDictSize = 0;

        // Get first 16 bits
        byte nBits; // Number of bits in bit buffer
        long nBitBuffer; // Stores bits until there are enough to output a byte of data


        nBitBuffer = in.get() & 0xFFL;
        nBitBuffer += (in.get() & 0xFFL) << 8;
        nBits = 16;

        // Decompress until output buffer is full
        int i; // Index into tables
        int nCopyLen;
        while (out.hasRemaining()) {

            // Fill bit buffer with at least 16 bits
            while (nBits < 16) {
                // If input buffer is empty before end of stream, buffer is incomplete
                if (!in.hasRemaining()) {
                    // Store the current size of output
                    // nOutSize = pOutPos - pOutBuffer;
                    throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");
                }

                nBitBuffer += (in.get() & 0xFF) << nBits;
                nBits += 8;
            }

            // First bit is 1; copy from dictionary
            if ((nBitBuffer & 1) != 0) {

                // Remove first bit from bit buffer
                nBitBuffer >>= 1;
                nBits--;

                // Find the base value for the copy length
                for (i = 0; i <= 0x0F; i++) {
                    if (TRUNCATE_VALUE(nBitBuffer, LenBits[i] & 0xFF) == (LenCode[i] & 0xFF))
                        break;
                }

                // Remove value from bit buffer
                nBitBuffer >>= LenBits[i] & 0xFF;
                nBits -= LenBits[i] & 0xFF;

                // Store the copy length
                nCopyLen = (int) ((LenBase[i] & 0xFFFF) + TRUNCATE_VALUE(nBitBuffer, ExLenBits[i] & 0xFF)); // Length of data to copy from the dictionary

                // Remove the extra bits from the bit buffer
                nBitBuffer >>= ExLenBits[i] & 0xFF;
                nBits -= ExLenBits[i] & 0xFF;

                // If copy length is 519, the end of the stream has been reached
                if (nCopyLen == 519)
                    break;

                // Fill bit buffer with at least 14 bits
                while (nBits < 14) {
                    // If input buffer is empty before end of stream, buffer is incomplete
                    if (!in.hasRemaining()) {
                        // Store the current size of output
                        // nOutSize = pOutPos - pOutBuffer;
                        throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");
                    }

                    nBitBuffer += (in.get() & 0xFF) << nBits;
                    nBits += 8;
                }

                // Find most significant 6 bits of offset into the dictionary
                for (i = 0; i <= 0x3F; i++) {
                    if (TRUNCATE_VALUE(nBitBuffer, OffsBits[i] & 0xFF) == (OffsCode[i] & 0xFF))
                        break;
                }

                // Remove value from bit buffer
                nBitBuffer >>= OffsBits[i] & 0xFF;
                nBits -= OffsBits[i] & 0xFF;

                // If the copy length is 2, there are only two more bits in the dictionary
                // offset; otherwise, there are 4, 5, or 6 bits left, depending on what
                // the dictionary size is
                int pCopyOffs; // Offset to data to copy from the dictionary
                if (nCopyLen == 2) {

                    // Store the exact offset to a byte in the dictionary
                    pCopyOffs = (int) (pDictPos - 1 - ((i << 2) + (nBitBuffer & 0x03)));

                    // Remove the rest of the dictionary offset from the bit buffer
                    nBitBuffer >>= 2;
                    nBits -= 2;
                } else {

                    // Store the exact offset to a byte in the dictionary
                    pCopyOffs = (int) (pDictPos - 1 - ((i << nDictSizeByte) + TRUNCATE_VALUE(nBitBuffer, nDictSizeByte)));

                    // Remove the rest of the dictionary offset from the bit buffer
                    nBitBuffer >>= nDictSizeByte;
                    nBits -= nDictSizeByte;
                }

                // While there are still bytes left, copy bytes from the dictionary
                while (nCopyLen-- > 0) {

                    // If output buffer has become full, stop immediately!
                    if (!out.hasRemaining())
                        throw new IllegalArgumentException("PK_ERR_BUFFER_TOO_SMALL: Output buffer is full: " + out.position() + " / " + out.limit());


                    // Check whether the offset is a valid one into the dictionary
                    while (pCopyOffs < 0)
                        pCopyOffs += nCurDictSize;
                    while (pCopyOffs >= nCurDictSize)
                        pCopyOffs -= nCurDictSize;

                    // Copy the byte from the dictionary and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = *pCopyOffs++;
                    byte b = Dict[pCopyOffs++];
                    out.put(b);
                    Dict[pDictPos++] = b;

                    // If the dictionary is not full yet, increment the current dictionary size
                    if (nCurDictSize < nDictSize)
                        nCurDictSize++;

                    // If the current end of the dictionary is past the end of the buffer,
                    // wrap around back to the start
                    if (pDictPos >= nDictSize)
                        pDictPos = 0;
                }
            }

            // First bit is 0; literal byte
            else {

                // Fixed size literal byte
                if (nLitSize == PK_LITERAL_SIZE_FIXED) {

                    // Copy the byte and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = (byte)(nBitBuffer >> 1);
                    byte b = (byte) (nBitBuffer >> 1);
                    out.put(b);
                    Dict[pDictPos++] = b;

                    // Remove the byte from the bit buffer
                    nBitBuffer >>= 9;
                    nBits -= 9;
                }

                // Variable size literal byte
                else {

                    // Remove the first bit from the bit buffer
                    nBitBuffer >>= 1;
                    nBits--;

                    // Find the actual byte from the bit sequence
                    for (i = 0; i <= 0xFF; i++) {
                        if (TRUNCATE_VALUE(nBitBuffer, ChBits[i] & 0xFF) == (ChCode[i] & 0xFFFF))
                            break;
                    }

                    // Copy the byte and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = (byte)i;
                    byte b = (byte) i;
                    out.put(b);
                    Dict[pDictPos++] = b;

                    // Remove the byte from the bit buffer
                    nBitBuffer >>= ChBits[i] & 0xFF;
                    nBits -= ChBits[i] & 0xFF;
                }

                // If the dictionary is not full yet, increment the current dictionary size
                if (nCurDictSize < nDictSize)
                    nCurDictSize++;

                // If the current end of the dictionary is past the end of the buffer,
                // wrap around back to the start
                if (pDictPos >= nDictSize)
                    pDictPos = 0;
            }
        }

        return out.position();
    }

    public static int pkexplode(byte[] pInBuffer, byte[] pOutBuffer, int pOutBufferOffs) {
        // Compressed data cannot be less than 4 bytes;
        // this is not possible in any case whatsoever
        if (pInBuffer.length < 4)
            throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");

        int pOutPos = pOutBufferOffs;
        // This is 1 because in an mpq-sector, the first byte is the compression type flag
        int pInPos = 1;

        // Get header from compressed data
        byte nLitSize = pInBuffer[pInPos++];
        byte nDictSizeByte = pInBuffer[pInPos++];

        // Check for a valid compression type
        if (nLitSize != PK_LITERAL_SIZE_FIXED && nLitSize != PK_LITERAL_SIZE_VARIABLE)
            throw new IllegalArgumentException("PK_ERR_BAD_DATA: Invalid LitSize: " + nLitSize);

        // Only dictionary sizes of 1024, 2048, and 4096 are allowed.
        // The values 4, 5, and 6 correspond with those sizes
        if (4 > nDictSizeByte || nDictSizeByte > 6)
            throw new IllegalArgumentException("PK_ERR_BAD_DATA: Invalid DictSizeByte: " + nDictSizeByte);

        // Store actual dictionary size
        int nDictSize = 64 << nDictSizeByte;

        // Initialize dictionary position
        byte[] Dict = new byte[0x1000]; // Sliding dictionary used for compression and decompression
        int pDictPos = 0;

        // Initialize current dictionary size to zero
        int nCurDictSize = 0;

        // Get first 16 bits
        byte nBits; // Number of bits in bit buffer
        long nBitBuffer; // Stores bits until there are enough to output a byte of data

        nBitBuffer = pInBuffer[pInPos++] & 0xFFL;
        nBitBuffer += (pInBuffer[pInPos++] & 0xFFL) << 8;
        nBits = 16;

        // Decompress until output buffer is full
        int i; // Index into tables
        int nCopyLen;
        while (pOutPos < pOutBuffer.length) {

            // Fill bit buffer with at least 16 bits
            while (nBits < 16) {
                // If input buffer is empty before end of stream, buffer is incomplete
                if (pInPos >= pInBuffer.length) {
                    // Store the current size of output
                    // nOutSize = pOutPos - pOutBuffer;
                    throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");
                }

                nBitBuffer += (pInBuffer[pInPos++] & 0xFF) << nBits;
                nBits += 8;
            }

            // First bit is 1; copy from dictionary
            if ((nBitBuffer & 1) != 0) {

                // Remove first bit from bit buffer
                nBitBuffer >>= 1;
                nBits--;

                // Find the base value for the copy length
                for (i = 0; i <= 0x0F; i++) {
                    if (TRUNCATE_VALUE(nBitBuffer, LenBits[i] & 0xFF) == (LenCode[i] & 0xFF))
                        break;
                }

                // Remove value from bit buffer
                nBitBuffer >>= LenBits[i] & 0xFF;
                nBits -= LenBits[i] & 0xFF;

                // Store the copy length
                nCopyLen = (int) ((LenBase[i] & 0xFFFF) + TRUNCATE_VALUE(nBitBuffer, ExLenBits[i] & 0xFF)); // Length of data to copy from the dictionary

                // Remove the extra bits from the bit buffer
                nBitBuffer >>= ExLenBits[i] & 0xFF;
                nBits -= ExLenBits[i] & 0xFF;

                // If copy length is 519, the end of the stream has been reached
                if (nCopyLen == 519)
                    break;

                // Fill bit buffer with at least 14 bits
                while (nBits < 14) {
                    // If input buffer is empty before end of stream, buffer is incomplete
                    if (pInPos >= pInBuffer.length) {
                        // Store the current size of output
                        // nOutSize = pOutPos - pOutBuffer;
                        throw new IllegalArgumentException("PK_ERR_INCOMPLETE_INPUT: Incomplete input");
                    }

                    nBitBuffer += (pInBuffer[pInPos++] & 0xFF) << nBits;
                    nBits += 8;
                }

                // Find most significant 6 bits of offset into the dictionary
                for (i = 0; i <= 0x3F; i++) {
                    if (TRUNCATE_VALUE(nBitBuffer, OffsBits[i] & 0xFF) == (OffsCode[i] & 0xFF))
                        break;
                }

                // Remove value from bit buffer
                nBitBuffer >>= OffsBits[i] & 0xFF;
                nBits -= OffsBits[i] & 0xFF;

                // If the copy length is 2, there are only two more bits in the dictionary
                // offset; otherwise, there are 4, 5, or 6 bits left, depending on what
                // the dictionary size is
                int pCopyOffs; // Offset to data to copy from the dictionary
                if (nCopyLen == 2) {

                    // Store the exact offset to a byte in the dictionary
                    pCopyOffs = (int) (pDictPos - 1 - ((i << 2) + (nBitBuffer & 0x03)));

                    // Remove the rest of the dictionary offset from the bit buffer
                    nBitBuffer >>= 2;
                    nBits -= 2;
                } else {

                    // Store the exact offset to a byte in the dictionary
                    pCopyOffs = (int) (pDictPos - 1 - ((i << nDictSizeByte) + TRUNCATE_VALUE(nBitBuffer, nDictSizeByte)));

                    // Remove the rest of the dictionary offset from the bit buffer
                    nBitBuffer >>= nDictSizeByte;
                    nBits -= nDictSizeByte;
                }

                // While there are still bytes left, copy bytes from the dictionary
                while (nCopyLen-- > 0) {

                    // If output buffer has become full, stop immediately!
                    if (pOutPos >= pOutBuffer.length)
                        throw new IllegalArgumentException("PK_ERR_BUFFER_TOO_SMALL: Output buffer is full: " + pOutPos + " / " + pOutBuffer.length);


                    // Check whether the offset is a valid one into the dictionary
                    while (pCopyOffs < 0)
                        pCopyOffs += nCurDictSize;
                    while (pCopyOffs >= nCurDictSize)
                        pCopyOffs -= nCurDictSize;

                    // Copy the byte from the dictionary and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = *pCopyOffs++;
                    Dict[pDictPos++] = pOutBuffer[pOutPos++] = Dict[pCopyOffs++];

                    // If the dictionary is not full yet, increment the current dictionary size
                    if (nCurDictSize < nDictSize)
                        nCurDictSize++;

                    // If the current end of the dictionary is past the end of the buffer,
                    // wrap around back to the start
                    if (pDictPos >= nDictSize)
                        pDictPos = 0;
                }
            }

            // First bit is 0; literal byte
            else {

                // Fixed size literal byte
                if (nLitSize == PK_LITERAL_SIZE_FIXED) {

                    // Copy the byte and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = (byte)(nBitBuffer >> 1);
                    Dict[pDictPos++] = pOutBuffer[pOutPos++] = (byte) (nBitBuffer >> 1);

                    // Remove the byte from the bit buffer
                    nBitBuffer >>= 9;
                    nBits -= 9;
                }

                // Variable size literal byte
                else {

                    // Remove the first bit from the bit buffer
                    nBitBuffer >>= 1;
                    nBits--;

                    // Find the actual byte from the bit sequence
                    for (i = 0; i <= 0xFF; i++) {
                        if (TRUNCATE_VALUE(nBitBuffer, ChBits[i] & 0xFF) == (ChCode[i] & 0xFFFF))
                            break;
                    }

                    // Copy the byte and add it to the end of the dictionary
                    // *pDictPos++ = *pOutPos++ = (byte)i;
                    Dict[pDictPos++] = pOutBuffer[pOutPos++] = (byte) i;

                    // Remove the byte from the bit buffer
                    nBitBuffer >>= ChBits[i] & 0xFF;
                    nBits -= ChBits[i] & 0xFF;
                }

                // If the dictionary is not full yet, increment the current dictionary size
                if (nCurDictSize < nDictSize)
                    nCurDictSize++;

                // If the current end of the dictionary is past the end of the buffer,
                // wrap around back to the start
                if (pDictPos >= nDictSize)
                    pDictPos = 0;
            }
        }

        return pOutPos;
    }

}
