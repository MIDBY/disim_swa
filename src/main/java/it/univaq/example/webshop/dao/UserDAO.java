package it.univaq.example.webshop.dao;

import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DAO;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.univaq.framework.data.DataLayer;
import it.univaq.framework.data.OptimisticLockException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DAO {

    private PreparedStatement sUserByID, sUserByUsername, sUserByEmail, sUsersBySubscriptionMonth, sUsersByAccepted, sUsersByGroup, iUser, iUserGroup, uUser, uUserGroup;

    public UserDAO(DataLayer d) {
        super(d);
    }

    
    public void init() throws DataException {
        try {
            super.init();
            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sUserByID = connection.prepareStatement("SELECT * FROM utente WHERE id=?");
            sUserByUsername = connection.prepareStatement("SELECT id FROM utente WHERE username=?");
            sUserByEmail = connection.prepareStatement("SELECT id FROM utente WHERE email=?");
            sUsersBySubscriptionMonth = connection.prepareStatement("SELECT id FROM utente WHERE MONTH(dataIscrizione)=? and YEAR(dataIscrizione)=?");
            sUsersByAccepted = connection.prepareStatement("SELECT id FROM utente WHERE accettato=?");
            sUsersByGroup = connection.prepareStatement("SELECT idUtente FROM utente_gruppo WHERE idGruppo=?");
            iUser = connection.prepareStatement("INSERT INTO utente (username,email,password,indirizzo,accettato) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            iUserGroup = connection.prepareStatement("INSERT INTO utente_gruppo (idUtente,idGruppo) VALUES (?,(SELECT id FROM gruppo WHERE nome=?))");
            uUser = connection.prepareStatement("UPDATE utente SET username=?,email=?,password=?,indirizzo=?,dataIscrizione=?,accettato=?,versione=? WHERE id=? and versione=?");
            uUserGroup = connection.prepareStatement("UPDATE utente_gruppo SET idGruppo=(SELECT id FROM gruppo WHERE nome=?) WHERE idUtente=?");
        } catch (SQLException ex) {
            throw new DataException("Error initializing webshop data layer", ex);
        }
    }

    
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent è una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sUserByID.close();
            sUserByUsername.close();
            sUserByEmail.close();
            sUsersBySubscriptionMonth.close();
            sUsersByAccepted.close();
            sUsersByGroup.close();
            iUser.close();
            iUserGroup.close();
            uUser.close();

        } catch (SQLException ex) {
            ex.getStackTrace();
        }
        super.destroy();
    }

//metodi "factory" che permettono di creare
//e inizializzare opportune implementazioni
//delle interfacce del modello dati, nascondendo
//all'utente tutti i particolari
//factory methods to create and initialize
//suitable implementations of the data model interfaces,
//hiding all the implementation details
    
    public User createUser() {
        return new User();
    }

    //helper
    private User createUser(ResultSet rs) throws DataException {
        try {
            User a = (User) createUser();
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
            throw new DataException("Unable to create user object form ResultSet", ex);
        }
    }

    
    public User getUser(int user_key) throws DataException {
        User u = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(User.class, user_key)) {
            u = dataLayer.getCache().get(User.class, user_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sUserByID.setInt(1, user_key);
                try ( ResultSet rs = sUserByID.executeQuery()) {
                    if (rs.next()) {
                        //notare come utilizziamo il costrutture
                        //"helper" della classe AuthorImpl
                        //per creare rapidamente un'istanza a
                        //partire dal record corrente
                        //note how we use here the helper constructor
                        //of the AuthorImpl class to quickly
                        //create an instance from the current record

                        u = createUser(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(User.class, u);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load user by ID", ex);
            }
        }
        return u;
    }

    
    public User getUserByUsername(String username) throws DataException {
        try {
            sUserByUsername.setString(1, username);
            try ( ResultSet rs = sUserByUsername.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find users by username", ex);
        }
        return null;
    }

    
    public User getUserByEmail(String email) throws DataException {
        try {
            sUserByEmail.setString(1, email);
            try ( ResultSet rs = sUserByEmail.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find users by email", ex);
        }
        return null;
    }

    
    public List<User> getUsersBySubscriptionMonth(LocalDate date) throws DataException {
        List<User> result = new ArrayList<User>();
        try {
            sUsersBySubscriptionMonth.setInt(1, date.getMonthValue());
            sUsersBySubscriptionMonth.setInt(2, date.getYear());
            try ( ResultSet rs = sUsersBySubscriptionMonth.executeQuery()) {
                while (rs.next()) {
                    result.add((User) getUser(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find users by subscription date", ex);
        }
        return result;
    }

    
    public List<User> getUsersByAccepted(boolean accepted) throws DataException {
        List<User> result = new ArrayList<User>();
        try {
            sUsersByAccepted.setBoolean(1, accepted);
            try ( ResultSet rs = sUsersByAccepted.executeQuery()) {
                while (rs.next()) {
                    result.add((User) getUser(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find users by accepted", ex);
        }
        return result;
    }

    
    public List<User> getUsersByGroup(int group_key) throws DataException {
        List<User> result = new ArrayList<User>();
        try {
            sUsersByGroup.setInt(1, group_key);
            try ( ResultSet rs = sUsersByGroup.executeQuery()) {
                while (rs.next()) {
                    result.add((User) getUser(rs.getInt("idUtente")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find user by group", ex);
        }
        return result;
    }

    
    public void setUser(User user) throws DataException {
        try {
            if (user.getKey() != null && user.getKey() > 0) { //update
                //non facciamo nulla se l'oggetto è un proxy e indica di non aver subito modifiche
                //do not store the object if it is a proxy and does not indicate any modification
                if (user instanceof DataItemProxy && !((DataItemProxy) user).isModified()) {
                    return;
                }
                uUser.setString(1, user.getUsername());
                uUser.setString(2, user.getEmail());
                uUser.setString(3, user.getPassword());
                uUser.setString(4, user.getAddress());
                uUser.setString(5, user.getSubscriptionDate().toString());
                uUser.setBoolean(6, user.isAccepted());

                long current_version = user.getVersion();
                long next_version = current_version + 1;

                uUser.setLong(7, next_version);
                uUser.setInt(8, user.getKey());
                uUser.setLong(9, current_version);

                if (uUser.executeUpdate() == 0) {
                    throw new OptimisticLockException(user);
                } else {
                    user.setVersion(next_version);
                }
            } else { //insert
                iUser.setString(1, user.getUsername());
                iUser.setString(2, user.getEmail());
                iUser.setString(3, user.getPassword());
                iUser.setString(4, user.getAddress());
                //per default database la data iscrizione è inserita quella odierna
                iUser.setBoolean(5, user.isAccepted());

                if (iUser.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try ( ResultSet keys = iUser.getGeneratedKeys()) {
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
                            //inseriamo il nuovo oggetto nella cache
                            //add the new object to the cache
                            dataLayer.getCache().add(User.class, user);
                        }
                    }
                }
                iUserGroup.setInt(1, user.getKey());
                iUserGroup.setString(2, UserRoleEnum.ORDINANTE.toString());
                iUserGroup.executeUpdate();
            }

            if (user instanceof DataItemProxy) {
                ((DataItemProxy) user).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store user", ex);
        }
    }

    
    public void changeUserGroup(int user_key, UserRoleEnum value) throws DataException {
        try {
            if (user_key > 0) { //update
                uUserGroup.setString(1, value.name());
                uUserGroup.setInt(2, user_key);

                uUserGroup.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to change user group", ex);
        }
    }
}