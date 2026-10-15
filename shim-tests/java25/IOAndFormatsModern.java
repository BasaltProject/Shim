/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

public class IOAndFormatsModern {
    public static void main(String[] args) throws Throwable {
        testInputStreamModern();
        testOutputStreamModern();
        testReaderWriterModern();
        testHexFormat();
        testBase64();
        System.out.println("ALL IO AND FORMATS MODERN TESTS PASSED");
    }

    private static void testInputStreamModern() throws Throwable {
        byte[] data = "Hello Modern Input Streams!".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] all = in.readAllBytes();
        if (!Arrays.equals(data, all)) {
            throw new AssertionError("readAllBytes mismatch");
        }
        if (in.readAllBytes().length != 0) {
            throw new AssertionError("readAllBytes at EOF should be empty");
        }

        in = new ByteArrayInputStream(data);
        byte[] first5 = in.readNBytes(5);
        if (!Arrays.equals(Arrays.copyOfRange(data, 0, 5), first5)) {
            throw new AssertionError("readNBytes(5) mismatch: " + new String(first5, StandardCharsets.UTF_8));
        }

        byte[] target = new byte[10];
        int readCount = in.readNBytes(target, 2, 7);
        if (readCount != 7) {
            throw new AssertionError("expected 7 read bytes, got: " + readCount);
        }
        if (!Arrays.equals(Arrays.copyOfRange(data, 5, 12), Arrays.copyOfRange(target, 2, 9))) {
            throw new AssertionError("readNBytes buffer mismatch");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long transferred = in.transferTo(out);
        if (transferred != (data.length - 12)) {
            throw new AssertionError("transferTo byte count mismatch: " + transferred);
        }
        if (!Arrays.equals(Arrays.copyOfRange(data, 12, data.length), out.toByteArray())) {
            throw new AssertionError("transferTo content mismatch");
        }

        InputStream nullIn = InputStream.nullInputStream();
        if (nullIn.read() != -1) {
            throw new AssertionError("nullInputStream.read() must be -1");
        }
        if (nullIn.readAllBytes().length != 0) {
            throw new AssertionError("nullInputStream.readAllBytes() must be empty");
        }
        if (nullIn.transferTo(out) != 0) {
            throw new AssertionError("nullInputStream.transferTo must return 0");
        }
        nullIn.close();
        try {
            nullIn.read();
            throw new AssertionError("read after close on nullInputStream should throw");
        } catch (IOException expected) {
            // expected
        }

        System.out.println("InputStream modern methods verified");
    }

    private static void testOutputStreamModern() throws Throwable {
        OutputStream nullOut = OutputStream.nullOutputStream();
        nullOut.write(42);
        nullOut.write(new byte[] { 1, 2, 3 });
        nullOut.write(new byte[] { 1, 2, 3, 4, 5 }, 1, 3);
        nullOut.flush();
        nullOut.close();
        try {
            nullOut.write(10);
            throw new AssertionError("write after close on nullOutputStream should throw");
        } catch (IOException expected) {
            // expected
        }
        System.out.println("OutputStream modern methods verified");
    }

    private static void testReaderWriterModern() throws Throwable {
        String text = "Transferred Character Data: \u00a9 2026!";
        StringReader reader = new StringReader(text);
        StringWriter writer = new StringWriter();
        long transferred = reader.transferTo(writer);
        if (transferred != text.length()) {
            throw new AssertionError("transferTo length mismatch: " + transferred);
        }
        if (!text.equals(writer.toString())) {
            throw new AssertionError("transferred text mismatch: " + writer.toString());
        }

        Reader nullR = Reader.nullReader();
        if (nullR.read() != -1) {
            throw new AssertionError("nullReader.read() must be -1");
        }
        nullR.close();
        try {
            nullR.read();
            throw new AssertionError("read on closed nullReader should throw");
        } catch (IOException expected) {
            // expected
        }

        Writer nullW = Writer.nullWriter();
        nullW.write("discard");
        nullW.write('x');
        nullW.append("discarded seq");
        nullW.flush();
        nullW.close();
        try {
            nullW.write("fail");
            throw new AssertionError("write on closed nullWriter should throw");
        } catch (IOException expected) {
            // expected
        }
        System.out.println("Reader and Writer modern methods verified");
    }

