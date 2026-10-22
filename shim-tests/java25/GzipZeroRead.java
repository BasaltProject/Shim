/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipZeroRead {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bos)) {
            out.write(new byte[] {1, 2, 3, 4});
        }
        Thread idle = new Thread(() -> { });
        idle.interrupt();
        idle.start();
        idle.join();
        idle.interrupt();
        System.out.println(idle.isInterrupted());
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray())))) {
            System.out.println(in.readNBytes(0).length + " " + in.readByte() + " " + in.read(new byte[2], 0, 0));
            System.out.println(in.readNBytes(3).length + " " + in.read());
        }
    }
}
