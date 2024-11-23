package wse.utils;

import java.util.HashMap;

public class HttpCodes {
	private static final HashMap<Integer, String> HTTP_CODES = new HashMap<Integer, String>();

	static {
		// Informational
		HTTP_CODES.put(100, "Continue");
		HTTP_CODES.put(101, "Switching Protocols");
		HTTP_CODES.put(102, "Processing");

		// Success
		HTTP_CODES.put(200, "OK");
		HTTP_CODES.put(201, "Created");
		HTTP_CODES.put(202, "Accepted");
		HTTP_CODES.put(203, "Non-Authoritative Information");
		HTTP_CODES.put(204, "No Content");
		HTTP_CODES.put(205, "Reset Content");
		HTTP_CODES.put(206, "Partial Content");
		HTTP_CODES.put(207, "Multi-Status");
		HTTP_CODES.put(208, "Already Reported");
		HTTP_CODES.put(226, "IM Used");

		// Redirection
		HTTP_CODES.put(300, "Multiple Choices");
		HTTP_CODES.put(301, "Moved Permanently");
		HTTP_CODES.put(302, "Found");
		HTTP_CODES.put(303, "See Other");
		HTTP_CODES.put(304, "Not Modified");
		HTTP_CODES.put(305, "Use Proxy");
		HTTP_CODES.put(306, "Switch Proxy");
		HTTP_CODES.put(307, "Temporary Redirect");
		HTTP_CODES.put(308, "Permanent Redirect");

		// Client Error
		HTTP_CODES.put(400, "Bad Request");
		HTTP_CODES.put(401, "Unauthorized");
		HTTP_CODES.put(402, "Payment Required");
		HTTP_CODES.put(403, "Forbidden");
		HTTP_CODES.put(404, "Not Found");
		HTTP_CODES.put(405, "Method Not Allowed");
		HTTP_CODES.put(406, "Not Acceptable");
		HTTP_CODES.put(407, "Proxy Authentication Required");
		HTTP_CODES.put(408, "Request Time-out");
		HTTP_CODES.put(409, "Conflict");
		HTTP_CODES.put(410, "Gone");
		HTTP_CODES.put(411, "Length Required");
		HTTP_CODES.put(412, "Precondition Failed");
		HTTP_CODES.put(413, "Payload Too Large");
		HTTP_CODES.put(414, "URI Too Long");
		HTTP_CODES.put(415, "Unsupported Media Type");
		HTTP_CODES.put(416, "Range Not Satisfiable");
		HTTP_CODES.put(417, "Expectation Failed");
		HTTP_CODES.put(418, "I'm a teapot");
		HTTP_CODES.put(420, "SecurityRetry");
		HTTP_CODES.put(421, "Misdirected Request");
		HTTP_CODES.put(422, "Unprocessable Entity");
		HTTP_CODES.put(423, "Locked");
		HTTP_CODES.put(424, "Failed Dependency");
		HTTP_CODES.put(426, "Upgrade Required");
		HTTP_CODES.put(428, "Precondition Required");
		HTTP_CODES.put(429, "Too Many Requests");
		HTTP_CODES.put(431, "Request Header Fields Too Large");

		// Server Error
		HTTP_CODES.put(500, "Internal Server Error");
		HTTP_CODES.put(501, "Not Implemented");
		HTTP_CODES.put(502, "Bad Gateway");
		HTTP_CODES.put(503, "Service Unavailable");
		HTTP_CODES.put(504, "Gateway Time-out");
		HTTP_CODES.put(505, "HTTP Version Not Supported");
		HTTP_CODES.put(506, "Variant Also Negotiates");
		HTTP_CODES.put(507, "Insufficient Storage");
		HTTP_CODES.put(508, "Loop Detected");
		HTTP_CODES.put(510, "Not extended");
		HTTP_CODES.put(511, "Network Authentication Required");

		// Internet Information Services
		HTTP_CODES.put(440, "Login Time-out");
		HTTP_CODES.put(449, "Retry With");
		HTTP_CODES.put(451, "Unavailable For Legal Reasons");

		// nginx
		HTTP_CODES.put(444, "No Response");
		HTTP_CODES.put(495, "SSL Certificate Error");
		HTTP_CODES.put(496, "SSL Certificate Required");
		HTTP_CODES.put(497, "HTTP Request Send to HTTPS Port");
		HTTP_CODES.put(499, "Client Closed Request");

		// Cloudflare
		HTTP_CODES.put(520, "Unknown Error");
		HTTP_CODES.put(521, "Web Server Is Down");
		HTTP_CODES.put(522, "Connection Timed Out");
		HTTP_CODES.put(523, "Origin Is Unreachable");
		HTTP_CODES.put(524, "A Timeout Occured");
		HTTP_CODES.put(525, "SSL Hanshake Failed");
		HTTP_CODES.put(526, "Invalid SSL Certificate");
		HTTP_CODES.put(527, "Railgun Error");
	}