    private static void testHexFormat() throws Throwable {
        HexFormat defaultHf = HexFormat.of();
        if (defaultHf.isUpperCase()) {
            throw new AssertionError("default HexFormat must be lowercase");
        }
        if (!"".equals(defaultHf.delimiter())) {
            throw new AssertionError("default delimiter must be empty");
        }

        byte[] raw = new byte[] { 0, 10, 15, 16, (byte) 255 };
        String formatted = defaultHf.formatHex(raw);
        if (!"000a0f10ff".equals(formatted)) {
            throw new AssertionError("expected 000a0f10ff, got: " + formatted);
        }

        byte[] parsed = defaultHf.parseHex(formatted);
        if (!Arrays.equals(raw, parsed)) {
            throw new AssertionError("parseHex mismatch: " + Arrays.toString(parsed));
        }

        HexFormat custom = HexFormat.ofDelimiter(":")
                .withPrefix("#")
                .withSuffix(";")
                .withUpperCase();
        String customFormatted = custom.formatHex(new byte[] { 10, 31 });
        if (!"#0A;:#1F;".equals(customFormatted)) {
            throw new AssertionError("custom HexFormat mismatch: " + customFormatted);
        }
        byte[] customParsed = custom.parseHex(customFormatted);
        if (!Arrays.equals(new byte[] { 10, 31 }, customParsed)) {
            throw new AssertionError("custom parseHex mismatch: " + Arrays.toString(customParsed));
        }

        if (HexFormat.fromHexDigit('a') != 10 || HexFormat.fromHexDigit('F') != 15 || HexFormat.fromHexDigit('0') != 0) {
            throw new AssertionError("fromHexDigit mismatch");
        }
        try {
            HexFormat.fromHexDigit('z');
            throw new AssertionError("fromHexDigit('z') should throw");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        if (HexFormat.fromHexDigits("12ab") != 0x12ab) {
            throw new AssertionError("fromHexDigits mismatch: " + HexFormat.fromHexDigits("12ab"));
        }
        if (HexFormat.fromHexDigitsToLong("123456789abcdef0") != 0x123456789abcdef0L) {
            throw new AssertionError("fromHexDigitsToLong mismatch");
        }

        if (!"0a".equals(defaultHf.toHexDigits((byte) 10))) {
            throw new AssertionError("toHexDigits(byte) mismatch: " + defaultHf.toHexDigits((byte) 10));
        }
        if (!"000000ff".equals(defaultHf.toHexDigits(255))) {
            throw new AssertionError("toHexDigits(int) mismatch: " + defaultHf.toHexDigits(255));
        }
        if (!"00000000000000ff".equals(defaultHf.toHexDigits(255L))) {
            throw new AssertionError("toHexDigits(long) mismatch: " + defaultHf.toHexDigits(255L));
        }
        if (!"uppercase: false, delimiter: \"\", prefix: \"\", suffix: \"\"".equals(defaultHf.toString())) {
            throw new AssertionError("HexFormat.toString() mismatch: " + defaultHf.toString());
        }

        System.out.println("HexFormat verified");
    }

    private static void testBase64() throws Throwable {
        String plain = "Basalt Shim Modern JVM 2026!";
        byte[] plainBytes = plain.getBytes(StandardCharsets.UTF_8);

        Base64.Encoder encoder = Base64.getEncoder();
        Base64.Decoder decoder = Base64.getDecoder();

        String encoded = encoder.encodeToString(plainBytes);
        byte[] decoded = decoder.decode(encoded);
        if (!plain.equals(new String(decoded, StandardCharsets.UTF_8))) {
            throw new AssertionError("Base64 standard roundtrip mismatch: " + new String(decoded, StandardCharsets.UTF_8));
        }

        Base64.Encoder noPadEnc = encoder.withoutPadding();
        String noPad = noPadEnc.encodeToString(new byte[] { 1, 2 });
        if (noPad.contains("=")) {
            throw new AssertionError("withoutPadding should not produce '=': " + noPad);
        }
        byte[] noPadDec = decoder.decode(noPad);
        if (!Arrays.equals(new byte[] { 1, 2 }, noPadDec)) {
            throw new AssertionError("decode without padding mismatch: " + Arrays.toString(noPadDec));
        }

        byte[] urlRaw = new byte[] { -1, -2, -3, 62, 63 };
        String urlEnc = Base64.getUrlEncoder().encodeToString(urlRaw);
        if (urlEnc.contains("+") || urlEnc.contains("/")) {
            throw new AssertionError("URL encoder should not produce + or /: " + urlEnc);
        }
        byte[] urlDec = Base64.getUrlDecoder().decode(urlEnc);
        if (!Arrays.equals(urlRaw, urlDec)) {
            throw new AssertionError("URL Base64 roundtrip mismatch");
        }

        ByteBuffer bbIn = ByteBuffer.wrap(plainBytes);
        ByteBuffer bbOut = encoder.encode(bbIn);
        ByteBuffer bbDec = decoder.decode(bbOut);
        byte[] bbDecBytes = new byte[bbDec.remaining()];
        bbDec.get(bbDecBytes);
        if (!Arrays.equals(plainBytes, bbDecBytes)) {
            throw new AssertionError("ByteBuffer Base64 roundtrip mismatch");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream wrappedOut = encoder.wrap(baos);
        wrappedOut.write(plainBytes);
        wrappedOut.close();
        if (!encoded.equals(baos.toString("UTF-8"))) {
            throw new AssertionError("Base64 wrap(OutputStream) mismatch: " + baos.toString("UTF-8"));
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream wrappedIn = decoder.wrap(bais);
        ByteArrayOutputStream streamDecOut = new ByteArrayOutputStream();
        wrappedIn.transferTo(streamDecOut);
        if (!plain.equals(streamDecOut.toString("UTF-8"))) {
            throw new AssertionError("Base64 wrap(InputStream) mismatch: " + streamDecOut.toString("UTF-8"));
        }

        System.out.println("Base64 verified");
    }
}
