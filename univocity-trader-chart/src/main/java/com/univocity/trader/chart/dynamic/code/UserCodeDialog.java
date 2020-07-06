package com.univocity.trader.chart.dynamic.code;

import javax.swing.*;
import java.awt.event.*;

public class UserCodeDialog extends JDialog implements ActionListener {

	private final UserCode<?> userCode;

	public UserCodeDialog(UserCode<?> userCode) {
		this.userCode = userCode;

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(false);

		setContentPane(userCode);
		pack();
		setLocationRelativeTo(null);

		userCode.getBtClose().addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		userCode.getBtClose().removeActionListener(this);
		this.dispose();
	}
}
