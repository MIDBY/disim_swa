package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;

@Path("database/user")
public class UserResource {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String SQL_SELECT_ALL = "SELECT * FROM utente";
    private static final String sUserByID = "SELECT * FROM utente WHERE id=?";
    private static final String sUserByUsername = "SELECT id FROM utente WHERE username=?";
    private static final String sUserByEmail = "SELECT id FROM utente WHERE email=?";
    private static final String sUsersBySubscriptionMonth = "SELECT id FROM utente WHERE MONTH(dataIscrizione)=? and YEAR(dataIscrizione)=?";
    private static final String sUsersByAccepted = "SELECT id FROM utente WHERE accettato=?";
    private static final String sUsersByGroup = "SELECT idUtente FROM utente_gruppo WHERE idGruppo=?";
    private static final String sUsersByGroupName = "SELECT idUtente FROM utente_gruppo WHERE idGruppo=(SELECT id FROM gruppo WHERE nome=?)";
    private static final String iUser = "INSERT INTO utente (username,email,password,indirizzo,accettato) VALUES(?,?,?,?,?)";
    private static final String iUserGroup = "INSERT INTO utente_gruppo (idUtente,idGruppo) VALUES (?,(SELECT id FROM gruppo WHERE nome=?))";
    private static final String uUser = "UPDATE utente SET username=?,email=?,password=?,indirizzo=?,dataIscrizione=?,accettato=?,versione=? WHERE id=? and versione=?";
    private static final String uUserGroup = "UPDATE utente_gruppo SET idGruppo=(SELECT id FROM gruppo WHERE nome=?) WHERE idUtente=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }

    public static User createUser() {
        return new User();
    }

    private static User createUser(ResultSet rs) throws RESTWebApplicationException {
        try {
            User a = createUser();
            a.setKey(rs.getInt("id"));
            a.setUsername(rs.getString("username"));
            a.setEmail(rs.getString("email"));
            a.setPassword(rs.getString("password"));
            a.setAddress(rs.getString("indirizzo"));
            a.setSubscriptionDate(rs.getObject("dataIscrizione", LocalDate.class));
            a.setAccepted(rs.getBoolean("accettato"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create user object form ResultSet: " + ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws RESTWebApplicationException {
        try {
            List<User> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createUser(rs));
                    }
                }
            }
            return Response.ok(l).build();
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int user_key) throws RESTWebApplicationException {
        User l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserByID)) {
                ps.setInt(1, user_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createUser(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(l).build();
    }   

    public static User getUserData(int user_key) throws RESTWebApplicationException {
        User l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserByID)) {
                ps.setInt(1, user_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createUser(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return l;
    }   
    
    @GET
    @Path("{username: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("username") String username) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserByUsername)) {
            ps.setString(1, username);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    @GET
    @Path("{email: [a-zA-Z0-9]+@[a-zA-Z].[a-z]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(@PathParam("email") String email) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserByEmail)) {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static User getUserByEmailData(String email) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserByEmail)) {
            ps.setString(1, email);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getUserData(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    @GET
    @Path("{month: [1]?[0-9]}/{year: [1-9][0-9][0-9][0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersBySubscriptionMonth(@PathParam("month") int month, @PathParam("year") int year) throws RESTWebApplicationException {
        List<User> result = new ArrayList<User>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersBySubscriptionMonth)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUserData(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("{accepted: (true,false)}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByAccepted(@PathParam("accepted") boolean accepted) throws RESTWebApplicationException {
        List<User> result = new ArrayList<User>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByAccepted)) {
                ps.setBoolean(1, accepted);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUserData(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("{idgroup: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("idgroup") int group_key) throws RESTWebApplicationException {
        List<User> result = new ArrayList<User>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByGroup)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUserData(rs.getInt("idUtente")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("{group: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("group") String group) throws RESTWebApplicationException {
        List<User> result = new ArrayList<User>();
        try {
            if(UserRoleEnum.valueOf(group) != null) {
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByGroupName)) {
                    ps.setString(1, UserRoleEnum.valueOf(group).toString());
                    try ( ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            result.add(getUserData(rs.getInt("idUtente")));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(result).build();
    }

    public void setUser(User user) throws RESTWebApplicationException {
        try {
            if (user.getKey() != null && user.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uUser)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getAddress());
                    ps.setString(5, user.getSubscriptionDate().toString());
                    ps.setBoolean(6, user.isAccepted());

                    long current_version = user.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(7, next_version);
                    ps.setInt(8, user.getKey());
                    ps.setLong(9, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("User not updated");
                    } else {
                        user.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iUser)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getAddress());
                    ps.setBoolean(5, user.isAccepted());

                    if (ps.executeUpdate() == 1) {
                        //per leggere la chiave generata dal database
                        //per il record appena inserito, usiamo il metodo
                        //getGeneratedKeys sullo statement.
                        //to read the generated record key from the database
                        //we use the getGeneratedKeys method on the same statement
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            //il valore restituito è un ResultSet con un record
                            //per ciascuna chiave generata (uno solo nel nostro caso)
                            //the returned value is a ResultSet with a distinct record for
                            //each generated key (only one in our case)
                            if (keys.next()) {
                                //i campi del record sono le componenti della chiave
                                //(nel nostro caso, un solo intero)
                                //the record fields are the key componenets
                                //(a single integer in our case)
                                int key = keys.getInt(1);
                                //aggiornaimo la chiave in caso di inserimento
                                //after an insert, uopdate the object key
                                user.setKey(key);
                            }
                        }
                    }
                }
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iUserGroup)) {
                    ps.setInt(1, user.getKey());
                    ps.setString(2, UserRoleEnum.ORDINANTE.toString());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    
    public void changeUserGroup(int user_key, UserRoleEnum value) throws RESTWebApplicationException {
        try {
            if (user_key > 0) { //update                
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uUserGroup)) {
                    ps.setString(1, value.name());
                    ps.setInt(2, user_key);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
}