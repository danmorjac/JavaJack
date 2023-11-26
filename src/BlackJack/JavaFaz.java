package BlackJack;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Color;

public class JavaFaz {

	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private boolean isServer = true;
	private static int PORT = 5005;
	private static String IP = "127.0.0.1";

	private boolean plantarse = false;
	private static boolean Plantados=false;
	private boolean PlantadoServidor=false;
	private boolean PlantadoJugador=false;

	private Blackjack blackjack = new Blackjack();	
	private int puntuacionContrario;

	String url = "jdbc:mysql://127.0.0.1:33306/BlackJack"; 
	String usuario = "root"; 
	String contraseña = "alumnoalumno"; 

	private JFrame frame;
	private JTextArea Info;
	private JButton pedirCartaButton;
	private JButton plantarseButton;
	private JTextArea AreaJugador;
	private JPanel BordePlantarse;
	private JPanel bordePedir;

	public JavaFaz() {
		initComponents();
	}

	private void initComponents() {
		frame = new JFrame("Blackjack");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(638, 420);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(139, 69, 19));

		pedirCartaButton = new JButton("Pedir Carta");
		pedirCartaButton.setEnabled(false);
		pedirCartaButton.setBackground(new Color(255, 255, 204));
		pedirCartaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				repartirCarta();
				mostrarDatos();