	public static String getCodeMessage(int code) {
		return HTTP_CODES.get(code);
	}

	public static boolean validCodeMessage(int code) {
		return HTTP_CODES.containsKey(code);
	}

	// Informational
	public static final int CONTINUE = 100;
	public static final int SWITCHING_PROTOCOLS = 101;
	public static final int PROCESSING = 102;

	// Success
	public static final int OK = 200;
	public static final int CREATED = 201;
	public static final int ACCEPTED = 202;
	public static final int NON_AUTHORITATIVE_INFORMATION = 203;
	public static final int NO_CONTENT = 204;
	public static final int RESET_CONTENT = 205;
	public static final int PARTIAL_CONTENT = 206;
	public static final int MULTI_STATUS = 207;
	public static final int ALREADY_REPORTED = 208;
	public static final int IM_USED = 226;

	// Redirection
	public static final int MULTIPLE_CHOICES = 300;
	public static final int MOVED_PERMANENTLY = 301;
	public static final int FOUND = 302;
	public static final int SEE_OTHER = 303;
	public static final int NOT_MODIFIED = 304;
	public static final int USE_PROXY = 305;
	public static final int SWITCH_PROXY = 306;
	public static final int TEMPORARY_REDIRECT = 307;
	public static final int PERMANENT_REDIRECT = 308;

	// Client Error
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int PAYMENT_REQUIRED = 402;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int METHOD_NOT_ALLOWED = 405;
	public static final int NOT_ACCEPTABLE = 406;
	public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
	public static final int REQUEST_TIME_OUT = 408;
	public static final int CONFLICT = 409;
	public static final int GONE = 410;
	public static final int LENGTH_REQUIRED = 411;
	public static final int PRECONDITION_FAILED = 412;
	public static final int PAYLOAD_TOO_LARGE = 413;
	public static final int URI_TOO_LONG = 414;
	public static final int UNSUPPORTED_MEDIA_TYPE = 415;
	public static final int RANGE_NOT_SATISFIABLE = 416;
	public static final int EXPECTATION_FAILED = 417;
	public static final int IM_A_TEAPOT = 418;
	public static final int SECURITY_RETRY = 420;
	public static final int MISDIRECTED_REQUEST = 421;
	public static final int UNPROCESSABLE_ENTITY = 422;
	public static final int LOCKED = 423;
	public static final int FAILED_DEPENDENCY = 424;
	public static final int UPGRADE_REQUIRED = 426;
	public static final int PRECONDITION_REQUIRED = 428;
	public static final int TOO_MANY_REQUESTS = 429;
	public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

	// Server Error
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int NOT_IMPLEMENTED = 501;
	public static final int BAD_GATEWAY = 502;
	public static final int SERVICE_UNAVAILABLE = 503;
	public static final int GATEWAY_TIME_OUT = 504;
	public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
	public static final int VARIANT_ALSO_NEGOTIATES = 506;
	public static final int INSUFFICIENT_STORAGE = 507;
	public static final int LOOP_DETECTED = 508;
	public static final int NOT_EXTENDED = 510;
	public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

	// Internet information services
	public static final int LOGIN_TIME_OUT = 440;
	public static final int RETRY_WITH = 449;
	public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

	// nginx
	public static final int NO_RESPONSE = 444;
	public static final int SSL_CERTIFICATE_ERROR = 495;
	public static final int SSL_CERTIFICATE_REQUIRED = 496;
	public static final int HTTP_REQUEST_SEND_TO_HTTPS_PORT = 497;
	public static final int CLIENT_CLOSED_REQUEST = 499;

	// Cloudflare
	public static final int UNKNOWN_ERROR = 520;
	public static final int WEB_SERVER_IS_DOWN = 521;
	public static final int CONNECTION_TIMED_OUT = 522;
	public static final int ORIGIN_IS_UNREACHABLE = 523;
	public static final int A_TIMEOUT_OCCURED = 524;
	public static final int SSL_HANSHAKE_FAILED = 525;
	public static final int INVALID_SSL_CERTIFICATE = 526;
	public static final int RAILGUN_ERROR = 527;

	public static boolean isSuccessCode(int code) {
		return (code >= 200 && code < 300);
	}

	public static boolean isRedirectionCode(int code) {
		return (code >= 300 && code < 400);
	}

	public static boolean isClientError(int code) {
		return (code >= 400 && code <= 431);
	}

	public static boolean isServerError(int code) {
		return (code >= 500 && code <= 511);
	}
}
