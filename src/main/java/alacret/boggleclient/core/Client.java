package alacret.boggleclient.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

	private static final String DEFAULT_SERVER_IP = "127.0.0.1";
	private static final int DEFAULT_SERVER_PORT = 8000;
	private static final String DEFAULT_PLAYER_NAME = "player" + Math.random();
	private static final int ROUND_TIME = 10000;

	public static void main(String[] args) {
		String serverIp = null;
		int serverPort;
		String playerName = null;
		try {
			serverIp = args[0];
		} catch (Exception e) {
			serverIp = DEFAULT_SERVER_IP;
		}
		try {
			serverPort = Integer.valueOf(args[1]);
		} catch (Exception e) {
			serverPort = DEFAULT_SERVER_PORT;
		}
		try {
			playerName = args[2];
		} catch (Exception e) {
			playerName = DEFAULT_PLAYER_NAME;
		}

		try {
			// Connecting to server
			Socket socket = new Socket(serverIp, serverPort);
			PrintWriter pW = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			pW.println("connect:" + playerName);
			String response = br.readLine();
			System.out.println("The server response: " + response);
			if (response.equals("ok"))
				new ClientReadyToPlay(socket);
			else
				System.out.println("Server busy... try again later");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	final private static class ClientReadyToPlay {
		private Socket socket;

		public ClientReadyToPlay(Socket socket) {
			System.out.println("client ready to play..");
			this.socket = socket;
			startRound();
		}

		private void startRound() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				while (true) {
					System.out.println("Starting round, waiting for the cube");
					String command = br.readLine();
					if (command.split(":")[0].equals("cube"))
						play(command);
					else if (command.split(":")[0].equals("results"))
						done(command);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void play(String command) throws IOException {
			String[] split = command.split(":");
			char[][] localCube = parseCube(split[1]);
			printPrettyCube(localCube);
			System.out.println("Start playing... you'v got 3 minutes");
			final List<String> words = new ArrayList<>();

			// long initialTime = System.currentTimeMillis();

			final BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			final Scanner scanner = new Scanner(System.in);

			Thread playThread = new Thread() {
				@Override
				public void run() {
					System.out.println("Enter a new word:");
					while (true) {
						String nextLine;
						try {
							nextLine = br.readLine();
							if (nextLine.length() < 3) {
								System.out
										.println("The words must be at least 3 letters long");
								continue;

							}
							words.add(nextLine);
							System.out.println("Enter a new word:");
						} catch (IOException e) {
							System.out.println("timwe out reached");
							break;
						}

					}
				}
			};
			playThread.start();

			try {
				Thread.sleep(ROUND_TIME);
				System.out.println("waking up");
				scanner.close();

			} catch (InterruptedException e) {

			}

			// while (true) {
			// long currenTime = System.currentTimeMillis();
			// if (currenTime - initialTime > ROUND_TIME) {
			// System.out
			// .println("Timeout rechead... The last word is not included");
			// break;
			// }
			//
			// if (scanner.hasNextLine()) {
			// String nextLine = scanner.nextLine();
			// if (nextLine.length() < 3) {
			// System.out
			// .println("The words must be at least 3 letters long");
			// continue;
			// }
			// words.add(nextLine);
			// System.out.println("enter a new word:");
			// }
			//
			// }

			// Thread playThread = new Thread() {
			// @Override
			// public void run() {
			// Scanner scanner = new Scanner(System.in);
			// while (true) {
			// System.out.println("Enter a new word:");
			// String nextLine = scanner.nextLine();
			// if (isInterrupted()) {
			// scanner.close();
			// return;
			// }
			// words.add(nextLine);
			// }
			// }
			// };
			// playThread.start();
			//
			// try {
			// Thread.sleep(30000);
			// // Thread.sleep(180000);
			// playThread.interrupt();
			//
			// } catch (InterruptedException e) {
			// }

			System.out.println(words.size() + " words sended");
			String wordsString = wordsToString(words);

			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			pw.println("words:" + wordsString);
			scanner.close();
		}

		private void done(String command) throws IOException {
			System.out.println("finishing game... the results are:");
			String[] split = command.split(":")[1].split(",");
			for (String string : split)
				System.out.println(string);

			System.out.println("Goodbye...");
			System.exit(0);

		}

		private String wordsToString(List<String> words) {
			if (words.size() == 0)
				return "";
			StringBuilder sb = new StringBuilder();
			for (String string : words) {
				sb.append(string);
				sb.append(",");
			}
			return sb.substring(0, sb.length() - 1);
		}

		private void printPrettyCube(char[][] cube) {
			System.out.println("\nCube: ");
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++)
					System.out.print(cube[i][j] + "\t");
				System.out.println();
			}
			System.out.println();
		}

		private char[][] parseCube(String string) {
			String[] columns = string.split(",,");
			char[][] cube = new char[4][4];
			for (int i = 0; i < 4; i++) {
				String[] rows = columns[i].split(",");
				for (int j = 0; j < 4; j++)
					cube[i][j] = rows[j].charAt(0);
			}
			return cube;
		}
	}
}
