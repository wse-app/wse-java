package wse.utils.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.HttpResult;
import wse.utils.exception.WseHttpException;
import wse.utils.log.Loggers;
import wse.utils.stream.CombinedInputStream;

public class HttpBuilder {

	private static final Logger log = WSE.getLogger();
	private static final boolean ENABLE_PARTIAL_READ = true;
	
	public static class ParseResult {
		public final String[] header;
		public final InputStream content;
		public ParseResult(String[] header, InputStream content) {
			super();
			this.header = header;
			this.content = content;
		}
		
	}
	
	public HttpBuilder() {
		
	}
	
	public static ParseResult parseInput(final InputStream input) throws IOException {
		
		log.finest("Parsing http from InputStream Image:\n" + input);
		try {
			byte[] buff = new byte[8192];
			int read_actual;
			int pointer = 0;

			int[] split = null;
			
			int last_search = 0;
			
			
			// Read header
			if (!ENABLE_PARTIAL_READ) {
				
				try {		
					
					while((read_actual = input.read(buff, pointer, buff.length - pointer)) != -1) {
						pointer += read_actual;
					}
					
				}catch(SocketTimeoutException e) {
					e.printStackTrace();
				}
				
				split = searchHeaderEnd(buff, 0, pointer);
				
			}else {
				while (true) {
					read_actual = input.read(buff, pointer, buff.length - pointer);
					if (read_actual != -1) {
						pointer += read_actual;
						if (pointer >= buff.length) {
							break;
						}else {
							
							split = searchHeaderEnd2(buff, Math.max(0, last_search - 3), pointer - 1);
							if (split != null)
								break;
							last_search = pointer;
							
						}
					}else {
						break;
					}
				}
			}
			

			if (split == null && pointer > 4)
				split = searchHeaderEnd2(buff, 0, pointer - 1);
			
			if (split == null || split[0] == -1) {
				
				log.severe("Could not find header end, " + pointer + " bytes read");
				if (pointer > 0) {
					Loggers.hexdump(log, Level.FINER, buff, 0, pointer);
				}
				
				// Send 413 Entity Too Large 
				throw new WseHttpException("Could not find header end", 413);
			}
			int content_start = split[1];
			int content_length = pointer - content_start;
			
			@SuppressWarnings("resource")
			InputStream cis = new CombinedInputStream(new ByteArrayInputStream(buff, content_start, content_length), input).closeWhenDone(false);
			return new ParseResult(splitNL(buff, 0, split[0]), cis);
		} catch (IOException e) {
			throw (e);
		}
	}
	
	public static HttpResult read(final InputStream input, boolean modifyContent) throws IOException {
		ParseResult pr = parseInput(input);
		if (pr == null)
			return null;
		return new HttpResult(HttpHeader.read(pr.header), pr.content, modifyContent);
	}

	public static int[] searchHeaderEnd(byte[] data, int start, int end) {

		int n = '\n', r = '\r';
		int[] res = { 0, 0 };

		try {
			for (int i = start; i <= end; i += 2) {

				if (data[i] == n) {
					if (data[i - 1] == n) {
						res[0] = i - 1;
						res[1] = i + 1; // +2
						return res;
					}

					if (i < end && data[i + 1] == n) {
						res[0] = i;
						res[1] = i + 2; // +2
						return res;
					}

					if (i < end - 1 && data[i + 1] == r && data[i - 1] == r && data[i + 2] == n) {
						res[0] = i - 1;
						res[1] = i + 3; // +4
						return res;
					}

				} else if (data[i] == r && i < end - 2) {
					if (data[i + 2] == r && data[i + 1] == n && data[i + 3] == n) {
						res[0] = i;
						res[1] = i + 4; // +4
						return res;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static int[] searchHeaderEnd2(byte[] data, int start, int end) {

		int n = '\n', r = '\r';
		int[] res = { 0, 0 };

		
		int length = end - start + 1;
		try {
			for (int i = start, j = 0, k = length - 1; i <= end; i += 4, j += 4, k -= 4) {
//				System.out.println(i + ", " + j + ", " + k + ", \"" + ((char)data[i]+"").replace("\r", "\\r").replace("\n", "\\n") + "\"");

				if (data[i] == n && j != 0) {
					if (data[i-1] == r) {
						if (k >= 2 && data[i + 2] == n && data[i+1] == r) {
							// Landed on \r\n\r\n index 1
							res[0] = i - 1;
							res[1] = i + 3; // +4
							return res;
						}else if (data[i-2] == n && data[i-3] == r) {
							// Landed on \r\n\r\n index 3
							res[0] = i - 3;
							res[1] = i + 1;
							return res;
						}
					}
				}else if (data[i] == r) {
					if (k >= 3 && data[i+2] == r && data[i+1] == n && data[i+3] == n) {
						// Landed on \r\n\r\n index 0
						res[0] = i - 0;
						res[1] = i + 4;
						return res;
					}else if (k >= 1 && j >= 2 && data[i-2] == r && data[i+1] == n && data[i-1] == n) {
						// Landed on \r\n\r\n index 2
						res[0] = i - 2;
						res[1] = i + 2;
						return res;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String[] splitNL(byte[] data, int start, int length) {
		
		String text = new String(data, start, length);
		return text.replace("\r", "").split("\n");
	}
}
