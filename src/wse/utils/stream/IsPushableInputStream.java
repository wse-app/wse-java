package wse.utils.stream;

public interface IsPushableInputStream extends IsInputStream{
	void push(byte[] b, int off, int len);
}
