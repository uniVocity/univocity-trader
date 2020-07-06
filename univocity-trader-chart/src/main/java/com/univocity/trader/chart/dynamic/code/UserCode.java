package com.univocity.trader.chart.dynamic.code;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.function.*;

public class UserCode extends JPanel {

	private RSyntaxTextArea textArea;
	private RTextScrollPane textAreaScroll;
	private AutoCompletion autoCompletion;
	private DefaultCompletionProvider completionProvider;
	private JButton btTest;

	public UserCode() {
		setLayout(new BorderLayout());
		add(getTextAreaScroll(), BorderLayout.CENTER);
		add(getBtTest(), BorderLayout.SOUTH);
	}

	private JButton getBtTest() {
		if (btTest == null) {
			btTest = new JButton("Test");
			btTest.addActionListener(e -> compileAndRun());
		}
		return btTest;
	}


	private RTextScrollPane getTextAreaScroll() {
		if (textAreaScroll == null) {
			textAreaScroll = new RTextScrollPane(getTextArea());
		}
		return textAreaScroll;
	}

	private RSyntaxTextArea getTextArea() {
		if (textArea == null) {
			textArea = new RSyntaxTextArea(20, 80);
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
			textArea.setCodeFoldingEnabled(true);
			getAutoCompletion().install(textArea);
		}
		return textArea;
	}

	private AutoCompletion getAutoCompletion() {
		if (autoCompletion == null) {
			autoCompletion = new AutoCompletion(getCompletionProvider());
		}
		return autoCompletion;
	}

	private CompletionProvider getCompletionProvider() {
		if (completionProvider == null) {
			completionProvider = new DefaultCompletionProvider();

			completionProvider.addCompletion(new BasicCompletion(completionProvider, "close"));
			completionProvider.addCompletion(new BasicCompletion(completionProvider, "open"));
			completionProvider.addCompletion(new BasicCompletion(completionProvider, "volume"));
			completionProvider.addCompletion(new BasicCompletion(completionProvider, "high"));
			completionProvider.addCompletion(new BasicCompletion(completionProvider, "low"));
		}
		return completionProvider;
	}

	private void compileAndRun() {
		MemoryJavaCompiler compiler = new MemoryJavaCompiler();
		String source = getTextArea().getText();
		StringWriter errorOutput = new StringWriter();

		ToDoubleFunction<Candle> f;
		try {
			f = compiler.compileString("Custom", source, errorOutput);
			double result = f.applyAsDouble(new Candle(10, 10, 10.0, 20.0, 10.0, 10.0, 10.0));
			System.out.println(result);
		} catch (Exception e) {
			errorOutput.flush();
			String errors = errorOutput.toString();
			System.err.println(errors);
		}
	}

	public static void main(String... args) {
		final String source = "" +
				"import com.univocity.trader.candles.*;\n" +
				"import java.util.function.*;\n" +
				"public final class Custom implements ToDoubleFunction<Candle> {\n" +
				"\tpublic double applyAsDouble(Candle candle) {\n" +
				"\t\treturn (candle.high + candle.low) / 2.0;\n" +
				"\t}\n" +
				"}\n";


		UserCode userCode = new UserCode();
		userCode.getTextArea().setText(source);

		WindowUtils.displayTestFrame(userCode);
	}
}
