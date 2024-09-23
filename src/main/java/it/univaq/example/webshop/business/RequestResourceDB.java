package it.univaq.example.webshop.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class RequestResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sRequestByID = "SELECT * FROM richiesta WHERE id=?";
    private static final String sRequestsByCategory = "SELECT id FROM richiesta WHERE idCategoria=?";
    private static final String sRequestsByOrdering = "SELECT id FROM richiesta WHERE idOrdinante=?";
    private static final String sRequestsByTechnician = "SELECT id FROM richiesta WHERE idTecnico=?";
    private static final String sRequestsByRequestState = "SELECT id FROM richiesta WHERE statoRichiesta=?";
    private static final String sRequestsByOrderState = "SELECT id FROM richiesta WHERE statoOrdine=?";
    private static final String sUnassignedRequests = "SELECT id FROM richiesta WHERE idTecnico IS NULL";
    private static final String sRequestsByCreationMonth = "SELECT id FROM richiesta WHERE MONTH(dataCreazione)=? and YEAR(dataCreazione)=?";
    private static final String sRequests = "SELECT id FROM richiesta";
    private static final String iRequest = "INSERT INTO richiesta (titolo,descrizione,idCategoria,idOrdinante,idTecnico,statoRichiesta,statoOrdine,dataCreazione,note) VALUES(?,?,?,?,?,?,?,?,?)";
    private static final String uRequest = "UPDATE richiesta SET titolo=?,descrizione=?,idCategoria=?,idOrdinante=?,idTecnico=?,statoRichiesta=?,statoOrdine=?,dataCreazione=?,note=?,versione=? WHERE id=? and versione=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }


    public static Request createRequest() {
        return new Request();
    }

    private static Request createRequest(ResultSet rs) throws RESTWebApplicationException {
        try {
            Request a = createRequest();
            a.setKey(rs.getInt("id"));
            a.setTitle(rs.getString("titolo"));
            a.setDescription(rs.getString("descrizione"));
            a.setCategory(CategoryResourceDB.getCategory(rs.getInt("idCategoria")));
            a.setOrdering(UserResourceDB.getUser(rs.getInt("idOrdinante")));
            a.setTechnician(UserResourceDB.getUser(rs.getInt("idTecnico")));
            a.setCreationDate(rs.getObject("dataCreazione", LocalDate.class));
            a.setRequestState(RequestStateEnum.valueOf(rs.getString("statoRichiesta")));
            a.setOrderState(OrderStateEnum.valueOf(rs.getString("statoOrdine")));
            a.setNotes(rs.getString("note"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create request object form ResultSet: " + ex.getMessage());
        }
    }

    public static Request getRequest(int request_key) throws RESTWebApplicationException {
        Request l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestByID)) {
                ps.setInt(1, request_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createRequest(rs);
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

    
    public static List<Request> getRequestsByCategory(int category_key) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByCategory)) {
                ps.setInt(1, category_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getRequestsByOrdering(int user_key) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByOrdering)) {
                ps.setInt(1, user_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getRequestsByTechnician(int user_key) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByTechnician)) {
                ps.setInt(1, user_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getRequestsByRequestState(RequestStateEnum value) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByRequestState)) {
                ps.setString(1, value.toString());
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getRequestsByOrderState(OrderStateEnum value) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByOrderState)) {
                ps.setString(1, value.toString());
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getUnassignedRequests() throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sUnassignedRequests)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static List<Request> getRequestsByCreationMonth(int month, int year) throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestsByCreationMonth)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
        
    public static List<Request> getRequests() throws RESTWebApplicationException {
        try {
            List<Request> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequests)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequest(rs.getInt("id")));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static Request setRequest(Request request) throws RESTWebApplicationException {
        try {
            if (request.getKey() != null && request.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uRequest)) {
                    ps.setString(1, request.getTitle());
                    ps.setString(2, request.getDescription());
                    if (request.getCategory() != null) {
                        ps.setInt(3, request.getCategory().getKey());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }                
                    if (request.getOrdering() != null) {
                        ps.setInt(4, request.getOrdering().getKey());
                    } else {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }       
                    if (request.getTechnician() != null) {
                        ps.setInt(5, request.getTechnician().getKey());
                    } else {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    }   
                    ps.setString(6, request.getRequestState().toString());
                    if(request.getOrderState().toString().equals(""))
                        ps.setString(7, OrderStateEnum.EMPTY.name());
                    else
                        ps.setString(7, request.getOrderState().toString());                            
                    ps.setObject(8, request.getCreationDate());
                    ps.setString(9, request.getNotes());

                    long current_version = request.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(10, next_version);
                    ps.setInt(11, request.getKey());
                    ps.setLong(12, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("Request not updated");
                    } else {
                        request.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iRequest, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, request.getTitle());
                    ps.setString(2, request.getDescription());
                    if (request.getCategory() != null) {
                        ps.setInt(3, request.getCategory().getKey());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }                
                    if (request.getOrdering() != null) {
                        ps.setInt(4, request.getOrdering().getKey());
                    } else {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }       
                    if (request.getTechnician() != null) {
                        ps.setInt(5, request.getTechnician().getKey());
                    } else {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    }   
                    ps.setString(6, request.getRequestState().toString());
                    ps.setString(7, request.getOrderState().name());
                    ps.setObject(8, request.getCreationDate());
                    ps.setString(9, request.getNotes());

                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                request.setKey(key);
                            }
                        }
                    }
                }
            }
            return request;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
}