				if (getValor() > 21) {

					Plantados=true;

					AreaJugador.append("\nTe has pasado de 21");
				}
			}

		});
		pedirCartaButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		pedirCartaButton.setBounds(101, 299, 159, 71);
		panel.setLayout(null);
		panel.add(pedirCartaButton);

		plantarseButton = new JButton("Plantarse");
		plantarseButton.setEnabled(false);
		plantarseButton.setBackground(new Color(255, 255, 204));
		plantarseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Plantados=true;
				plantarse = true;
			}

		});
		plantarseButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		plantarseButton.setBounds(378, 299, 159, 71);
		panel.add(plantarseButton);

		frame.getContentPane().add(panel);

		AreaJugador = new JTextArea();
		AreaJugador.setBackground(new Color(50, 205, 50));
		AreaJugador.setFont(new Font("Monospaced", Font.BOLD, 16));
		AreaJugador.setToolTipText("");
		AreaJugador.setEditable(false);
		AreaJugador.setBounds(10, 79, 602, 205);

		panel.add(AreaJugador);

		Info = new JTextArea();
		Info.setBackground(new Color(245, 245, 220));
		Info.setEditable(false);
		Info.setBounds(101, 11, 430, 60);
		panel.add(Info);

		bordePedir = new JPanel();
		bordePedir.setBackground(new Color(255, 204, 51));
		bordePedir.setBounds(99, 297, 163, 75);
		panel.add(bordePedir);

		BordePlantarse = new JPanel();
		BordePlantarse.setBackground(new Color(255, 204, 51));
		BordePlantarse.setBounds(376, 297, 163, 75);
		panel.add(BordePlantarse);


		frame.setVisible(true);

		plantarseButton.setVisible(false);
		pedirCartaButton.setVisible(false);

		bordePedir.setVisible(false);
		BordePlantarse.setVisible(false);
	}

	private void iniciarServidor() throws IOException {
		serverSocket = new ServerSocket(PORT);
		Info.append("Servidor iniciado en el puerto " + PORT);

		Info.append("\n Esperando conexión...");
		socket = serverSocket.accept();
		Info.append("\nConectado con " + socket.getInetAddress());

		establecerFlujos();
	}

	public void conectarAlServidor() throws UnknownHostException, IOException {
		socket = new Socket(IP, PORT);
		Info.append("\nConectado con " + socket.getInetAddress());

		establecerFlujos();
	}

	private void establecerFlujos() throws IOException {
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		outputStream.flush();
		inputStream = new ObjectInputStream(socket.getInputStream());
	}

	private void leerBaraja() throws ClassNotFoundException, IOException {
		Object ob = inputStream.readObject();

		List<Carta> otraBaraja = (List<Carta>) ob;

		blackjack.setBaraja(otraBaraja);
	}

	private void leerResultado() throws ClassNotFoundException, IOException {
		Object ob = inputStream.readObject();

		Integer res = (Integer) ob;

		puntuacionContrario = res.intValue();
	}

	private void enviarBaraja() throws IOException {
		outputStream.writeObject(blackjack.getBaraja());
	}


	private void enviarResultado() throws IOException {
		Integer res = null;

		if (isServer) {
			res = Integer.valueOf(blackjack.getValorServidor());
		} else {
			res = Integer.valueOf(blackjack.getValorJugador());
		}

		outputStream.writeObject(res);
	}

	private void repartirCarta() {
		blackjack.repartirCarta(isServer);
	}

	public void mostrarDatos() {
		String datos;

		if (isServer) {
			datos = blackjack.getManoServidor().toString() + "\n";
			datos += "Valor: " + blackjack.getValorServidor();
		} else {
			datos = blackjack.getManoJugador().toString() + "\n";
			datos += "Valor: " + blackjack.getValorJugador();
		}

		AreaJugador.setText(datos);
	}

	public void Plantados() {


		if(PlantadoServidor==true && PlantadoJugador==true) {

			Plantados=true;
		}
	}

	public boolean Plantarse() {
		return true;

	}

	public int getValor() {
		int valor;

		if (isServer) {
			valor = blackjack.getValorServidor();
		} else {
			valor = blackjack.getValorJugador();
		}

		return valor;
	}

	public void jugar() throws IOException {


		while(!plantarse) {

			mostrarDatos();

			int opcion2 = Integer.parseInt(JOptionPane.showInputDialog("Que deseas hacer? \n Pedir carta: 1\n Plantarse: 2"));

			if (opcion2==1 ) {
				repartirCarta();
				mostrarDatos();

				if (getValor() > 21) {

					Plantados=true;
					plantarse = true;
					AreaJugador.append("\nTe has pasado de 21");
				}
			}

			else if (opcion2 == 2) {

				Plantados=true;
				plantarse = true;
			}
			else {

				Plantados=true;
				plantarse = true;
			}
		}


	}


	public void getGanador() throws ClassNotFoundException {
		String resultado = "";

		if (puntuacionContrario <= 21 && (getValor() > 21 || puntuacionContrario > getValor())) {

			AreaJugador.append("\nGana tu oponente\nCon una puntuación de: " + puntuacionContrario);
			resultado = "Perdedor";

		} else if (getValor() <= 21 && (puntuacionContrario > 21 || getValor() > puntuacionContrario)) {

			AreaJugador.append("\nGanaste tú\nPuntuación del oponente: " + puntuacionContrario);
			resultado = "Ganador";

		} else {
			AreaJugador.append("\nNadie gana");
			resultado = "Empate";
		}

		if(resultado.equals("Ganador")) {
			String nombreJugador = JOptionPane.showInputDialog(frame, "Ingresa tu nombre:");

			guardarGanadorEnBD(nombreJugador, resultado);
		}
		AreaJugador.append("\nResultado guardado en la Base de datos");
	}

	private void guardarGanadorEnBD(String nombreJugador, String resultado) {
		Connection con = null;
		PreparedStatement st = null;

		try {
			con = DriverManager.getConnection(url, usuario,contraseña);

			// Consulta para insertar o actualizar el resultado
			String sql = "INSERT INTO resultados (nombre, ganador, veces_ganado) VALUES (?, ?, 1) " +
					"ON DUPLICATE KEY UPDATE ganador = ?, veces_ganado = veces_ganado + 1 ";
			st = con.prepareStatement(sql);
			st.setString(1, nombreJugador);
			st.setString(2, resultado);
			st.setString(3, resultado);
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		int opcion = Integer.parseInt(JOptionPane.showInputDialog("Elige el modo de juego:\n 1: Servidor\n 2: Cliente"));

		JavaFaz controlador = new JavaFaz();
		controlador.blackjack.barajar();

		try {
			switch (opcion) {
			case 1:
				controlador.iniciarServidor();

				for (int i = 0; i < 2; i++)
					controlador.repartirCarta();
				controlador.mostrarDatos();

				controlador.enviarBaraja();
				controlador.leerBaraja();
				break;
			case 2:
				controlador.isServer = false;
				controlador.conectarAlServidor();

				controlador.leerBaraja();

				for (int i = 0; i < 2; i++)
					controlador.repartirCarta();
				controlador.mostrarDatos();

				controlador.enviarBaraja();
				break;
			}

			Thread juegoThread = new Thread(() -> {
				try {
					while (!controlador.Plantados) {
						if (controlador.isServer) {
							controlador.jugar();
							controlador.enviarBaraja();
							controlador.leerBaraja();

						} else {
							controlador.leerBaraja();
							controlador.jugar();
							controlador.enviarBaraja();

						}
					}

					if (controlador.isServer) {
						controlador.enviarResultado();
						controlador.leerResultado();
					} else {
						controlador.leerResultado();
						controlador.enviarResultado();
					}

					controlador.getGanador();

				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			});

			juegoThread.start(); 
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}