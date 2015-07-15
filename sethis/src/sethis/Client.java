/*******************************************************************************
 * Copyright (c) 2015 Ramona Seay
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package sethis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class Client
{
	private static BufferedReader userInputReader = null;
	private static Document       document        = null;
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Initializing Sethis Client....");
		System.out.print("Using Java version ");
		System.out.println(System.getProperty("java.version"));

		InetAddress serverIPAddress = InetAddress.getByName("localhost");
		int port = 1337;
		InetSocketAddress serverAddress = new InetSocketAddress(
		                                                        serverIPAddress,
		                                                        port);

		// Get a selector
		Selector selector = Selector.open();

		// Create and configure a client socket channel
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(serverAddress);
		
		// Register the channel for connect, read, and write operations
		int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ
				| SelectionKey.OP_WRITE;
		channel.register(selector, operations);
		
		Client.userInputReader = new BufferedReader(
		                                            new InputStreamReader(
		                                                                  System.in));

		Client.document = new PlainDocument();
		ClientFrame frame = new ClientFrame(Client.document);

		while (true)
			if (selector.select() > 0)
			{
				boolean doneStatus = Client.processReadySet(selector
				        .selectedKeys());
				if (doneStatus) break;
			}
		
		channel.close();
	}

	public static boolean processReadySet(Set<SelectionKey> readySet)
			throws Exception
	{
		boolean ret = false;
		
		SelectionKey key = null;
		Iterator<SelectionKey> iterator = null;
		iterator = readySet.iterator();

		while (iterator.hasNext())
		{
			key = iterator.next();

			// Remove the key from the ready set
			iterator.remove();

			if (key.isConnectable())
			{
				boolean connected = Client.processConnect(key);
				if (!connected) return true; // Exit
			}

			if (key.isReadable())
			{
				String message = Client.processRead(key);
				System.out.println("[Server]: " + message);
				Client.document.insertString(Client.document.getLength(),
				                             "[Server]: " + message + "\n",
				                             null);
			}

			if (key.isWritable())
			{
				String message = Client.getUserInput();
				if (message.equalsIgnoreCase("bye")) ret = true; // Exit
				Client.processWrite(key, message);
			}
		}

		return ret; // Not done yet
	}
	
	public static boolean processConnect(SelectionKey key)
	{
		System.out.println("processConnect()");

		SocketChannel channel = (SocketChannel)key.channel();
		
		try
		{
			// Call the finishConnect() in a loop as it is non-blocking
			// for your channel
			while (channel.isConnectionPending())
				channel.finishConnect();
		}
		catch (IOException e)
		{
			// Cancel the channel's registration with the selector
			key.cancel();
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public static String processRead(SelectionKey key) throws Exception
	{
		System.out.println("processRead()");
		
		SocketChannel sChannel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		sChannel.read(buffer);
		buffer.flip();
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer charBuffer = decoder.decode(buffer);
		String message = charBuffer.toString();
		return message;
	}
	
	public static void processWrite(SelectionKey key, String message)
			throws Exception
	{
		System.out.println("processWrite(\"" + message + "\")");

		SocketChannel sChannel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
		sChannel.write(buffer);
	}

	public static String getUserInput() throws IOException
	{
		String promptMessage = "Please enter a message (Bye to quit):";
		System.out.print(promptMessage);
		String userMessage = Client.userInputReader.readLine();
		return userMessage;
	}
}
