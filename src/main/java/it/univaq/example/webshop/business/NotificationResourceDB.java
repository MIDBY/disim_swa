package it.univaq.example.webshop.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.OptimisticLockException;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class NotificationResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sNotificationByID = "SELECT * FROM notifica WHERE id=?";
    private static final String sNotificationsByUser = "SELECT id FROM notifica WHERE idDestinatario=? ORDER BY dataCreazione DESC";
    private static final String sNotificationsNotReadByUser = "SELECT id FROM notifica WHERE idDestinatario=? and letto=0";
    private static final String sUserNotificationsByType = "SELECT id FROM notifica WHERE idDestinatario=? and tipo=?";
    private static final String iNotification = "INSERT INTO notifica (idDestinatario,messaggio,link,tipo,dataCreazione) VALUES(?,?,?,?,?)";
    private static final String uNotification = "UPDATE notifica SET idDestinatario=?,messaggio=?,link=?,tipo=?,dataCreazione=?,letto=?,versione=? WHERE id=? and versione=?";
    private static final String dNotification = "DELETE FROM notifica WHERE id=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }

    public static Notification createNotification() {
        return new Notification();
    }

    private static Notification createNotification(ResultSet rs) throws RESTWebApplicationException {
        try {
            Notification a = createNotification();
            a.setKey(rs.getInt("id"));
            a.setRecipient(UserResourceDB.getUser(rs.getInt("idDestinatario")));
            a.setMessage(rs.getString("messaggio"));
            a.setLink(rs.getString("link"));
            a.setType(NotificationTypeEnum.valueOf(rs.getString("tipo")));
            a.setCreationDate(rs.getObject("dataCreazione", LocalDateTime.class));
            a.setRead(rs.getBoolean("letto"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create notification object form ResultSet: " + ex.getMessage());
        }
    }

    public static Notification getNotification(int notification_key) throws RESTWebApplicationException {
        Notification l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sNotificationByID)) {
                ps.setInt(1, notification_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createNotification(rs);
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

    public static List<Notification> getNotificationsByUser(int user_key) throws DataException {
        List<Notification> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sNotificationsByUser)) {
            ps.setInt(1, user_key);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getNotification(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }
    
    public static List<Notification> getNotificationsNotReadByUser(int notification_key) throws RESTWebApplicationException {
        List<Notification> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sNotificationsNotReadByUser)) {
            ps.setInt(1, notification_key);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getNotification(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<Notification> getUserNotificationsByType(int user_key, NotificationTypeEnum value) throws RESTWebApplicationException {
        List<Notification> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUserNotificationsByType)) {
            ps.setInt(1, user_key);
            ps.setString(2, value.toString());
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getNotification(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static Notification setNotification(Notification notification) throws RESTWebApplicationException {
        try {
            if (notification.getKey() != null && notification.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uNotification)) {
                    if (notification.getRecipient() != null) {
                        ps.setInt(1, notification.getRecipient().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }
                    ps.setString(2, notification.getMessage());
                    ps.setString(3, notification.getLink());
                    ps.setString(4, notification.getType().toString());
                    ps.setObject(5, notification.getCreationDate());
                    ps.setBoolean(6, notification.isRead());

                    long current_version = notification.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(7, next_version);
                    ps.setInt(8, notification.getKey());
                    ps.setLong(9, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("User not updated");
                    } else {
                        notification.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iNotification, Statement.RETURN_GENERATED_KEYS)) {
                    if (notification.getRecipient() != null) {
                        ps.setInt(1, notification.getRecipient().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }
                    ps.setString(2, notification.getMessage());
                    ps.setString(3, notification.getLink());
                    ps.setString(4, notification.getType().toString());
                    ps.setObject(5, notification.getCreationDate());

                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                notification.setKey(key);
                            }
                        }
                    }
                }
            }
            return notification;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static void deleteNotification(Notification notification) throws DataException {
        try {
            if (notification.getKey() != null && notification.getKey() > 0) { //delete
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(dNotification)) {
                    ps.setInt(1, notification.getKey());
                    if (ps.executeUpdate() == 0) {
                        throw new OptimisticLockException(notification);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
}