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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	public static void main(String[] args)
	{
		System.out.println("Initializing Sethis Server....");
		System.out.print("Using Java version ");
		System.out.println(System.getProperty("java.version"));
		
		try
		{
			int port = 1337;
			int queueSize = 100;
			InetAddress localhost = InetAddress.getByName("localhost");
			
			// Create a Server socket
			ServerSocket serverSocket = new ServerSocket(port, queueSize,
			                                             localhost);
			System.out.println("Server started at: " + serverSocket);
			
			// Keep accepting client connections in an infinite loop
			while (true)
			{
				System.out.println("Waiting for a connection....");
				
				// Accept a connection
				final Socket activeSocket = serverSocket.accept();
				
				System.out
				        .println("Received a connection from " + activeSocket);
				
				// Create a new thread to handle the connection
				Runnable runnable = () -> Server
				        .handleClientRequest(activeSocket);
				new Thread(runnable).start();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void handleClientRequest(Socket socket)
	{
		BufferedReader socketReader = null;
		BufferedWriter socketWriter = null;
		
		try
		{
			socketReader = new BufferedReader(
			                                  new InputStreamReader(socket
			                                          .getInputStream()));
			socketWriter = new BufferedWriter(
			                                  new OutputStreamWriter(socket
			                                          .getOutputStream()));

			String inMessage = null;
			while ((inMessage = socketReader.readLine()) != null)
			{
				System.out.println("Received from client: " + inMessage);

				// Echo the received message to the client
				String outMessage = inMessage;
				socketWriter.write(outMessage);
				socketWriter.write("\n");
				socketWriter.flush();
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
