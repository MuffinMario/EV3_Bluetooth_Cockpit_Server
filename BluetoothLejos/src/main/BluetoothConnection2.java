package main;

import java.io.IOException;

import com.sun.jna.LastErrorException;

import lejos.internal.io.NativeSocket;
import lejos.remote.nxt.*;

public class BluetoothConnection2 extends NXTConnection {
	NativeSocket socket;
	int mode;
	byte[] header = new byte[2];
	byte[] buffer = new byte[256];
	boolean eof = false;

	BluetoothConnection2(NativeSocket socket, int mode) {
		this.socket = socket;
		this.mode = mode;
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	// TODO : Need robust versions of read and write
	@Override
	public int read(byte[] buf, int length) {
		if (eof)
			return -1;
		try {
			System.out.println("Reading ...");
			if (mode != NXTConnection.RAW) {
				int len = socket.read(buffer, length);
				if (len > 2) {
					for (int i = 0; i < len - 2; i++)
						buf[i] = buffer[i + 2];
					return len - 2;
				} else
					return len;
			} else {
				int len = socket.read(buf, length);
				return len;
			}
		} catch (LastErrorException e) {
			int errno = e.getErrorCode();
			System.out.println("Error code is " + errno);

			if (errno == 104) {
				eof = true;
				return -1;
			} else
				return -2;
		}
	}

	@Override
	public int write(byte[] buffer, int numBytes) {

		if (mode != NXTConnection.RAW) {
			header[0] = (byte) numBytes;
			header[1] = (byte) (numBytes >> 8);
			socket.write(header, 0, 2);
		}
		socket.write(buffer, 0, numBytes);
		return 0;
	}

	@Override
	public int read(byte[] buf, int length, boolean b) {
		if (eof)
			return -1;
		try {
			if (mode != NXTConnection.RAW) {
				int len = socket.read(buffer, length);
				System.out.println("Read " + len + " bytes");
				if (len > 2) {
					for (int i = 0; i < len - 2; i++)
						buf[i] = buffer[i + 2];
					return len - 2;
				} else
					return len;
			} else
				return socket.read(buf, length);
		} catch (LastErrorException e) {
			int errno = e.getErrorCode();

			if (errno == 104) {
				eof = true;
				return -1;
			} else
				return -2;
		}
	}

}
