package com.univocity.trader.chart.dynamic.code;

import com.univocity.trader.candles.*;
import com.univocity.trader.chart.gui.*;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.function.*;

public class UserCode<T> extends JSplitPane {

	private RSyntaxTextArea codeArea;
	private RTextScrollPane codeAreaScroll;
	private AutoCompletion autoCompletion;
	private DefaultCompletionProvider completionProvider;

	private RSyntaxTextArea outputArea;
	private RTextScrollPane outputAreaScroll;

	private JButton btSave;
	private JButton btTest;
	private JToggleButton btLock;
	private JPanel controlPanel;
	private JPanel editorPanel;

	private String lastWorkingVersion;
	private T lastInstanceBuilt;
	private final Function<T, ?> resultProducer;

	public static final String CANDLE_CLOSE = "" +
			"import com.univocity.trader.candles.*;\n" +
			"import java.util.function.*;\n\n" +
			"public final class Custom implements ToDoubleFunction<Candle> {\n\n" +
			"\tpublic double applyAsDouble(Candle candle) {\n" +
			"\t\treturn candle.close;\n" +
			"\t}\n" +
			"}\n";


	public <I> UserCode() {
		this(null);
	}

	public <I> UserCode(Function<T, I> resultProducer) {
		super(JSplitPane.VERTICAL_SPLIT, false);
		setLeftComponent(getEditorPanel());
		setRightComponent(getOutputAreaScroll());
		setDividerLocation(400);
		setDividerSize(2);
		this.resultProducer = resultProducer;
	}

	private JPanel getEditorPanel() {
		if (editorPanel == null) {
			editorPanel = new JPanel(new BorderLayout());
			editorPanel.add(getControlPanel(), BorderLayout.SOUTH);
			editorPanel.add(getCodeAreaScroll(), BorderLayout.CENTER);
		}
		return editorPanel;
	}

	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			controlPanel.add(getBtSave());
			controlPanel.add(getBtTest());
			controlPanel.add(getBtLock());
		}
		return controlPanel;
	}

	private JButton getBtTest() {
		if (btTest == null) {
			btTest = new JButton("Test");
			btTest.addActionListener(e -> compileAndRun());
		}
		return btTest;
	}

	private JButton getBtSave() {
		if (btSave == null) {
			btSave = new JButton("Save");
			btSave.addActionListener(e -> save());
		}
		return btSave;
	}

	private JToggleButton getBtLock() {
		if (btLock == null) {
			btLock = new JToggleButton("Unlock");
			btLock.setSelected(true);
			btLock.addActionListener(e -> lockClicked());
			lockClicked();
		}
		return btLock;
	}

	private void lockClicked() {
		getCodeArea().setEditable(!getBtLock().isSelected());
		getBtLock().setText(!getBtLock().isSelected() ? "Lock" : "Unlock");
	}


	private RTextScrollPane getCodeAreaScroll() {
		if (codeAreaScroll == null) {
			codeAreaScroll = new RTextScrollPane(getCodeArea());
		}
		return codeAreaScroll;
	}

	private RTextScrollPane getOutputAreaScroll() {
		if (outputAreaScroll == null) {
			outputAreaScroll = new RTextScrollPane(getOutputArea());
		}
		return outputAreaScroll;
	}

	private RSyntaxTextArea getCodeArea() {
		if (codeArea == null) {
			codeArea = new RSyntaxTextArea(20, 80);
			codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
			codeArea.setCodeFoldingEnabled(true);
			getAutoCompletion().install(codeArea);
		}
		return codeArea;
	}

	private RSyntaxTextArea getOutputArea() {
		if (outputArea == null) {
			outputArea = new RSyntaxTextArea(20, 80);
			outputArea.setEditable(false);
			outputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		}
		return outputArea;
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
		outputArea.setText("Compiling...");

		SwingUtilities.invokeLater(() -> {
			MemoryJavaCompiler compiler = new MemoryJavaCompiler();
			String source = getSourceCode();
			StringWriter errorOutput = new StringWriter();
			try {
				T instance = compiler.compileString("Custom", source, errorOutput);
				if (resultProducer != null) {
					Object result = resultProducer.apply(instance);
					outputArea.setText("Result:\n" + result);
				} else {
					outputArea.setText("Success!");
				}
				lastWorkingVersion = source;
				this.lastInstanceBuilt = instance;
				CodeUpdateListener[] listeners = listenerList.getListeners(CodeUpdateListener.class);
				for (int i = 0; i < listeners.length; i++) {
					listeners[i].actionPerformed(null);
				}
			} catch (Exception e) {
				errorOutput.flush();
				String errors = errorOutput.toString();
				outputArea.setText(errors);

				if (errors.isBlank()) {
					outputArea.setText("Internal error: " + e.getMessage());
				}
			}
		});
	}

	public void save() {
		String code = getSourceCode();

	}

	public String getSourceCode() {
		return getCodeArea().getText();
	}

	public String getLastWorkingVersion() {
		return lastWorkingVersion;
	}

	public T lastInstanceBuilt() {
		return lastInstanceBuilt;
	}

	public void setSourceCode(String sourceCode) {
		getCodeArea().setText(sourceCode);
		lastWorkingVersion = sourceCode;
	}

	public void addCodeUpdateListener(CodeUpdateListener l) {
		listenerList.add(CodeUpdateListener.class, l);
	}

	public interface CodeUpdateListener extends ActionListener {

	}

	public static void main(String... args) {
		Candle testCandle = new Candle(10, 10, 10.0, 20.0, 10.0, 10.0, 10.0);

		UserCode<ToDoubleFunction<Candle>> userCode = new UserCode<>(f -> f.applyAsDouble(testCandle));
		userCode.setSourceCode(CANDLE_CLOSE);

		WindowUtils.displayTestFrame(userCode);
	}
}
