import java.io.Serializable;

/**
 * Protocolo de comunicación para operaciones de archivos
 */
public class FileProtocol implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Comandos del protocolo
    public static final String CMD_LIST_FILES = "LIST";
    public static final String CMD_READ_FILE = "READ";
    public static final String CMD_WRITE_FILE = "WRITE";
    public static final String CMD_DELETE_FILE = "DELETE";
    public static final String CMD_CREATE_FILE = "CREATE";
    public static final String CMD_MODIFY_FILE = "MODIFY";
    public static final String CMD_DISCONNECT = "DISCONNECT";
    
    // Respuestas
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_FILE_CONTENT = "CONTENT";
    public static final String RESP_FILE_LIST = "FILELIST";
    
    private String command;
    private String fileName;
    private String content;
    private String response;
    private String[] fileList;
    
    public FileProtocol() {}
    
    public FileProtocol(String command) {
        this.command = command;
    }
    
    public FileProtocol(String command, String fileName) {
        this.command = command;
        this.fileName = fileName;
    }
    
    public FileProtocol(String command, String fileName, String content) {
        this.command = command;
        this.fileName = fileName;
        this.content = content;
    }
    
    // Getters y Setters
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public String[] getFileList() { return fileList; }
    public void setFileList(String[] fileList) { this.fileList = fileList; }
}
