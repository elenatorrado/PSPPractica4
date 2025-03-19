package es.studium.clienteftp;


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class ClienteFTP extends JFrame 
{
	private static final long serialVersionUID = 1L;
	// Campos de la cabecera parte superior
	static JTextField txtServidor = new JTextField();
	static JTextField txtUsuario = new JTextField();
	static JTextField txtDirectorioRaiz = new JTextField();
	// Campos de mensajes parte inferior
	private static JTextField txtArbolDirectoriosConstruido = new JTextField();
	private static JTextField txtActualizarArbol = new JTextField();
	// Botones
	JButton botonCargar = new JButton("Subir fichero");
	JButton botonDescargar = new JButton("Descargar fichero");
	JButton botonBorrar = new JButton("Eliminar fichero");
	JButton botonCreaDir = new JButton("Crear carpeta");
	JButton botonDelDir = new JButton("Eliminar carpeta");
	JButton botonSalir = new JButton("Salir");
	// Lista para los datos del directorio
	static JList<String> listaDirec = new JList<String>();
	// contenedor
	private final Container c = getContentPane();
	// Datos del servidor FTP - Servidor local
	static FTPClient cliente = new FTPClient();// cliente FTP
	String servidor = "127.0.0.1";
	String user = "elena";
	String pasw = "studium";
	boolean login;
	static String direcInicial = "/";
	// para saber el directorio y fichero seleccionado
	static String direcSelec = direcInicial;
	static String ficheroSelec = "";
	public static void main(String[] args) throws IOException 
	{
		new ClienteFTP();
	} // final del main

	public ClienteFTP() throws IOException
	{
		super("CLIENTE Basico FTP");
		//para ver los comandos que se originan
		cliente.addProtocolCommandListener(new PrintCommandListener(new PrintWriter (System.out)));
		cliente.connect(servidor); //conexi n al servidor
		cliente.enterLocalPassiveMode();
		login = cliente.login(user, pasw);
		//Se establece el directorio de trabajo actual
		cliente.changeWorkingDirectory(direcInicial);
		//Obteniendo ficheros y directorios del directorio actual
		FTPFile[] files = cliente.listFiles();
		llenarLista(files,direcInicial);
		//Construyendo la lista de ficheros y directorios
		//del directorio de trabajo actual		
		//preparar campos de pantalla
		txtArbolDirectoriosConstruido.setText("<< ARBOL DE DIRECTORIOS CONSTRUIDO >>");
		txtServidor.setText("Servidor FTP: "+servidor);
		txtUsuario.setText("Usuario: "+user);
		txtDirectorioRaiz.setText("DIRECTORIO RAIZ: "+direcInicial);
		//Preparaci n de la lista
		//se configura el tipo de selecci n para que solo se pueda
		//seleccionar un elemento de la lista

		listaDirec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//barra de desplazamiento para la lista
		JScrollPane barraDesplazamiento = new JScrollPane(listaDirec);
		barraDesplazamiento.setPreferredSize(new Dimension(335,420));
		barraDesplazamiento.setBounds(new Rectangle(5,65,335,420));
		c.add(barraDesplazamiento);
		c.add(txtServidor);
		c.add(txtUsuario);
		c.add(txtDirectorioRaiz);
		c.add(txtArbolDirectoriosConstruido);
		c.add(txtActualizarArbol);
		JButton botonVolverAtras = new JButton("Volver atrás");
		botonVolverAtras.setBounds(345, 10, 140, 30);
		c.add(botonVolverAtras);
		JButton botonEliminarDir = new JButton("Eliminar directorio");
		botonEliminarDir.setBounds(345, 50, 140, 30);
		c.add(botonEliminarDir);
		JButton botonRenombrarDir = new JButton("Renombrar directorio");
		botonRenombrarDir.setBounds(345, 90, 140, 30);
		c.add(botonRenombrarDir);
		JButton botonRenombrarFichero = new JButton("Renombrar fichero");
		botonRenombrarFichero.setBounds(345, 130, 140, 30);
		c.add(botonRenombrarFichero);
		
		c.add(botonCargar);
		c.add(botonCreaDir);
		c.add(botonDelDir);
		c.add(botonDescargar);
		c.add(botonBorrar);
		c.add(botonSalir);
		c.setLayout(null);
		//se a aden el resto de los campos de pantalla
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		setSize(510,600);
		setVisible(true);
		listaDirec.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {  // Detectamos doble clic
		            String selectedItem = listaDirec.getSelectedValue();
		            if (selectedItem != null && selectedItem.startsWith("(DIR)")) {
		                // Es un directorio, cambiamos a él
		                String dirName = selectedItem.replace("(DIR) ", "");
		                String newDir = direcSelec + "/" + dirName;
		                try {
		                    cliente.changeWorkingDirectory(newDir);
		                    FTPFile[] files = cliente.listFiles();
		                    llenarLista(files, newDir);  // Llenamos la lista con los nuevos archivos
		                    txtArbolDirectoriosConstruido.setText("DIRECTORIO ACTUAL: " + newDir);
		                } catch (IOException ex) {
		                    ex.printStackTrace();
		                }
		            }
		        }
		    }
		});

		//Acciones al pulsar en la lista o en los botones
		listaDirec.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent lse)
			{
				// TODO Auto-generated method stub
				String fic = "";
				if (lse.getValueIsAdjusting()) 
				{
					ficheroSelec ="";
					//elemento que se ha seleccionado de la lista
					fic =listaDirec.getSelectedValue().toString();
					//Se trata de un fichero
					ficheroSelec = direcSelec;
					txtArbolDirectoriosConstruido.setText("FICHERO SELECCIONADO: " + ficheroSelec);
					ficheroSelec = fic;//nos quedamos con el nocmbre
					txtActualizarArbol.setText("DIRECTORIO ACTUAL: " + direcSelec);
				}
			}
		});
		
		    
		
             
	
		botonVolverAtras.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        if (!direcSelec.equals("/")) {
		            String parentDir = new File(direcSelec).getParent();
		            try {
		                cliente.changeWorkingDirectory(parentDir);
		                FTPFile[] files = cliente.listFiles();
		                llenarLista(files, parentDir);  // Llenamos la lista con los nuevos archivos
		                txtArbolDirectoriosConstruido.setText("DIRECTORIO ACTUAL: " + parentDir);
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		        }
		    }
		});
		botonRenombrarDir.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        if (cliente == null || !cliente.isConnected()) {
		            JOptionPane.showMessageDialog(null, "No hay conexión con el servidor FTP.");
		            return;
		        }

		        if (ficheroSelec == null || ficheroSelec.trim().isEmpty()) {
		            JOptionPane.showMessageDialog(null, "No se ha seleccionado ningún elemento.");
		            return;
		        }

		        String oldPath = direcSelec + "/" + ficheroSelec;
		        boolean esDirectorio = false;

		        try {
		            FTPFile[] files = cliente.listFiles(direcSelec);

		            // Verificar si es un directorio
		            for (FTPFile file : files) {
		                if (file.getName().equals(ficheroSelec)) {
		                    if (file.isDirectory()) {
		                        esDirectorio = true;
		                    }
		                    break;
		                }
		            }

		            if (!esDirectorio) {
		                JOptionPane.showMessageDialog(null, "El elemento seleccionado no es un directorio.");
		                return;
		            }

		            // Si es un directorio, pedimos el nuevo nombre
		            String newName = JOptionPane.showInputDialog(null, "Introduce el nuevo nombre para el directorio");

		            if (newName == null || newName.trim().isEmpty()) {
		                JOptionPane.showMessageDialog(null, "El nuevo nombre no puede estar vacío.");
		                return;
		            }

		            String newPath = direcSelec + "/" + newName.trim();

		            // Intentamos renombrar el directorio
		            if (cliente.rename(oldPath, newPath)) {
		                JOptionPane.showMessageDialog(null, "Directorio renombrado con éxito.");
		                llenarLista(cliente.listFiles(direcSelec), direcSelec);
		            } else {
		                JOptionPane.showMessageDialog(null, "No se pudo renombrar el directorio.");
		            }

		        } catch (IOException ex) {
		            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor FTP: " + ex.getMessage());
		            ex.printStackTrace();
		        }
		    }
		});




		botonRenombrarFichero.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        if (cliente == null || !cliente.isConnected()) {
		            JOptionPane.showMessageDialog(null, "No hay conexión con el servidor FTP.");
		            return;
		        }

		        if (ficheroSelec == null || ficheroSelec.trim().isEmpty()) {
		            JOptionPane.showMessageDialog(null, "No se ha seleccionado ningún elemento.");
		            return;
		        }

		        String oldPath = direcSelec + "/" + ficheroSelec;
		        boolean esFichero = false;

		        try {
		            FTPFile[] files = cliente.listFiles(direcSelec);

		            // Verificar si es un fichero
		            for (FTPFile file : files) {
		                if (file.getName().equals(ficheroSelec)) {
		                    if (file.isFile()) {
		                        esFichero = true;
		                    }
		                    break;
		                }
		            }

		            if (!esFichero) {
		                JOptionPane.showMessageDialog(null, "El elemento seleccionado no es un fichero.");
		                return;
		            }

		            // Si es un fichero, pedimos el nuevo nombre
		            String newName = JOptionPane.showInputDialog(null, "Introduce el nuevo nombre para el fichero");

		            if (newName == null || newName.trim().isEmpty()) {
		                JOptionPane.showMessageDialog(null, "El nuevo nombre no puede estar vacío.");
		                return;
		            }

		            String newPath = direcSelec + "/" + newName.trim();

		            // Intentamos renombrar el fichero
		            if (cliente.rename(oldPath, newPath)) {
		                JOptionPane.showMessageDialog(null, "Fichero renombrado con éxito.");
		                llenarLista(cliente.listFiles(direcSelec), direcSelec);
		            } else {
		                JOptionPane.showMessageDialog(null, "No se pudo renombrar el fichero.");
		            }

		        } catch (IOException ex) {
		            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor FTP: " + ex.getMessage());
		            ex.printStackTrace();
		        }
		    }
		});



		botonSalir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					cliente.disconnect();
				}
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		botonCreaDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String nombreCarpeta = JOptionPane.showInputDialog(null, "Introduce el nombre del directorio","carpeta");
				if (!(nombreCarpeta==null)) 
				{
					String directorio = direcSelec;
					if (!direcSelec.equals("/"))
						directorio = directorio + "/";
					//nombre del directorio a crear
					directorio += nombreCarpeta.trim(); 
					//quita blancos a derecha y a izquierda
					try 
					{
						if (cliente.makeDirectory(directorio))
						{
							String m = nombreCarpeta.trim()+ " => Se ha creado correctamente ...";
							JOptionPane.showMessageDialog(null, m);
							txtArbolDirectoriosConstruido.setText(m);
							//directorio de trabajo actual
							cliente.changeWorkingDirectory(direcSelec);
							FTPFile[] ff2 = null;
							//obtener ficheros del directorio actual
							ff2 = cliente.listFiles();
							//llenar la lista
							llenarLista(ff2, direcSelec);
						}
						else
							JOptionPane.showMessageDialog(null, nombreCarpeta.trim() + " => No se ha podido crear ...");
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				} // final del if
			}
		}); // final del bot n CreaDir
		botonDelDir.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String nombreCarpeta = JOptionPane.showInputDialog(null,"Introduce el nombre del directorio a eliminar","carpeta");
				if (!(nombreCarpeta==null)) 
				{
					String directorio = direcSelec;
					if (!direcSelec.equals("/"))
						directorio = directorio + "/";
					//nombre del directorio a eliminar
					directorio += nombreCarpeta.trim(); //quita blancos a derecha y a izquierda
					try 
					{
						if(cliente.removeDirectory(directorio)) 
						{
							String m = nombreCarpeta.trim()+" => Se ha eliminado correctamente ...";
							JOptionPane.showMessageDialog(null, m);
							txtArbolDirectoriosConstruido.setText(m);
							//directorio de trabajo actual
							cliente.changeWorkingDirectory(direcSelec);
							FTPFile[] ff2 = null;
							//obtener ficheros del directorio actual
							ff2 = cliente.listFiles();
							//llenar la lista
							llenarLista(ff2, direcSelec);
						}
						else
							JOptionPane.showMessageDialog(null, nombreCarpeta.trim() + " => No se ha podido eliminar ...");
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				} 
				
			}
		}); 
		//final del bot n Eliminar Carpeta
		botonCargar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser f;
				File file;
				f = new JFileChooser();
				//solo se pueden seleccionar ficheros
				f.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//t tulo de la ventana
				f.setDialogTitle("Selecciona el fichero a subir al servidor FTP");
				//se muestra la ventana
				int returnVal = f.showDialog(f, "Cargar");
				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					//fichero seleccionado
					file = f.getSelectedFile();
					//nombre completo del fichero
					String archivo = file.getAbsolutePath();
					//solo nombre del fichero
					String nombreArchivo = file.getName();
					try 
					{
						SubirFichero(archivo, nombreArchivo);
					}
					catch (IOException e1) 
					{
						e1.printStackTrace(); 
					}
				}
			}
		}); //Fin bot n subir
		botonDescargar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String directorio = direcSelec;
				if (!direcSelec.equals("/"))
					directorio = directorio + "/";
				if (!direcSelec.equals("")) 
				{
					DescargarFichero(directorio + ficheroSelec, ficheroSelec);
				}
			}
		}); // Fin bot n descargar
		botonBorrar.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String directorio = direcSelec;
				if (!direcSelec.equals("/"))
					directorio = directorio + "/";
				if (!direcSelec.equals("")) 
				{
					BorrarFichero(directorio + ficheroSelec,ficheroSelec);
				}
			}
		});
	} // fin constructor
	private static void llenarLista(FTPFile[] files,String direc2) 
	{
		if (files == null)
			return;
		//se crea un objeto DefaultListModel
		DefaultListModel<String> modeloLista = new DefaultListModel<String>();
		modeloLista = new DefaultListModel<String>();
		//se definen propiedades para la lista, color y tipo de fuente

		listaDirec.setForeground(Color.blue);
		Font fuente = new Font("Courier", Font.PLAIN, 12);
		listaDirec.setFont(fuente);
		//se eliminan los elementos de la lista
		listaDirec.removeAll();
		try 
		{
			//se establece el directorio de trabajo actual
			cliente.changeWorkingDirectory(direc2);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		direcSelec = direc2; //directorio actual
		//se a ade el directorio de trabajo al listmodel, primerelementomodeloLista.addElement(direc2);
		//se recorre el array con los ficheros y directorios
		for (int i = 0; i < files.length; i++) 
		{
			if (!(files[i].getName()).equals(".") && !(files[i].getName()).equals("..")) 
			{
				//nos saltamos los directorios . y ..
				//Se obtiene el nombre del fichero o directorio
				String f = files[i].getName();
				//Si es directorio se a ade al nombre (DIR)
				if (files[i].isDirectory()) f = "(DIR) " + f;
				//se a ade el nombre del fichero o directorio al listmodel
				modeloLista.addElement(f);
			}//fin if
		}//fin for
		try 
		{
			//se asigna el listmodel al JList,
			//se muestra en pantalla la lista de ficheros y direc
			listaDirec.setModel(modeloLista);
		}
		catch (NullPointerException n) 
		{
			; //Se produce al cambiar de directorio
		}
	}//Fin llenarLista
	private boolean SubirFichero(String archivo, String soloNombre) throws IOException 
	{
		cliente.setFileType(FTP.BINARY_FILE_TYPE);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivo));
		boolean ok = false;
		//directorio de trabajo actual
		cliente.changeWorkingDirectory(direcSelec);
		if (cliente.storeFile(soloNombre, in)) 
		{
			String s = " " + soloNombre + " => Subido correctamente...";
			txtArbolDirectoriosConstruido.setText(s);
			txtActualizarArbol.setText("Se va a actualizar el  rbol de directorios...");
			JOptionPane.showMessageDialog(null, s);
			FTPFile[] ff2 = null;
			//obtener ficheros del directorio actual
			ff2 = cliente.listFiles();
			//llenar la lista con los ficheros del directorio actual
			llenarLista(ff2,direcSelec);
			ok = true;
		}
		else
			txtArbolDirectoriosConstruido.setText("No se ha podido subir... " + soloNombre);
		return ok;
	}// final de SubirFichero
	private void DescargarFichero(String NombreCompleto, String nombreFichero) 
	{
		File file;
		String archivoyCarpetaDestino = "";
		String carpetaDestino = "";
		JFileChooser f = new JFileChooser();
		//solo se pueden seleccionar directorios
		f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//t tulo de la ventana
		f.setDialogTitle("Selecciona el Directorio donde Descargar el Fichero");
		int returnVal = f.showDialog(null, "Descargar");
		if (returnVal == JFileChooser.APPROVE_OPTION) 
		{
			file = f.getSelectedFile();
			//obtener carpeta de destino
			carpetaDestino = (file.getAbsolutePath()).toString();
			//construimos el nombre completo que se crear  en nuestro disco
			archivoyCarpetaDestino = carpetaDestino + File.separator + nombreFichero;
			try 
			{
				cliente.setFileType(FTP.BINARY_FILE_TYPE);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(archivoyCarpetaDestino));
				if (cliente.retrieveFile(NombreCompleto, out))
					JOptionPane.showMessageDialog(null,	nombreFichero + " => Se ha descargado correctamente ...");
				else
					JOptionPane.showMessageDialog(null,	nombreFichero + " => No se ha podido descargar ...");
				out.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	} // Final de DescargarFichero
	private void BorrarFichero(String NombreCompleto, String nombreFichero) {
	    // Confirmación del usuario antes de eliminar el archivo
	    int seleccion = JOptionPane.showConfirmDialog(null, 
	        "¿Desea eliminar el fichero seleccionado?", 
	        "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

	    if (seleccion == JOptionPane.YES_OPTION) {
	        try {
	            // Verificar si el archivo existe y no es un directorio
	            FTPFile[] files = cliente.listFiles(direcSelec);
	            boolean esFichero = false;

	            for (FTPFile file : files) {
	                if (file.getName().equals(nombreFichero) && file.isFile()) {
	                    esFichero = true;
	                    break;
	                }
	            }

	            // Si no es un fichero, mostrar un mensaje de error y salir
	            if (!esFichero) {
	                JOptionPane.showMessageDialog(null, 
	                    "El elemento seleccionado no es un fichero o no existe.");
	                return;
	            }

	            // Intentar eliminar el fichero
	            if (cliente.deleteFile(NombreCompleto)) {
	                String mensaje = nombreFichero + " => Eliminado correctamente.";
	                JOptionPane.showMessageDialog(null, mensaje);
	                txtArbolDirectoriosConstruido.setText(mensaje);

	                // Actualizar la lista de archivos en la interfaz
	                cliente.changeWorkingDirectory(direcSelec);
	                FTPFile[] archivosActualizados = cliente.listFiles();
	                llenarLista(archivosActualizados, direcSelec);
	            } else {
	                JOptionPane.showMessageDialog(null, 
	                    nombreFichero + " => No se ha podido eliminar.");
	            }
	        } catch (IOException e1) {
	            JOptionPane.showMessageDialog(null, 
	                "Error al eliminar el fichero: " + e1.getMessage());
	            e1.printStackTrace();
	        }
	    }
	}
// Final de BorrarFichero
}// Final de la clase ClienteFTPBasico