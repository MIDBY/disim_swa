package it.univaq.example.webshop.business;

import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.framework.data.DAO;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import it.univaq.framework.data.DataLayer;
import it.univaq.framework.data.OptimisticLockException;

public class RequestDAO extends DAO {

    private PreparedStatement sRequestByID, sRequestsByCategory, sRequestsByOrdering, sRequestsByTechnician, sRequestsByRequestState,
     sRequestsByOrderState, sUnassignedRequests, sRequestsByCreationMonth, sRequests, iRequest, uRequest;

    public RequestDAO(DataLayer d) {
        super(d);
    }

    
    public void init() throws DataException {
        try {
            super.init();
            sRequestByID = connection.prepareStatement("SELECT * FROM richiesta WHERE id=?");
            sRequestsByCategory = connection.prepareStatement("SELECT id FROM richiesta WHERE idCategoria=?");
            sRequestsByOrdering = connection.prepareStatement("SELECT id FROM richiesta WHERE idOrdinante=?");
            sRequestsByTechnician = connection.prepareStatement("SELECT id FROM richiesta WHERE idTecnico=?");
            sRequestsByRequestState = connection.prepareStatement("SELECT id FROM richiesta WHERE statoRichiesta=?");
            sRequestsByOrderState = connection.prepareStatement("SELECT id FROM richiesta WHERE statoOrdine=?");
            sUnassignedRequests = connection.prepareStatement("SELECT id FROM richiesta WHERE idTecnico IS NULL");
            sRequestsByCreationMonth = connection.prepareStatement("SELECT id FROM richiesta WHERE MONTH(dataCreazione)=? and YEAR(dataCreazione)=?");
            sRequests = connection.prepareStatement("SELECT id FROM richiesta");
            iRequest = connection.prepareStatement("INSERT INTO richiesta (titolo,descrizione,idCategoria,idOrdinante,idTecnico,statoRichiesta,statoOrdine,dataCreazione,note) VALUES(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            uRequest = connection.prepareStatement("UPDATE richiesta SET titolo=?,descrizione=?,idCategoria=?,idOrdinante=?,idTecnico=?,statoRichiesta=?,statoOrdine=?,dataCreazione=?,note=?,versione=? WHERE id=? and versione=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing webshop data layer", ex);
        }
    }

    
    public void destroy() throws DataException {
        try {
            sRequestByID.close();
            sRequestsByCategory.close();
            sRequestsByOrdering.close();
            sRequestsByTechnician.close();
            sRequestsByRequestState.close();
            sRequestsByOrderState.close();
            sUnassignedRequests.close();
            sRequestsByCreationMonth.close();
            sRequests.close();
            iRequest.close();
            uRequest.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        super.destroy();
    }

    
    public Request createRequest() {
        return new Request();
    }

    //helper
    private Request createRequest(ResultSet rs) throws DataException {
        Request a = (Request) createRequest();
        try {
            a.setKey(rs.getInt("id"));
            a.setTitle(rs.getString("titolo"));
            a.setDescription(rs.getString("descrizione"));
            a.setCategory(CategoryDAO.getCategory(rs.getInt("idCategoria")));
            a.setOrdering(UserDAO.getUser(rs.getInt("idOrdinante")));
            a.setTechnician(UserDAO.getUser(rs.getInt("idTecnico")));
            a.setCreationDate(rs.getObject("dataCreazione", LocalDate.class));
            a.setRequestState(RequestStateEnum.valueOf(rs.getString("statoRichiesta")));
            a.setOrderState(OrderStateEnum.valueOf(rs.getString("statoOrdine")));
            a.setNotes(rs.getString("note"));
            a.setVersion(rs.getLong("versione"));
        } catch (SQLException ex) {
            throw new DataException("Unable to create request object form ResultSet", ex);
        }
        return a;
    }

    
    public Request getRequest(int request_key) throws DataException {
        Request a = null;
        if (dataLayer.getCache().has(Request.class, request_key)) {
            a = dataLayer.getCache().get(Request.class, request_key);
        } else {
            try {
                sRequestByID.setInt(1, request_key);
                try (ResultSet rs = sRequestByID.executeQuery()) {
                    if (rs.next()) {
                        a = createRequest(rs);
                        dataLayer.getCache().add(Request.class, a);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load request by ID", ex);
            }
        }
        return a;
    }

    
    public List<Request> getRequestsByCategory(int category_key) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByCategory.setInt(1, category_key);
            try (ResultSet rs = sRequestsByCategory.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by category", ex);
        }
        return result;
    }

    
    public List<Request> getRequestsByOrdering(int user_key) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByOrdering.setInt(1, user_key);
            try (ResultSet rs = sRequestsByOrdering.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by ordering", ex);
        }
        return result;
    }

    
    public List<Request> getRequestsByTechnician(int user_key) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByTechnician.setInt(1, user_key);
            try (ResultSet rs = sRequestsByTechnician.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by technician", ex);
        }
        return result;
    }

    
    public List<Request> getRequestsByRequestState(RequestStateEnum value) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByRequestState.setString(1, value.toString());
            try (ResultSet rs = sRequestsByRequestState.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by request state", ex);
        }
        return result;
    }

    
    public List<Request> getRequestsByOrderState(OrderStateEnum value) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByOrderState.setString(1, value.toString());
            try (ResultSet rs = sRequestsByOrderState.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by order state", ex);
        }
        return result;
    }

    
    public List<Request> getUnassignedRequests() throws DataException {
        List<Request> result = new ArrayList<Request>();
        try (ResultSet rs = sUnassignedRequests.executeQuery()) {
            while (rs.next()) {
                result.add((Request) getRequest(rs.getInt("id")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load unassigned requests", ex);
        }
        return result;
    }

    
    public List<Request> getRequestsByCreationMonth(LocalDate date) throws DataException {
        List<Request> result = new ArrayList<Request>();
        try {
            sRequestsByCreationMonth.setInt(1, date.getMonthValue());
            sRequestsByCreationMonth.setInt(2, date.getYear());
            try (ResultSet rs = sRequestsByCreationMonth.executeQuery()) {
                while (rs.next()) {
                    result.add((Request) getRequest(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests by creation date", ex);
        }
        return result;
    }

    
    public List<Request> getRequests() throws DataException {
        List<Request> result = new ArrayList<Request>();
        try (ResultSet rs = sRequests.executeQuery()) {
            while (rs.next()) {
                result.add((Request) getRequest(rs.getInt("id")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load requests", ex);
        }
        return result;
    }

    
    public void setRequest(Request request) throws DataException {
        try {
            if (request.getKey() != null && request.getKey() > 0) { //update
                if (request instanceof DataItemProxy && !((DataItemProxy) request).isModified()) {
                    return;
                }
                uRequest.setString(1, request.getTitle());
                uRequest.setString(2, request.getDescription());
                if (request.getCategory() != null) {
                    uRequest.setInt(3, request.getCategory().getKey());
                } else {
                    uRequest.setNull(3, java.sql.Types.INTEGER);
                }                
                if (request.getOrdering() != null) {
                    uRequest.setInt(4, request.getOrdering().getKey());
                } else {
                    uRequest.setNull(4, java.sql.Types.INTEGER);
                }       
                if (request.getTechnician() != null) {
                    uRequest.setInt(5, request.getTechnician().getKey());
                } else {
                    uRequest.setNull(5, java.sql.Types.INTEGER);
                }   
                uRequest.setString(6, request.getRequestState().toString());
                if(request.getOrderState().toString().equals(""))
                    uRequest.setString(7, OrderStateEnum.EMPTY.name());
                else
                    uRequest.setString(7, request.getOrderState().toString());                            
                uRequest.setObject(8, request.getCreationDate());
                uRequest.setString(9, request.getNotes());

                long current_version = request.getVersion();
                long next_version = current_version + 1;

                uRequest.setLong(10, next_version);
                uRequest.setInt(11, request.getKey());
                uRequest.setLong(12, current_version);

                if (uRequest.executeUpdate() == 0) {
                    throw new OptimisticLockException(request);
                } else {
                    request.setVersion(next_version);
                }
            } else { //insert
                iRequest.setString(1, request.getTitle());
                iRequest.setString(2, request.getDescription());
                if (request.getCategory() != null) {
                    iRequest.setInt(3, request.getCategory().getKey());
                } else {
                    iRequest.setNull(3, java.sql.Types.INTEGER);
                }                
                if (request.getOrdering() != null) {
                    iRequest.setInt(4, request.getOrdering().getKey());
                } else {
                    iRequest.setNull(4, java.sql.Types.INTEGER);
                }       
                if (request.getTechnician() != null) {
                    iRequest.setInt(5, request.getTechnician().getKey());
                } else {
                    iRequest.setNull(5, java.sql.Types.INTEGER);
                }   
                iRequest.setString(6, request.getRequestState().toString());
                iRequest.setString(7, request.getOrderState().name());
                iRequest.setObject(8, request.getCreationDate());
                iRequest.setString(9, request.getNotes());
                if (iRequest.executeUpdate() == 1) {
                    try (ResultSet keys = iRequest.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            request.setKey(key);
                            dataLayer.getCache().add(Request.class, request);
                        }
                    }
                }
            }
            if (request instanceof DataItemProxy) {
                ((DataItemProxy) request).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store request", ex);
        }
    }
}