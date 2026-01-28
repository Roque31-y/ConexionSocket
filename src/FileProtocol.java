// Importamos la interfaz Serializable que permite convertir objetos a bytes
// para poder enviarlos a traves de la red
import java.io.Serializable;

/**
 * Clase FileProtocol - Protocolo de comunicacion para operaciones de archivos
 * 
 * Esta clase define la estructura de los mensajes que se envian entre
 * el cliente y el servidor. Implementa Serializable para poder ser
 * transmitida a traves del socket como un objeto.
 * 
 * Funciona como un "paquete" que contiene:
 * - El comando a ejecutar (que quiere hacer el cliente)
 * - Los datos necesarios (nombre de archivo, contenido)
 * - La respuesta del servidor
 */
public class FileProtocol implements Serializable {
    
    // serialVersionUID: Identificador unico de version para la serializacion
    // Java lo usa para verificar que el objeto enviado y recibido sean compatibles
    // Si las versiones no coinciden, la deserializacion fallara
    private static final long serialVersionUID = 1L;
    
    // ============================================================================
    // CONSTANTES DE COMANDOS - Definen las operaciones que el cliente puede pedir
    // ============================================================================
    
    // Comando para solicitar la lista de archivos disponibles en el servidor
    public static final String CMD_LIST_FILES = "LIST";
    
    // Comando para leer/abrir el contenido de un archivo
    public static final String CMD_READ_FILE = "READ";
    
    // Comando para escribir contenido en un archivo (crear o sobrescribir)
    public static final String CMD_WRITE_FILE = "WRITE";
    
    // Comando para eliminar un archivo del servidor
    public static final String CMD_DELETE_FILE = "DELETE";
    
    // Comando para crear un nuevo archivo
    public static final String CMD_CREATE_FILE = "CREATE";
    
    // Comando para modificar un archivo existente
    public static final String CMD_MODIFY_FILE = "MODIFY";
    
    // Comando para cerrar la conexion con el servidor
    public static final String CMD_DISCONNECT = "DISCONNECT";
    
    // ============================================================================
    // CONSTANTES DE RESPUESTAS - Definen los tipos de respuesta del servidor
    // ============================================================================
    
    // Respuesta cuando la operacion se realizo correctamente
    public static final String RESP_OK = "OK";
    
    // Respuesta cuando ocurrio un error en la operacion
    public static final String RESP_ERROR = "ERROR";
    
    // Respuesta que indica que se esta enviando el contenido de un archivo
    public static final String RESP_FILE_CONTENT = "CONTENT";
    
    // Respuesta que indica que se esta enviando una lista de archivos
    public static final String RESP_FILE_LIST = "FILELIST";
    
    // ============================================================================
    // ATRIBUTOS - Variables que almacenan los datos del mensaje
    // ============================================================================
    
    // Almacena el comando que el cliente quiere ejecutar (LIST, READ, etc.)
    private String command;
    
    // Almacena el nombre del archivo sobre el que se quiere operar
    private String fileName;
    
    // Almacena el contenido del archivo (para leer, escribir o modificar)
    private String content;
    
    // Almacena el tipo de respuesta del servidor (OK, ERROR, etc.)
    private String response;
    
    // Almacena la lista de nombres de archivos (para el comando LIST)
    private String[] fileList;
    
    // ============================================================================
    // CONSTRUCTORES - Diferentes formas de crear un objeto FileProtocol
    // ============================================================================
    
    /**
     * Constructor vacio - Crea un objeto sin inicializar
     * Util cuando el servidor necesita crear una respuesta
     */
    public FileProtocol() {
        // No inicializa nada, los valores se asignan despues con setters
    }
    
    /**
     * Constructor con solo comando
     * Usado para operaciones que no necesitan parametros adicionales
     * Ejemplo: LIST (listar archivos) o DISCONNECT (desconectar)
     * 
     * @param command El comando a ejecutar
     */
    public FileProtocol(String command) {
        this.command = command;  // Asigna el comando recibido
    }
    
    /**
     * Constructor con comando y nombre de archivo
     * Usado para operaciones que necesitan especificar un archivo
     * Ejemplo: READ (leer archivo) o DELETE (eliminar archivo)
     * 
     * @param command El comando a ejecutar
     * @param fileName El nombre del archivo objetivo
     */
    public FileProtocol(String command, String fileName) {
        this.command = command;      // Asigna el comando
        this.fileName = fileName;    // Asigna el nombre del archivo
    }
    
    /**
     * Constructor completo con comando, nombre y contenido
     * Usado para operaciones que necesitan enviar contenido
     * Ejemplo: CREATE (crear archivo) o MODIFY (modificar archivo)
     * 
     * @param command El comando a ejecutar
     * @param fileName El nombre del archivo
     * @param content El contenido a escribir en el archivo
     */
    public FileProtocol(String command, String fileName, String content) {
        this.command = command;      // Asigna el comando
        this.fileName = fileName;    // Asigna el nombre del archivo
        this.content = content;      // Asigna el contenido
    }
    
    // ============================================================================
    // GETTERS Y SETTERS - Metodos para obtener y modificar los atributos
    // ============================================================================
    
    /**
     * Obtiene el comando almacenado
     * @return El comando (LIST, READ, WRITE, etc.)
     */
    public String getCommand() { 
        return command;  // Retorna el valor del comando
    }
    
    /**
     * Establece el comando a ejecutar
     * @param command El comando a asignar
     */
    public void setCommand(String command) { 
        this.command = command;  // Asigna el nuevo comando
    }
    
    /**
     * Obtiene el nombre del archivo
     * @return El nombre del archivo
     */
    public String getFileName() { 
        return fileName;  // Retorna el nombre del archivo
    }
    
    /**
     * Establece el nombre del archivo
     * @param fileName El nombre del archivo a asignar
     */
    public void setFileName(String fileName) { 
        this.fileName = fileName;  // Asigna el nuevo nombre
    }
    
    /**
     * Obtiene el contenido del archivo
     * @return El contenido como String
     */
    public String getContent() { 
        return content;  // Retorna el contenido
    }
    
    /**
     * Establece el contenido del archivo
     * @param content El contenido a asignar
     */
    public void setContent(String content) { 
        this.content = content;  // Asigna el nuevo contenido
    }
    
    /**
     * Obtiene el tipo de respuesta
     * @return La respuesta (OK, ERROR, etc.)
     */
    public String getResponse() { 
        return response;  // Retorna la respuesta
    }
    
    /**
     * Establece el tipo de respuesta
     * @param response La respuesta a asignar
     */
    public void setResponse(String response) { 
        this.response = response;  // Asigna la nueva respuesta
    }
    
    /**
     * Obtiene la lista de archivos
     * @return Array con los nombres de archivos
     */
    public String[] getFileList() { 
        return fileList;  // Retorna el array de archivos
    }
    
    /**
     * Establece la lista de archivos
     * @param fileList Array con los nombres de archivos
     */
    public void setFileList(String[] fileList) { 
        this.fileList = fileList;  // Asigna la nueva lista
    }
}
