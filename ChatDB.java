import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/*
 * This class represents a single chat's message history and users
 */
public class ChatDB extends DB {
    private int cID;
    private String name;
    public static final String folderName = "chats";
    private ArrayList<Integer> users;
    private ArrayList<Message> messages;
    
    private ChatDB(String path, int cID, String name) {
        super(path, Integer.toString(cID) + ".csv");
        this.cID = cID;
        this.name = name;
        this.users = new ArrayList<Integer>();
        this.messages = new ArrayList<Message>();
    }
    
    public static ChatDB create(String path, int cID, String name) {
        // Check if the chats folder already exists.  If it doesn't, try to make it.
        try {
            File folder = new File(path + File.separator + folderName);
            if (!folder.exists()) {
                if (!folder.mkdir())
                    throw new IOException("ERROR: Could not create folder for ChatDB");
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        ChatDB temp = new ChatDB(path + File.separator + folderName, cID, name);
        if (temp.checkCreate()) {
            return temp;
        }
        return null;
    }
    
    public boolean saveChat() {
        System.out.println("Saving " + dbName);
        // Set current DB to be dbname with OLD at the end (before the file extension) and returns a new file with the name of the db that is readable and writable
        File db = this.getNextFile();
        
        // If getNextFile failed for some reason, return false
        if (db == null) {
            return false;
        }
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(db, "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        
        // Now that we've got here, we can start saving the chat to file
        // First off we'll save the chat data (cID and name) as the first line
        String line = "";
        line += Integer.toString(cID) + "," + name;
        writer.println(line);
        
        // Then we'll save all the uID's of the chat members in the first line separated by commas
        // Make sure there are actually users first
        line = "";
        if (users.size() > 0) {
            for (int uID : users) {
                line += Integer.toString(uID) + ",";
            }
            // Get rid of the comma at the end
            line = line.substring(0, line.length()-1);
            writer.println(line);
        }
        // Else means there are no users in the chat
        else {
            writer.println("");
        }
        
        // Next up, write all the messages in chronological order
        // First check if there are any messages in the first place
        if (messages.size() > 0) {
            for (Message msg : messages) {
                line = Integer.toString(msg.getOwner()) + ": " + msg.getContents();
                writer.println(line);
            }
        }
        // Else means there are no messages in the chat to save
        else {
            writer.println("");
        }
        
        writer.close();
        System.out.println("ChatDB save finished");
        return true;
    }
    
    /*
     * path is the root path of the program, not the path to the chat
     */
    public static ChatDB loadChat(String path, int cID) {
        BufferedReader reader = null;
        File db = new File(path + File.separator + folderName + File.separator + Integer.toString(cID) + ".csv");
        ArrayList<Integer> tempUsers = new ArrayList<Integer>();
        ArrayList<Message> tempMessages = new ArrayList<Message>();
        String name = "";
        try {
            // Check to see if the db exists
            if (!db.exists())
                throw new IOException("ERROR: loadChat() failed because the database stored in ChatDB.path doesn't exist");
            
            // Then create the reader
            reader = new BufferedReader(new FileReader(db));
            
            String line;
            String[] splitted;
            
            // Read chat info first
            splitted = reader.readLine().split(",");
            // Make sure there are exactly two elements
            if (splitted.length != 2)
                throw new IOException("ERROR: The chat's info is not formatted properly");
            
            // Now there's exactly two elements in splitted.
            name = splitted[1];
            
            // Next, try to read the users in the chat
            splitted = reader.readLine().split(",");
            // If theres at least one user, load the user(s).  Otherwise, continue
            if (splitted.length > 0) {
                // For each user in splitted, add it as an integer to tempUsers
                for (String user : splitted) {
                    tempUsers.add(Integer.parseInt(user));
                }
            }
            
            Message msg;
            // The rest of the lines will are messages
            while ((line = reader.readLine()) != null) {
                // This means we've reached the end of the messages
                if (line.equals(""))
                    break;
                
                splitted = line.split(": ", 2);
                if (splitted.length != 2)
                    throw new IOException("ERROR: One of the messages being loaded isn't formatted properly");
                msg = new Message(splitted[1], Integer.parseInt(splitted[0]));
                tempMessages.add(msg);
            }
            
            // All users and messages should be in tempUsers and tempMessages respectively
            // Time to create our ChatDB object!
            ChatDB tempChatDB = ChatDB.create(path, cID, name);
            //ChatDB tempChatDB = new ChatDB(dbPath, cID, name);
            tempChatDB.setMessages(tempMessages);
            tempChatDB.setUsers(tempUsers);
            
            reader.close();
            return tempChatDB;
        } catch (Exception e) {
            System.out.println(e);
            try {
                reader.close();
            } catch (Exception f) {
                System.out.println(f);
                return null;
            }
            return null;
        }
    }
    
    public ArrayList<Integer> getUsers() {
        return users;
    }
    
    public boolean addUser(int uID) {
        if (users.contains(uID)) {
            return false;
        }
        else {
            users.add(uID);
            return true;
        }
    }
    
    public void addMessage(Message msg) {
        messages.add(msg);
    }
    
    public int getcID() {
        return cID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setUsers(ArrayList<Integer> users) {
        this.users = users;
    }
    
    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
