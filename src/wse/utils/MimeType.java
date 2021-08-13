package wse.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MimeType {

	private static final Map<String, MimeType> name_mime = new HashMap<String, MimeType>();
	private static final Map<String, MimeType> ext_mime = new HashMap<String, MimeType>();

	private static final String APPLICATION = "application", AUDIO = "audio", IMAGE = "image", MESSAGE = "message",
			TEXT = "text", VIDEO = "video", MODEL = "model", FONT = "font", MULTIPART = "multipart";

	private static final String APPLICATION_S = APPLICATION + "/", AUDIO_S = AUDIO + "/", IMAGE_S = IMAGE + "/",
			MESSAGE_S = MESSAGE + "/", TEXT_S = TEXT + "/", VIDEO_S = VIDEO + "/", MODEL_S = MODEL + "/",
			FONT_S = FONT + "/", MULTIPART_S = MULTIPART + "/";

	
	private final String name;
	private final String[] sub_names;
	private final String[] extensions;

	private final String full_name;

	private MimeType(String name, String[] sub_names, String[] extensions) {
		this.name = name;
		this.sub_names = sub_names == null ? new String[0] : sub_names;
		this.extensions = extensions == null ? new String[0] : extensions;
		this.full_name = name + (this.sub_names.length > 0 ? "/" + this.sub_names[0] : "");

		if (this.sub_names.length == 0) {
			name_mime.put(this.full_name, this);
		} else {
			for (String s : this.sub_names) {
				String n = name + "/" + s;
				name_mime.put(n, this);
			}
		}
		for (String s : extensions) {
			ext_mime.put(s, this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(full_name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof MimeType)) {
			return false;
		}
		MimeType other = (MimeType) obj;
		return Objects.equals(full_name, other.full_name);
	}

	public static class Builder {
		private String[] sub_names;

		public Builder(String[] sub_names) {
			this.sub_names = sub_names;
		}

		public image image(String... extensions) {

			return new image(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public video video(String... extensions) {
			return new video(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public text text(String... extensions) {
			return new text(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public audio audio(String... extensions) {
			return new audio(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public application application(String... extensions) {
			return new application(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public model model(String... extensions) {
			return new model(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public message message(String... extensions) {
			return new message(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}

		public multipart multipart(String... extensions) {
			return new multipart(sub_names, extensions.length == 0 ? new String[] { sub_names[0] } : extensions);
		}
	}

	public static Builder b(String... names) {
		return new Builder(names);
	}

	static {
		application.init();
		audio.init();
		image.init();
		message.init();
		text.init();
		video.init();
		model.init();
		multipart.init();
	}

	public static final class application extends MimeType {

		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public application(String[] sub_names, String[] extensions) {
			super(APPLICATION, sub_names, extensions);
			values.add(this);
		}

		public boolean isApplication() {
			return true;
		}

		public static void init() {
		}

		public static final application x_www_form_urlencoded = b("x-www-form-urlencoded").application("*");
		/** HTML executable */
		public static final application hta = b("hta").application("hta");
		/** JSON */
		public static final application json = b("json").application("json");
		/** Android Package Archive */
		public static final application apk = b("vnd.android.package-archive").application("apk");
		/** iOS Package Archive */
		public static final application ipa = b("x-ios-app").application("ipa");
		/** fractal image file */
		public static final application fractals = b("fractals").application("fif");
		/** Photoshop custom shapes file */
		public static final application x_csh = b("x-csh").application("csh");
		/** Microsoft Publisher file */
		public static final application x_mspublisher = b("x-mspublisher").application("pub");
		/** unformatted manual page */
		public static final application x_troff = b("x-troff").application("roff", "t", "tr");
		/** Flash file */
		public static final application x_shockwave_flash = b("x-shockwave-flash").application("swf");
		/** ActiveX script */
		public static final application olescript = b("olescript").application("axs");
		/** uniform standard tape archive format file */
		public static final application x_ustar = b("x-ustar").application("ustar");
		/** Turbo Tax tax schedule list */
		public static final application x_msschedule = b("x-msschedule").application("scd");
		/** Excel Add-in file */
		public static final application vnd_ms_excel = b("vnd.ms-excel").application("xla", "xlc", "xlm", "xls", "xlt",
				"xlw");
		/** set payment initiation */
		public static final application set_payment_initiation = b("set-payment-initiation").application("setpay");
		/** system 5 release 4 CPIO file */
		public static final application x_sv4cpio = b("x-sv4cpio").application("sv4cpio");
		/** Binary file */
		public static final application octet_stream = b("octet-stream").application("*", "bin", "class", "dms", "exe",
				"lha", "lzh");
		public static final application java_archive = b("java-archive", "x-java-archive", "x-jar").application("jar");
		/** Word document */
		public static final application msword = b("msword").application("doc", "dot");
		/** Acrobat file */
		public static final application pdf = b("pdf").application("pdf");
		/** Unix manual */
		public static final application x_troff_man = b("x-troff-man").application("man");
		/** JavaScript file */
		public static final application x_javascript = b("x-javascript").application("js");
		/** system 5 release 4 CPIO checksum data */
		public static final application x_sv4crc = b("x-sv4crc").application("sv4crc");
		/** Microsoft Project file */
		public static final application vnd_ms_project = b("vnd.ms-project").application("mpp");
		/** Windows meta file */
		public static final application x_msmetafile = b("x-msmetafile").application("wmf");
		/** Windows CardSpace file */
		public static final application x_mscardfile = b("x-mscardfile").application("crd");
		/** computable document format file */
		public static final application x_netcdf = b("x-netcdf").application("cdf", "nc");
		/** Tcl script */
		public static final application x_tcl = b("x-tcl").application("tcl");
		/** personal information exchange file */
		public static final application x_pkcs12 = b("x-pkcs12").application("p12", "pfx");
		/** Corel Envoy */
		public static final application envoy = b("envoy").application("evy");
		/** PKCS #7 certificate file */
		public static final application x_pkcs7_mime = b("x-pkcs7-mime").application("p7c", "p7m");
		/** Microsoft Money file */
		public static final application x_msmoney = b("x-msmoney").application("mny");
		/** rich text format file */
		public static final application rtf = b("rtf").application("rtf");
		/** binary CPIO archive */
		public static final application x_bcpio = b("x-bcpio").application("bcpio");
		/** hierarchical data format file */
		public static final application x_hdf = b("x-hdf").application("hdf");
		/** LaTeX info document */
		public static final application x_texinfo = b("x-texinfo").application("texi", "texinfo");
		/** certificate request file */
		public static final application pkcs10 = b("pkcs10").application("p10");
		/** internet security certificate */
		public static final application x_x509_ca_cert = b("x-x509-ca-cert").application("cer", "crt", "der");
		/** certificate request response file */
		public static final application x_pkcs7_certreqresp = b("x-pkcs7-certreqresp").application("p7r");
		/** Bash shell script */
		public static final application x_sh = b("x-sh").application("sh");
		/** dynamic link library */
		public static final application x_msdownload = b("x-msdownload").application("dll");
		/** Windows help file */
		public static final application winhlp = b("winhlp").application("hlp");
		/** 3ds Max script file */
		public static final application x_troff_ms = b("x-troff-ms").application("ms");
		/** digitally signed email message */
		public static final application x_pkcs7_signature = b("x-pkcs7-signature").application("p7s");
		/** gzipped tar file */
		public static final application x_compressed = b("x-compressed").application("tgz");
		/** FTR media file */
		public static final application x_msterminal = b("x-msterminal").application("trm");
		/** source code */
		public static final application x_wais_source = b("x-wais-source").application("src");
		/** public key security object */
		public static final application ynd_ms_pkipko = b("ynd.ms-pkipko").application("pko");
		/** Adobe Illustrator file */
		public static final application postscript = b("postscript").application("ai", "eps", "ps");
		/** Unix shar archive */
		public static final application x_shar = b("x-shar").application("shar");
		/** Windows catalog file */
		public static final application vnd_ms_pkiseccat = b("vnd.ms-pkiseccat").application("cat");
		/** CrazyTalk clip file */
		public static final application x_msclip = b("x-msclip").application("clp");
		/** Kodak RAW image file */
		public static final application x_director = b("x-director").application("dcr", "dir", "dxr");
		/** WordPerfect macro */
		public static final application vnd_ms_works = b("vnd.ms-works").application("wcm", "wdb", "wks", "wps");
		/** readme text file */
		public static final application x_troff_me = b("x-troff-me").application("me");
		/** Atari ST Program */
		public static final application internet_property_stream = b("internet-property-stream").application("acx");
		/** Font WOFF */
		public static final application font_woff = b("font-woff").application("font-woff");
		/** stereolithography file */
		public static final application vnd_ms_pkistl = b("vnd.ms-pkistl").application("stl");
		/** ARC+ architectural file */
		public static final application x_iphone = b("x-iphone").application("iii");
		/** Outlook mail message */
		public static final application vnd_ms_outlook = b("vnd.ms-outlook").application("msg");
		/** Microsoft Access database */
		public static final application x_msaccess = b("x-msaccess").application("mdb");
		/** Outlook profile file */
		public static final application pics_rules = b("pics-rules").application("prf");
		/** device independent format file */
		public static final application x_dvi = b("x-dvi").application("dvi");
		/** LaTex document */
		public static final application x_latex = b("x-latex").application("latex");
		/** Microsoft Write file */
		public static final application x_mswrite = b("x-mswrite").application("wri");
		/** Gnu zipped archive */
		public static final application x_gzip = b("x-gzip").application("gz");
		/** PKCS #7 certificate file */
		public static final application x_pkcs7_certificates = b("x-pkcs7-certificates").application("p7b", "spc");
		/** set registration initiation */
		public static final application set_registration_initiation = b("set-registration-initiation")
				.application("setreg");
		/** internet settings file */
		public static final application x_internet_signup = b("x-internet-signup").application("ins", "isp");
		/** serialized certificate store file */
		public static final application vnd_ms_pkicertstore = b("vnd.ms-pkicertstore").application("sst");
		/** Unix compressed file */
		public static final application x_compress = b("x-compress").application("z");
		/** MSX computers archive format */
		public static final application x_perfmon = b("x-perfmon").application("pma", "pmc", "pml", "pmr", "pmw");
		/** certificate revocation list file */
		public static final application pkix_crl = b("pkix-crl").application("crl");
		/** Microsoft media viewer file */
		public static final application x_msmediaview = b("x-msmediaview").application("m13", "m14", "mvb");
		/** Unix CPIO archive */
		public static final application x_cpio = b("x-cpio").application("cpio");
		/** PowerPoint template */
		public static final application vnd_ms_powerpoint = b("vnd.ms-powerpoint").application("pot", "pps", "ppt");
		/** BinHex encoded file */
		public static final application mac_binhex40 = b("mac-binhex40").application("hqx");
		/** LaTeX source document */
		public static final application x_tex = b("x-tex").application("tex");
		/** computable document format file */
		public static final application x_cdf = b("x-cdf").application("cdf");
		/** consolidated Unix file archive */
		public static final application x_tar = b("x-tar").application("tar");
		/** Windows print spool file */
		public static final application futuresplash = b("futuresplash").application("spl");
		/** Stuffit archive file */
		public static final application x_stuffit = b("x-stuffit").application("sit");
		/** CALS raster image */
		public static final application oda = b("oda").application("oda");
		/** zipped file */
		public static final application zip = b("zip").application("zip");
		/** Gnu tar archive */
		public static final application x_gtar = b("x-gtar").application("gtar");
		/** Soap 1.2 */
		public static final application soap_xml = b("soap+xml").application("xml");
		/** XML */
		public static final application xml = b("xml").application("xml", "xsd", "wsdl");
		public static final application xhtml = b("xhtml+xml").application("xhtml");
		public static final application signed_exchange = b("signed-exchange").application("sxg");

		public static final application any = b("*").application("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class audio extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public audio(String[] sub_names, String[] extensions) {
			super(AUDIO, sub_names, extensions);
			values.add(this);
		}

		public boolean isAudio() {
			return true;
		}

		public static void init() {
		}

		/** WAVE audio file */
		public static final audio x_wav = b("x-wav").audio("wav");
		/** audio interchange file format */
		public static final audio x_aiff = b("x-aiff").audio("aif", "aifc", "aiff");
		/** midi file */
		public static final audio mid = b("mid").audio("mid", "rmi");
		/** MP3 file */
		public static final audio mpeg = b("mpeg").audio("mp3");
		/** media playlist file */
		public static final audio x_mpegurl = b("x-mpegurl").audio("m3u");
		/** audio file */
		public static final audio basic = b("basic").audio("au", "snd");
		/** Real Audio file */
		public static final audio x_pn_realaudio = b("x-pn-realaudio").audio("ra", "ram");

		public static final audio any = b("*").audio("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class image extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public image(String[] sub_names, String[] extensions) {
			super(IMAGE, sub_names, extensions);
			values.add(this);
		}

		public boolean isImage() {
			return true;
		}

		public static void init() {
		}

		/** image file */
		public static final image ief = b("ief").image("ief");
		/** portable graymap image */
		public static final image x_portable_graymap = b("x-portable-graymap").image("pgm");
		/** graphic interchange format */
		public static final image gif = b("gif").image("gif");
		/** X11 bitmap */
		public static final image x_xbitmap = b("x-xbitmap").image("xbm");
		/** portable bitmap image */
		public static final image x_portable_bitmap = b("x-portable-bitmap").image("pbm");
		/** Corel metafile exchange image file */
		public static final image x_cmx = b("x-cmx").image("cmx");
		/** RGB bitmap */
		public static final image x_rgb = b("x-rgb").image("rgb");
		/** portable pixmap image */
		public static final image x_portable_pixmap = b("x-portable-pixmap").image("ppm");
		/** X11 pixmap */
		public static final image x_xpixmap = b("x-xpixmap").image("xpm");
		/** JPEG file interchange format */
		public static final image pipeg = b("pipeg").image("jfif");
		/** JPEG image */
		public static final image jpeg = b("jpeg").image("jpg", "jpeg", "jpe");
		/** Bitmap */
		public static final image bmp = b("bmp").image("bmp");
		/** icon */
		public static final image x_icon = b("x-icon").image("ico");
		/** compiled source code */
		public static final image cis_cod = b("cis-cod").image("cod");
		/** scalable vector graphics */
		public static final image svg = b("svg+xml").image("svg");
		/** image format for the web */
		public static final image webp = b("webp").image("webp");

		public static final image avif = b("avif").image("avif");
		/** Sun raster graphic */
		public static final image x_cmu_raster = b("x-cmu-raster").image("ras");
		/** png: portable network graphics */
		public static final image png = b("png", "x-png", "apng").image("png");
		/** svg: scalable vector graphics */
		/** X-Windows dump image */
		public static final image x_xwindowdump = b("x-xwindowdump").image("xwd");
		/** TIF image */
		public static final image tiff = b("tiff").image("tif", "tiff");
		/** portable any map image */
		public static final image x_portable_anymap = b("x-portable-anymap").image("pnm");

		public static final image any = b("*").image("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class message extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public message(String[] sub_names, String[] extensions) {
			super(MESSAGE, sub_names, extensions);
			values.add(this);
		}

		public boolean isMessage() {
			return true;
		}

		public static void init() {
		}

		/** MHTML web archive */
		public static final message rfc822 = b("rfc822").message("mht", "mhtml", "nws");

		public static final message http = b("http").message("http");

		public static final message any = b("*").message("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class text extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public text(String[] sub_names, String[] extensions) {
			super(TEXT, sub_names, extensions);
			values.add(this);
		}

		public boolean isText() {
			return true;
		}

		public static void init() {
		}

		/** tab separated values file */
		public static final text tab_separated_values = b("tab-separated-values").text("tsv");
		/** H.323 internet telephony file */
		public static final text h323 = b("h323").text("323");
		/** TeX font encoding file */
		public static final text x_setext = b("x-setext").text("etx");
		/** hypertext template file */
		public static final text webviewhtml = b("webviewhtml").text("htt");
		/** NetMeeting user location service file */
		public static final text iuls = b("iuls").text("uls");
		/** HTML file */
		public static final text html = b("html").text("htm", "html", "stm");
		/** Cascading Style Sheet */
		public static final text css = b("css").text("css");
		/** XML Document */
		public static final text xml = b("xml").text("xml", "xsd", "wsdl");
		/** rich text file */
		public static final text richtext = b("richtext").text("rtx");
		/** HTML component file */
		public static final text x_component = b("x-component").text("htc");
		/** BASIC source code file */
		public static final text plain = b("plain").text("bas", "c", "h", "txt", "ini");
		/** vCard file */
		public static final text x_vcard = b("x-vcard").text("vcf");
		/** Scitext continuous tone file */
		public static final text scriptlet = b("scriptlet").text("sct");
		/** Markdown */
		public static final text markdown = b("markdown", "x-markdown").text("sct");

		public static final text any = b("*").text("*");

		public MimeType anySub() {
			return any;
		}

	}

	public static final class video extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public video(String[] sub_names, String[] extensions) {
			super(VIDEO, sub_names, extensions);
			values.add(this);
		}

		public boolean isVideo() {
			return true;
		}

		public static void init() {
		}

		/** audio video interleave file */
		public static final video x_msvideo = b("x-msvideo").video("avi");
		/** Logos library system file */
		public static final video x_la_asf = b("x-la-asf").video("lsf", "lsx");
		/** advanced systems format file */
		public static final video x_ms_asf = b("x-ms-asf").video("asf", "asr", "asx");
		/** Apple QuickTime movie */
		public static final video quicktime = b("quicktime").video("mov", "qt");
		/** MPEG-4 */
		public static final video mp4 = b("mp4").video("mp4");
		/** Apple QuickTime movie */
		public static final video x_sgi_movie = b("x-sgi-movie").video("movie");
		/** MPEG-2 audio file */
		public static final video mpeg = b("mpeg").video("mp2", "mpa", "mpe", "mpeg", "mpg", "mpv2");

		public static final video any = b("*").video("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class model extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public model(String[] sub_names, String[] extensions) {
			super(MODEL, sub_names, extensions);
			values.add(this);
		}

		public boolean isModel() {
			return true;
		}

		public static void init() {
		}

		/**  */
		public static final model iges = b("iges").model("iges", "igs");
		/**  */
		public static final model vnd_dwf = b("vnd.dwf").model("dwf");
		/**  */
		public static final model x_pov = b("x-pov").model("pov");
		/**  */
		public static final model vrml = b("vrml").model("vrml", "wrl", "wrz");

		public static final model any = b("*").model("*");

		public MimeType anySub() {
			return any;
		}
	}

	public static final class multipart extends MimeType {
		private static final List<MimeType> values = new ArrayList<>();

		public static Iterable<MimeType> values() {
			return Collections.unmodifiableList(values);
		}

		public multipart(String[] sub_names, String[] extensions) {
			super(MULTIPART, sub_names, extensions);
			values.add(this);
		}

		public boolean isMultipart() {
			return true;
		}

		public static void init() {
		}

		/**  */
		public static final multipart form_data = b("form-data").multipart("*");
		/**  */
		public static final multipart www_form_urlencoded = b("x-www-form-urlencoded", "www-form-urlencoded")
				.multipart("*");

		public static final multipart digest = b("digest").multipart("*");

		public static final multipart any = b("*").multipart("*");

		public MimeType anySub() {
			return any;
		}

	}

	public static final MimeType any = new MimeType("*", new String[] { "*" }, new String[0]);

	/**
	 * Returns the any form of the mimetype. <br>
	 * <br>
	 * For example: <br>
	 * <b>image/png</b> returns <b>image/*</b><br>
	 * <b>application/xml</b> returns <b>application/*</b><br>
	 * <b>message/*</b> returns <b>message/*</b><br>
	 * 
	 * @return the /* (any) form of the mimetype.
	 */
	public MimeType anySub() {
		return any;
	}

	public static MimeType getByName(String mimeType) {
		return name_mime.get(mimeType);
	}

	public static MimeType getByExtension(String extension) {
		return ext_mime.get(extension);
	}

	public static Iterable<MimeType> all() {
		return name_mime.values();
	}

	/**
	 * Returns the type name of this MimeType. <br><br>
	 * <b>Example:</b> <code>MimeType.text.plain.getName()</code> returns
	 * <code>text</code>
	 * 
	 * @return the name of this MimeType
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the first (or only, if there is only one) sub-name of this MimeType. <br><br>
	 * <b>Example:</b> <code>MimeType.text.plain.getSubName()</code> returns
	 * <code>plain</code>
	 * 
	 * @return the first sub-name of this MimeType
	 */
	public String getSubName() {
		return sub_names[0];
	}

	/**
	 * Returns the full name of this MimeType. <br><br>
	 * <b>Example:</b> <code>MimeType.text.plain.getFullName()</code> returns
	 * <code>text/plain</code>
	 * 
	 * @return the full name of this MimeType
	 */
	public String getFullName() {
		return full_name;
	}

	/**
	 * Returns the full name of this MimeType
	 * 
	 * @return the result of {@link MimeType#getFullName()}
	 * @see {@link MimeType#getFullName()}
	 */
	@Override
	public String toString() {
		return getFullName();
	}

	public String withCharset(String charset) {
		return charset == null ? getFullName() : getFullName() + "; charset=" + charset;
	}

	public String withCharset(Charset charset) {
		return charset == null ? getFullName() : getFullName() + "; charset=" + charset.displayName();
	}

	public String withBoundary(String boundary) {
		return boundary == null ? getFullName() : getFullName() + "; boundary=" + boundary;
	}

	public List<String> getSubNames() {
		return Collections.unmodifiableList(Arrays.asList(sub_names));
	}

	public List<String> getKnownExtensions() {
		return Collections.unmodifiableList(Arrays.asList(extensions));
	}

	public boolean contains(MimeType other) {
		if (other == null)
			return false;
		if (this == MimeType.any)
			return true;
		if (this == other)
			return true;
		if (this.getClass() != other.getClass())
			return false;
		if (this == anySub())
			return true;
		return false;
	}

	public boolean isVideo() {
		return false;
	}

	public boolean isApplication() {
		return false;
	}

	public boolean isAudio() {
		return false;
	}

	public boolean isFont() {
		return false;
	}

	public boolean isImage() {
		return false;
	}

	public boolean isModel() {
		return false;
	}

	public boolean isText() {
		return false;
	}

	public boolean isMessage() {
		return false;
	}

	public boolean isMultipart() {
		return false;
	}

	public static boolean isVideo(MimeType type) {
		return type.isVideo();
	}

	public static boolean isApplication(MimeType type) {
		return type.isApplication();
	}

	public static boolean isAudio(MimeType type) {
		return type.isAudio();
	}

	public static boolean isFont(MimeType type) {
		return type.isFont();
	}

	public static boolean isImage(MimeType type) {
		return type.isImage();
	}

	public static boolean isModel(MimeType type) {
		return type.isModel();
	}

	public static boolean isText(MimeType type) {
		return type.isText();
	}

	public static boolean isMessage(MimeType type) {
		return type.isMessage();
	}

	public static boolean isMultipart(MimeType type) {
		return type.isMultipart();
	}

	public static boolean isVideo(String type) {
		return startsWith(type, VIDEO_S);
	}

	public static boolean isApplication(String type) {
		return startsWith(type, APPLICATION_S);
	}

	public static boolean isAudio(String type) {
		return startsWith(type, AUDIO_S);
	}

	public static boolean isFont(String type) {
		return startsWith(type, FONT_S);
	}

	public static boolean isImage(String type) {
		return startsWith(type, IMAGE_S);
	}

	public static boolean isModel(String type) {
		return startsWith(type, MODEL_S);
	}

	public static boolean isText(String type) {
		return startsWith(type, TEXT_S);
	}

	public static boolean isMessage(String type) {
		return startsWith(type, MESSAGE_S);
	}

	public static boolean isMultipart(String type) {
		return startsWith(type, MULTIPART_S);
	}

	private static boolean startsWith(String type, String startsWith) {

		if (type == null || startsWith == null)
			return false;

		return (type.startsWith(startsWith));
	}

}
