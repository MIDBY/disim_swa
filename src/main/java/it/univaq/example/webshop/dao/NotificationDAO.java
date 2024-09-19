package it.univaq.example.webshop.dao;

import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.framework.data.DAO;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.univaq.framework.data.DataLayer;
import it.univaq.framework.data.OptimisticLockException;

public class NotificationDAO extends DAO {

    private PreparedStatement sNotificationByID, sNotificationsByUser, sNotificationsNotReadByUser, iNotification, uNotification, dNotification;

    public NotificationDAO(DataLayer d) {
        super(d);
    }

    
    public void init() throws DataException {
        try {
            super.init();
            sNotificationByID = connection.prepareStatement("SELECT * FROM notifica WHERE id=?");
            sNotificationsByUser = connection.prepareStatement("SELECT id FROM notifica WHERE idDestinatario=? ORDER BY dataCreazione DESC");
            sNotificationsNotReadByUser = connection.prepareStatement("SELECT id FROM notifica WHERE idDestinatario=? and letto=0");
            iNotification = connection.prepareStatement("INSERT INTO notifica (idDestinatario,messaggio,link,tipo,dataCreazione) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            uNotification = connection.prepareStatement("UPDATE notifica SET idDestinatario=?,messaggio=?,link=?,tipo=?,dataCreazione=?,letto=?,versione=? WHERE id=? and versione=?");
            dNotification = connection.prepareStatement("DELETE FROM notifica WHERE id=?");
        } catch (SQLException ex) {
            throw new DataException("Error initializing webshop data layer", ex);
        }
    }

    
    public void destroy() throws DataException {
        try {
            sNotificationByID.close();
            sNotificationsByUser.close();
            sNotificationsNotReadByUser.close();
            iNotification.close();
            uNotification.close();
            dNotification.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        super.destroy();
    }

    
    public Notification createNotification() {
        return new Notification();
    }

    //helper
    private Notification createNotification(ResultSet rs) throws DataException {
        try {
            Notification a = (Notification)createNotification();
            a.setKey(rs.getInt("id"));
            a.setRecipientKey(rs.getInt("idDestinatario"));
            a.setMessage(rs.getString("messaggio"));
            a.setLink(rs.getString("link"));
            a.setType(NotificationTypeEnum.valueOf(rs.getString("tipo")));
            a.setCreationDate(rs.getObject("dataCreazione", LocalDateTime.class));
            a.setRead(rs.getBoolean("letto"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new DataException("Unable to create notification object form ResultSet", ex);
        }
    }

    
    public Notification getNotification(int notification_key) throws DataException {
        Notification a = null;
        if (dataLayer.getCache().has(Notification.class, notification_key)) {
            a = dataLayer.getCache().get(Notification.class, notification_key);
        } else {
            try {
                sNotificationByID.setInt(1, notification_key);
                try (ResultSet rs = sNotificationByID.executeQuery()) {
                    if (rs.next()) {
                        a = createNotification(rs);
                        dataLayer.getCache().add(Notification.class, a);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load notification by ID", ex);
            }
        }
        return a;
    }

    
    public List<Notification> getNotificationsByUser(int user_key) throws DataException {
        List<Notification> result = new ArrayList<Notification>();
        try {
            sNotificationsByUser.setInt(1, user_key);
            try (ResultSet rs = sNotificationsByUser.executeQuery()) {
                while (rs.next()) {
                    result.add(getNotification(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load notifications", ex);
        }
        return result;
    }

    
    public List<Notification> getNotificationsNotReadByUser(int user_key) throws DataException {
        List<Notification> result = new ArrayList<Notification>();
        try {
            sNotificationsNotReadByUser.setInt(1, user_key);
            try (ResultSet rs = sNotificationsNotReadByUser.executeQuery()) {
                while (rs.next()) {
                    result.add(getNotification(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load not read notifications", ex);
        }
        return result;
    }

    
    public void setNotification(Notification notification) throws DataException {
        try {
            if (notification.getKey() != null && notification.getKey() > 0) { //update
                if (notification instanceof DataItemProxy && !((DataItemProxy) notification).isModified()) {
                    return;
                }
                if (notification.getRecipient() != null) {
                    uNotification.setInt(1, notification.getRecipient().getKey());
                } else {
                    uNotification.setNull(1, java.sql.Types.INTEGER);
                }
                uNotification.setString(2, notification.getMessage());
                uNotification.setString(3, notification.getLink());
                uNotification.setString(4, notification.getType().toString());
                uNotification.setObject(5, notification.getCreationDate());
                uNotification.setBoolean(6, notification.isRead());

                long current_version = notification.getVersion();
                long next_version = current_version + 1;

                uNotification.setLong(7, next_version);
                uNotification.setInt(8, notification.getKey());
                uNotification.setLong(9, current_version);

                if (uNotification.executeUpdate() == 0) {
                    throw new OptimisticLockException(notification);
                } else {
                    notification.setVersion(next_version);
                }
            } else { //insert
                if (notification.getRecipient() != null) {
                    iNotification.setInt(1, notification.getRecipient().getKey());
                } else {
                    iNotification.setNull(1, java.sql.Types.INTEGER);
                }
                iNotification.setString(2, notification.getMessage());
                iNotification.setString(3, notification.getLink());
                iNotification.setString(4, notification.getType().toString());
                iNotification.setObject(5, notification.getCreationDate());
                if (iNotification.executeUpdate() == 1) {
                    try (ResultSet keys = iNotification.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            notification.setKey(key);
                            dataLayer.getCache().add(Notification.class, notification);
                        }
                    }
                }
            }
            if (notification instanceof DataItemProxy) {
                ((DataItemProxy) notification).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store notification", ex);
        }
    }

    
    public void deleteNotification(Notification notification) throws DataException {
        try {
            if (notification.getKey() != null && notification.getKey() > 0) { //delete
                dNotification.setInt(1, notification.getKey());
                if (dNotification.executeUpdate() == 0) {
                    throw new OptimisticLockException(notification);
                } else {
                    dataLayer.getCache().delete(Notification.class, notification);
                }
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to delete notification", ex);
        }
    }
}