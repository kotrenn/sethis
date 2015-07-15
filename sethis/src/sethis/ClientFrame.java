package sethis;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Document;

public class ClientFrame extends JFrame
{
	JTextArea m_textArea;

	public ClientFrame(Document document)
	{
		super("Sethis Client");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		m_textArea = new JTextArea(document, "", 80, 40);
		m_textArea.setEditable(false);
		add(m_textArea);
		
		setVisible(true);
		pack();
	}
}
