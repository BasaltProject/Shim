/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class FileChannelsAndMappedBuffersModern {

  public static void main(String[] args) throws Exception {
    testExceptionsAndHierarchy();
    testFileChannelBasics();
    testPositionalAndScattering();
    testTransfersAndTruncate();
    testFileLocking();
    testMappedByteBuffer();
    testStreamAndRafChannels();
    testSelectionKeyAndSelector();
    System.out.println("ALL FILE CHANNELS AND MAPPED BUFFERS TESTS COMPLETED");
  }

  private static void testExceptionsAndHierarchy() {
    ClosedChannelException cce = new ClosedChannelException();
    if (!(cce instanceof IOException)) throw new AssertionError();

    AsynchronousCloseException ace = new AsynchronousCloseException();
    if (!(ace instanceof ClosedChannelException)) throw new AssertionError();

    ClosedByInterruptException cbie = new ClosedByInterruptException();
    if (!(cbie instanceof AsynchronousCloseException)) throw new AssertionError();

    NonReadableChannelException nrce = new NonReadableChannelException();
    if (!(nrce instanceof IllegalStateException)) throw new AssertionError();

    NonWritableChannelException nwce = new NonWritableChannelException();
    if (!(nwce instanceof IllegalStateException)) throw new AssertionError();

    OverlappingFileLockException ofle = new OverlappingFileLockException();
    if (!(ofle instanceof IllegalStateException)) throw new AssertionError();

    FileLockInterruptionException flie = new FileLockInterruptionException();
    if (!(flie instanceof IOException)) throw new AssertionError();

    System.out.println("Hierarchy: OK");
  }

  private static void testFileChannelBasics() throws Exception {
    Path tempFile = Files.createTempFile("shim-test-fc", ".dat");
    try {
      try (FileChannel fc = FileChannel.open(tempFile,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        if (!fc.isOpen()) throw new AssertionError();
        if (fc.size() != 0) throw new AssertionError("Expected size 0, got " + fc.size());
        if (fc.position() != 0) throw new AssertionError();

        byte[] data = "Hello Java 25 FileChannel!".getBytes("UTF-8");
        ByteBuffer src = ByteBuffer.wrap(data);
        int written = fc.write(src);
        if (written != data.length) throw new AssertionError();
        if (fc.position() != data.length) throw new AssertionError();
        if (fc.size() != data.length) throw new AssertionError();
        fc.force(true);
      }

      // Read back via SeekableByteChannel from Files.newByteChannel
      try (SeekableByteChannel sbc = Files.newByteChannel(tempFile, StandardOpenOption.READ)) {
        if (sbc.size() != 26) throw new AssertionError("Expected 26, got " + sbc.size());
        ByteBuffer dst = ByteBuffer.allocate(32);
        int read = sbc.read(dst);
        if (read != 26) throw new AssertionError("Expected read 26, got " + read);
        dst.flip();
        byte[] readBytes = new byte[read];
        dst.get(readBytes);
        String s = new String(readBytes, "UTF-8");
        if (!s.equals("Hello Java 25 FileChannel!")) throw new AssertionError("Got: " + s);
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }

    System.out.println("Basics: OK");
  }

  private static void testPositionalAndScattering() throws Exception {
    Path tempFile = Files.createTempFile("shim-test-pos", ".dat");
    try {
      try (FileChannel fc = FileChannel.open(tempFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
        byte[] init = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        fc.write(ByteBuffer.wrap(init));

        // Positional write without altering channel position
        fc.position(2);
        ByteBuffer patch = ByteBuffer.wrap(new byte[] { 99, 98 });
        int pw = fc.write(patch, 6); // write at position 6
        if (pw != 2) throw new AssertionError();
        if (fc.position() != 2) throw new AssertionError("Channel position should remain 2, was " + fc.position());

        // Positional read without altering channel position
        ByteBuffer readBuf = ByteBuffer.allocate(2);
        int pr = fc.read(readBuf, 6);
        if (pr != 2) throw new AssertionError();
        if (fc.position() != 2) throw new AssertionError("Channel position should remain 2");
        readBuf.flip();
        if (readBuf.get() != 99 || readBuf.get() != 98) throw new AssertionError();

        // Gathering write
        fc.position(fc.size());
        ByteBuffer b1 = ByteBuffer.wrap(new byte[] { 10, 11 });
        ByteBuffer b2 = ByteBuffer.wrap(new byte[] { 12, 13 });
        long gw = fc.write(new ByteBuffer[] { b1, b2 });
        if (gw != 4) throw new AssertionError();

        // Scattering read
        fc.position(0);
        ByteBuffer s1 = ByteBuffer.allocate(5);
        ByteBuffer s2 = ByteBuffer.allocate(5);
        long sr = fc.read(new ByteBuffer[] { s1, s2 });
        if (sr != 10) throw new AssertionError("Expected 10, got " + sr);
        s1.flip();
        if (s1.get(0) != 0 || s1.get(4) != 4) throw new AssertionError();
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }

    System.out.println("Positional and Scattering: OK");
  }

  private static void testTransfersAndTruncate() throws Exception {
    Path file1 = Files.createTempFile("shim-src", ".dat");
    Path file2 = Files.createTempFile("shim-dst", ".dat");
    try {
      try (FileChannel fc1 = FileChannel.open(file1, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
        byte[] payload = "Source content for transfer verification".getBytes("UTF-8");
        fc1.write(ByteBuffer.wrap(payload));
      }

      try (FileChannel src = FileChannel.open(file1, StandardOpenOption.READ);
           FileChannel dst = FileChannel.open(file2, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
        long transferred = src.transferTo(0, src.size(), dst);
        if (transferred != src.size()) throw new AssertionError("Transferred " + transferred);
      }

      try (FileChannel check = FileChannel.open(file2, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        if (check.size() != 40) throw new AssertionError("Size: " + check.size());

        // Truncate
        check.truncate(14);
        if (check.size() != 14) throw new AssertionError("Truncated size: " + check.size());

        ByteBuffer b = ByteBuffer.allocate(14);
        check.position(0);
        check.read(b);
        b.flip();
        String s = new String(b.array(), 0, 14, "UTF-8");
        if (!s.equals("Source content")) throw new AssertionError("Got: " + s);
      }
    } finally {
      Files.deleteIfExists(file1);
      Files.deleteIfExists(file2);
    }

    System.out.println("Transfers and Truncate: OK");
  }

  private static void testFileLocking() throws Exception {
    Path tempFile = Files.createTempFile("shim-lock", ".dat");
    try {
      try (FileChannel fc = FileChannel.open(tempFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
        fc.write(ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 }));

        try (FileLock lock = fc.tryLock()) {
          if (lock == null) throw new AssertionError("Lock was null");
          if (!lock.isValid()) throw new AssertionError();
          if (lock.channel() != fc) throw new AssertionError();
          if (lock.acquiredBy() != fc) throw new AssertionError();
          if (lock.isShared()) throw new AssertionError();
          if (lock.position() != 0) throw new AssertionError();
          if (!lock.overlaps(0, 10)) throw new AssertionError();
          if (lock.toString() == null || lock.toString().isEmpty()) throw new AssertionError();
        }

        // Test custom region lock
        FileLock regionLock = fc.lock(100, 50, false);
        if (regionLock.position() != 100 || regionLock.size() != 50) throw new AssertionError();
        if (!regionLock.overlaps(120, 10)) throw new AssertionError();
        if (regionLock.overlaps(0, 50)) throw new AssertionError();
        regionLock.release();
        if (regionLock.isValid()) throw new AssertionError();
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }

    System.out.println("Locking: OK");
  }

  private static void testMappedByteBuffer() throws Exception {
    Path tempFile = Files.createTempFile("shim-map", ".dat");
    try {
      try (FileChannel fc = FileChannel.open(tempFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
        byte[] orig = new byte[] { 10, 20, 30, 40, 50 };
        fc.write(ByteBuffer.wrap(orig));

        MappedByteBuffer mapped = fc.map(FileChannel.MapMode.READ_ONLY, 0, 5);
        if (mapped == null) throw new AssertionError();
        if (!mapped.isLoaded()) throw new AssertionError();
        mapped.load();
        mapped.force();
        mapped.force(0, 5);

        if (mapped.capacity() != 5) throw new AssertionError("Mapped cap: " + mapped.capacity());
        if (mapped.get(0) != 10 || mapped.get(4) != 50) throw new AssertionError();
        if (!mapped.isDirect()) throw new AssertionError();

        // Check covariant returns
        MappedByteBuffer p = mapped.position(1);
        if (p != mapped || mapped.position() != 1) throw new AssertionError();
        mapped.rewind();
        if (mapped.position() != 0) throw new AssertionError();
      }
    } finally {
      Files.deleteIfExists(tempFile);
    }

    System.out.println("MappedByteBuffer: OK");
  }

  private static void testStreamAndRafChannels() throws Exception {
    File tempFile = File.createTempFile("shim-stream-fc", ".tmp");
    try {
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        if (!fos.getFD().valid()) throw new AssertionError();
        try (FileChannel fc = fos.getChannel()) {
          fc.write(ByteBuffer.wrap(new byte[] { 65, 66, 67 }));
        }
      }

      try (FileInputStream fis = new FileInputStream(tempFile)) {
        if (!fis.getFD().valid()) throw new AssertionError();
        try (FileChannel fc = fis.getChannel()) {
          ByteBuffer buf = ByteBuffer.allocate(3);
          int n = fc.read(buf);
          if (n != 3) throw new AssertionError();
          buf.flip();
          if (buf.get(0) != 65 || buf.get(2) != 67) throw new AssertionError();
        }
      }

      try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
        if (!raf.getFD().valid()) throw new AssertionError();
        raf.setLength(5);
        if (raf.length() != 5) throw new AssertionError();
        FileChannel fc = raf.getChannel();
        if (fc.size() != 5) throw new AssertionError();
        fc.position(3);
        fc.write(ByteBuffer.wrap(new byte[] { 68, 69 }));
        if (raf.length() != 5) throw new AssertionError();
        raf.seek(3);
        if (raf.readByte() != 68 || raf.readByte() != 69) throw new AssertionError();
        fc.close();
      }
    } finally {
      tempFile.delete();
    }

    System.out.println("Stream and RAF Channels: OK");
  }

  private static void testSelectionKeyAndSelector() throws Exception {
    try (Selector selector = Selector.open()) {
      if (!selector.isOpen()) throw new AssertionError();
      if (!(selector instanceof Closeable)) throw new AssertionError();

      // Functional select invocation
      selector.selectNow(key -> {
        throw new AssertionError("Should not have ready keys");
      });
    }

    System.out.println("Selector: OK");
  }
}
