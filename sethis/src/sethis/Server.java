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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

public class Server
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Initializing Sethis Server....");
		System.out.print("Using Java version ");
		System.out.println(System.getProperty("java.version"));
		
		int port = 1337;
		InetAddress hostIPAddress = InetAddress.getByName("localhost");
		
		// Get a selector
		Selector selector = Selector.open();
		
		// Get a server socket channel
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		
		// Make the server socket channel non-blocking and bind it to an
		// address
		ssChannel.configureBlocking(false);
		ssChannel.bind(new InetSocketAddress(hostIPAddress, port));
		
		// Register a socket server channel with the selector for accept
		// operation,
		// so that it can be notified when a new connection request arrives
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		// Now we will keep waiting in a loop for any kind of request
		// that arrives to the server - connection, read, or write
		// request. If a connection request comes in, we will accept
		// the request and register a new socket channel with the selector
		// for read and write operations. If read or write requests come
		// in, we will forward that request to the registered channel.
		while (true)
		{
			if (selector.select() <= 0) continue;
			Server.processReadySet(selector.selectedKeys());
		}
	}

	public static void processReadySet(Set<SelectionKey> readySet)
			throws Exception
	{
		SelectionKey key = null;
		Iterator<SelectionKey> iterator = null;
		iterator = readySet.iterator();
		while (iterator.hasNext())
		{
			key = iterator.next();
			
			// Remove the key from the ready key set
			iterator.remove();
			
			// Process the key according to the operation it is ready for
			if (key.isAcceptable()) Server.processAccept(key);
			
			if (key.isReadable())
			{
				String message = Server.processRead(key);
				if (message.length() > 0) Server.echoMessage(key, message);
			}
		}
	}
	
	public static void processAccept(SelectionKey key) throws IOException
	{
		// This method call indicates that we got a new connection
		// request. Accept the connection request and register the new
		// socket channel with the selector, so that the client can
		// communicate on a new channel.
		ServerSocketChannel ssChannel = (ServerSocketChannel)key.channel();
		SocketChannel sChannel = ssChannel.accept();
		sChannel.configureBlocking(false);
		
		// Register only for read. Our message is small and we write it
		// back to the client as soon as we read it.
		sChannel.register(key.selector(), SelectionKey.OP_READ);
	}

	public static String processRead(SelectionKey key) throws Exception
	{
		SocketChannel sChannel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int bytesCount = sChannel.read(buffer);
		String message = "";

		if (bytesCount > 0)
		{
			buffer.flip();
			Charset charset = Charset.forName("UTF-8");
			CharsetDecoder decoder = charset.newDecoder();
			CharBuffer charBuffer = decoder.decode(buffer);
			message = charBuffer.toString();
			System.out.println("Received message: " + message);
		}

		return message;
	}

	public static void echoMessage(SelectionKey key, String message)
	                                                                throws IOException
	{
		SocketChannel sChannel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
		sChannel.write(buffer);
	}
}
