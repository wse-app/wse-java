package wse.server.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import wse.utils.HttpCodes;
import wse.utils.MimeType;
import wse.utils.exception.WseException;
import wse.utils.http.Credentials;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.http.StreamUtils;
import wse.utils.http.TransferEncoding;

public class PublicFolderServlet extends AuthenticationServlet {

	private final List<File> public_folder = new LinkedList<>();

	private boolean allow_read = true;
	private boolean allow_write = true;

	private int max_depth = 10;
	
	private File defaultFile;

	public PublicFolderServlet() {
		File f = getRootFolder();
		validateFile(f);
		public_folder.add(f);
	}

	/**
	 * Must be overridden, or use constructor
	 * {@link PublicFolderServlet#PublicFolderServlet(String)}
	 * 
	 * @return
	 */
	protected File getRootFolder() {
		return null;
	}

	public PublicFolderServlet(String public_path) {
		this(new File(public_path));
	}

	public PublicFolderServlet(File... roots) {
		for (File f : roots) {
			validateFile(f);
			public_folder.add(f);
		}
	}

	private final void validateFile(File f) {
		if (f == null)
			throw new WseException("Invalid public file: null");
		if (!f.exists())
			throw new WseException("Invalid path: " + f.getPath());
		if (!f.isDirectory())
			throw new WseException("Public folder is not a directory: " + f.getPath());

		if (!f.canWrite())
			log.warning("PublicFolderServlet can not write to public folder, PUT and DELETE will be disabled");
		if (!f.canRead())
			log.warning("PublicFolderServlet can not write to public folder, GET will be disabled");
	}

	public boolean getAllowRead() {
		return allow_read;
	}

	public void setAllowRead(boolean allow_read) {
		this.allow_read = allow_read;
	}

	public boolean getAllowWrite() {
		return allow_write;
	}

	public void setAllowWrite(boolean allow_write) {
		this.allow_write = allow_write;
	}

	public int getMaxDepth() {
		return max_depth;
	}

	public PublicFolderServlet setDefaultPath(String defaultFilePath) {
		for (File file : public_folder) {
			File f = new File(file, defaultFilePath);
			
			if (!f.exists() || !f.canRead() || f.isHidden() || !f.isFile() ) {
				continue;
			}else {
				this.defaultFile = f;
				return this;
			}
		}
		log.warning("PublicFolderServlet could not find specified default path " + defaultFilePath);
		return this;
	}
	
	public PublicFolderServlet setDefaultPath(File defaultFilePath) {
		log.warning("PublicFolderServlet could not find specified default path " + defaultFilePath);
		this.defaultFile = defaultFilePath;
		return this;
	}
	
	public void setMaxDepth(int max_depth) {
		this.max_depth = max_depth;
	}

	@Override
	public void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (!allow_read) {
			response.sendError(HttpCodes.FORBIDDEN);
			return;
		}

		File f;
		if ((f = getValidDestination(request, response)) == null)
			return;
		
		if (!f.exists() || !f.canRead() || f.isHidden() || !f.isFile()) {
			response.sendError(HttpCodes.NOT_FOUND, "File not found: (" + f.getAbsolutePath() + "): " + f.exists() + " / " + f.canRead() + " / " + !f.isHidden() + " / " + f.isFile() );
			return;
		}

		String path = f.getAbsolutePath();
		String extension = path.substring(path.lastIndexOf('.') + 1);
		MimeType mt = MimeType.getByExtension(extension);

		response.setContentLength((int) f.length());
		
		if (mt != null) {
			response.setContentType(mt);			
		}else {
			response.setContentType(this.getMimeType(request.getRequestPath()));
		}

		response.writeHeader();
		if (request.getMethod() == HttpMethod.HEAD)
			return;

		byte[] buff = new byte[(int) Math.min(f.length(), 500000)];
		int a;
		try {
			try (InputStream in = new FileInputStream(f)) {
				while ((a = in.read(buff)) != -1) {
					response.write(buff, 0, a);
				}
			}
		} catch (Exception e) {
			throw new WseException(e.getMessage(), e);
		}
	}
	
	public String getMimeType(String requestedPath) {
		return null;
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

		{
			// RFC 7231 page 28, Content-Range is ment for PATCH
			if (request.getAttribute("Content-Range") != null) {
				response.sendError(HttpCodes.BAD_REQUEST);
				return;
			}
		}

		if (!allow_write) {
			response.sendError(HttpCodes.FORBIDDEN);
			return;
		}
		File f;
		if ((f = getValidDestination(request, response)) == null)
			return;

		if (!f.getParentFile().equals(public_folder)) {
			if (!f.getParentFile().mkdirs()) {
				response.sendError(HttpCodes.FORBIDDEN, "Destination could not be created");
				return;
			}
		}
		try (FileOutputStream writeTo = new FileOutputStream(f, false)) {
			StreamUtils.write(request.getContent(), writeTo, 20000);
		} catch (Exception e) {
			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
			return;
		}

		response.setContentLength(0);
		response.setStatusCode(HttpCodes.CREATED);
		response.writeHeader();
	}

	@Override
	public void getOptions(HttpHeader header) {
		super.getOptions(header);
		
		header.setAccept(MimeType.any);
		header.setAcceptEncoding(TransferEncoding.IDENTITY);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!allow_write) {
			response.sendError(HttpCodes.FORBIDDEN);
			return;
		}

		File f;
		if ((f = getValidDestination(request, response)) == null)
			return;

		if (f.exists()) {
			if (!f.delete()) {
				response.sendError(HttpCodes.INTERNAL_SERVER_ERROR);
				return;
			}
		}

		response.setContentLength(0);
		response.setStatusCode(HttpCodes.OK);
		response.writeHeader();
	}

	public File getValidDestination(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestPath();

		if (path.contains("..")) {
			response.sendError(HttpCodes.NOT_FOUND);
			return null;
		}

		
		File f = null;
		for (File root : public_folder) {
			File t = new File(root.getPath() + path);
			
			if (!t.exists() || !t.canRead() || t.isHidden() || !t.isFile() ) 
				continue;
			
			if (t.equals(root)) {
				if (this.defaultFile != null) 
					return this.defaultFile;
				
			}
			
			if (!ensureIsChildOf(root, t, Math.max(this.max_depth, 1))) {
				
				if (this.defaultFile != null) {
					return this.defaultFile;
				}
				response.sendError(HttpCodes.NOT_FOUND);
				return null;
			}
			
			f = t;
		}
		
		if (f == null) {
			if (this.defaultFile != null) {
				return this.defaultFile;
			}
			response.sendError(HttpCodes.NOT_FOUND);
			return null;
		}
		
		return f;
	}

	public boolean ensureIsChildOf(File parent, File child, int maxLoops) {

		for (int i = 0; i < maxLoops; i++) {
			child = child.getParentFile();
			if (child == null)
				return false;
			if (child.equals(parent))
				return true;
		}
		return false;
	}

	@Override
	public boolean isAuthorized(HttpHeader request, Credentials credentials) {
		return true;
	}
}
