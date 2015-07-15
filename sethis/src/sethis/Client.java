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
import java.net.Socket;

public class Client
{
	public static void main(String[] args)
	{
		System.out.println("Initializing Sethis Client....");
		System.out.print("Using Java version ");
		System.out.println(System.getProperty("java.version"));
		
		Socket socket = null;
		BufferedReader socketReader = null;
		BufferedWriter socketWriter = null;
		
		try
		{
			String host = "localhost";
			int port = 1337;
			
			// Create a socket to connect to the server
			socket = new Socket(host, port);
			System.out.println("Started client socket at "
			                   + socket.getLocalSocketAddress());
			
			// Create buffered readers and writers using the socket's input and
			// output streams
			socketReader = new BufferedReader(
			                                  new InputStreamReader(socket
			                                          .getInputStream()));
			socketWriter = new BufferedWriter(
			                                  new OutputStreamWriter(socket
			                                          .getOutputStream()));
			
			// Create a buffered reader for user input
			BufferedReader consoleReader = new BufferedReader(
			                                                  new InputStreamReader(
			                                                                        System.in));
			
			String promptMessage = "Please enter a message (Bye to quit):";
			String outMessage = null;
			
			System.out.print(promptMessage);
			while ((outMessage = consoleReader.readLine()) != null)
			{
				if (outMessage.equalsIgnoreCase("bye")) break;
				
				// Add a new line to the message for the server
				socketWriter.write(outMessage);
				socketWriter.write("\n");
				socketWriter.flush();
				
				// Read and display the message from the server
				String inMessage = socketReader.readLine();
				System.out.println("Server: " + inMessage);
				
				System.out.println(""); // Print a blank line
				System.out.print(promptMessage);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (socket != null) try
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
