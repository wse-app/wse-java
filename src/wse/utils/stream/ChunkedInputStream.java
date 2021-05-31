package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class ChunkedInputStream extends WseInputStream {

	private long current_chunk_size = 0;
	private long current_counter = 0;

	private byte[] header_buff = new byte[2048];
	private int header_counter = 0;
	private int[] header_split;

	public ChunkedInputStream(InputStream readFrom) {
		super(readFrom);
	}
	
	@Override
	public int read() throws IOException {
		if (header_split == null) {
			if (!getHeader())
				return -1;
		}

		int left = (int) (current_chunk_size - current_counter);
		if (left == 0) {
			if (!getHeader())
				return -1;
			return read();
		}

		int content_left_in_header_buffer = (int) (header_counter - header_split[1] - current_counter);
		if (content_left_in_header_buffer == 0) {
			current_counter += 1;
			return readFrom.read();
		}
		current_counter += 1;
		return header_buff[(int) (header_split[1] + current_counter - 1)];
	}

	private boolean getHeader() throws IOException {
		if (!readHeader()) {
//			System.out.println("getHeader() got no header");
			return false;
		}
		if (!parseHeader()) {
//			System.out.println("getHeader() failed to parse header");
			return false;
		}
		if (current_chunk_size == 0) {
//			System.out.println("getHeader() got chunk size 0");
			return false;
		}
//		System.out.println("getHeader() returns true");
		return true;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
//		if (true)
//			return super.read(b, off, len);
		
//		System.out.println("==== READ ===");
		
//		System.out.println("Is null b: " + (header_split == null));
		if (header_split == null) {
			if (!getHeader()) {
//				System.out.println("==== READ END ===");
				return -1;				
			}
		}
//		System.out.println("Is null a: " + (header_split == null));
		

		int left_in_chunk = (int) (current_chunk_size - current_counter);
//		System.out.println("left_in_chunk: " + left_in_chunk);
		if (left_in_chunk <= 0) {
			if (!getHeader())
			{
//				System.out.println("==== READ END ===");
				return -1;				
			}
//			System.out.println("==== READ CONTINUE ===");
			return read(b, off, len);
		}

		int content_left_in_header_buffer = (int) (header_counter - header_split[1] - current_counter);
//		System.out.println("content_left_in_header_buffer: " + content_left_in_header_buffer + " (" + header_counter + " , " +  header_split[1] + " , " + current_counter + ")");
		if (content_left_in_header_buffer <= 0) {

			int res = readFrom.read(b, off, Math.min(left_in_chunk, len));
			if (res != -1) {
				current_counter += res;
			}
//			System.out.println("read " + res + "/" + len + " bytes from readFrom stream");
//			System.out.println("==== READ END ===");
			return res;
		}

		int read = Math.min(Math.min(content_left_in_header_buffer, left_in_chunk), len);
//		System.out.println("copied " + read + "/" + content_left_in_header_buffer + " from header buffer");
		System.arraycopy(header_buff, (int) (header_split[1] + current_counter), b, off, read);
		if (read != -1) {
			current_counter += read;
		}
		
//		System.out.println("==== READ END ===");
		return read;
	}
	
	private void shiftHeader(int shiftAmount, int length) {
//		System.out.println("Shifting2 " + shiftAmount);
		System.arraycopy(header_buff, shiftAmount, header_buff, 0, length);
	}

	private boolean readHeader() throws IOException {

//		System.out.println("reading header");
		if (header_split != null) {
//			System.out.println("looking for header in left-over data buffer");
			
			int curr_pos = (int) (header_split[1] + current_counter);
//			System.out.println(current_counter);
			
			int left_in_header = header_counter - curr_pos;
			
//			System.out.println("Left in header: " + left_in_header);
			if (left_in_header > 0) {
				
//				System.out.println("Shifting: " + curr_pos + ", " + left_in_header);
				shiftHeader(curr_pos, left_in_header);
				header_counter -= curr_pos;
				
				header_split = searchCRLF(header_buff, 0, header_counter - 1);
				
				if (header_split != null) {
					return true;
				}
				// Fill up header
			}else {
				header_counter = 0;
			}
		} else {
			header_counter = 0;
		}
		int a = 0;
		int write_offset = 0;
		header_split = null;
		int current_read = 0;
		while ((a = readFrom.read(header_buff, write_offset, header_buff.length - header_counter)) != -1) {
			current_read += a;
			header_split = searchCRLF(header_buff, 0, header_counter + current_read - 1);
			header_counter += a;
			if (header_split != null) {
				return true;
			}
			write_offset = current_read;
		}

		return false;
	}

	private boolean parseHeader() {
		if (header_split == null)
			return false;

		for (int i = 0; i < header_split[0]; i++) {
			if (header_buff[i] == ';') {
				this.current_chunk_size = Long.parseLong(new String(header_buff, 0, i - 1), 16);
				this.current_counter = 0;
				return true;
			}
		}

		String len = new String(header_buff, 0, header_split[0]);
		this.current_chunk_size = Long.parseLong(len.trim(), 16);
//		System.out.println("Got len: -->" + len + "<-- (" + this.current_chunk_size + ")");
		this.current_counter = 0;
		return true;
	}

	public static int[] searchCRLF(byte[] data, int start, int end) {
		if (start < 0)
			start = 0;
		if (end >= data.length)
			end = data.length - 1;
		int n = '\n', r = '\r';
		int[] res = { 0, 0 };
		
//		System.out.println("searching crlf 1: -->" + new String(data, start, (end + 1) - start).replace("\n", "\\n").replace("\r", "\\r") + "<--");
//		try {
//			throw new WseException("?");
//		}catch(WseException e) {
//			e.printStackTrace(System.out);
//		}
		
		
		try {
			for (int i = start; i <= end; i++) {
				if (i == 0)
					continue;
				
				if (i == 1 && data[i] == n && data[i-1] == r)
					continue;
				
				if (data[i] == n) {
					res[0] = i;
					res[1] = i + 1;
					return res;

				} else if (data[i] == r && i < end && data[i + 1] == n && i >= 1) {
					res[0] = i;
					res[1] = i + 2;
					return res;
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}

		return null;
	}

}
