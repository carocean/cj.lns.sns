package cj.lns.chip.sns.server.fs.sink;

public interface IAuthFactory {

	void off(String format);

	void on(String format);

	void flagSelectorPass(String selector);

}
