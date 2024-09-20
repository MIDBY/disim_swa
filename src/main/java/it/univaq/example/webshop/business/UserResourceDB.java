package it.univaq.example.webshop.business;

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

public class UserResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
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

    public static User getUser(int user_key) throws RESTWebApplicationException {
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
    
    public static User getUserByUsername(String username) throws RESTWebApplicationException {
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

    public static User getUserByEmail(String email) throws RESTWebApplicationException {
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

    public static List<User> getUsers() throws RESTWebApplicationException {
        List<User> l = new ArrayList<>();
        l.addAll(getUsersByGroup(UserRoleEnum.TECNICO));
        l.addAll(getUsersByGroup(UserRoleEnum.ORDINANTE));
        return l;
    }

    public static List<User> getUsersBySubscriptionMonth(int month, int year) throws RESTWebApplicationException {
        List<User> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersBySubscriptionMonth)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUser(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<User> getUsersByAccepted(boolean accepted) throws RESTWebApplicationException {
        List<User> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByAccepted)) {
                ps.setBoolean(1, accepted);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUser(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<User> getUsersByGroup(int group_key) throws RESTWebApplicationException {
        List<User> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByGroup)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUser(rs.getInt("idUtente")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<User> getUsersByGroup(UserRoleEnum group) throws RESTWebApplicationException {
        List<User> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUsersByGroupName)) {
                ps.setString(1, group.toString());
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getUser(rs.getInt("idUtente")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static User setUser(User user) throws RESTWebApplicationException {
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
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
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
            return user;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    
    public static void changeUserGroup(int user_key, UserRoleEnum value) throws RESTWebApplicationException {
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